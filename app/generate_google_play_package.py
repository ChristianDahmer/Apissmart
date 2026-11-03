#!/usr/bin/env python3
import os
import subprocess
import shutil

ROOT_DIR = os.getcwd()
PACKAGE_DIR = os.path.join(ROOT_DIR, "public", "release", "google_play_console")
RELEASE_DIR = os.path.join(ROOT_DIR, "release", "google_play_console")

if os.path.exists(PACKAGE_DIR):
    shutil.rmtree(PACKAGE_DIR)

os.makedirs(PACKAGE_DIR, exist_ok=True)

# Subdirectories for Google Play Console assets
subdirs = [
    "01_icone_do_app_512x512",
    "02_grafico_de_recurso_feature_graphic_1024x500",
    "03_prints_do_celular_com_moldura_phone_mockups",
    "capturas_de_tela_inscritas_com_legendas_google_play",
    "04_screenshots_telas_diretas_alta_resolucao",
    "04_screenshots_telas_diretas_alta_resolucao/dark",
    "04_screenshots_telas_diretas_alta_resolucao/light",
    "05_ficha_cadastral_google_play_textos",
    "06_politica_de_privacidade_e_seguranca_dados"
]

for d in subdirs:
    os.makedirs(os.path.join(PACKAGE_DIR, d), exist_ok=True)

def run_cmd(cmd):
    subprocess.run(cmd, shell=True, check=True)

print("🚀 Gerando pacote completo para publicação no Google Play Console...")

# -------------------------------------------------------------
# 1. ÍCONE DO APP 512x512 (ABELHA RAINHA 3D OFICIAL)
# -------------------------------------------------------------
print("\n🐝 1. Gerando Ícone Oficial da Abelha Rainha 3D (512x512)...")
src_bee = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_smart_bee_icon_1790690258058.jpg")
icon_dir = os.path.join(PACKAGE_DIR, "01_icone_do_app_512x512")

# Arquivo principal para upload no Play Console (512x512 PNG 32-bit sem máscara)
icon_main = os.path.join(icon_dir, "icone_app_google_play_512x512.png")
run_cmd(f"convert '{src_bee}' -resize 512x512! -quality 100 '{icon_main}'")

# Versão arredondada preview
icon_rounded = os.path.join(icon_dir, "icone_app_arredondado_preview_512x512.png")
run_cmd(f"""convert '{src_bee}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black polygon 0,0 0,64 64,0 fill white circle 64,64 64,0" \
     \( +clone -flip \) -composite \( +clone -flop \) -composite \) \
  -alpha off -compose CopyOpacity -composite '{icon_rounded}'""")

# Versão circular
icon_circle = os.path.join(icon_dir, "icone_app_circular_preview_512x512.png")
run_cmd(f"""convert '{src_bee}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black circle 256,256 256,0" \) \
  -alpha off -compose CopyOpacity -composite '{icon_circle}'""")

print("  ✅ Ícone do app gerado com sucesso!")

# -------------------------------------------------------------
# 2. FEATURE GRAPHIC / ELEMENTO GRÁFICO DE RECURSO (1024x500)
# (Requisito obrigatório da Google Play Store: 1024 x 500 px)
# -------------------------------------------------------------
print("\n🎨 2. Criando Feature Graphic oficial (1024x500)...")
feature_dir = os.path.join(PACKAGE_DIR, "02_grafico_de_recurso_feature_graphic_1024x500")
feature_path = os.path.join(feature_dir, "grafico_recurso_feature_graphic_1024x500.png")
banner_path = os.path.join(feature_dir, "banner_promocional_loja_1024x500.jpg")

# Criar mockup de celular menor para o feature graphic (220x450)
dash_dark = os.path.join(ROOT_DIR, "public/release/screenshots/dark/01_dashboard_screen_dark.png")
tmp_phone_fg = "/tmp/phone_fg.png"

run_cmd(f"""
convert -size 220x450 xc:none \
  -fill '#1E293B' -draw 'roundrectangle 0,0,219,449 16,16' \
  -fill '#0F172A' -draw 'roundrectangle 3,3,216,446 14,14' \
  \( '{dash_dark}' -resize 204x424! \) -geometry +8+14 -composite \
  -fill '#000000' -draw 'roundrectangle 70,8 150,18 4,4' \
  '{tmp_phone_fg}'
""")

