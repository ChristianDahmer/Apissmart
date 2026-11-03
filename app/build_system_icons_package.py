#!/usr/bin/env python3
import os
import subprocess
import shutil

ROOT_DIR = os.getcwd()
OUTPUT_DIR = os.path.join(ROOT_DIR, "public", "release", "icones")
RELEASE_OUTPUT_DIR = os.path.join(ROOT_DIR, "release", "icones")
PUBLIC_DIR = os.path.join(ROOT_DIR, "public", "icones")

os.makedirs(OUTPUT_DIR, exist_ok=True)
os.makedirs(RELEASE_OUTPUT_DIR, exist_ok=True)
os.makedirs(PUBLIC_DIR, exist_ok=True)

# Subfolders
folders = [
    "01_icone_oficial_google_play_512x512",
    "02_icones_do_sistema_e_modulos",
    "03_icones_android_densidades_mipmaps",
    "04_vetores_svg_e_xml"
]

for folder in folders:
    os.makedirs(os.path.join(OUTPUT_DIR, folder), exist_ok=True)

print("⚡ Processing system icon assets with ImageMagick...")

# Source images in app
src_icon_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_smart_icon_1790192172161.jpg")
src_bee_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_smart_bee_icon_1790690258058.jpg")
src_honey_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/honeycomb_bg_3d_1790683773548.jpg")
src_hero_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apissmart_hero_banner_1792749788729.jpg")
src_hero2_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_hero_banner_1790192183160.jpg")
src_mipmap_xxxhdpi = os.path.join(ROOT_DIR, "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png")

# 1. Google Play Store 512x512 High Res Icons
gplay_dir = os.path.join(OUTPUT_DIR, "01_icone_oficial_google_play_512x512")

# Square 512x512
cmd1 = f"convert '{src_icon_jpg}' -resize 512x512! -quality 95 '{os.path.join(gplay_dir, 'icone_oficial_quadrado_512x512.png')}'"
subprocess.run(cmd1, shell=True, check=True)

# Rounded corners 512x512 (Google Play style)
cmd2 = f"""convert '{src_icon_jpg}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black polygon 0,0 0,60 60,0 fill white circle 60,60 60,0" \
     \( +clone -flip \) -composite \( +clone -flop \) -composite \) \
  -alpha off -compose CopyOpacity -composite '{os.path.join(gplay_dir, "icone_oficial_google_play_512x512.png")}'"""
subprocess.run(cmd2, shell=True, check=True)

# Transparent background / circular badge
cmd3 = f"""convert '{src_icon_jpg}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black circle 256,256 256,0" \) \
  -alpha off -compose CopyOpacity -composite '{os.path.join(gplay_dir, "icone_oficial_circulo_512x512.png")}'"""
subprocess.run(cmd3, shell=True, check=True)

print("  ✅ Created 512x512 Google Play Store icons")

# 2. System and Module Icons
mod_dir = os.path.join(OUTPUT_DIR, "02_icones_do_sistema_e_modulos")

# Bee icon 512x512
cmd_bee = f"convert '{src_bee_jpg}' -resize 512x512! -quality 95 '{os.path.join(mod_dir, 'icone_abelha_rainha_3d_512x512.png')}'"
subprocess.run(cmd_bee, shell=True, check=True)

# Honeycomb 3D texture 512x512
cmd_honey = f"convert '{src_honey_jpg}' -resize 512x512! -quality 95 '{os.path.join(mod_dir, 'icone_favo_mel_textura_512x512.png')}'"
subprocess.run(cmd_honey, shell=True, check=True)

# Hero banners HD
cmd_hero = f"convert '{src_hero_jpg}' -quality 92 '{os.path.join(mod_dir, 'banner_hero_apissmart_hd.jpg')}'"
subprocess.run(cmd_hero, shell=True, check=True)

cmd_hero2 = f"convert '{src_hero2_jpg}' -quality 92 '{os.path.join(mod_dir, 'banner_tecnico_apissmart_hd.jpg')}'"
subprocess.run(cmd_hero2, shell=True, check=True)

print("  ✅ Created System & Module icons")

# 3. Android Density Mipmaps
mip_dir = os.path.join(OUTPUT_DIR, "03_icones_android_densidades_mipmaps")

densities = [
    ("mipmap_xxxhdpi_192x192.png", 192),
    ("mipmap_xxhdpi_144x144.png", 144),
    ("mipmap_xhdpi_96x96.png", 96),
    ("mipmap_hdpi_72x72.png", 72),
    ("mipmap_mdpi_48x48.png", 48)
]

for filename, size in densities:
    target_path = os.path.join(mip_dir, filename)
    cmd_size = f"convert '{src_icon_jpg}' -resize {size}x{size}! -quality 95 '{target_path}'"
    subprocess.run(cmd_size, shell=True, check=True)

