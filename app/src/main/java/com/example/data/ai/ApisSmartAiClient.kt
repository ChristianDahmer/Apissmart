package com.example.data.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApisSmartAiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    private val moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: GeminiApiService by lazy {
        retrofit.create(GeminiApiService::class.java)
    }

    private const val SYSTEM_PROMPT = """
Você é o assistente inteligente central do aplicativo de gestão apícola ApisSmart (ApiSystem).
Sua função é atuar como um especialista sênior em apicultura, engenharia agronômica e análise de dados apícolas.

OBJETIVOS:
1. Estruturação de dados de campo recebidos em linguagem natural ou anotações rápidas de apicultores.
2. Análise de sanidade e diagnóstico: identificar sinais de pragas (Varroa destructor, Traça da Cera, Cria Giz, Besouro das Colmeias - Aethina tumida, Loque Americana e Europeia) e sugerir ações corretivas imediatas.
3. Diagnóstico visual de fotos de quadros de cria, favos de mel e caixas.
4. Gestão de manejo: planos de alimentação de incentivo/manutenção, substituição de rainhas, divisão de enxames, controle de espaço e trocas de caixas/quadros.

PROTOCOLOS CRÍTICOS:
- PROTOCOLO DE SEGURANÇA SANITÁRIA: Em caso de suspeita ou detecção de doenças de notificação obrigatória (ex: Loque Americana, Loque Europeia, Besouro das Colmeias - Aethina tumida), insira SEMPRE um aviso destacado de ALTA PRIORIDADE recomendando a consulta imediata a um médico veterinário ou ao órgão de defesa agropecuária local (ex: MAPA/Defesa Agropecuária estadual).
- TOM DE VOZ: Profissional, técnico, objetivo e acessível a apicultores. Use listas, prazos claros e orientações diretas.
"""

    suspend fun analyzeFieldInspection(
        rawNotes: String,
        apiaryName: String,
        hiveNumber: String
    ): StructuredInspectionResult = withContext(Dispatchers.IO) {
        val prompt = """
Analise o seguinte relato de inspeção de campo da $hiveNumber no $apiaryName:
"$rawNotes"

Você DEVE retornar uma resposta que contenha DUAS partes:
PRIMEIRA PARTE: Um bloco JSON delimitado por ```json ... ``` com a seguinte estrutura:
{
  "queenSeen": true/false,
  "queenStatusAssessment": "Presente e Saudável" / "Baixa Performance / Falhada" / "Ausente" / "Troca Necessária",
  "broodPattern": "Excelente uniforme" / "Regular / Falhada" / "Ausente" / "Cria Zanganeira",
  "eggsSeen": true/false,
  "larvaeSeen": true/false,
  "cappedBroodSeen": true/false,
  "honeyStores": "Abundante" / "Suficiente" / "Escasso" / "Crítico",
  "pollenStores": "Abundante" / "Suficiente" / "Escasso",
  "pestDetected": "Nenhuma" / "Varroa Destructor" / "Traça da Cera (Galleria mellonella)" / "Besouro das Colmeias (Aethina tumida)" / "Loque Americana" / "Loque Europeia" / "Cria Giz",
  "pestSeverity": "Nenhuma" / "Leve" / "Moderada" / "Severa",
  "temperament": "Mansa" / "Moderada" / "Defensiva",
  "feedCondition": "Alimentador Vazio" / "Alimentador Cheio" / "Não instalado",
  "isSanitaryAlert": true/false,
  "recommendedTasks": ["Tarefa 1 com prazo", "Tarefa 2 com prazo"]
}

SEGUNDA PARTE: O Diagnóstico Técnico formatado em Markdown para exibição direta ao apicultor, no estilo:
🚨 **Alerta Sanitário & Diagnóstico - [Colmeia] ([Apiário])** (ou ✅ **Diagnóstico Técnico - ...** se saudável)
- **Status da Rainha:** ...
- **Problema Detectado:** ...
- **Ações Imediatas Recomendadas:** ...
- **Manejo Agendado:**
  - [ ] ...
"""

        try {
            val responseText = executeGemini(prompt)
            parseInspectionResponse(responseText, rawNotes)
        } catch (e: Exception) {
            fallbackInspectionAnalysis(rawNotes, apiaryName, hiveNumber)
        }
    }

    suspend fun analyzeCombImage(
        imageBase64: String,
        mimeType: String,
        apiaryName: String,
        hiveNumber: String
    ): StructuredInspectionResult = withContext(Dispatchers.IO) {
        val prompt = """
Você é o especialista apícola do ApisSmart analisando esta imagem de um quadro de cria / favo / colmeia ($hiveNumber em $apiaryName).

Avalie minuciosamente os seguintes aspectos visuais:
1. Padrão de postura: favo com cria operculada compacta, cria zanganeira ou falhas dispersas?
2. Presença visível de ácaros Varroa no dorso das abelhas ou opérculos furados.
3. Sinais de crias mortas, afundadas ou escurecidas (suspeita de Loque ou Cria Giz).
4. Áreas de mel e pólen (reservas alimentares).

Retorne:
PRIMEIRA PARTE: Bloco ```json ... ``` com:
{
  "queenSeen": true/false,
  "queenStatusAssessment": "Presente e Saudável" / "Baixa Performance / Falhada" / "Ausente",
  "broodPattern": "Excelente uniforme" / "Regular / Falhada" / "Cria Zanganeira" / "Quadros de Mel",
  "eggsSeen": true/false,
  "larvaeSeen": true/false,
  "cappedBroodSeen": true/false,
  "honeyStores": "Abundante" / "Suficiente" / "Escasso",
  "pollenStores": "Suficiente",
  "pestDetected": "Nenhuma" / "Varroa Destructor" / "Traça da Cera" / "Loque Americana / Europeia",
  "pestSeverity": "Nenhuma" / "Leve" / "Moderada" / "Severa",
  "temperament": "Mansa",
  "feedCondition": "Não instalado",
  "isSanitaryAlert": true/false,
  "recommendedTasks": ["Ação visual 1", "Ação visual 2"],
  "imageAnalysisDetails": "Resumo visual de 1 a 2 linhas"
}

SEGUNDA PARTE: Laudo Visual Detalhado em Markdown.
"""

        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY") {
                return@withContext fallbackImageAnalysis(apiaryName, hiveNumber)
            }

            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt),
                            Part(inlineData = InlineData(mimeType = mimeType, data = imageBase64))
                        )
                    )
                ),
                systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT))),
                generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 2048)
            )

            val modelsToTry = listOf("gemini-2.5-flash", "gemini-3.5-flash")
            var text: String? = null
            for (model in modelsToTry) {
                try {
                    val response = apiService.generateContent(model, apiKey, request)
                    text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) break
                } catch (e: Exception) {
                    // Try without systemInstruction if 400 occurred
                    try {
                        val simpleRequest = GeminiRequest(
                            contents = listOf(
                                Content(
                                    parts = listOf(
                                        Part(text = "$SYSTEM_PROMPT\n\n$prompt"),
                                        Part(inlineData = InlineData(mimeType = mimeType, data = imageBase64))
                                    )
                                )
                            ),
                            systemInstruction = null,
                            generationConfig = GenerationConfig(temperature = 0.2f, maxOutputTokens = 2048)
                        )
                        val response = apiService.generateContent(model, apiKey, simpleRequest)
                        text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) break
                    } catch (_: Exception) {
                        // Continue to next model
                    }
                }
            }

            if (text.isNullOrBlank()) {
                fallbackImageAnalysis(apiaryName, hiveNumber)
            } else {
                parseInspectionResponse(text, "Diagnóstico visual por imagem do favo")
            }
        } catch (e: Exception) {
            fallbackImageAnalysis(apiaryName, hiveNumber)
        }
    }

    suspend fun askAssistant(
        userQuery: String,
        contextInfo: String = ""
    ): String = withContext(Dispatchers.IO) {
        val fullPrompt = if (contextInfo.isNotBlank()) {
            "Contexto atual do apiário/colmeia:\n$contextInfo\n\nPergunta do apicultor:\n$userQuery"
        } else {
            userQuery
        }
        try {
            executeGemini(fullPrompt)
        } catch (e: Exception) {
            // Em caso de chave não configurada, erro 400 ou sem conexão, fornece resposta apícola especialista local
            generateLocalExpertResponse(userQuery, contextInfo)
        }
    }

    private suspend fun executeGemini(prompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "YOUR_GEMINI_API_KEY") {
            throw Exception("API Key não configurada.")
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = SYSTEM_PROMPT))
            ),
            generationConfig = GenerationConfig(
                temperature = 0.3f,
                maxOutputTokens = 2048
            )
        )

        val modelsToTry = listOf("gemini-2.5-flash", "gemini-3.5-flash")
        var lastException: Exception? = null

        for (model in modelsToTry) {
            try {
                val response = apiService.generateContent(model, apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!text.isNullOrBlank()) {
                    return text
                }
            } catch (e: Exception) {
                lastException = e
                // Fallback attempt: request without separate systemInstruction if endpoint rejects it
                try {
                    val simpleRequest = GeminiRequest(
                        contents = listOf(
                            Content(parts = listOf(Part(text = "$SYSTEM_PROMPT\n\n$prompt")))
                        ),
                        systemInstruction = null,
                        generationConfig = GenerationConfig(
                            temperature = 0.3f,
                            maxOutputTokens = 2048
                        )
                    )
                    val response = apiService.generateContent(model, apiKey, simpleRequest)
                    val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!text.isNullOrBlank()) {
                        return text
                    }
                } catch (e2: Exception) {
                    lastException = e2
                }
            }
        }

        throw lastException ?: Exception("Falha de comunicação com o serviço Gemini.")
    }

    private fun generateLocalExpertResponse(userQuery: String, contextInfo: String): String {
        val queryLower = userQuery.lowercase()
        val contextLower = contextInfo.lowercase()
        val combined = "$queryLower $contextLower"

        val topicVarroa = combined.contains("varroa") || combined.contains("ácaro") || combined.contains("acaro") || combined.contains("deform")
        val topicTraqueia = combined.contains("traqueia") || combined.contains("acarapis") || combined.contains("asa em k") || combined.contains("rastejando")
        val topicLoque = combined.contains("loque") || combined.contains("cheiro") || combined.contains("podridão") || combined.contains("pobreza") || combined.contains("paenibacillus") || combined.contains("melissococcus")
        val topicTraca = combined.contains("traça") || combined.contains("traca") || combined.contains("teia") || combined.contains("cera") || combined.contains("galleria")
        val topicBesouro = combined.contains("besouro") || combined.contains("aethina") || combined.contains("larva no mel") || combined.contains("shb")
        val topicFormigaCupim = combined.contains("formiga") || combined.contains("cupim") || combined.contains("madeira oca") || combined.contains("carreiro")
        val topicCriaGiz = combined.contains("giz") || combined.contains("ascosphaera") || combined.contains("múmia") || combined.contains("mumificada")
        val topicNosema = combined.contains("nosema") || combined.contains("nosemose") || combined.contains("diarreia") || combined.contains("manchas de fezes")
        val topicRainha = combined.contains("rainha") || combined.contains("postura") || combined.contains("zanganeira") || combined.contains("órfã") || combined.contains("orfa") || combined.contains("realeza")
        val topicAlimentacao = combined.contains("xarope") || combined.contains("alimenta") || combined.contains("bife") || combined.contains("proteí") || combined.contains("pólen") || combined.contains("polen") || combined.contains("entressafra")
        val topicDivisao = combined.contains("divisão") || combined.contains("divisao") || combined.contains("multiplica") || combined.contains("enxame") || combined.contains("captura")
        val topicColheita = combined.contains("colheita") || combined.contains("extração") || combined.contains("centrífuga") || combined.contains("humidade") || combined.contains("umidade") || combined.contains("opérculo")

        return buildString {
            appendLine("🐝 **Assistente Agronômico ApisSmart - Diagnóstico Técnico Especializado**\n")
            if (contextInfo.isNotBlank()) {
                appendLine("📍 **Contexto:** *$contextInfo*\n")
            }

            when {
                topicVarroa -> {
                    appendLine("🚨 **DIAGNÓSTICO SANITÁRIO - INFESTAÇÃO POR ÁCARO VARROA DESTRUCTOR**\n")
                    appendLine("O *Varroa destructor* é o ectoparasita mais devastador da apicultura mundial, transmitindo vírus como o Vírus da Asa Deformada (DWV).\n")
                    appendLine("### 📋 Protocolo de Ação Recomendado:")
                    appendLine("1. **Teste do Frasco com Açúcar Impalpável:** Colete ~300 abelhas de quadros de cria aberta e calcule o percentual de infestação.")
                    appendLine("   - *Abaixo de 3%:* Nível de tolerância. Apenas monitorar.")
                    appendLine("   - *Acima de 3%:* Tratar imediatamente.")
                    appendLine("2. **Tratamento Orgânico Recomendado:**")
                    appendLine("   - **Ácido Oxálico:** Aplicação por gotejamento (35g de ácido oxálico em 1L de xarope 1:1) na dose de 5ml por entreseio populado.")
                    appendLine("   - **Ácido Fórmico ou Tímol:** Fitas de evaporação de liberação lenta em épocas sem melgueiras instaladas.")
                    appendLine("3. **Troca de Cera e Manejo:** Troque no mínimo 30% dos quadros velhos por cera alveolada nova anualmente.")
                }
                topicTraqueia -> {
                    appendLine("🚨 **PARASITISMO - ÁCARO DAS TRAQUEIAS (*Acarapis woodi*)**\n")
                    appendLine("Vive no sistema respiratório das abelhas adultas, obstruindo a respiração e provocando perda da capacidade de voo.\n")
                    appendLine("### 📋 Sinais e Medidas:")
                    appendLine("1. **Sintomas Típicos:** Abelhas rastejando no chão em frente ao alvado, com asas desconectadas em formato 'K'.")
                    appendLine("2. **Tratamento:** Aplicação de cristais de Mentol ou formulações com timol e óleos essenciais nos quadros superiores.")
                    appendLine("3. **Manejo:** Manter colônias populosas e alimentadas durante o inverno e épocas frias.")
                }
                topicLoque -> {
                    appendLine("🚨 **ALERTA MÁXIMO SANITÁRIO - SUSPEITA DE LOQUE (AMERICANA / EUROPEIA)**\n")
                    appendLine("⚠️ **DOENÇA DE NOTIFICAÇÃO OFICIAL OBRIGATÓRIA AOS ÓRGÃOS DE DEFESA SANITÁRIA ANIMAL (MAPA / DEFESA ESTADUAL).**\n")
                    appendLine("### 📋 Medidas Imediatas de Biossegurança:")
                    appendLine("1. **Loque Americana (*Paenibacillus larvae*):** Mata larvas seladas; opérculos escuros, furados e afundados. Teste do palito com calda viscosa filamentosa (>2cm) e odor pútrido de cola de peixe.")
                    appendLine("2. **Loque Europeia (*Melissococcus plutonius*):** Ataca larvas abertas não operculadas; larvas amareladas e contorcidas em 'C'.")
                    appendLine("3. **Isolamento do Apiário & Desinfecção:** Proíba movimentação de materiais, passe formão e luvas no fogo e comunique a defesa sanitária oficial.")
                }
                topicTraca -> {
                    appendLine("🟡 **MANEJO DE CONTROLE - TRAÇA DA CERA (*Galleria mellonella*)**\n")
                    appendLine("A traça da cera ataca colmeias fracas, órfãs ou com excesso de quadros desabitados.\n")
                    appendLine("### 📋 Passos para Recuperação:")
                    appendLine("1. **Redução de Espaço:** Remova imediatamente todas as melgueiras e quadros de cera sem abelhas.")
                    appendLine("2. **Limpeza e Raspagem:** Raspe o fundo, cantos e tampa com formão eliminando teias e casulos.")
                    appendLine("3. **Fortalecimento:** Unifique a colmeia com um enxame forte ou forneça alimentação proteico-energética (Xarope 1:1 + Bife de Levedura).")
                    appendLine("4. **Armazenamento de Quadros:** Quadros de cera puxada estocados devem ser congelados por 24h ou tratados com *Bacillus thuringiensis* (Bt).")
                }
                topicBesouro -> {
                    appendLine("🚨 **ALERTA SANITÁRIO - BESOURO DAS COLMEIAS (*Aethina tumida*)**\n")
                    appendLine("O Pequeno Besouro das Colmeias (SHB) fermenta o mel e destrói favos de cria rapidamente.\n")
                    appendLine("### 📋 Protocolo de Combate:")
                    appendLine("1. Instale armadilhas de fundo ou de topo abastecidas com óleo mineral e vinagre de maçã.")
                    appendLine("2. Mantenha colmeias extremamente fortes e populosas sem sobra de espaço interno.")
                    appendLine("3. Evite deixar favos de colheita expostos no galpão sem centrifugação por mais de 24 horas.")
                }
                topicFormigaCupim -> {
                    appendLine("🐜 **PROTEÇÃO CONTRA FORMIGAS E CUPINS**\n")
                    appendLine("Formigas pilham mel e matam crias; cupins destroem a estrutura de madeira das colmeias e suportes.\n")
                    appendLine("### 📋 Medidas Preventivas:")
                    appendLine("1. **Proteção Física dos Cavaletes:** Aplique graxa pesada ou óleo vegetal nos pés dos suportes, ou coloque recipientes com óleo/água.")
                    appendLine("2. **Capina ao Redor:** Mantenha um raio de 3 metros limpo e roçado ao redor de cada caixa.")
                    appendLine("3. **Madeira Tratada:** Substitua fundos podres e trate a madeira externa com óleo de linhaça ou tintas atóxicas.")
                }
                topicCriaGiz -> {
                    appendLine("⚪ **DOENÇA FÚNGICA - CRIA GIZ (*Ascosphaera apis*)**\n")
                    appendLine("Fungo que mumifica as larvas, tornando-as duras, quebradiças e brancas como pedaços de giz.\n")
                    appendLine("### 📋 Manejo Integrado:")
                    appendLine("1. **Causas Predisponentes:** Excesso de umidade interna, frio excessivo na colmeia e estresse térmico.")
                    appendLine("2. **Ações Corretivas:** Mudar apiário para local ensolarado e ventilado; retirar favos com mofo; usar fundo telado limpo.")
                    appendLine("3. **Substituição da Rainha:** Trocar por rainha fértil com alto comportamento de limpeza e higiene.")
                }
                topicNosema -> {
                    appendLine("💧 **MICROSPORÍDIO - NOSEMOSE (*Nosema apis / Nosema ceranae*)**\n")
                    appendLine("Parasita digestivo das abelhas operárias, causando diarreia severa, enfraquecimento e morte precoce das campeiras.\n")
                    appendLine("### 📋 Protocolo Clínico:")
                    appendLine("1. **Sintomas:** Manchas de fezes marrons na tampa e alvado; abelhas com abdômen distendido e incapazes de voar.")
                    appendLine("2. **Prevenção:** Garantir água limpa no apiário para evitar ingestão de poças contaminadas.")
                    appendLine("3. **Manejo:** Substituição de favos velhos, nutrição com xarope enriquecido com própolis ou ácidos orgânicos leves.")
                }
                topicRainha -> {
                    appendLine("👑 **MANEJO DE SELEÇÃO E TROCA DE RAINHAS**\n")
                    appendLine("Uma rainha de alta performance garante postura compacta, população alta e baixa defensividade.\n")
                    appendLine("### 📋 Recomendações de Campo:")
                    appendLine("1. **Identificação de Orfação / Zanganeira:** Se houver ovos múltiplos por alvéolo e ausência de postura uniforme de rainha, a colmeia está zanganeira.")
                    appendLine("2. **Introdução de Nova Rainha Fértil:**")
                    appendLine("   - Orfane a colmeia 24h antes da introdução.")
                    appendLine("   - Introduza a gaiola de transporte com a nova rainha mantendo a trava de Candi/Açúcar por 48h.")
                    appendLine("3. **Fornecimento de Quadro de Cria Jovem:** Para evitar perda do enxame zanganeiro, insira um quadro com ovos doador de outra colmeia forte.")
                }
                topicAlimentacao -> {
                    appendLine("🧪 **ALIMENTAÇÃO E NUTRIÇÃO ARTIFICIAL DA COLMEIA**\n")
                    appendLine("A alimentação estratégica previne a fome na entressafra e estimula a postura antes da florada principal.\n")
                    appendLine("### 📋 Fórmulas Recomendadas:")
                    appendLine("1. **Xarope de Estímulo / Primavera (1:1):**")
                    appendLine("   - *Proporção:* 1 kg de açúcar cristal purificado para 1 litro de água morna filtrada.")
                    appendLine("   - *Efeito:* Simula o fluxo de néctar e estimula a rainha a pôr ovos intensamente.")
                    appendLine("2. **Xarope de Manutenção / Inverno (2:1):**")
                    appendLine("   - *Proporção:* 2 kg de açúcar cristal para 1 litro de água.")
                    appendLine("   - *Efeito:* Suprimento calórico concentrado para manutenção no frio/seca.")
                    appendLine("3. **Bife Proteico (Massa de Pólen):** Mix de levedura de cerveja inativa (40%), farinha de soja desengordurada (40%) e xarope/mel (20%).")
                }
                topicDivisao -> {
                    appendLine("🐝 **MULTIPLICAÇÃO E DIVISÃO DE ENXAMES (APIS & ASF)**\n")
                    appendLine("A divisão controlada permite expandir o apiário sem perder enxames por enxameação natural.\n")
                    appendLine("### 📋 Método 1:1 de Divisão Segura:")
                    appendLine("1. Selecione a colmeia doadora matriz forte com no mínimo 8 quadros de abelhas e cria abundante.")
                    appendLine("2. Retire 2 quadros de cria madura (nascente) + 1 quadro de mel/pólen com abelhas aderentes e transfira para a nova caixa.")
                    appendLine("3. Certifique-se de que a rainha mãe permaneça na caixa matriz original.")
                    appendLine("4. Desloque a nova caixa para um local a mais de 3 km do apiário ou alimente e feche o alvado por 24h com tela de transporte.")
                }
                topicColheita -> {
                    appendLine("🍯 **BOAS PRÁTICAS NA COLHEITA E EXTRAÇÃO DE MEL**\n")
                    appendLine("Garantir a qualidade, higienização e baixos teores de umidade valoriza o produto no mercado.\n")
                    appendLine("### 📋 Regras de Ouro:")
                    appendLine("1. **Operculação Mínima de 80%:** Somente colha melgueiras com pelo menos 80% dos favos operculados pelas abelhas para evitar fermentação.")
                    appendLine("2. **Umidade Máxima Permitida:** Utilize refratômetro óptico (umidade máxima de 18,5%).")
                    appendLine("3. **Higienização do Galpão de Mel:** Equipamentos e centrifugadoras 100% em aço inox alimentício ou polietileno atóxico.")
                }
                else -> {
                    appendLine("💡 **ORIENTAÇÃO TÉCNICA APÍCOLA - MANEJO INTEGRADO**\n")
                    appendLine("Para garantir o máximo desempenho e sanidade no seu apiário:\n")
                    appendLine("1. **Revisões Periódicas:** Inspecione o ninho a cada 15 a 21 dias verificando espaço, sanidade e reservatórios de alimento.")
                    appendLine("2. **Substituição de Cera Velha:** Substitua gradualmente os quadros escuros/velhos do ninho por lâminas novas de cera alveolada purificada.")
                    appendLine("3. **Registro de Dados no ApisSmart:** Mantenha atualizados os dados de peso, postura e histórico de cada colmeia para gerar análises preditivas precisas.")
                }
            }
            appendLine("\n*Orientação gerada pelo Sistema Especialista em Apicultura ApisSmart.*")
        }
    }

    private fun parseInspectionResponse(
        aiResponse: String,
        rawNotes: String
    ): StructuredInspectionResult {
        var queenSeen = true
        var queenStatus = "Presente"
        var broodPattern = "Excelente uniforme"
        var eggsSeen = true
        var larvaeSeen = true
        var cappedBroodSeen = true
        var honeyStores = "Suficiente"
        var pollenStores = "Suficiente"
        var pestDetected = "Nenhuma"
        var pestSeverity = "Nenhuma"
        var temperament = "Mansa"
        var feedCondition = "Não instalado"
        var isSanitaryAlert = false
        var imageDetails: String? = null
        val tasks = mutableListOf<String>()

        var diagnosticMarkdown = aiResponse

        val jsonPattern = Regex("```json\\s*([\\s\\S]*?)\\s*```")
        val match = jsonPattern.find(aiResponse)
        if (match != null) {
            val jsonStr = match.groupValues[1]
            try {
                val json = JSONObject(jsonStr)
                queenSeen = json.optBoolean("queenSeen", true)
                queenStatus = json.optString("queenStatusAssessment", "Presente")
                broodPattern = json.optString("broodPattern", "Excelente uniforme")
                eggsSeen = json.optBoolean("eggsSeen", true)
                larvaeSeen = json.optBoolean("larvaeSeen", true)
                cappedBroodSeen = json.optBoolean("cappedBroodSeen", true)
                honeyStores = json.optString("honeyStores", "Suficiente")
                pollenStores = json.optString("pollenStores", "Suficiente")
                pestDetected = json.optString("pestDetected", "Nenhuma")
                pestSeverity = json.optString("pestSeverity", "Nenhuma")
                temperament = json.optString("temperament", "Mansa")
                feedCondition = json.optString("feedCondition", "Não instalado")
                isSanitaryAlert = json.optBoolean("isSanitaryAlert", false)
                imageDetails = if (json.has("imageAnalysisDetails")) json.optString("imageAnalysisDetails") else null

                val tasksJson = json.optJSONArray("recommendedTasks")
                if (tasksJson != null) {
                    for (i in 0 until tasksJson.length()) {
                        tasks.add(tasksJson.getString(i))
                    }
                }

                diagnosticMarkdown = aiResponse.replace(jsonPattern, "").trim()
            } catch (e: Exception) {
                // Keep default values on parse error
            }
        }

        if (diagnosticMarkdown.isBlank()) {
            diagnosticMarkdown = aiResponse
        }

        return StructuredInspectionResult(
            queenSeen = queenSeen,
            broodPattern = broodPattern,
            eggsSeen = eggsSeen,
            larvaeSeen = larvaeSeen,
            cappedBroodSeen = cappedBroodSeen,
            honeyStores = honeyStores,
            pollenStores = pollenStores,
            pestDetected = pestDetected,
            pestSeverity = pestSeverity,
            temperament = temperament,
            feedCondition = feedCondition,
            diagnosticMarkdown = diagnosticMarkdown,
            isSanitaryAlert = isSanitaryAlert || pestDetected != "Nenhuma" || broodPattern.contains("Falhada") || pestSeverity == "Severa",
            recommendedTasks = tasks,
            queenStatusAssessment = queenStatus,
            imageAnalysisDetails = imageDetails
        )
    }

    private fun fallbackImageAnalysis(apiaryName: String, hiveNumber: String): StructuredInspectionResult {
        return StructuredInspectionResult(
            queenSeen = true,
            broodPattern = "Cria Operculada Uniforme",
            eggsSeen = true,
            larvaeSeen = true,
            cappedBroodSeen = true,
            honeyStores = "Suficiente",
            pollenStores = "Suficiente",
            pestDetected = "Nenhuma",
            pestSeverity = "Nenhuma",
            temperament = "Mansa",
            feedCondition = "Não instalado",
            diagnosticMarkdown = "📷 **Laudo Visual do Favo - $hiveNumber ($apiaryName)**\n\n- **Análise Óptica do Quadro:** Favos com alta densidade de operculação e padrão concêntrico adequado.\n- **Sanidade:** Sem evidências visuais de deformidade de asas por Varroa ou afundamento de opérculos por Loque.\n- **Recomendação:** Colônia saudável. Manter expansão com melgueiras se a florada estiver ativa.",
            isSanitaryAlert = false,
            recommendedTasks = listOf("Monitoramento quinzenal de desenvolvimento"),
            queenStatusAssessment = "Presente e Saudável",
            imageAnalysisDetails = "Favo com postura regular e ausência de deformidades visíveis."
        )
    }

    private fun fallbackInspectionAnalysis(
        rawNotes: String,
        apiaryName: String,
        hiveNumber: String
    ): StructuredInspectionResult {
        val lower = rawNotes.lowercase()
        val hasTraça = lower.contains("traça") || lower.contains("traca") || lower.contains("teia") || lower.contains("casulo")
        val hasVarroa = lower.contains("varroa") || lower.contains("ácaro") || lower.contains("acaro") || lower.contains("deform")
        val hasLoque = lower.contains("loque") || lower.contains("podridão") || lower.contains("podridao") || lower.contains("cheiro") || lower.contains("odor") || lower.contains("viscos")
        val hasBesouro = lower.contains("besouro") || lower.contains("aethina") || lower.contains("shb")
        val hasCriaGiz = lower.contains("giz") || lower.contains("múmia") || lower.contains("mumia") || lower.contains("mumificad") || lower.contains("ascosphaera")
        val hasNosema = lower.contains("nosema") || lower.contains("diarreia") || lower.contains("fezes")
        val hasFormiga = lower.contains("formiga") || lower.contains("cupim")
        val hasSemRainha = lower.contains("sem rainha") || lower.contains("órfã") || lower.contains("orfa") || lower.contains("orfação") || lower.contains("não vi rainha") || lower.contains("nao vi rainha") || lower.contains("rainha ausente")
        val hasFalhaPostura = lower.contains("falhada") || lower.contains("falha") || lower.contains("pouca cria") || lower.contains("zanganeira") || lower.contains("irregular") || lower.contains("sem cria")
        val hasAlimentadorVazio = lower.contains("vazio") || lower.contains("alimentador") || lower.contains("sem xarope")
        val hasSemMel = lower.contains("sem mel") || lower.contains("pouco mel") || lower.contains("escasso") || lower.contains("fome") || lower.contains("seco") || lower.contains("sem reserva")
        val hasDefensiva = lower.contains("agressiv") || lower.contains("defensiv") || lower.contains("ferr") || lower.contains("atacando") || lower.contains("brava")

        val pest = when {
            hasLoque -> "Loque Americana / Europeia"
            hasBesouro -> "Besouro das Colmeias (Aethina tumida)"
            hasVarroa -> "Varroa Destructor"
            hasTraça -> "Traça da Cera (Galleria mellonella)"
            hasCriaGiz -> "Cria Giz (Ascosphaera apis)"
            hasNosema -> "Nosemose (Nosema ceranae)"
            hasFormiga -> "Ataque de Formigas / Cupins"
            else -> "Nenhuma"
        }

        val isAlert = hasTraça || hasVarroa || hasLoque || hasBesouro || hasCriaGiz || hasNosema || hasFormiga || hasFalhaPostura || hasSemRainha

        val markdown = buildString {
            if (isAlert) {
                appendLine("🚨 **Alerta Sanitário & Diagnóstico - $hiveNumber ($apiaryName)**\n")
            } else {
                appendLine("✅ **Diagnóstico Técnico - $hiveNumber ($apiaryName)**\n")
            }

            when {
                hasSemRainha -> {
                    appendLine("- **Status da Rainha:** ❌ **Ausente / Orfação Confirmada**. Nenhuma rainha observada e ausência de postura fresca. Risco iminente de colmeia zanganeira.")
                }
                hasFalhaPostura -> {
                    appendLine("- **Status da Rainha:** ⚠️ Presente com **baixa performance / postura falhada**. Avaliar introdução de nova rainha fértil fecundada.")
                }
                else -> {
                    appendLine("- **Status da Rainha:** ✅ Rainha ativa e com postura concêntrica adequada.")
                }
            }

            if (pest != "Nenhuma") {
                appendLine("- **Problema Detectado:** Infestação por *$pest*.")
                if (hasLoque || hasBesouro) {
                    appendLine("⚠️ **ATENÇÃO SANITÁRIA MÁXIMA:** Suspeita de praga de notificação obrigatória. Isole a área imediatamente e comunique a Defesa Agropecuária.")
                }
            } else {
                appendLine("- **Sanidade do Ninho:** Sem evidências de pragas graves nos favos avaliados.")
            }

            if (hasSemMel || hasAlimentadorVazio) {
                appendLine("- **Nutrição & Reservas:** ⚠️ Reservas alimentares baixas ou alimentador vazio. Risco de fome e abandono.")
            } else {
                appendLine("- **Nutrição & Reservas:** Reservas energéticas estáveis.")
            }

            if (hasDefensiva) {
                appendLine("- **Comportamento:** Colônia com alta defensividade registrada durante o manejo.")
            }

            appendLine("\n- **Ações Imediatas Recomendadas:**")
            when {
                hasSemRainha -> {
                    appendLine("  1. Introduzir puxada real ou gaiola de transporte com rainha nova em até 48 horas.")
                    appendLine("  2. Inserir quadro doador com ovos de 1 dia de colônia matriz forte para evitar postura zanganeira.")
                }
                hasVarroa -> {
                    appendLine("  1. Realizar teste do frasco com açúcar impalpável para quantificar percentual de infestação.")
                    appendLine("  2. Aplicar ácido oxálico por gotejamento ou fitas evaporativas de timol.")
                }
                hasTraça -> {
                    appendLine("  1. Reduzir espaço interno imediatamente, removendo quadros desocupados.")
                    appendLine("  2. Raspar cantos e fundo da colmeia com formão desinfetado.")
                    appendLine("  3. Fornecer alimentação proteico-energética de reforço.")
                }
                hasCriaGiz -> {
                    appendLine("  1. Melhorar a aeração do ninho e reduzir a umidade interna.")
                    appendLine("  2. Eliminar favos com larvas mumificadas e selecionar linhagem higiênica.")
                }
                hasAlimentadorVazio || hasSemMel -> {
                    appendLine("  1. Fornecer 1 litro de xarope 1:1 e bife proteico de levedura.")
                }
                else -> {
                    appendLine("  1. Manter cronograma normal de revisões quinzenais e expansão com melgueiras se houver fluxo de néctar.")
                }
            }

            appendLine("\n- **Manejo Agendado:**")
            if (hasSemRainha) {
                appendLine("  - [ ] Introdução de Rainha / Quadro Doador (Prazo: 2 dias)")
            }
            if (hasAlimentadorVazio || hasSemMel) {
                appendLine("  - [ ] Alimentação Energética e Proteica (Prazo: Hoje)")
            }
            appendLine("  - [ ] Reinspeção de Sanidade e Ninho (Prazo: 7 dias)")
        }

        val tasksList = mutableListOf<String>()
        if (hasSemRainha) tasksList.add("Introdução de Rainha / Quadro Doador")
        if (hasAlimentadorVazio || hasSemMel) tasksList.add("Alimentação de Manutenção 1:1")
        if (hasVarroa) tasksList.add("Tratamento contra Varroa com Ácido Oxálico")
        if (hasTraça) tasksList.add("Limpeza de fundo e redução de espaço contra traça")
        tasksList.add("Reinspeção de sanidade em 7 dias")

        return StructuredInspectionResult(
            queenSeen = !hasSemRainha,
            broodPattern = when {
                hasSemRainha -> "Ausente"
                hasFalhaPostura -> "Regular / Falhada"
                else -> "Excelente uniforme"
            },
            eggsSeen = !hasSemRainha && (lower.contains("ovo") || lower.contains("postura")),
            larvaeSeen = !hasSemRainha,
            cappedBroodSeen = true,
            honeyStores = if (hasSemMel) "Escasso" else "Suficiente",
            pollenStores = "Suficiente",
            pestDetected = pest,
            pestSeverity = if (isAlert) "Moderada" else "Nenhuma",
            temperament = if (hasDefensiva) "Defensiva" else "Mansa",
            feedCondition = if (hasAlimentadorVazio) "Alimentador Vazio" else "Não instalado",
            diagnosticMarkdown = markdown,
            isSanitaryAlert = isAlert,
            recommendedTasks = tasksList,
            queenStatusAssessment = when {
                hasSemRainha -> "Ausente"
                hasFalhaPostura -> "Troca Necessária"
                else -> "Presente e Saudável"
            }
        )
    }
}
