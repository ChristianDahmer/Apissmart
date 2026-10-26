# Política de Privacidade — ApisSmart

**Última atualização:** 26 de Outubro de 2026  
**Versão do Aplicativo:** 4.44 (Build 74)  
**Desenvolvedor:** Christian Dahmer  
**Contato:** christiandahmer@gmail.com | WhatsApp: +55 (51) 99677-6214  
**Aplicativo:** ApisSmart (com.aistudio.apissmart.bzkv)  

---

## 1. Introdução e Compromisso com a Privacidade
O **ApisSmart** é uma plataforma desenvolvida para a gestão zootécnica, sanitária e financeira de apiários e meliponários. Esta Política de Privacidade descreve com absoluta clareza e transparência como tratamos as informações dos usuários e as permissões de dispositivos móveis, em estrita conformidade com a **Lei Geral de Proteção de Dados Pessoais do Brasil (LGPD - Lei nº 13.709/2018)** e as **Políticas de Dados do Desenvolvedor do Google Play**.

---

## 2. Coleta e Armazenamento Local de Dados (Soberania dos Dados)
* **Arquitetura 100% Offline e Local:** Todas as informações registradas pelo usuário — incluindo cadastro de apiários, colmeias, histórico de rainhas, inspeções sanitárias, anotações de manejo, controle de colheita, pesagem de melgueiras, estoques e lançamentos financeiros — são armazenadas **exclusivamente de forma local** no dispositivo móvel em banco de dados SQLite criptografado (Android Room Database).
* **Sem Rastreamento Oculto ou Venda de Dados:** O ApisSmart **não vende, não aluga e não compartilha** dados operacionais, produtivos ou pessoais com agências de publicidade, redes de anúncios ou quaisquer terceiros.
* **Backups de Responsabilidade do Usuário:** As funções de exportação de relatórios (PDF/CSV) e backups (JSON/DB) são executadas unicamente sob comando voluntário do usuário, que escolhe onde salvar seus arquivos (armazenamento local ou nuvem pessoal).

---

## 3. Uso Transparente de Permissões e Dados Sensíveis do Dispositivo

O ApisSmart segue o princípio do menor privilégio, solicitando acesso apenas aos recursos estritamente necessários para o funcionamento das ferramentas contratadas pelo usuário:

### 📸 Câmera (`CAMERA`)
* **Finalidade Específica:** Utilizada exclusivamente quando o usuário aciona o recurso de **Diagnóstico Fitossanitário por Inteligência Artificial** para fotografar quadros de favos (identificação de crias, postura e pragas como *Varroa destructor*) ou para ler **QR Codes** de identificação afixados nas colmeias.
* **Tratamento:** As imagens são processadas pontualmente para análise da colmeia e não são utilizadas para identificação biométrica pessoal nem compartilhadas com redes externas.

### 🎙️ Microfone e Áudio (`RECORD_AUDIO`)
* **Finalidade Específica:** Utilizado unicamente quando o usuário ativa o recurso **"Modo Luvas & Voz (Hands-Free)"**, permitindo ditar observações e relatórios de revisão no apiário sem necessidade de retirar luvas ou macacão apícola.
* **Tratamento:** O áudio é convertido em texto em tempo real (Speech-to-Text). Nenhuma gravação de áudio contínua em segundo plano é realizada e nenhum arquivo de voz é armazenado ou enviado para fins publicitários.

### 📍 Localização Geográfica (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
* **Finalidade Específica:** Utilizada exclusivamente para:
  1. Fixar a coordenada GPS precisa do apiário no mapa de satélite;
  2. Calcular o raio de forrageamento (pasto apícola de 1,5 km a 3 km);
  3. Obter a previsão agroclimática meteorológica oficial para a região do apiário.
* **Tratamento:** A localização só é obtida quando o usuário cadastra ou visualiza apiários. **Não há rastreamento contínuo em segundo plano** quando o aplicativo está fechado.

### 📶 Bluetooth e Escaneamento BLE (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`)
* **Finalidade Específica:** Utilizado no módulo de **Rastreamento de Tags BLE & Radar Antifurto** para localizar beacons ou sensores de temperatura/umidade instalados nas caixas apícolas do usuário.
* **Tratamento:** Apenas escaneia os identificadores das tags cadastradas pelo usuário. Não acessa nem monitora dispositivos pessoais de terceiros.

---

## 4. Pagamentos e Faturamento (Google Play Billing)
* Todas as assinaturas (Mensal, Anual) e aquisições de licenças Pro via loja de aplicativos são processadas diretamente pela infraestrutura segura do **Google Play Billing**.
* O ApisSmart **não coleta nem armazena números de cartão de crédito**, contas bancárias ou informações financeiras confidenciais de faturamento.
* O gerenciamento e cancelamento de assinaturas podem ser realizados a qualquer instante pelo usuário no painel do Google Play Store.

---

## 5. Direitos do Titular dos Dados (LGPD)
Em conformidade com a LGPD (Lei 13.709/2018), o usuário tem garantidos os direitos de:
1. **Acesso e Confirmação:** Visualizar todos os dados mantidos no aplicativo a qualquer momento.
2. **Correção:** Editar ou atualizar qualquer registro incompleto ou incorreto.
3. **Portabilidade:** Exportar seus relatórios e base de dados completa em formatos abertos (JSON, CSV, PDF).
4. **Eliminação:** Apagar individualmente colmeias, apiários e lançamentos, ou realizar a exclusão total e imediata de todos os dados do aplicativo através da opção de redefinição de fábrica.

---

## 6. Segurança da Informação
Empregamos medidas técnicas e administrativas compatíveis com os padrões da indústria para proteger seus dados contra acessos não autorizados ou destruição, mantendo o banco de dados no *sandbox* isolado do sistema operacional Android.

---

## 7. Contato do Encarregado de Dados (DPO) e Suporte
Para esclarecer dúvidas sobre esta Política de Privacidade ou solicitar suporte sobre o tratamento de dados:
* **Responsável:** Christian Dahmer
* **E-mail:** christiandahmer@gmail.com
* **WhatsApp de Suporte:** +55 (51) 99677-6214
