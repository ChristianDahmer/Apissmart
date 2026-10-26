package com.example.util

import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class BotanicalBiome(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val description: String,
    val keyRegions: String
) {
    CERRADO(
        title = "Cerrado & Savana Central",
        emoji = "🌾",
        subtitle = "Planalto Central, MG, GO, MT, MS, DF, TO, Oeste da BA",
        description = "Clima tropical estacional com estação seca pronunciada (Maio a Setembro) e chuvas de verão. Apresenta grande diversidade de arbustos nectaríferos, como Cipó-uva e Assa-peixe, e árvores frutíferas como Pequi.",
        keyRegions = "Centro-Oeste, Triângulo Mineiro, Norte de MG, Sul de GO, TO, Oeste da Bahia"
    ),
    MATA_ATLANTICA(
        title = "Mata Atlântica & Litoral",
        emoji = "🌳",
        subtitle = "Sudeste, Leste do Sul, Faixa Litorânea e Serrana",
        description = "Alta umidade, serras e florestas biodiversas com grande ocorrência de Eucaliptos, Alecrim-do-campo (própolis verde), Silvestres aromáticos e floradas de Citros (Laranjeira).",
        keyRegions = "SP, RJ, ES, Leste de MG, Leste do PR e SC, Litoral Sul ao Nordeste"
    ),
    CAATINGA(
        title = "Caatinga & Semiárido",
        emoji = "🌵",
        subtitle = "Sertão Nordestino, Agreste, Norte de MG",
        description = "Clima semiárido com chuvas concentradas no verão/outono. As floradas são explosivas logo após as primeiras chuvas (Marmeleiro, Jurema-preta, Jitirana e Bamburral). Produz méis claros e transparentes de altíssimo valor de mercado.",
        keyRegions = "CE, PI, RN, PB, PE, AL, SE, Sertão da BA, Vale do Jequitinhonha MG"
    ),
    PAMPA(
        title = "Pampa & Planalto Sul",
        emoji = "🍃",
        subtitle = "Rio Grande do Sul, Planalto Serrano de SC e PR",
        description = "Clima temperado/subtropical com invernos rigorosos e geadas. Destaque para Canola no inverno, Bracatinga (mel de melato nos anos pares), Trevo-branco, Macieira e pastagens ricas.",
        keyRegions = "RS, Planalto Serrano Catarinense, Centro-Sul do PR"
    ),
    PANTANAL(
        title = "Pantanal & Bacias",
        emoji = "🌊",
        subtitle = "Planície Inundável de Mato Grosso e Mato Grosso do Sul",
        description = "Ciclos dinâmicos de cheia e vazante. Grande abundância de floradas ripárias, Aguapé, Cambará, Piúvas (Ipês) e Pimenteira silvestre com néctar abundante no outono e inverno.",
        keyRegions = "MT, MS, Pantanal Norte e Sul, Bacia do Rio Paraguai"
    ),
    AMAZONIA(
        title = "Amazônia & Norte",
        emoji = "🌴",
        subtitle = "Norte do Brasil, Calha Amazônica",
        description = "Floresta equatorial úmida. Floradas de grande porte arbóreo (Castanheira-do-Brasil, Açaizeiros, Cupuaçuzeiros e Andirobas). Produção constante com picos na estação menos chuvosa.",
        keyRegions = "AM, PA, AC, RO, RR, AP, Norte de MT, Oeste do MA"
    ),
    GENERAL_BRASIL(
        title = "Ampla Ocorrência (Geral)",
        emoji = "🇧🇷",
        subtitle = "Culturas agrícolas e espécies adaptadas em todo o país",
        description = "Espécies agrícolas cultivadas para grãos e frutas (Girassol, Canola, Café, Citros, Eucaliptos, Melancia, Milho) e plantas ruderais silvestres de ampla ocorrência.",
        keyRegions = "Nacional / Qualquer região do Brasil"
    )
}

enum class BotanicalResource(
    val title: String,
    val emoji: String,
    val badgeColorHex: Long,
    val description: String
) {
    NECTAR("Néctar", "🍯", 0xFFFFA000, "Fonte de carboidratos, energia e produção de mel"),
    POLLEN("Pólen", "🟡", 0xFFFBC02D, "Fonte de proteínas, lipídios e vitaminas para criação de larvas"),
    NECTAR_AND_POLLEN("Néctar & Pólen", "🍯🟡", 0xFF388E3C, "Fornecimento completo para postura, desenvolvimento e safra"),
    RESIN("Resina (Própolis)", "🌿", 0xFF00796B, "Coleta de exsudatos para fabricação de própolis e vedação"),
    HONEYDEW("Melato", "🌲", 0xFF5D4037, "Seiva açucarada excretada por insetos sugadores (ex: Bracatinga)")
}

enum class BloomStatus(
    val label: String,
    val iconEmoji: String,
    val tagColor: Color
) {
    PEAK_BLOOM("Pico de Floração", "🌸", Color(0xFFE91E63)),
    ACTIVE_BLOOM("Em Floração", "🌼", Color(0xFF2E7D32)),
    PRE_BLOOM("Pré-Safra (Estimular)", "🌱", Color(0xFF0288D1)),
    POST_BLOOM("Pós-Safra (Colher/Retirar)", "🍯", Color(0xFFEF6C00)),
    OFF_SEASON("Entressafra (Manutenção)", "🍂", Color(0xFF6D4C41))
}

data class FloralSpecies(
    val id: String,
    val popularName: String,
    val scientificName: String,
    val family: String,
    val biome: BotanicalBiome,
    val resourceType: BotanicalResource,
    val bloomMonths: List<Int>, // 1 = Jan, ..., 12 = Dez
    val peakMonths: List<Int>,
    val honeyColor: String,
    val honeyFlavor: String,
    val crystallizationSpeed: String,
    val apiculturalValue: String,
    val harvestRecommendation: String,
    val nutritionRecommendation: String,
    val pollenAbundance: String, // Baixa, Média, Alta, Muito Alta
    val nectarAbundance: String, // Baixa, Média, Alta, Muito Alta
    val specialNotes: String = ""
)