# Criar o Feature Graphic 1024x500 oficial com Abelha 3D e Mockup
tmp_bee_fg = "/tmp/bee_fg.png"
run_cmd(f"convert '{src_bee}' -resize 300x300! '{tmp_bee_fg}'")

cmd_feature = f"""
convert -size 1024x500 xc:none \
  -sparse-color Barycentric '0,0 #0F172A 0,500 #1E293B 1024,250 #0B0F19' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1024,8' \
  '{tmp_phone_fg}' -gravity east -geometry +60+0 -composite \
  '{tmp_bee_fg}' -gravity west -geometry +50-20 -composite \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 18 -gravity north-west -annotate +370+70 '🐝 APLICATIVO OFICIAL PARA APICULTURA & MELIPONICULTURA' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 40 -gravity north-west -annotate +370+105 'ApisSmart' \
  -fill '#E2E8F0' -font 'DejaVu-Sans' -pointsize 20 -gravity north-west -annotate +370+165 'Gestão Zootécnica, Fitossanitária e Financeira' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 17 -gravity north-west -annotate +370+220 '⚡ 100% OFFLINE NO CAMPO' \
  -fill '#FFFFFF' -font 'DejaVu-Sans' -pointsize 16 -gravity north-west -annotate +370+250 '🎙️ Modo Mãos-Livres: dite com luvas sem sujar o celular' \
  -fill '#FFFFFF' -font 'DejaVu-Sans' -pointsize 16 -gravity north-west -annotate +370+280 '🗺️ Mapeamento GPS em imagens de satélite reais' \
  -fill '#FFFFFF' -font 'DejaVu-Sans' -pointsize 16 -gravity north-west -annotate +370+310 '🍯 Controle de melgueiras, floradas e pesagem líquida' \
  -fill '#FFFFFF' -font 'DejaVu-Sans' -pointsize 16 -gravity north-west -annotate +370+340 '📊 Custo por kg de mel e lucratividade por colmeia' \
  -fill '#10B981' -draw 'roundrectangle 370,390 730,445 10,10' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 18 -gravity north-west -annotate +405+406 '🎁 TESTE 30 DIAS GRÁTIS' \
  '{feature_path}'
"""
run_cmd(cmd_feature)
run_cmd(f"convert '{feature_path}' -quality 95 '{banner_path}'")
print("  ✅ Feature Graphic (1024x500) gerado com sucesso!")

# -------------------------------------------------------------
# 3. PRINTS DO CELULAR COM MOLDURA REALISTA (PHONE MOCKUPS)
# 9 telas formatadas em 1080x1920 (proporção 9:16 vertical)
# com mockup do dispositivo, títulos de impacto e benefícios
# -------------------------------------------------------------
print("\n📱 3. Gerando 9 Prints do Celular com Moldura Realista (1080x1920)...")
mockup_out_dir = os.path.join(PACKAGE_DIR, "03_prints_do_celular_com_moldura_phone_mockups")

