import os
import subprocess

PUBLIC_RELEASE_DIR = "public/release"
SCREENSHOTS_DARK_DIR = os.path.join(PUBLIC_RELEASE_DIR, "screenshots/dark")
MARKETING_DIR = os.path.join(PUBLIC_RELEASE_DIR, "marketing")
MARKETING_REAL_DIR = os.path.join(PUBLIC_RELEASE_DIR, "marketing_real")

os.makedirs(MARKETING_DIR, exist_ok=True)
os.makedirs(MARKETING_REAL_DIR, exist_ok=True)

def run_cmd(cmd):
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"Error executing command: {cmd}\nStderr: {result.stderr}")
    return result.returncode == 0

def make_phone_mockup(screenshot_path, output_path, phone_w, phone_h, corner_r=24):
    """
    Creates a phone mockup of size (phone_w, phone_h) around screenshot_path
    """
    border_w = 12
    screen_w = phone_w - (border_w * 2)
    screen_h = phone_h - (border_w * 2) - 16
    
    # 1. Resize screenshot to screen inner dimensions with rounded bottom/top
    tmp_screen = "/tmp/tmp_screen.png"
    tmp_frame = "/tmp/tmp_frame.png"
    
    cmd_screen = f"convert '{screenshot_path}' -resize {screen_w}x{screen_h}^ -gravity center -extent {screen_w}x{screen_h} '{tmp_screen}'"
    run_cmd(cmd_screen)
    
    # 2. Draw frame with rounded corners, notch bar, and shadow
    notch_w = int(phone_w * 0.35)
    notch_h = 18
    notch_x1 = (phone_w - notch_w) // 2
    notch_x2 = notch_x1 + notch_w
    
    cmd_frame = f"""
    convert -size {phone_w}x{phone_h} xc:none \
      -fill '#1E293B' -draw 'roundrectangle 0,0,{phone_w-1},{phone_h-1} {corner_r},{corner_r}' \
      -fill '#0F172A' -draw 'roundrectangle 4,4,{phone_w-5},{phone_h-5} {corner_r-2},{corner_r-2}' \
      '{tmp_frame}'
    """
    run_cmd(cmd_frame)
    
    # Composite screen onto frame
    cmd_comp = f"""
    convert '{tmp_frame}' \
      '{tmp_screen}' -geometry +{border_w}+{border_w+8} -composite \
      -fill '#000000' -draw 'roundrectangle {notch_x1},{border_w+2} {notch_x2},{border_w+2+notch_h} 8,8' \
      '{output_path}'
    """
    run_cmd(cmd_comp)

print("Building marketing mockups...")

# Create phone mockups for key screens
mockup_dash = "/tmp/mockup_dash.png"
mockup_colheita = "/tmp/mockup_colheita.png"
mockup_finance = "/tmp/mockup_finance.png"
mockup_ia = "/tmp/mockup_ia.png"

make_phone_mockup(os.path.join(SCREENSHOTS_DARK_DIR, "01_dashboard_screen_dark.png"), mockup_dash, 440, 920)
make_phone_mockup(os.path.join(SCREENSHOTS_DARK_DIR, "04_colheita_e_producao_dark.png"), mockup_colheita, 380, 800)
make_phone_mockup(os.path.join(SCREENSHOTS_DARK_DIR, "07_gestao_financeira_dark.png"), mockup_finance, 380, 800)
make_phone_mockup(os.path.join(SCREENSHOTS_DARK_DIR, "08_assistente_tecnico_ia_dark.png"), mockup_ia, 380, 800)

print("Phone mockups generated.")

# 1. Folder Promocional Frente (1200x1600 - 3:4)
frente_path = os.path.join(MARKETING_DIR, "folder_promocional_frente_real.jpg")
cmd_frente = f"""
convert -size 1200x1600 xc:none \
  -sparse-color Barycentric '0,0 #0D1117 0,1600 #161B22 1200,800 #1C2333' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1200,12' \
  -fill '#10B981' -draw 'rectangle 0,1588 1200,1600' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north -annotate +0+35 '🐝  APISSMART v4.41  •  TECNOLOGIA AGROTECHO  ' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 42 -gravity north -annotate +0+80 'O Aplicativo Definitivo para o\\nApicultor e Meliponicultor' \
  -fill '#9CA3AF' -font 'DejaVu-Sans' -pointsize 24 -gravity north -annotate +0+185 'Gestão Zootécnica Inteligente no Seu Bolso — 100% Offline' \
  '{mockup_dash}' -gravity north -geometry +0+250 -composite \
  -fill '#1E293B' -draw 'roundrectangle 60,1220 1140,1460 20,20' \
  -fill '#0F172A' -draw 'roundrectangle 64,1224 1136,1456 18,18' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +100+1245 '⚡ 100% OFFLINE: Funcione no pasto apícola sem sinal' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +100+1295 '🎙️ COMANDO DE VOZ: Dite inspeções com luvas sujas de própolis' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +100+1345 '🗺️ SATÉLITE & GPS: Mapeamento de apiários, colmeias e voo' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +100+1395 '📊 GESTÃO FINANCEIRA: Custo por kg de mel e lucro real' \
  -fill '#10B981' -draw 'roundrectangle 60,1480 1140,1560 16,16' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 30 -gravity north -annotate +0+1498 '🎁 TESTE GRÁTIS POR 30 DIAS!  📲 WhatsApp: (51) 99677-6214' \
  -quality 95 '{frente_path}'
"""
run_cmd(cmd_frente)
print(f"Created {frente_path}")

