#!/usr/bin/env python3
import os
import subprocess
import shutil

ROOT_DIR = os.getcwd()
OUTPUT_DIR = os.path.join(ROOT_DIR, "public", "release", "icones")
RELEASE_OUTPUT_DIR = os.path.join(ROOT_DIR, "release", "icones")
PUBLIC_DIR = os.path.join(ROOT_DIR, "public", "icones")

# Ensure clean directory structure
if os.path.exists(OUTPUT_DIR):
    shutil.rmtree(OUTPUT_DIR)

os.makedirs(OUTPUT_DIR, exist_ok=True)

folders = [
    "01_icone_oficial_google_play_512x512",
    "02_outras_variacoes_e_logotipos",
    "03_icones_android_densidades_mipmaps",
    "04_vetores_svg_e_xml"
]

for folder in folders:
    os.makedirs(os.path.join(OUTPUT_DIR, folder), exist_ok=True)

# Sources
src_bee_official = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_smart_bee_icon_1790690258058.jpg")
src_logo_geometric = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_smart_icon_1790192172161.jpg")
src_honey_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/honeycomb_bg_3d_1790683773548.jpg")
src_hero_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apissmart_hero_banner_1792749788729.jpg")
src_hero2_jpg = os.path.join(ROOT_DIR, "app/src/main/res/drawable/apis_hero_banner_1790192183160.jpg")

print("🐝 Rebuilding official ApisSmart system icons with true 3D Bee asset...")

# 1. Official Google Play Store Icons (512x512)
gplay_dir = os.path.join(OUTPUT_DIR, "01_icone_oficial_google_play_512x512")

# Oficial Quadrado (Padrão Oficial Recomendado pela Google Play Console: 512x512, sem transparência externa, o Google Play aplica a máscara automaticamente)
cmd_gplay_square = f"convert '{src_bee_official}' -resize 512x512! -quality 98 '{os.path.join(gplay_dir, 'icone_oficial_google_play_512x512.png')}'"
subprocess.run(cmd_gplay_square, shell=True, check=True)

# Versão com máscara arredondada pré-visualização (estilo Play Store / iOS)
cmd_gplay_rounded = f"""convert '{src_bee_official}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black polygon 0,0 0,64 64,0 fill white circle 64,64 64,0" \
     \( +clone -flip \) -composite \( +clone -flop \) -composite \) \
  -alpha off -compose CopyOpacity -composite '{os.path.join(gplay_dir, "icone_oficial_arredondado_preview_512x512.png")}'"""
subprocess.run(cmd_gplay_rounded, shell=True, check=True)

# Versão Circular (Round Icon)
cmd_gplay_circle = f"""convert '{src_bee_official}' -resize 512x512! \
  \( +clone -threshold -1 -draw "fill black circle 256,256 256,0" \) \
  -alpha off -compose CopyOpacity -composite '{os.path.join(gplay_dir, "icone_oficial_circular_512x512.png")}'"""
subprocess.run(cmd_gplay_circle, shell=True, check=True)

print("  ✅ Created 01_icone_oficial_google_play_512x512")

# 2. Outras Variações e Logotipos
var_dir = os.path.join(OUTPUT_DIR, "02_outras_variacoes_e_logotipos")

cmd_geom = f"convert '{src_logo_geometric}' -resize 512x512! -quality 95 '{os.path.join(var_dir, 'logotipo_geometrico_vetorial_512x512.png')}'"
subprocess.run(cmd_geom, shell=True, check=True)

cmd_honey = f"convert '{src_honey_jpg}' -resize 512x512! -quality 95 '{os.path.join(var_dir, 'textura_favo_mel_3d_512x512.png')}'"
subprocess.run(cmd_honey, shell=True, check=True)

cmd_hero = f"convert '{src_hero_jpg}' -quality 92 '{os.path.join(var_dir, 'banner_hero_apissmart_hd.jpg')}'"
subprocess.run(cmd_hero, shell=True, check=True)

cmd_hero2 = f"convert '{src_hero2_jpg}' -quality 92 '{os.path.join(var_dir, 'banner_tecnico_apissmart_hd.jpg')}'"
subprocess.run(cmd_hero2, shell=True, check=True)

print("  ✅ Created 02_outras_variacoes_e_logotipos")

# 3. Android Density Mipmaps (Generated from the true 3D Bee icon)
mip_dir = os.path.join(OUTPUT_DIR, "03_icones_android_densidades_mipmaps")

densities = [
    ("mipmap_xxxhdpi_192x192.png", 192, "mipmap-xxxhdpi"),
    ("mipmap_xxhdpi_144x144.png", 144, "mipmap-xxhdpi"),
    ("mipmap_xhdpi_96x96.png", 96, "mipmap-xhdpi"),
    ("mipmap_hdpi_72x72.png", 72, "mipmap-hdpi"),
    ("mipmap_mdpi_48x48.png", 48, "mipmap-mdpi")
]