screens_info = [
    {
        "file": "01_dashboard_screen_dark.png",
        "out": "01_print_celular_dashboard_geral.png",
        "tag": "PAINEL INTELIGENTE",
        "title": "Visão Geral do Apiário em Tempo Real",
        "desc": "Previsão climática agropecuária, alertas de revisão e tarefas pendentes."
    },
    {
        "file": "02_apiarios_e_colmeias_dark.png",
        "out": "02_print_celular_apiarios_e_colmeias.png",
        "tag": "MAPA GPS & SATÉLITE",
        "title": "Mapeamento Preciso de Colmeias e Caixas",
        "desc": "Localização geográfica com cálculo de raio de pasto apícola de 1,5 a 3 km."
    },
    {
        "file": "03_inspecoes_sanitarias_dark.png",
        "out": "03_print_celular_inspecoes_e_voz.png",
        "tag": "MODO MÃOS-LIVRES & VOZ",
        "title": "Dite com Luvas Sujas de Própolis",
        "desc": "Comando de voz hands-free com diagnóstico fitossanitário inteligente."
    },
    {
        "file": "04_colheita_e_producao_dark.png",
        "out": "04_print_celular_colheita_e_mel.png",
        "tag": "CONTROLE DE COLHEITA",
        "title": "Rastreabilidade Completa da Safra",
        "desc": "Pesagem de melgueiras, lotes de mel, teor de umidade e tipo de florada."
    },
    {
        "file": "05_estoque_e_insumos_dark.png",
        "out": "05_print_celular_estoque_insumos.png",
        "tag": "ESTOQUE DE INSUMOS",
        "title": "Alimentação, Cera Alveolada e Materiais",
        "desc": "Alertas automáticos de estoque baixo e controle de caixas e quadros."
    },
    {
        "file": "06_cronograma_manejos_dark.png",
        "out": "06_print_celular_cronograma_manejos.png",
        "tag": "CRONOGRAMA DE MANEJO",
        "title": "Calendário Apícola e Troca de Rainhas",
        "desc": "Planejamento de revisões, divisões de enxames e floradas da região."
    },
    {
        "file": "07_gestao_financeira_dark.png",
        "out": "07_print_celular_gestao_financeira.png",
        "tag": "GESTÃO FINANCEIRA",
        "title": "Custo Exato por Kg de Mel e Lucro Real",
        "desc": "Fluxo de caixa profissional com receitas, despesas e margem de lucro."
    },
    {
        "file": "08_assistente_tecnico_ia_dark.png",
        "out": "08_print_celular_assistente_ia.png",
        "tag": "INTELIGÊNCIA ARTIFICIAL",
        "title": "Diagnósticos Sanitários com IA Gemini",
        "desc": "Detecção de Varroa, loque, traças e avaliação de postura de rainha."
    },
    {
        "file": "09_administracao_licenca_dark.png",
        "out": "09_print_celular_licenca_e_offline.png",
        "tag": "100% OFFLINE NO CAMPO",
        "title": "Funciona sem Internet com 30 Dias Grátis",
        "desc": "Soberania total dos dados: banco SQLite local e conformidade LGPD."
    }
]

for idx, item in enumerate(screens_info, 1):
    src_screen = os.path.join(ROOT_DIR, "public/release/screenshots/dark", item["file"])
    # Check if dark screenshot is valid, otherwise fallback to light version
    try:
        subprocess.check_output(f'identify "{src_screen}"', shell=True)
    except Exception:
        src_screen = os.path.join(ROOT_DIR, "public/release/screenshots", item["file"].replace("_dark", ""))

    target_out = os.path.join(mockup_out_dir, item["out"])
    
    # 1. Gerar o frame realista do celular (540 x 1120)
    phone_w = 540
    phone_h = 1120
    screen_w = 504
    screen_h = 1060
    corner_r = 32
    border_w = 18
    notch_w = 160
    notch_h = 24
    notch_x1 = (phone_w - notch_w) // 2
    notch_x2 = notch_x1 + notch_w
    
    tmp_phone = f"/tmp/phone_mock_{idx}.png"
    tmp_scr = f"/tmp/screen_resized_{idx}.png"
    
    run_cmd(f"convert '{src_screen}' -resize {screen_w}x{screen_h}! '{tmp_scr}'")
    
    cmd_phone = f"""
    convert -size {phone_w}x{phone_h} xc:none \
      -fill '#1E293B' -draw 'roundrectangle 0,0,{phone_w-1},{phone_h-1} {corner_r},{corner_r}' \
      -fill '#0A0E17' -draw 'roundrectangle 4,4,{phone_w-5},{phone_h-5} {corner_r-2},{corner_r-2}' \
      '{tmp_scr}' -geometry +{border_w}+{border_w+14} -composite \
      -fill '#000000' -draw 'roundrectangle {notch_x1},{border_w+2} {notch_x2},{border_w+2+notch_h} 10,10' \
      '{tmp_phone}'
    """
    run_cmd(cmd_phone)
    
    # 2. Criar a imagem completa 1080x1920 com degradê elegante, textos superiores e telefone
    cmd_poster = f"""
    convert -size 1080x1920 xc:'#0D1117' \
      -sparse-color Barycentric '0,0 #0D1117 0,1920 #161B22 1080,960 #1C2333' \
      -fill '#F59E0B' -draw 'rectangle 0,0 1080,12' \
      -fill '#10B981' -draw 'roundrectangle 70,60 480,110 12,12' \
      -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 22 -gravity north-west -annotate +90+73 '{item["tag"]}' \
      -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 38 -gravity north-west -annotate +70+130 '{item["title"]}' \
      -fill '#E2E8F0' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +70+185 '{item["desc"]}' \
      '{tmp_phone}' -gravity north -geometry +0+250 -composite \
      -fill '#1E293B' -draw 'roundrectangle 80,1830 1000,1890 14,14' \
      -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 20 -gravity north -annotate +0+1848 '🐝 ApisSmart • Gestão Apícola Profissional 100% Offline' \
      -quality 95 '{target_out}'
    """
    run_cmd(cmd_poster)
    shutil.copy(target_out, os.path.join(PACKAGE_DIR, "capturas_de_tela_inscritas_com_legendas_google_play", item["out"]))
    print(f"  ✅ [{idx}/9] Criado print do celular inscrito: {item['out']}")