# 2. Folder Promocional Verso (1200x1600 - 3:4)
verso_path = os.path.join(MARKETING_DIR, "folder_promocional_verso_real.jpg")
cmd_verso = f"""
convert -size 1200x1600 xc:none \
  -sparse-color Barycentric '0,0 #0D1117 0,1600 #161B22 1200,800 #1F2937' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1200,12' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north -annotate +0+35 'RECURSOS ZOOTÉCNICOS & CONTROLE INTEGRADO' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 40 -gravity north -annotate +0+80 'Gestão Completa do Campo à Comercialização' \
  '{mockup_colheita}' -gravity north-west -geometry +100+160 -composite \
  '{mockup_finance}' -gravity north-east -geometry +100+160 -composite \
  -fill '#1E293B' -draw 'roundrectangle 60,1000 1140,1460 20,20' \
  -fill '#0F172A' -draw 'roundrectangle 64,1004 1136,1456 18,18' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1025 '🍯 COLHEITA & PRODUÇÃO:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1025 'Lotes de mel, florada, pesagem e % de umidade' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1095 '💰 BALANÇO FINANCEIRO:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1095 'Receitas, despesas, margem de lucro e custo por kg' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1165 '🔬 DIAGNÓSTICO COM IA:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1165 'Varroa destructor, traças de cera, postura e sanidade' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1235 '📦 ESTOQUE DE INSUMOS:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1235 'Cera alviolada, alimento, caixas e materiais apícolas' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1305 '🗓️ CRONOGRAMA MANEJOS:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1305 'Alimentação artificial, divisão e troca de rainhas' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north-west -annotate +90+1375 '📱 QR CODE E RASTREIO:' \
  -fill '#E5E7EB' -font 'DejaVu-Sans' -pointsize 22 -gravity north-west -annotate +400+1375 'Etiquetas inteligentes para tambores e melgueiras' \
  -fill '#10B981' -draw 'roundrectangle 60,1480 1140,1560 16,16' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 28 -gravity north -annotate +0+1500 '📲 Fale no WhatsApp: (51) 99677-6214  •  Christian Dahmer' \
  -quality 95 '{verso_path}'
"""
run_cmd(cmd_verso)
print(f"Created {verso_path}")

# 3. Anúncio Feed Instagram/Facebook (1080x1080 - 1:1)
feed_path = os.path.join(MARKETING_DIR, "anuncio_feed_1x1_real.jpg")
cmd_feed = f"""
convert -size 1080x1080 xc:none \
  -sparse-color Barycentric '0,0 #0F172A 0,1080 #1E293B 1080,540 #111827' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1080,10' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north -annotate +0+30 '🐝 APISSMART APICULTURA DIGITAL' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 36 -gravity north -annotate +0+70 'Sua Apicultura em Outro Nível\\nde Produtividade e Gestão' \
  '{mockup_dash}' -gravity north-west -geometry +60+170 -composite \
  '{mockup_ia}' -gravity north-east -geometry +60+220 -composite \
  -fill '#1E293B' -draw 'roundrectangle 50,830 1030,970 16,16' \
  -fill '#0F172A' -draw 'roundrectangle 54,834 1026,966 14,14' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 22 -gravity north-west -annotate +80+855 '⚡ 100% OFFLINE   🎙️ VOZ COM IA   🗺️ MAPA SATÉLITE   📊 FINANCEIRO' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 22 -gravity north-west -annotate +80+905 '🎁 30 DIAS GRÁTIS  •  Desenvolvido por Christian Dahmer' \
  -fill '#10B981' -draw 'roundrectangle 50,990 1030,1055 12,12' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 24 -gravity north -annotate +0+1005 '📲 WhatsApp: (51) 99677-6214 — Teste Agora sem Burocracia!' \
  -quality 95 '{feed_path}'
"""
run_cmd(cmd_feed)
print(f"Created {feed_path}")

