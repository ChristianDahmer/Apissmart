# 📋 Guia de Publicação Google Play: Política de Privacidade & Segurança dos Dados

> Este guia passo a passo foi elaborado especificamente para o **ApisSmart (com.aistudio.apissmart.bzkv)** para garantir 100% de aprovação na análise da Google Play Store.

---

## 🌐 1. URL da Política de Privacidade (Obrigatório na Ficha do App)

O Google Play Console exige uma **URL pública acessível via navegador** (sem login, sem download de PDF) contendo a política de privacidade completa.

### Arquivo HTML Pronto no Projeto:
Já criamos a página web oficial, moderna e responsiva em:
* `public/politica-de-privacidade.html`
* `public/privacy-policy.html`

### Opções para Hospedar a URL Pública:
1. **Opção 1 (GitHub Pages - Mais recomendada e gratuita):**
   * Suba o arquivo `politica-de-privacidade.html` em um repositório no GitHub (ou ative o GitHub Pages na aba *Settings -> Pages*).
   * Sua URL ficará: `https://seu-usuario.github.io/apissmart/politica-de-privacidade.html`
2. **Opção 2 (Google Sites - Gratuita e rápida):**
   * Crie uma página simples no [Google Sites](https://sites.google.com), cole o texto da política e publique.
3. **Opção 3 (Firebase Hosting / Vercel / Domínio Próprio):**
   * Suba o conteúdo da pasta `public/` para o seu domínio (ex: `https://apissmart.com.br/politica-de-privacidade`).

### Onde colar no Google Play Console:
1. Acesse o [Google Play Console](https://play.google.com/console).
2. Selecione o aplicativo **ApisSmart**.
3. No menu lateral esquerdo, vá em **Conteúdo do app** (em *Política e programas*).
4. Em **Política de Privacidade**, clique em **Iniciar** (ou *Gerenciar*).
5. Cole a URL pública da sua política de privacidade e clique em **Salvar**.

---

## 🛡️ 2. Como Preencher a Seção "Segurança dos Dados" (Data Safety Form)

No Google Play Console, vá em **Conteúdo do app -> Segurança dos dados**. Siga o preenchimento exato abaixo:

### 🔹 Visão Geral
* **O app coleta ou compartilha algum dos tipos de dados de usuário necessários?**
  * Selecione: **Sim** (devido às permissões de Câmera, Microfone e Localização).
* **Todos os dados de usuários coletados pelo app são criptografados em trânsito?**
  * Selecione: **Sim** (todas as conexões externas de previsão de tempo e Google Play usam HTTPS/TLS).
* **O app oferece um método para os usuários solicitarem a exclusão dos dados?**
  * Selecione: **Sim** (os usuários podem excluir dados diretamente no app ou solicitar por e-mail para `christiandahmer@gmail.com`).

---

### 🔹 Declaração Específica por Permissão Sensível

| Dado / Permissão | Coletado? | Compartilhado? | Processado Efemeramente? | Finalidade Principal |
| :--- | :--- | :--- | :--- | :--- |
| **Localização precisa (`ACCESS_FINE_LOCATION`)** | Sim (Coletado) | **Não** (Não compartilhado) | Sim | **Funcionalidade do app**: Marcar a localização do apiário no mapa, calcular raio de pasto apícola e obter clima oficial. |
| **Fotos e vídeos (Câmera - `CAMERA`)** | Sim (Coletado) | **Não** (Não compartilhado) | Sim | **Funcionalidade do app**: Fotografar quadros de favos para diagnóstico com IA e escanear QR Code das colmeias. |
| **Gravação de voz ou som (`RECORD_AUDIO`)** | Sim (Coletado) | **Não** (Não compartilhado) | **Sim** | **Funcionalidade do app**: Transcrição de voz em texto no "Modo Luvas & Voz" para anotações sem tirar luvas. |
| **Identificadores do dispositivo (Device ID)** | Sim (Coletado) | **Não** (Não compartilhado) | Não | **Gerenciamento de contas e licenças**: Validação da licença e período de teste gratuito do aparelho. |
| **Informações financeiras / Compras** | Sim | **Não** | Não | **Gerenciamento de compras**: Intermediado com segurança pelo Google Play Billing. |

---

## 👶 3. Público-Alvo e Famílias
1. No menu **Conteúdo do app -> Público-alvo e conteúdo**:
   * Faixas etárias: Selecione **18 anos ou mais** (ou *16 a 17 anos* e *18 anos ou mais*).
   * **O app pode atrair o interesse involuntário de crianças?** Selecione: **Não** (Trata-se de um software de zootecnia e gestão agropecuária profissional).

---

## 📞 4. Dados de Contato e DPO (Encarregado)
* **Desenvolvedor:** Christian Dahmer
* **E-mail de Suporte:** `christiandahmer@gmail.com`
* **WhatsApp Oficial:** `+55 (51) 99677-6214`