# -------------------------------------------------------------
# 4. SCREENSHOTS DIRETAS DAS TELAS (DARK E LIGHT)
# -------------------------------------------------------------
print("\n📸 4. Copiando screenshots diretas das telas (Dark e Light)...")
src_dark = os.path.join(ROOT_DIR, "public/release/screenshots/dark")
src_light = os.path.join(ROOT_DIR, "public/release/screenshots")
dest_dark = os.path.join(PACKAGE_DIR, "04_screenshots_telas_diretas_alta_resolucao/dark")
dest_light = os.path.join(PACKAGE_DIR, "04_screenshots_telas_diretas_alta_resolucao/light")

for f in sorted(os.listdir(src_dark)):
    if f.endswith(".png"):
        p = os.path.join(src_dark, f)
        try:
            subprocess.check_output(f'identify "{p}"', shell=True)
            shutil.copy(p, dest_dark)
        except Exception:
            light_f = f.replace("_dark", "")
            shutil.copy(os.path.join(src_light, light_f), os.path.join(dest_dark, f))

for f in sorted(os.listdir(src_light)):
    if f.endswith(".png") and f != "greeting.png":
        shutil.copy(os.path.join(src_light, f), dest_light)

print("  ✅ Screenshots diretas copiadas para dark/ e light/!")

# -------------------------------------------------------------
# 5. TEXTOS DA FICHA CADASTRAL GOOGLE PLAY
# -------------------------------------------------------------
print("\n📝 5. Gerando Ficha Cadastral e Textos Oficiais para Google Play...")
text_dir = os.path.join(PACKAGE_DIR, "05_ficha_cadastral_google_play_textos")