data class SeasonalAnalysis(
    val currentMonth: Int,
    val monthName: String,
    val seasonName: String,
    val activeSpecies: List<FloralSpecies>,
    val peakSpecies: List<FloralSpecies>,
    val totalSpeciesCount: Int,
    val nectarFlowIndex: String, // Nenhum, Fraco, Moderado, Intenso (Grande Safra)
    val pollenSupplyIndex: String, // Escasso, Médio, Abundante
    val managementPhase: String, // ENTRESSAFRA_CRITICA, PRE_SAFRA_ESTIMULO, GRANDE_SAFRA_MELGUEIRAS, COLHEITA_MADURACAO, MANUTENCAO_OUTONO
    val harvestAdvice: String,
    val nutritionAdvice: String,
    val recommendedActions: List<String>
)

object FloralCalendarEngine {

    val MONTH_NAMES = listOf(
        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    )

    val MONTH_ABBR = listOf(
        "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
        "Jul", "Ago", "Set", "Out", "Nov", "Dez"
    )

    /**
     * Catálogo botânico especializado da apicultura brasileira
     */
    val BOTANICAL_SPECIES_CATALOG: List<FloralSpecies> = listOf(
        // === CERRADO ===
        FloralSpecies(
            id = "cer_cipo_uva",
            popularName = "Cipó-uva",
            scientificName = "Serjania erecta / caracasana",
            family = "Sapindaceae",
            biome = BotanicalBiome.CERRADO,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(5, 6, 7),
            peakMonths = listOf(6),
            honeyColor = "Claro a Âmbar Claro (transparente a amarelo suave)",
            honeyFlavor = "Suave, floral aromático, altamente apreciado pelo consumidor",
            crystallizationSpeed = "Lenta e fina (não cristaliza rapidamente)",
            apiculturalValue = "Excepcional - Principal mel do Cerrado brasileiro",
            harvestRecommendation = "Instalar melgueiras limpas com cera alveolada 15 a 20 dias antes (meados de Maio). Colher e centrifugar imediatamente no final de Julho para evitar mistura com floradas escuras.",
            nutritionRecommendation = "Fornecer alimentação estimulante (xarope 1:1) com promotor de crescimento de 30 a 45 dias antes (Abril) para elevar a postura da rainha a 2.000 ovos/dia e ter pico populacional de campeiras.",
            pollenAbundance = "Média",
            nectarAbundance = "Muito Alta",
            specialNotes = "Mel nobre que atinge excelente cotação de mercado. Ideal para selos de denominação regional."
        ),
        FloralSpecies(
            id = "cer_assa_peixe",
            popularName = "Assa-peixe",
            scientificName = "Vernonia polysphaera",
            family = "Asteraceae",
            biome = BotanicalBiome.CERRADO,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(4, 5, 6, 7),
            peakMonths = listOf(5, 6),
            honeyColor = "Âmbar Claro brilhante",
            honeyFlavor = "Floral persistente, levemente balsâmico com notas medicinais",
            crystallizationSpeed = "Média a rápida, textura aveludada",
            apiculturalValue = "Muito Alta - Fornece néctar abundante e excelente pólen",
            harvestRecommendation = "Acompanhar operculação dos favos (mínimo 80% operculado). Verificar umidade com refratômetro (manter abaixo de 18% para evitar fermentação).",
            nutritionRecommendation = "Pólen abundante na natureza dispensando suplementação proteica durante o pico. Se houver frio tardio, verificar reservas no ninho.",
            pollenAbundance = "Alta",
            nectarAbundance = "Alta",
            specialNotes = "Propriedades tradicionalmente associadas a benefícios para o trato respiratório."
        ),
        FloralSpecies(
            id = "cer_pequi",
            popularName = "Pequi",
            scientificName = "Caryocar brasiliense",
            family = "Caryocaraceae",
            biome = BotanicalBiome.CERRADO,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(9, 10, 11),
            peakMonths = listOf(10),
            honeyColor = "Dourado a Âmbar",
            honeyFlavor = "Intenso, característico e frutado",
            crystallizationSpeed = "Média",
            apiculturalValue = "Alta - Estimulante primaveril pós-seca",
            harvestRecommendation = "Geralmente coincide com o início das chuvas da primavera. Monitorar introdução de melgueiras extras se o fluxo nectarífero for forte.",
            nutritionRecommendation = "Excelente para transição pós-estiagem. Se as primeiras chuvas atrasarem, continuar com alimentação de subsistência até a abertura das gemas florais.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Alta",
            specialNotes = "Árvore símbolo do Cerrado. O pólen é muito nutritivo para retomada da postura."
        ),
        FloralSpecies(
            id = "cer_angico",
            popularName = "Angico-do-Cerrado",
            scientificName = "Anadenanthera colubrina / peregrina",
            family = "Fabaceae",
            biome = BotanicalBiome.CERRADO,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(9, 10, 11, 12),
            peakMonths = listOf(10, 11),
            honeyColor = "Âmbar Escuro a Castanho",
            honeyFlavor = "Encorpado, sabor amadeirado marcante e rico em minerais",
            crystallizationSpeed = "Média",
            apiculturalValue = "Alta - Grande carga de pólen de alta digestibilidade",
            harvestRecommendation = "Se o objetivo for mel claro comercial, colha o Cipó-uva antes do Angico. Mel de Angico é ideal para blend e mel enriquecido em ferro.",
            nutritionRecommendation = "Elimina qualquer carência de pólen na colmeia. Excelente momento para divisão de enxames e produção de rainhas.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Alta",
            specialNotes = "Árvore nativa imponente com excelente suporte ecológico e fixação de nitrogênio."
        ),
        FloralSpecies(
            id = "cer_barbatimao",
            popularName = "Barbatimão",
            scientificName = "Stryphnodendron adstringens",
            family = "Fabaceae",
            biome = BotanicalBiome.CERRADO,
            resourceType = BotanicalResource.POLLEN,
            bloomMonths = listOf(10, 11, 12),
            peakMonths = listOf(11),
            honeyColor = "Âmbar Escuro",
            honeyFlavor = "Adstringente e marcante",
            crystallizationSpeed = "Lenta",
            apiculturalValue = "Média para mel / Alta para manutenção e própolis",
            harvestRecommendation = "Utilizado primariamente para manutenção do enxame e postura da rainha.",
            nutritionRecommendation = "Suplementação proteica não necessária devido ao pólen abundante da floração.",
            pollenAbundance = "Alta",
            nectarAbundance = "Baixa",
            specialNotes = "Casca rica em taninos; as abelhas também coletam exsudatos."
        ),

        // === MATA ATLÂNTICA ===
        FloralSpecies(
            id = "ma_eucalipto",
            popularName = "Eucalipto",
            scientificName = "Eucalyptus grandis / robusta / urophylla",
            family = "Myrtaceae",
            biome = BotanicalBiome.MATA_ATLANTICA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(3, 4, 5, 6, 7),
            peakMonths = listOf(4, 5),
            honeyColor = "Âmbar Escuro a Castanho avermelhado",
            honeyFlavor = "Intenso, amadeirado, resinoso, leve toque mentolado",
            crystallizationSpeed = "Rápida, com grânulos grossos a médios",
            apiculturalValue = "Excepcional - Maior volume de produção apícola do Sudeste/Sul",
            harvestRecommendation = "Colheita de outono/inverno. Instalar 2 a 3 melgueiras por colmeia devido ao grande fluxo diário. Desopercular favos imediatamente pós-colheita.",
            nutritionRecommendation = "Garantir colmeias fortes em Fevereiro com alimentação proteica antes do fluxo. Atenção no final da florada (Julho/Agosto) pois o corte de néctar é brusco.",
            pollenAbundance = "Alta",
            nectarAbundance = "Muito Alta",
            specialNotes = "Mel rico em ferro, fósforo e minerais. Um dos mais exportados do Brasil."
        ),
        FloralSpecies(
            id = "ma_laranjeira",
            popularName = "Flor de Laranjeira (Citros)",
            scientificName = "Citrus sinensis / reticulata",
            family = "Rutaceae",
            biome = BotanicalBiome.MATA_ATLANTICA,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(8, 9, 10),
            peakMonths = listOf(9),
            honeyColor = "Extra Claro a Branco Água (límpido e nobre)",
            honeyFlavor = "Suave, cítrico perfumado, aroma floral inconfundível",
            crystallizationSpeed = "Lenta",
            apiculturalValue = "Excepcional - Cotação premium de mercado para exportação e gourmet",
            harvestRecommendation = "A florada dura apenas 20 a 30 dias. Instale tela excluidora de rainha. Colher assim que 75% dos favos estiverem operculados para preservar a florada monofloral pura.",
            nutritionRecommendation = "Alimentação pré-safra em Julho é crucial: xarope de sacarose 1:1 e bife proteico para ter enxame populoso e jovem exatamente no início de Agosto.",
            pollenAbundance = "Média",
            nectarAbundance = "Muito Alta",
            specialNotes = "Exige coordenação com citricultores para evitar contaminação por defensivos agrícolas."
        ),
        FloralSpecies(
            id = "ma_alecrim_campo",
            popularName = "Alecrim-do-campo (Vassourinha)",
            scientificName = "Baccharis dracunculifolia",
            family = "Asteraceae",
            biome = BotanicalBiome.MATA_ATLANTICA,
            resourceType = BotanicalResource.RESIN,
            bloomMonths = listOf(2, 3, 4, 5),
            peakMonths = listOf(3, 4),
            honeyColor = "Âmbar Claro esverdeado",
            honeyFlavor = "Canforado, balsâmico e resinoso",
            crystallizationSpeed = "Média",
            apiculturalValue = "Excepcional para PRÓPOLIS VERDE e mel funcional",
            harvestRecommendation = "Instalar coletores de própolis tipo tela (CPI) ou grade inteligente. Trocar coletores a cada 7 a 10 dias para própolis de primeira qualidade.",
            nutritionRecommendation = "Manter boa suplementação energética nas colmeias produtoras de própolis para que não haja consumo excessivo das reservas internas.",
            pollenAbundance = "Média",
            nectarAbundance = "Média",
            specialNotes = "Planta exclusiva responsável pela consagrada Própolis Verde brasileira (Artepina C)."
        ),
        FloralSpecies(
            id = "ma_aroeira_vermelha",
            popularName = "Aroeira-vermelha",
            scientificName = "Schinus terebinthifolius",
            family = "Anacardiaceae",
            biome = BotanicalBiome.MATA_ATLANTICA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(2, 3, 4, 5),
            peakMonths = listOf(3),
            honeyColor = "Âmbar Escuro com reflexos avermelhados",
            honeyFlavor = "Marcante, picante suave, notas de especiarias e pimenta-rosa",
            crystallizationSpeed = "Lenta",
            apiculturalValue = "Muito Alta - Rico em fenólicos e antioxidantes",
            harvestRecommendation = "Excelente para colheita de outono. Monitorar umidade cuidadosamente.",
            nutritionRecommendation = "Fornece pólen nutritivo que prepara as colmeias para o período frio de inverno.",
            pollenAbundance = "Alta",
            nectarAbundance = "Alta",
            specialNotes = "Produto com Indicação Geográfica no Norte de Minas e Litoral Sudeste."
        ),
        FloralSpecies(
            id = "ma_capixingui",
            popularName = "Capixingui",
            scientificName = "Croton floribundus",
            family = "Euphorbiaceae",
            biome = BotanicalBiome.MATA_ATLANTICA,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(11, 12, 1),
            peakMonths = listOf(12),
            honeyColor = "Âmbar Claro límpido",
            honeyFlavor = "Agradável, doce suave, bouquet floral",
            crystallizationSpeed = "Média",
            apiculturalValue = "Alta - Suporte e produção de pleno verão",
            harvestRecommendation = "Colheita de Dezembro/Janeiro. Permite renovação de favos velhos com puxada rápida de cera.",
            nutritionRecommendation = "Dispensada qualquer alimentação complementar devido à pujança da floração.",
            pollenAbundance = "Média",
            nectarAbundance = "Muito Alta",
            specialNotes = "Árvore pioneira frequente em matas secundárias e beiras de mata."
        ),

        // === CAATINGA ===
        FloralSpecies(
            id = "caa_marmeleiro",
            popularName = "Marmeleiro-da-Caatinga",
            scientificName = "Croton sonderianus",
            family = "Euphorbiaceae",
            biome = BotanicalBiome.CAATINGA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(1, 2, 3, 4),
            peakMonths = listOf(2, 3),
            honeyColor = "Branco Água a Âmbar Extra Claro (quase transparente)",
            honeyFlavor = "Delicado, suave, floral sutil, não enjoativo",
            crystallizationSpeed = "Muito lenta (mantém-se líquido por longo tempo)",
            apiculturalValue = "Excepcional - Ouro da Caatinga e líder de exportação orgânica",
            harvestRecommendation = "A floração ocorre 5 a 10 dias após a primeira chuva significativa. Ter melgueiras montadas com antecedência. Colheita veloz logo ao opercular.",
            nutritionRecommendation = "Na época de seca pré-chuvas (Outubro/Dezembro), alimentar com xarope e proteína para manter o enxame vivo para a explosão floral pós-chuva.",
            pollenAbundance = "Alta",
            nectarAbundance = "Muito Alta",
            specialNotes = "Principal mel comercial orgânico do semiárido brasileiro (PI, CE, RN, BA)."
        ),
        FloralSpecies(
            id = "caa_jurema_preta",
            popularName = "Jurema-preta",
            scientificName = "Mimosa tenuiflora",
            family = "Fabaceae",
            biome = BotanicalBiome.CAATINGA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(2, 3, 4, 5),
            peakMonths = listOf(3, 4),
            honeyColor = "Âmbar Claro a Âmbar",
            honeyFlavor = "Encorpado, leve aroma de ervas silvestres",
            crystallizationSpeed = "Média",
            apiculturalValue = "Muito Alta - Explosão de pólen e sustentação de cria",
            harvestRecommendation = "Pode ser misturado harmonicamente com o Marmeleiro gerando o clássico Mel Silvestre da Caatinga.",
            nutritionRecommendation = "Excelente para produção de própolis marrom e multiplicação de colônias.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Alta",
            specialNotes = "Espécie de grande resiliência hídrica e regeneração rápida."
        ),
        FloralSpecies(
            id = "caa_jitirana",
            popularName = "Jitirana",
            scientificName = "Ipomoea bahiensis / carnea",
            family = "Convolvulaceae",
            biome = BotanicalBiome.CAATINGA,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(3, 4, 5, 6),
            peakMonths = listOf(4),
            honeyColor = "Âmbar Claro",
            honeyFlavor = "Doce suave, floral",
            crystallizationSpeed = "Média",
            apiculturalValue = "Alta - Encerramento de safra no semiárido",
            harvestRecommendation = "Última colheita comercial antes da entrada do período de estiagem nordestina.",
            nutritionRecommendation = "Deixar pelo menos 2 a 3 favos de mel operculados no ninho como reserva estratégica para a seca que se inicia em Julho.",
            pollenAbundance = "Média",
            nectarAbundance = "Alta",
            specialNotes = "Trepadeira nativa que cobre a vegetação logo após a estação chuvosa."
        ),
        FloralSpecies(
            id = "caa_bamburral",
            popularName = "Bamburral",
            scientificName = "Hyptis suaveolens",
            family = "Lamiaceae",
            biome = BotanicalBiome.CAATINGA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(4, 5, 6, 7),
            peakMonths = listOf(5),
            honeyColor = "Âmbar",
            honeyFlavor = "Aromático com leve toque mentolado",
            crystallizationSpeed = "Rápida",
            apiculturalValue = "Média a Alta - Transição para o período seco",
            harvestRecommendation = "Aproveitar para colher o excedente mas manter ninho abastecido.",
            nutritionRecommendation = "Após o término da floração em Julho, iniciar monitoramento estrito de traças e planejar sombreamento dos apiários.",
            pollenAbundance = "Alta",
            nectarAbundance = "Média",
            specialNotes = "Excelente planta aromática medicinal com óleos essenciais protetores."
        ),

        // === PAMPA & SUL ===
        FloralSpecies(
            id = "pam_canola",
            popularName = "Canola",
            scientificName = "Brassica napus",
            family = "Brassicaceae",
            biome = BotanicalBiome.PAMPA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(6, 7, 8),
            peakMonths = listOf(7),
            honeyColor = "Branco a Marfim claro",
            honeyFlavor = "Suave, amanteigado, pouco ácido",
            crystallizationSpeed = "Ultrarrápida (3 a 7 dias, grânulos finíssimos)",
            apiculturalValue = "Excepcional - Safra de inverno em pleno clima frio",
            harvestRecommendation = "ATENÇÃO CRÍTICA: Centrifugar no máximo 48 horas após a colheita, caso contrário o mel cristaliza dentro do favo e inutiliza a cera.",
            nutritionRecommendation = "Fornecer pólen abundante no meio do inverno, permitindo que a colmeia saia fortalecida para a primavera.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Muito Alta",
            specialNotes = "Cultura agrícola de grãos que revolucionou a apicultura de inverno no RS e PR."
        ),
        FloralSpecies(
            id = "pam_bracatinga",
            popularName = "Bracatinga (Melato)",
            scientificName = "Mimosa scabrella (com Cochonilhas)",
            family = "Fabaceae",
            biome = BotanicalBiome.PAMPA,
            resourceType = BotanicalResource.HONEYDEW,
            bloomMonths = listOf(4, 5, 6, 7),
            peakMonths = listOf(5, 6),
            honeyColor = "Castanho Escuro a Preto mineral",
            honeyFlavor = "Amargo suave, notas de melaço, malte e baixa doçura",
            crystallizationSpeed = "Quase não cristaliza devido ao alto teor de frutose e oligossacarídeos",
            apiculturalValue = "Excepcional - Selo de Denominação de Origem (D.O. Planalto Sul)",
            harvestRecommendation = "Ocorre predominantemente nos anos pares nos planaltos frios (>900m altitude). Monitorar excreção de melato pelas cochonilhas nos troncos.",
            nutritionRecommendation = "Excelente para manutenção invernal, mas atentar para evitar disenteria se as abelhas ficarem confinadas muitos dias pelo frio intenso.",
            pollenAbundance = "Baixa (fornece melato)",
            nectarAbundance = "Muito Alta",
            specialNotes = "Mel de prestígio internacional com altíssimo valor de exportação para a Alemanha e Europa."
        ),
        FloralSpecies(
            id = "pam_trevo_branco",
            popularName = "Trevo-branco",
            scientificName = "Trifolium repens",
            family = "Fabaceae",
            biome = BotanicalBiome.PAMPA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(10, 11, 12, 1),
            peakMonths = listOf(11, 12),
            honeyColor = "Branco Água límpido",
            honeyFlavor = "Puro, doce sutil, clássico padrão de mesa",
            crystallizationSpeed = "Média, textura amanteigada",
            apiculturalValue = "Alta - Padrão internacional de mel de pastagem",
            harvestRecommendation = "Instalar melgueiras adicionais na primavera. Evitar sobreposição com capins altos que dificultem o voo.",
            nutritionRecommendation = "Época de fartura plena; excelente para produção de núcleos de fecundação.",
            pollenAbundance = "Alta",
            nectarAbundance = "Muito Alta",
            specialNotes = "Fixadora de nitrogênio em pastagens mistas do Bioma Pampa."
        ),
        FloralSpecies(
            id = "pam_macieira",
            popularName = "Macieira (Polinização)",
            scientificName = "Malus domestica",
            family = "Rosaceae",
            biome = BotanicalBiome.PAMPA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(9, 10),
            peakMonths = listOf(9),
            honeyColor = "Âmbar Claro",
            honeyFlavor = "Frutado com notas florais frescas",
            crystallizationSpeed = "Média",
            apiculturalValue = "Muito Alta - Serviços de polinização remunerada",
            harvestRecommendation = "Renda mista: locação de colmeias para polinização dos pomares em Vacaria/RS e Fraiburgo/SC somada à colheita de mel primaveril.",
            nutritionRecommendation = "Estimular colmeias com xarope 4 semanas antes para levar enxames no padrão de 8 a 10 quadros de cria na abertura dos pomares.",
            pollenAbundance = "Alta",
            nectarAbundance = "Alta",
            specialNotes = "Cada colmeia pode gerar remuneração direta por serviço de polinização."
        ),

        // === PANTANAL ===
        FloralSpecies(
            id = "pan_aguape",
            popularName = "Aguapé (Camalote)",
            scientificName = "Eichhornia crassipes / azurea",
            family = "Pontederiaceae",
            biome = BotanicalBiome.PANTANAL,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(10, 11, 12, 1, 2),
            peakMonths = listOf(11, 12),
            honeyColor = "Âmbar Claro a Dourado",
            honeyFlavor = "Doce leve, suave e refrescante",
            crystallizationSpeed = "Lenta",
            apiculturalValue = "Alta - Grandes manchas fluviais na época das cheias",
            harvestRecommendation = "Acesso aos apiários pantaneiros via barco/chalanas. Monitorar operculação com frequência.",
            nutritionRecommendation = "Em épocas de seca severa pantaneira, atentar para incêndios florestais e fornecer água limpa com bebedouros de flutuador.",
            pollenAbundance = "Média",
            nectarAbundance = "Muito Alta",
            specialNotes = "Espécie aquática flutuante de crescimento vigoroso nas lagoas pantaneiras."
        ),
        FloralSpecies(
            id = "pan_piuva",
            popularName = "Piúva (Ipê-roxo-do-Pantanal)",
            scientificName = "Handroanthus impetiginosus / heptaphyllus",
            family = "Bignoniaceae",
            biome = BotanicalBiome.PANTANAL,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(6, 7, 8),
            peakMonths = listOf(7),
            honeyColor = "Âmbar Dourado",
            honeyFlavor = "Floral delicado e aromático",
            crystallizationSpeed = "Média",
            apiculturalValue = "Muito Alta - Espetáculo floral de inverno pantaneiro",
            harvestRecommendation = "Colheita de inverno. Permite aproveitamento sem necessidade de alimentação artificial.",
            nutritionRecommendation = "Excelente para manutenção de rainhas ativas no meio do ano.",
            pollenAbundance = "Alta",
            nectarAbundance = "Alta",
            specialNotes = "As árvores perdem as folhas e cobrem o Pantanal com um tapete de flores roxas."
        ),

        // === AMAZÔNIA ===
        FloralSpecies(
            id = "amz_castanheira",
            popularName = "Castanheira-do-Brasil",
            scientificName = "Bertholletia excelsa",
            family = "Lecythidaceae",
            biome = BotanicalBiome.AMAZONIA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(10, 11, 12, 1),
            peakMonths = listOf(11, 12),
            honeyColor = "Âmbar avermelhado",
            honeyFlavor = "Amendoado, untuoso, rico em selênio e minerais",
            crystallizationSpeed = "Lenta",
            apiculturalValue = "Excepcional para a floresta em pé e mel gourmet amazônico",
            harvestRecommendation = "Safra da estação de transição para o verão amazônico. Cuidados estritos com a umidade relativa do ar (>85%) na desoperculação.",
            nutritionRecommendation = "O pólen vigoroso da castanheira promove ninhadas densas e saudáveis.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Muito Alta",
            specialNotes = "Gigante da floresta. As abelhas realizam polinização cruzada vital para a castanha."
        ),
        FloralSpecies(
            id = "amz_acaizeiro",
            popularName = "Açaizeiro",
            scientificName = "Euterpe oleracea / precatoria",
            family = "Arecaceae",
            biome = BotanicalBiome.AMAZONIA,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(5, 6, 7, 8, 9),
            peakMonths = listOf(6, 7),
            honeyColor = "Âmbar Escuro a Castanho",
            honeyFlavor = "Frutado tropical, notas de açaí fresco e polpa de frutas",
            crystallizationSpeed = "Média",
            apiculturalValue = "Muito Alta - Floração abundante em várzeas e igapós",
            harvestRecommendation = "Utilizar desumidificador no entreposto de mel devido à alta umidade da região Norte.",
            nutritionRecommendation = "As inflorescências em cacho produzem pólen quase o ano todo nas várzeas.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Alta",
            specialNotes = "Palmeira nativa de altíssima rentabilidade sustentável integrada."
        ),

        // === CULTURAS AGRÍCOLAS E AMPLA OCORRÊNCIA ===
        FloralSpecies(
            id = "gen_girassol",
            popularName = "Girassol",
            scientificName = "Helianthus annuus",
            family = "Asteraceae",
            biome = BotanicalBiome.GENERAL_BRASIL,
            resourceType = BotanicalResource.NECTAR_AND_POLLEN,
            bloomMonths = listOf(11, 12, 1, 2),
            peakMonths = listOf(12, 1),
            honeyColor = "Amarelo Ouro brilhante",
            honeyFlavor = "Agradável, quente, com notas de nozes",
            crystallizationSpeed = "Rápida, textura de pomada fina",
            apiculturalValue = "Muito Alta - Excelente para safrinha agrícola",
            harvestRecommendation = "Colheita rápida assim que os capítulos florais começarem a secar.",
            nutritionRecommendation = "Gera uma explosão de pólen amarelo nas colmeias, favorecendo a troca natural de cera.",
            pollenAbundance = "Muito Alta",
            nectarAbundance = "Alta",
            specialNotes = "Excelente consórcio com produtores de grãos e óleos vegetais."
        ),
        FloralSpecies(
            id = "gen_cafe",
            popularName = "Flor do Cafeeiro",
            scientificName = "Coffea arabica / canephora",
            family = "Rubiaceae",
            biome = BotanicalBiome.GENERAL_BRASIL,
            resourceType = BotanicalResource.NECTAR,
            bloomMonths = listOf(9, 10, 11),
            peakMonths = listOf(9, 10),
            honeyColor = "Âmbar Claro dourado",
            honeyFlavor = "Floral requintado com delicado aroma de jasmim e cafezal",
            crystallizationSpeed = "Média",
            apiculturalValue = "Alta - Florada curta (3 a 5 dias por abertura) porém intensíssima",
            harvestRecommendation = "Como as aberturas duram poucos dias, é preciso que as colmeias já estejam no cafezal com as melgueiras postas antes da primeira florada.",
            nutritionRecommendation = "Alimentar colmeias 30 dias antes (Agosto) para que as operárias estejam no auge da força de forrageamento.",
            pollenAbundance = "Média",
            nectarAbundance = "Muito Alta",
            specialNotes = "A presença das abelhas aumenta a produtividade de grãos do café em até 20% a 30%."
        )
    )

