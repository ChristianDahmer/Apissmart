# MANUAL COMPLETO DO SISTEMA - APISSMART v4.41
**Gestão Inteligente e Profissional de Apiários e Produção Apícola**

---

## 1. Visão Geral do Sistema
O **ApisSmart** é a solução definitiva para apicultores amadores e comerciais, oferecendo monitoramento avançado de apiários, controle de colmeias, rastreamento via Bluetooth, previsão do tempo integrada, mapeamento geoespacial via satélite, controle financeiro completo e ferramentas de inteligência para maximizar a produção de mel e derivados.

---

## 2. Funcionalidades Principais e Telas

### 2.1. Dashboard Principal
- **Visão Geral em Tempo Real**: Resumo do total de apiários, colmeias ativas, produção estimada e alertas sanitários.
- **Atalhos Rápidos**: Acesso imediato ao cadastro de novas colmeias, inspeções de campo, leitor de QR Code e clima.
- **Indicadores de Desempenho**: Gráficos de produtividade e saúde do apiário atualizados dinamicamente.

### 2.2. Gestão de Apiários e Colmeias
- **Cadastro Detalhado**: Identificação por código/QR Code, tipo de colmeia (Langstroth, Rational, Top Bar), data de instalação e linhagem da rainha.
- **Inspeções de Campo**: Registro de postura da rainha, temperament, reservas de alimento, presença de pragas e necessidade de alimentação artificial.
- **Histórico Completo**: Linha do tempo com todas as intervenções e evolução sanitária por colmeia.

### 2.3. Mapa Geral de Produção & Geolocalização (Atualizado v4.41)
- **Mapa Geoespacial com Satélite**: Visualização via Esri ArcGIS satellite tiles para localização exata dos apiários e colmeias no terreno.
- **Card de Legendas Otimizado**: Legendas e filtros redesenhados (com rolagem suave e compactação) para nunca sobrepor os botões de controle do mapa.
- **Definição de Coordenadas por Toque**: Tela interativa de seleção de geolocalização por mapa (`MapLocationPickerDialog`) com zoom, arrasto e marcação precisa de GPS.

### 2.4. Rastreamento Bluetooth & Modo Mãos-Livres
- **Tags Bluetooth**: Localização de colmeias e equipamentos no campo com suporte a sinal RSSI e alertas sonoros/vibração.
- **Comandos de Voz (Mãos-Livres)**: Registro de inspeções por voz durante o manejo no apiário sem precisar remover os EPIs ou luvas.

### 2.5. Clima e Previsão do Tempo Apícola
- **Previsão Localizada**: Condições climáticas em tempo real para a localização dos apiários (temperatura, umidade, vento, índice UV e probabilidade de chuva).
- **Alertas de Manejo**: Recomendações baseadas no clima para abertura de colmeias e colheita de mel.

### 2.6. Gestão Financeira e Comercial
- **Controle de Custos e Receitas**: Lançamento de insumos (cera, açúcar, medicamentos, embalagens) e vendas de mel, própolis, pólen e rainhas.
- **Relatórios de Lucratividade**: Gráficos de fluxo de caixa e margem de lucro por safra.

### 2.7. Leitor de QR Code e Rastreabilidade
- **Identificação Rápida**: Leitura de etiquetas QR Code fixadas nas caixas para acesso instantâneo ao prontuário da colmeia.

### 2.8. Sincronização e Backup (Nuvem / Google Drive)
- **Segurança de Dados**: Sincronização segura via Firebase Firestore e opções de backup/restauração via Google Drive.

---

## 3. Especificações Técnicas
- **Plataforma**: Android (Kotlin & Jetpack Compose).
- **Arquitetura**: MVVM com Clean Architecture.
- **Banco de Dados**: Firebase Firestore (Nuvem) + Room (Cache Local).
- **Design System**: Material Design 3 com suporte a temas dinâmicos e layout responsivo (Tablets, Foldables e Smartphones).
- **Versão Atual**: 4.41 (Release APK e AAB disponíveis na pasta de lançamentos).

---
*Documentação atualizada em Outubro de 2026.*