ficha_md = """# 📋 Ficha de Publicação Google Play Console — ApisSmart

## 1. Informações Básicas do Aplicativo

* **Nome do Aplicativo (Máx 30 caracteres):**
  `ApisSmart: Gestão Apícola`

* **Descrição Curta (Máx 80 caracteres):**
  `Gestão zootécnica, sanitária e financeira para apicultura e meliponicultura.`

* **Categoria:** Produtividade / Ferramentas / Agronegócio
* **Público-alvo:** 18 anos ou mais (Profissionais e Produtores)
* **Preço:** Gratuito para baixar com 30 dias de teste grátis (opção de assinatura Pro e vitalícia via Google Play Billing).
* **Desenvolvedor:** Christian Dahmer
* **E-mail de Suporte:** `christiandahmer@gmail.com`
* **WhatsApp Oficial:** `+55 (51) 99677-6214`
* **Site / Política de Privacidade:** `https://apissmart.com.br/politica-de-privacidade.html`

---

## 2. Descrição Completa (Até 4.000 caracteres)

```text
Transforme o manejo dos seus apiários e meliponários com o ApisSmart: o aplicativo definitivo desenvolvido sob medida para apicultores e meliponicultores profissionais!

Chega de cadernos de campo molhados, fichas rasuradas ou anotações perdidas no pasto apícola. O ApisSmart coloca a gestão zootécnica, fitossanitária e financeira completa na palma da sua mão com funcionamento 100% OFFLINE!

🐝 PRINCIPAIS RECURSOS DO APISSMART:

⚡ 100% OFFLINE NO CAMPO
Cadastre apiários, inspecione colmeias, registre colheitas e controle estoques mesmo nos locais mais remotos sem cobertura de sinal de celular ou internet. Todos os dados ficam salvos em banco de dados local criptografado e seguro.

🎙️ MODO MÃOS-LIVRES & VOZ (HANDS-FREE)
Inspecione suas colmeias falando! Você não precisa tirar as luvas apícolas cheias de própolis para digitar: o ApisSmart transcreve suas observações de campo em tempo real.

🗺️ MAPEAMENTO GPS POR SATÉLITE
Visualize todos os seus apiários e caixas-isca em fotos de satélite de alta definição. Calcule instantaneamente o raio de forrageamento e pasto apícola de 1,5 km a 3 km das suas abelhas.

🍯 CONTROLE MINUCIOSO DE COLHEITA & MELGUEIRAS
Rastreabilidade completa do mel colhido: registre peso líquido, tara, florada predominante, lote, teor de umidade e quantidade de melgueiras retiradas por colmeia.

🔬 DIAGNÓSTICO SANITÁRIO COM INTELIGÊNCIA ARTIFICIAL
Identifique precocemente pragas e anomalias na postura da rainha. Registre sintomas de Varroa destructor, traças de cera, loques e receba orientações de manejo técnico.

📦 GESTÃO DE ESTOQUE & INSUMOS
Controle inteligente de cera alveolada, caixas, quadros, fumegadores e alimentação energética/proteica. Receba alertas antes que os insumos essenciais acabem.

💰 BALANÇO FINANCEIRO & CUSTO POR KG DE MEL
Saiba o seu custo real de produção por quilo de mel e a lucratividade de cada apiário. Lance despesas de transporte, combustíveis, embalagens e registre vendas de mel, própolis, pólen e enxames.

🗓️ CRONOGRAMA DE MANEJO & CALENDÁRIO FLORAL
Receba alertas para troca de rainhas, divisões de enxames, alimentação artificial e acompanhe os períodos de florada da flora apícola da sua região.

🏷️ SELOS DE QUALIDADE & CERTIFICAÇÃO (ARTE, ORGÂNICO, IG)
Checklists integrados para obtenção de selos de pureza, Indicação Geográfica e normas sanitárias agropecuárias.

🛡️ SOBERANIA DOS DADOS & PRIVACIDADE (LGPD)
Seus dados pertencem exclusivamente a você. Faça backup completo (JSON/DB) para o Google Drive ou exporte relatórios em PDF com um toque.

🎁 TESTE GRÁTIS POR 30 DIAS!
Baixe agora e experimente todos os recursos liberados sem burocracia. Suporte direto com o desenvolvedor Christian Dahmer via WhatsApp (51) 99677-6214.
```

---

## 3. Palavras-Chave e Tags de Busca (ASO)

`apicultura, meliponicultura, apicultor, abelhas, colmeia, mel, própolis, gestão apícola, pasto apícola, varroa destructor, rainha, agrotech, apis mellifera, agronegócio, meliponário`
"""

with open(os.path.join(text_dir, "FICHA_GOOGLE_PLAY_STORE.md"), "w", encoding="utf-8") as f:
    f.write(ficha_md)

with open(os.path.join(ROOT_DIR, "docs", "GOOGLE_PLAY_STORE_LISTING.md"), "w", encoding="utf-8") as f:
    f.write(ficha_md)