    /**
     * Detecta o bioma botânico com base na latitude, longitude e nome do local do apiário.
     */
    fun detectBiome(
        latitude: Double?,
        longitude: Double?,
        locationName: String? = null
    ): BotanicalBiome {
        val locUpper = (locationName ?: "").uppercase()

        // 1. Checagem textual prioritária (siglas de estados ou nomes de cidades brasileiras)
        if (locUpper.contains("RS") || locUpper.contains("RIO GRANDE DO SUL") ||
            locUpper.contains("PELOTAS") || locUpper.contains("PORTO ALEGRE") ||
            locUpper.contains("BAGÉ") || locUpper.contains("SANTA MARIA") ||
            locUpper.contains("VACARIA") || locUpper.contains("SERRA GAÚCHA")
        ) {
            return BotanicalBiome.PAMPA
        }

        if (locUpper.contains("PANTANAL") || locUpper.contains("CORUMBÁ") ||
            locUpper.contains("AQUIDAUANA") || locUpper.contains("MIRANDA") ||
            locUpper.contains("CÁCERES") || locUpper.contains("POCONÉ")
        ) {
            return BotanicalBiome.PANTANAL
        }

        if (locUpper.contains("AMAZONAS") || locUpper.contains("PARÁ") || locUpper.contains("AMAZÔNIA") ||
            locUpper.contains("MANAUS") || locUpper.contains("BELÉM") || locUpper.contains("SANTARÉM") ||
            locUpper.contains("ACRE") || locUpper.contains("RONDÔNIA") || locUpper.contains("AMAPÁ") ||
            locUpper.contains("RORAIMA")
        ) {
            return BotanicalBiome.AMAZONIA
        }

        if (locUpper.contains("CAATINGA") || locUpper.contains("SEMIÁRIDO") ||
            locUpper.contains("PIAUÍ") || locUpper.contains("PICOS") ||
            locUpper.contains("CEARÁ") || locUpper.contains("SOBRAL") ||
            locUpper.contains("PERNAMBUCO") || locUpper.contains("PETROLINA") ||
            locUpper.contains("PARAÍBA") || locUpper.contains("RIO GRANDE DO NORTE") ||
            locUpper.contains("MOSSORÓ") || locUpper.contains("SERTÃO")
        ) {
            return BotanicalBiome.CAATINGA
        }

        if (locUpper.contains("CERRADO") || locUpper.contains("GOIÁS") || locUpper.contains("GOIANIA") ||
            locUpper.contains("BRASÍLIA") || locUpper.contains("DISTRITO FEDERAL") || locUpper.contains("DF") ||
            locUpper.contains("TOCANTINS") || locUpper.contains("PALMAS") ||
            locUpper.contains("MATO GROSSO DO SUL") || locUpper.contains("CAMPO GRANDE") ||
            locUpper.contains("CUIABÁ") || locUpper.contains("TRIÂNGULO MINEIRO") || locUpper.contains("UBERLÂNDIA")
        ) {
            return BotanicalBiome.CERRADO
        }

        if (locUpper.contains("SÃO PAULO") || locUpper.contains("SP") ||
            locUpper.contains("RIO DE JANEIRO") || locUpper.contains("RJ") ||
            locUpper.contains("ESPÍRITO SANTO") || locUpper.contains("ES") ||
            locUpper.contains("MINAS GERAIS") || locUpper.contains("BH") || locUpper.contains("BELO HORIZONTE") ||
            locUpper.contains("CURITIBA") || locUpper.contains("PARANÁ") || locUpper.contains("PR") ||
            locUpper.contains("FLORIANÓPOLIS") || locUpper.contains("SANTA CATARINA") || locUpper.contains("SC")
        ) {
            return BotanicalBiome.MATA_ATLANTICA
        }

        // 2. Classificação por Coordenadas Geográficas (Lat/Lon)
        if (latitude != null && longitude != null) {
            // Região Sul Extrema / Pampa (Lat < -27.5)
            if (latitude < -27.5) {
                return BotanicalBiome.PAMPA
            }

            // Bacia do Pantanal (entre -16° e -22° Lat, e -58.5° e -55° Lon)
            if (latitude in -22.0..-16.0 && longitude in -58.5..-55.0) {
                return BotanicalBiome.PANTANAL
            }

            // Amazônia (Lat > -13° e Lon < -45°)
            if (latitude > -13.0 && longitude < -45.0) {
                return BotanicalBiome.AMAZONIA
            }

            // Caatinga / Semiárido Nordestino (-2.5° a -15° Lat, e -45° a -35° Lon)
            if (latitude in -15.0..-2.5 && longitude in -45.0..-35.0) {
                return BotanicalBiome.CAATINGA
            }

            // Litoral e Mata Atlântica (Sudeste/Sul litorâneo: Lon > -48° e Lat entre -13° e -27.5°)
            if (longitude > -48.0 && latitude in -27.5..-13.0) {
                return BotanicalBiome.MATA_ATLANTICA
            }

            // Cerrado / Brasil Central (Planalto Central)
            if (latitude in -24.0..-10.0 && longitude in -60.0..-44.0) {
                return BotanicalBiome.CERRADO
            }
        }

        // Padrão amigável para apicultura nacional
        return BotanicalBiome.CERRADO
    }