print("  ✅ Created Android density mipmap icons")

# 4. Vector SVG and XML files
vec_dir = os.path.join(OUTPUT_DIR, "04_vetores_svg_e_xml")

xml_sources = [
    os.path.join(ROOT_DIR, "app/src/main/res/drawable/ic_beehive.xml"),
    os.path.join(ROOT_DIR, "app/src/main/res/drawable/ic_launcher_foreground.xml"),
    os.path.join(ROOT_DIR, "app/src/main/res/drawable/ic_launcher_background.xml"),
    os.path.join(ROOT_DIR, "app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml")
]

for xml_file in xml_sources:
    if os.path.exists(xml_file):
        shutil.copy(xml_file, vec_dir)

print("  ✅ Copied XML vector files")

# 5. Write Markdown Documentation
guide_path = os.path.join(OUTPUT_DIR, "GUIA_ICONES_E_IMAGENS.md")
guide_content = """# 🎨 Guia de Ícones e Imagens do Sistema — ApisSmart

Este pacote reúne todos os ativos visuais, ícones de alta resolução, logotipos e gráficos do sistema **ApisSmart (v4.44)**.

---

## 📁 Estrutura da Pasta

### 1. `01_icone_oficial_google_play_512x512/`
Ícones em resolução máxima de **512x512 pixels**, preparados especificamente para o envio ao Google Play Console e uso em sites e materiais de divulgação:
* **`icone_oficial_google_play_512x512.png`**: Versão oficial com cantos suavemente arredondados, pronta para a Ficha da Play Store.
* **`icone_oficial_quadrado_512x512.png`**: Versão inteira quadrada sem máscaras de corte.
* **`icone_oficial_circulo_512x512.png`**: Versão em emblema circular com fundo transparente.

---

### 2. `02_icones_do_sistema_e_modulos/`
Gráficos e ilustrações dos módulos principais do aplicativo:
* **`icone_abelha_rainha_3d_512x512.png`**: Ícone 3D em alta definição representando o manejo apícola e genética de rainhas.
* **`icone_favo_mel_textura_512x512.png`**: Textura hexagonal 3D de favo de mel utilizada no plano de fundo.
* **`banner_hero_apissmart_hd.jpg`**: Banner principal com mockup do aplicativo em modo escuro e apiário ao fundo.
* **`banner_tecnico_apissmart_hd.jpg`**: Banner secundário de capa técnica.

---

### 3. `03_icones_android_densidades_mipmaps/`
Ícone do aplicativo redimensionado nas densidades padrão do sistema Android para testes e instalações:
* **`mipmap_xxxhdpi_192x192.png`** (Extra Extra Extra High Density - 192x192 px)
* **`mipmap_xxhdpi_144x144.png`** (Extra Extra High Density - 144x144 px)
* **`mipmap_xhdpi_96x96.png`** (Extra High Density - 96x96 px)
* **`mipmap_hdpi_72x72.png`** (High Density - 72x72 px)
* **`mipmap_mdpi_48x48.png`** (Medium Density - 48x48 px)

---

### 4. `04_vetores_svg_e_xml/`
Arquivos de vetores nativos do Android (VectorDrawables) utilizados no aplicativo:
* **`ic_beehive.xml`**: Vetor oficial da colmeia zootécnica.
* **`ic_launcher_foreground.xml`**: Camada de primeiro plano do ícone adaptativo Android.
* **`ic_launcher_background.xml`**: Camada de fundo do ícone adaptativo Android.
* **`ic_launcher.xml`**: Configuração do ícone adaptativo para Android 8.0+.

---

## 📲 Como Utilizar no Google Play Console

1. Acesse o **Google Play Console** -> **Ficha do app principal** -> **Ativos de exibição**.
2. No campo **"Ícone do app"**, faça o upload do arquivo:  
   `01_icone_oficial_google_play_512x512/icone_oficial_google_play_512x512.png`
3. Salve as alterações.
"""

with open(guide_path, "w", encoding="utf-8") as f:
    f.write(guide_content)

print("  ✅ Created GUIA_ICONES_E_IMAGENS.md")

# Mirror to release/icones and public/icones
if os.path.exists(RELEASE_OUTPUT_DIR):
    shutil.rmtree(RELEASE_OUTPUT_DIR)
shutil.copytree(OUTPUT_DIR, RELEASE_OUTPUT_DIR)

if os.path.exists(PUBLIC_DIR):
    shutil.rmtree(PUBLIC_DIR)
shutil.copytree(OUTPUT_DIR, PUBLIC_DIR)

# Also copy to docs/ for documentation
shutil.copy(guide_path, os.path.join(ROOT_DIR, "docs", "GUIA_ICONES_E_IMAGENS.md"))

print("🎉 System icons directory build complete!")