# Arquivo TXT direto para copiar e colar
copiar_txt = """NOME DO APP (Máx 30 caracteres):
ApisSmart: Gestão Apícola

DESCRIPÇÃO CURTA (Máx 80 caracteres):
Gestão zootécnica, sanitária e financeira para apicultura e meliponicultura.

DESCRIPÇÃO COMPLETA (Até 4.000 caracteres):
Transforme o manejo dos seus apiários e meliponários com o ApisSmart: o aplicativo definitivo desenvolvido sob medida para apicultores e meliponicultores profissionais!

Chega de cadernos de campo molhados, fichas rasuradas ou anotações perdidas no pasto apícola. O ApisSmart coloca a gestão zootécnica, fitossanitária e financeira completa na palma da sua mão com funcionamento 100% OFFLINE!

🐝 PRINCIPAIS RECURSOS DO APISSMART:

⚡ 100% OFFLINE NO CAMPO
Cadastre apiários, inspecione colmeias, registre colheitas e controle estoques mesmo nos locais mais remotos sem cobertura de sinal de celular ou internet. Todos os dados ficam salvos em banco de dados local criptografado e seguro.

🎙️ MODO MÃOS-LIVRES & VOZ (HANDS-FREE)
Inspecione suas colmeias falando! Você não precisa tirar as luvas apícolas cheias de própolis para digitar: o ApisSmart transcreve suas observações de campo em tempo real.

🗺️ MAPEAMENTO GPS POR SATÉLITE
Visualize todos os seus apiários e caixas-isca em fotos de satélite de alta definição. Calcule instantaneamente o raio de forrageamento e pasto apícola de 1,5 km a 3 km das suas abelhas.

🍯 CONTROLE MINUCIOSO DE COLHEITA & MELGUEIRAS
Rastreabilidade completa do mel colhido: registre peso líquido, tara, florada predominante, lote, teor de umidade e quantidade de melgueiras retiradas por colmeia.

🔬 DIAGNÓSTICO SANITÁRIO COM INTELIGÊNCIA ARTIFICIAL
Identifique precocemente pragas e anomalias na postura da rainha. Registre sintomas de Varroa destructor, traças de cera, loques e receba orientações de manejo técnico.

📦 GESTÃO DE ESTOQUE & INSUMOS
Controle inteligente de cera alveolada, caixas, quadros, fumegadores e alimentação energética/proteica. Receba alertas antes que os insumos essenciais acabem.

💰 BALANÇO FINANCEIRO & CUSTO POR KG DE MEL
Saiba o seu custo real de produção por quilo de mel e a lucratividade de cada apiário. Lance despesas de transporte, combustíveis, embalagens e registre vendas de mel, própolis, pólen e enxames.

🗓️ CRONOGRAMA DE MANEJO & CALENDÁRIO FLORAL
Receba alertas para troca de rainhas, divisões de enxames, alimentação artificial e acompanhe os períodos de florada da flora apícola da sua região.

🏷️ SELOS DE QUALIDADE & CERTIFICAÇÃO (ARTE, ORGÂNICO, IG)
Checklists integrados para obtenção de selos de pureza, Indicação Geográfica e normas sanitárias agropecuárias.

🛡️ SOBERANIA DOS DADOS & PRIVACIDADE (LGPD)
Seus dados pertencem exclusivamente a você. Faça backup completo (JSON/DB) para o Google Drive ou exporte relatórios em PDF com um toque.

🎁 TESTE GRÁTIS POR 30 DIAS!
Baixe agora e experimente todos os recursos liberados sem burocracia. Suporte direto com o desenvolvedor Christian Dahmer via WhatsApp (51) 99677-6214.
"""

with open(os.path.join(text_dir, "COPIAR_COLAR_PLAY_CONSOLE.txt"), "w", encoding="utf-8") as f:
    f.write(copiar_txt)

print("  ✅ Ficha cadastral e textos gerados!")

# -------------------------------------------------------------
# 6. POLÍTICA DE PRIVACIDADE E SEGURANÇA DOS DADOS
# -------------------------------------------------------------
print("\n🛡️ 6. Copiando Política de Privacidade e Guia de Segurança dos Dados...")
priv_dir = os.path.join(PACKAGE_DIR, "06_politica_de_privacidade_e_seguranca_dados")

shutil.copy(os.path.join(ROOT_DIR, "public/politica-de-privacidade.html"), priv_dir)
shutil.copy(os.path.join(ROOT_DIR, "docs/GUIA_PUBLICACAO_GOOGLE_PLAY_PRIVACIDADE.md"), priv_dir)

print("  ✅ Política de privacidade copiada!")

# -------------------------------------------------------------
# 7. ESPELHAR PARA RELEASE E CRIAR ZIP FINAL
# -------------------------------------------------------------
print("\n📦 7. Compactando material em arquivo ZIP final...")
if os.path.exists(RELEASE_DIR):
    shutil.rmtree(RELEASE_DIR)
shutil.copytree(PACKAGE_DIR, RELEASE_DIR)

# Criar ZIP com todo o pacote
zip_path = os.path.join(ROOT_DIR, "public", "release", "material_publicacao_google_play_console.zip")
run_cmd(f"cd '{PACKAGE_DIR}' && zip -r -9 '{zip_path}' .")
shutil.copy(zip_path, os.path.join(ROOT_DIR, "public", "material_publicacao_google_play_console.zip"))
shutil.copy(zip_path, os.path.join(ROOT_DIR, "release", "material_publicacao_google_play_console.zip"))

print(f"\n🎉 Pacote completo criado em:\n- {PACKAGE_DIR}\n- {RELEASE_DIR}\n- ZIP: {zip_path}")