    /**
     * Retorna a lista de espécies aplicáveis a um bioma (inclui as gerais)
     */
    fun getSpeciesForBiome(biome: BotanicalBiome): List<FloralSpecies> {
        return BOTANICAL_SPECIES_CATALOG.filter {
            it.biome == biome || it.biome == BotanicalBiome.GENERAL_BRASIL
        }
    }

    /**
     * Retorna espécies com floração ativa no mês informado (1..12)
     */
    fun getSpeciesActiveInMonth(biome: BotanicalBiome, month: Int): List<FloralSpecies> {
        return getSpeciesForBiome(biome).filter { month in it.bloomMonths }
    }

    /**
     * Analisa as condições sazonais do mês atual para o bioma selecionado,
     * calculando orientações diretas de Colheita e Nutrição.
     */
    fun analyzeSeasonalConditions(biome: BotanicalBiome, month: Int): SeasonalAnalysis {
        val safeMonth = month.coerceIn(1, 12)
        val monthName = MONTH_NAMES[safeMonth - 1]
        val active = getSpeciesActiveInMonth(biome, safeMonth)
        val peak = active.filter { safeMonth in it.peakMonths }

        // Identificação da Estação no Hemisfério Sul
        val seasonName = when (safeMonth) {
            12, 1, 2 -> "Verão"
            3, 4, 5 -> "Outono"
            6, 7, 8 -> "Inverno"
            else -> "Primavera"
        }

        // Cálculo de fluxo de néctar e pólen
        val hasVeryHighNectar = active.any { it.nectarAbundance == "Muito Alta" }
        val hasHighNectar = active.any { it.nectarAbundance == "Alta" }
        val hasVeryHighPollen = active.any { it.pollenAbundance == "Muito Alta" }
        val hasHighPollen = active.any { it.pollenAbundance == "Alta" }

        val nectarFlowIndex = when {
            peak.isNotEmpty() && hasVeryHighNectar -> "Intenso (Grande Safra)"
            active.isNotEmpty() && (hasVeryHighNectar || hasHighNectar) -> "Moderado a Bom"
            active.isNotEmpty() -> "Leve / Manutenção"
            else -> "Nenhum / Escassez Crítica"
        }

        val pollenSupplyIndex = when {
            hasVeryHighPollen -> "Abundante"
            hasHighPollen -> "Médio a Bom"
            active.isNotEmpty() -> "Mínimo"
            else -> "Escasso / Carência"
        }

        // Determinação da fase de manejo
        val managementPhase = when {
            peak.isNotEmpty() && hasVeryHighNectar -> "GRANDE_SAFRA_MELGUEIRAS"
            active.isEmpty() -> "ENTRESSAFRA_CRITICA"
            safeMonth in listOf(7, 8) && biome in listOf(BotanicalBiome.CERRADO, BotanicalBiome.CAATINGA) -> "ENTRESSAFRA_CRITICA"
            safeMonth in listOf(5, 6, 7) && biome == BotanicalBiome.PAMPA -> "INVERNO_REDUCAO_ESPACO"
            safeMonth in listOf(8, 9) && biome in listOf(BotanicalBiome.MATA_ATLANTICA, BotanicalBiome.CERRADO) -> "PRE_SAFRA_ESTIMULO"
            safeMonth in listOf(11, 12) && biome == BotanicalBiome.CAATINGA -> "PRE_SAFRA_ESTIMULO"
            safeMonth in listOf(4, 5) -> "COLHEITA_MADURACAO"
            else -> "MANUTENCAO_REGULAR"
        }

        // Orientações práticas de colheita
        val harvestAdvice = when (managementPhase) {
            "GRANDE_SAFRA_MELGUEIRAS" -> {
                "Pico de fluxo nectarífero em andamento (${peak.joinToString { it.popularName }}). Adicionar 1 a 2 melgueiras extras com cera alveolada sobre as colmeias fortes. Checar operculação (mínimo 80% dos favos lacrados com cera) e umidade inferior a 18% para evitar fermentação."
            }
            "PRE_SAFRA_ESTIMULO" -> {
                "Preparação para a próxima grande safra. Limpar e desinfetar melgueiras no entreposto, aramar quadros novos com cera fresca e instalar telas excluidoras de rainha para manter as melgueiras livres de crias."
            }
            "COLHEITA_MADURACAO" -> {
                "Janela ideal de colheita e centrifugação. Retirar melgueiras operculadas com uso de soprador ou escape invertido tipo losango. Processar mel no mesmo dia para evitar absorção de umidade ambiente."
            }
            "ENTRESSAFRA_CRITICA" -> {
                "Período sem produção de mel excedente. NÃO colher favos do ninho! Deixar pelo menos 2 a 3 quadros de mel e pólen intactos como reserva alimentar de sobrevivência para a colônia."
            }
            else -> {
                "Monitorar peso das melgueiras e desenvolvimento das operárias forrageiras. Realizar revisão rápida a cada 15 dias sem estressar a postura da rainha."
            }
        }

        // Orientações práticas de nutrição
        val nutritionAdvice = when (managementPhase) {
            "ENTRESSAFRA_CRITICA" -> {
                "ALERTA DE ESCASSEZ: Fornecer Alimentação Energética de Subsistência com xarope denso (2 partes de açúcar cristal para 1 parte de água fervida) a cada 7 dias para evitar abandono e morte por inanição. Se o pólen estiver escasso, complementar com bife proteico (levedura de cerveja e farelo de soja desengordurada)."
            }
            "PRE_SAFRA_ESTIMULO" -> {
                "MANEJO ESTIMULANTE: Fornecer Alimentação Estimulante líquida (xarope 1:1) com promotor de crescimento 30 a 40 dias antes da florada alvo. Isso simula uma florada precoce e faz a rainha atingir pico de até 2.000 ovos/dia, garantindo 60.000 abelhas campeiras prontas no primeiro dia da safra."
            }
            "GRANDE_SAFRA_MELGUEIRAS" -> {
                "SUSPENDER ALIMENTAÇÃO ARTIFICIAL: A natureza está fornecendo néctar e pólen em abundância. Qualquer xarope ou aditivo fornecido agora contaminaria o mel comercial, gerando risco de adulteração e perda de selos de qualidade."
            }
            "INVERNO_REDUCAO_ESPACO" -> {
                "CLIMA FRIO: Reduzir alvados para evitar entrada de vento gelado e perda térmica. Alimentar internamente apenas com alimento pastoso (pasta cândi ou bife energético) para evitar excesso de umidade dentro da colmeia no inverno."
            }
            else -> {
                "Avaliar reservas de ninho durante inspeções. Se houver mais de 3 quadros de alimento no ninho, não é necessária intervenção artificial."
            }
        }

        val recommendedActions = mutableListOf<String>()
        when (managementPhase) {
            "GRANDE_SAFRA_MELGUEIRAS" -> {
                recommendedActions.add("Introduzir melgueiras com cera alveolada nova")
                recommendedActions.add("Instalar tela excluidora de rainha")
                recommendedActions.add("Monitorar operculação dos favos (mínimo 80%)")
                recommendedActions.add("Verificar umidade com refratômetro (< 18%)")
            }
            "PRE_SAFRA_ESTIMULO" -> {
                recommendedActions.add("Iniciar xarope estimulante 1:1 a cada 3 dias")
                recommendedActions.add("Fornecer bife proteico de 100g a 150g por colmeia")
                recommendedActions.add("Trocar quadros velhos e escuros do ninho")
                recommendedActions.add("Verificar fecundação e padrão de postura da rainha")
            }
            "ENTRESSAFRA_CRITICA" -> {
                recommendedActions.add("Fornecer xarope de subsistência 2:1 semanalmente")
                recommendedActions.add("Reduzir alvado contra pilhagem e predadores")
                recommendedActions.add("Checar bebedouros com água limpa perto do apiário")
                recommendedActions.add("Monitorar infestação de traças e formigas")
            }
            "COLHEITA_MADURACAO" -> {
                recommendedActions.add("Agendar dia ensolarado para colheita")
                recommendedActions.add("Utilizar escape de abelhas 24h antes da colheita")
                recommendedActions.add("Centrifugar em sala limpa e protegida de abelhas")
                recommendedActions.add("Filtrar e decantar mel por 72 horas")
            }
            else -> {
                recommendedActions.add("Revisão quinzenal de sanidade e postura")
                recommendedActions.add("Acompanhar abertura dos botões florais da região")
            }
        }

        return SeasonalAnalysis(
            currentMonth = safeMonth,
            monthName = monthName,
            seasonName = seasonName,
            activeSpecies = active,
            peakSpecies = peak,
            totalSpeciesCount = active.size,
            nectarFlowIndex = nectarFlowIndex,
            pollenSupplyIndex = pollenSupplyIndex,
            managementPhase = managementPhase,
            harvestAdvice = harvestAdvice,
            nutritionAdvice = nutritionAdvice,
            recommendedActions = recommendedActions
        )
    }

    /**
     * Determina o status da espécie no mês fornecido
     */
    fun getSpeciesBloomStatusInMonth(species: FloralSpecies, month: Int): BloomStatus {
        return when {
            month in species.peakMonths -> BloomStatus.PEAK_BLOOM
            month in species.bloomMonths -> BloomStatus.ACTIVE_BLOOM
            (month + 1 in species.bloomMonths) || (month == 12 && 1 in species.bloomMonths) -> BloomStatus.PRE_BLOOM
            (month - 1 in species.bloomMonths) || (month == 1 && 12 in species.bloomMonths) -> BloomStatus.POST_BLOOM
            else -> BloomStatus.OFF_SEASON
        }
    }
}