# 4. Anúncio Stories 9x16 (1080x1920)
stories_path = os.path.join(MARKETING_DIR, "anuncio_stories_9x16_real.jpg")
cmd_stories = f"""
convert -size 1080x1920 xc:none \
  -sparse-color Barycentric '0,0 #0D1117 0,1920 #161B22 1080,960 #1F2937' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1080,12' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 28 -gravity north -annotate +0+50 '🐝 APISSMART APICULTURA' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 46 -gravity north -annotate +0+110 'Dite Inspeções sem\\nTirar as Luvas!' \
  -fill '#9CA3AF' -font 'DejaVu-Sans' -pointsize 26 -gravity north -annotate +0+240 'Comando de Voz com IA e Funcionamento 100% Offline' \
  '{mockup_dash}' -gravity north -geometry +0+310 -composite \
  -fill '#1E293B' -draw 'roundrectangle 60,1310 1020,1650 20,20' \
  -fill '#0F172A' -draw 'roundrectangle 64,1314 1016,1646 18,18' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +90+1340 '✅ Mapeamento GPS por Satélite' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +90+1410 '✅ Controle de Pesagem & Melgueiras' \
  -fill '#F59E0B' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +90+1480 '✅ Custos por kg de mel & Lucro' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north-west -annotate +90+1550 '✅ Diagnóstico Sanitário Inteligente' \
  -fill '#10B981' -draw 'roundrectangle 60,1700 1020,1830 20,20' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 32 -gravity north -annotate +0+1730 '🎁 TESTE 30 DIAS GRÁTIS!' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 26 -gravity north -annotate +0+1780 '📲 Chame no WhatsApp: (51) 99677-6214' \
  -quality 95 '{stories_path}'
"""
run_cmd(cmd_stories)
print(f"Created {stories_path}")

# 5. Banner Horizontal 16x9 (1920x1080)
banner_path = os.path.join(MARKETING_DIR, "banner_horizontal_16x9_real.jpg")
cmd_banner = f"""
convert -size 1920x1080 xc:none \
  -sparse-color Barycentric '0,0 #0D1117 0,1080 #161B22 1920,540 #1F2937' \
  -fill '#F59E0B' -draw 'rectangle 0,0 1920,10' \
  -fill '#FBBF24' -font 'DejaVu-Sans-Bold' -pointsize 28 -gravity north -annotate +0+30 '🐝 SYSTEM APISSMART v4.41  •  SOLUÇÃO COMPLETA PARA APICULTURA & MELIPONICULTURA' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 44 -gravity north -annotate +0+75 'Transforme o Manejo dos seus Apiários com Inteligência Artificial' \
  '{mockup_dash}' -gravity north-west -geometry +120+180 -composite \
  '{mockup_colheita}' -gravity north -geometry +0+220 -composite \
  '{mockup_finance}' -gravity north-east -geometry +120+180 -composite \
  -fill '#10B981' -draw 'roundrectangle 200,980 1720,1060 16,16' \
  -fill '#FFFFFF' -font 'DejaVu-Sans-Bold' -pointsize 30 -gravity north -annotate +0+1000 '🚀 100% OFFLINE   •   30 DIAS TESTE GRÁTIS   •   📲 WhatsApp Suporte/Vendas: (51) 99677-6214 (Christian Dahmer)' \
  -quality 95 '{banner_path}'
"""
run_cmd(cmd_banner)
print(f"Created {banner_path}")

# Copy to specific alias files
aliases = [
    ("anuncio_feed_1x1_real.jpg", "anuncio_feed_whatsapp_51996776214.jpg"),
    ("anuncio_stories_9x16_real.jpg", "anuncio_stories_whatsapp_51996776214.jpg"),
    ("folder_promocional_frente_real.jpg", "folder_flyer_whatsapp_51996776214.jpg"),
    ("anuncio_feed_1x1_real.jpg", "anuncio_feed_dark_1x1.jpg"),
    ("anuncio_stories_9x16_real.jpg", "anuncio_stories_dark_9x16.jpg"),
    ("banner_horizontal_16x9_real.jpg", "banner_dark_social.jpg"),
    ("banner_horizontal_16x9_real.jpg", "banner_promocional.jpg"),
    ("folder_promocional_frente_real.jpg", "folder_dark_frente.jpg"),
    ("folder_promocional_verso_real.jpg", "folder_dark_verso.jpg"),
    ("folder_promocional_frente_real.jpg", "folder_promocional_frente.jpg"),
    ("folder_promocional_verso_real.jpg", "folder_promocional_verso.jpg"),
]

for src_name, dst_name in aliases:
    src_p = os.path.join(MARKETING_DIR, src_name)
    dst_p = os.path.join(MARKETING_DIR, dst_name)
    dst_real_p = os.path.join(MARKETING_REAL_DIR, dst_name)
    if os.path.exists(src_p):
        run_cmd(f"cp '{src_p}' '{dst_p}'")
        run_cmd(f"cp '{src_p}' '{dst_real_p}'")

print("All marketing piece images built successfully!")