for filename, size, res_folder in densities:
    target_path = os.path.join(mip_dir, filename)
    cmd_size = f"convert '{src_bee_official}' -resize {size}x{size}! -quality 95 '{target_path}'"
    subprocess.run(cmd_size, shell=True, check=True)
    
    # Also update the actual app mipmaps in app/src/main/res/
    app_res_dir = os.path.join(ROOT_DIR, "app/src/main/res", res_folder)
    os.makedirs(app_res_dir, exist_ok=True)
    app_target_square = os.path.join(app_res_dir, "ic_launcher.png")
    app_target_round = os.path.join(app_res_dir, "ic_launcher_round.png")
    
    subprocess.run(f"convert '{src_bee_official}' -resize {size}x{size}! -quality 95 '{app_target_square}'", shell=True, check=True)
    
    cmd_round = f"""convert '{src_bee_official}' -resize {size}x{size}! \
      \( +clone -threshold -1 -draw "fill black circle {size//2},{size//2} {size//2},0" \) \
      -alpha off -compose CopyOpacity -composite '{app_target_round}'"""
    subprocess.run(cmd_round, shell=True, check=True)

print("  ✅ Created 03_icones_android_densidades_mipmaps and updated app mipmap resources")

# 4. XML / SVG vectors
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

# 5. GUIA_ICONES_E_IMAGENS.md
guide_content = """# 🎨 Pacote Oficial de Ícones e Imagens do Sistema — ApisSmart (v4.44)

Este pacote contém os ativos gráficos oficiais do **ApisSmart**, com o verdadeiro ícone oficial da abelha em 3D no favo de mel melado e todas as suas variações.

---

## 🐝 O Verdadeiro Ícone Oficial do Sistema

O ícone oficial do **ApisSmart** adotado no aplicativo Android (conforme definido no `AndroidManifest.xml` e no `ic_launcher_foreground.xml`) é a **Abelha 3D carismática no favo de mel dourado escorrendo mel**:

* **`01_icone_oficial_google_play_512x512/icone_oficial_google_play_512x512.png`**:
  * **Dimensões:** 512 x 512 pixels (PNG de 32 bits, sem transparência externa, conforme as diretrizes oficiais do Google Play Console).
  * **Finalidade:** O arquivo exato para upload na seção **"Ícone do app"** no Google Play Console.
* **`01_icone_oficial_google_play_512x512/icone_oficial_arredondado_preview_512x512.png`**:
  * Pré-visualização com cantos arredondados (squircle).
* **`01_icone_oficial_google_play_512x512/icone_oficial_circular_512x512.png`**:
  * Versão em emblema circular com fundo transparente (Round Icon).

---

## 📁 Estrutura Completa das Pastas

### 1. `01_icone_oficial_google_play_512x512/`
Contém os ícones em resolução máxima de 512x512 pixels para a publicação na Play Store, web e imprensa.

### 2. `02_outras_variacoes_e_logotipos/`
* `logotipo_geometrico_vetorial_512x512.png`: Logotipo apícola geométrico alternativo.
* `textura_favo_mel_3d_512x512.png`: Textura de favo de mel em 3D.
* `banner_hero_apissmart_hd.jpg`: Banner promocional de capa com mockup.
* `banner_tecnico_apissmart_hd.jpg`: Banner técnico de divulgação.

### 3. `03_icones_android_densidades_mipmaps/`
Ícone oficial do aplicativo redimensionado para as densidades nativas do Android:
* `mipmap_xxxhdpi_192x192.png` (192 x 192 px)
* `mipmap_xxhdpi_144x144.png` (144 x 144 px)
* `mipmap_xhdpi_96x96.png` (96 x 96 px)
* `mipmap_hdpi_72x72.png` (72 x 72 px)
* `mipmap_mdpi_48x48.png` (48 x 48 px)

### 4. `04_vetores_svg_e_xml/`
Arquivos de configuração e vetores nativos do Android (Adaptive Icon).

---

## 📲 Como fazer o Upload no Google Play Console

1. Abra o [Google Play Console](https://play.google.com/console) e selecione o aplicativo **ApisSmart**.
2. No menu lateral, acesse: **Crescimento** -> **Presença na loja** -> **Ficha da loja principal**.
3. Na seção **Ativos de exibição**, localize o campo **Ícone do app (512 x 512 px)**.
4. Faça o upload do arquivo:  
   `01_icone_oficial_google_play_512x512/icone_oficial_google_play_512x512.png`
5. Clique em **Salvar**.
"""

with open(os.path.join(OUTPUT_DIR, "GUIA_ICONES_E_IMAGENS.md"), "w", encoding="utf-8") as f:
    f.write(guide_content)

with open(os.path.join(ROOT_DIR, "docs", "GUIA_ICONES_E_IMAGENS.md"), "w", encoding="utf-8") as f:
    f.write(guide_content)

# Mirror to release/icones and public/icones
if os.path.exists(RELEASE_OUTPUT_DIR):
    shutil.rmtree(RELEASE_OUTPUT_DIR)
shutil.copytree(OUTPUT_DIR, RELEASE_OUTPUT_DIR)

if os.path.exists(PUBLIC_DIR):
    shutil.rmtree(PUBLIC_DIR)
shutil.copytree(OUTPUT_DIR, PUBLIC_DIR)

print("🎉 True system icons directory rebuild complete!")
