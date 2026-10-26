#!/bin/bash
set -e

mkdir -p release/marketing_real public/marketing_real /tmp/mockups

echo "1. Creating Phone Frame Mockup Generator..."

# Function to wrap a screenshot in a smartphone frame
create_phone_mockup() {
    local input_png="$1"
    local output_png="$2"
    local target_width="$3"

    # Screen dimensions scaled
    local screen_w=$target_width
    local screen_h=$(( screen_w * 2399 / 1078 ))
    local border_w=16
    local phone_w=$(( screen_w + border_w * 2 ))
    local phone_h=$(( screen_h + border_w * 2 + 10 ))

    # 1. Resize screenshot
    convert "$input_png" -resize "${screen_w}x${screen_h}!" /tmp/screen_resized.png

    # 2. Create round corner mask for the screen
    convert -size "${screen_w}x${screen_h}" xc:none -fill white \
        -draw "roundrectangle 0,0,${screen_w},${screen_h},32,32" /tmp/screen_mask.png

    # 3. Apply mask to screen
    convert /tmp/screen_resized.png /tmp/screen_mask.png -alpha Off -compose CopyOpacity -composite /tmp/screen_rounded.png

    # 4. Create phone body (dark titanium frame with golden subtle edge)
    convert -size "${phone_w}x${phone_h}" xc:none \
        -fill "#121824" -stroke "#D97706" -strokewidth 2 \
        -draw "roundrectangle 0,0,${phone_w},${phone_h},40,40" /tmp/phone_body.png

    # 5. Composite rounded screen onto phone body
    convert /tmp/phone_body.png /tmp/screen_rounded.png -geometry "+${border_w}+${border_w}" -composite /tmp/phone_no_shadow.png

    # 6. Add camera punch-hole at top
    local cam_x=$(( phone_w / 2 ))
    local cam_y=$(( border_w + 14 ))
    convert /tmp/phone_no_shadow.png -fill "#0A0D14" -stroke "#222" -strokewidth 1 \
        -draw "circle ${cam_x},${cam_y},${cam_x},$(( cam_y + 5 ))" /tmp/phone_with_cam.png

    # 7. Add realistic drop shadow
    convert /tmp/phone_with_cam.png \( +clone -background black -shadow 80x15+0+15 \) +swap -background none -layers merge +repage "$output_png"
}

echo "2. Generating Mockups from REAL screenshots..."
create_phone_mockup "screenshots/dark/01_dashboard_screen_dark.png" "/tmp/mockups/phone_dashboard.png" 480
create_phone_mockup "screenshots/dark/02_apiarios_e_colmeias_dark.png" "/tmp/mockups/phone_apiarios.png" 380
create_phone_mockup "screenshots/dark/03_inspecoes_sanitarias_dark.png" "/tmp/mockups/phone_inspecoes.png" 360
create_phone_mockup "screenshots/dark/04_colheita_e_producao_dark.png" "/tmp/mockups/phone_colheita.png" 380
create_phone_mockup "screenshots/dark/07_gestao_financeira_dark.png" "/tmp/mockups/phone_financeiro.png" 380
create_phone_mockup "screenshots/dark/08_assistente_tecnico_ia_dark.png" "/tmp/mockups/phone_ia.png" 380

echo "3. Generating FOLDER FRENTE (1200x1600 - 3:4) with REAL Dashboard Screenshot..."
convert -size 1200x1600 \
    gradient:'#0F172A-#080D1A' \
    -fill '#D97706' -draw "rectangle 0,0,1200,12" \
    -fill '#F59E0B' -pointsize 42 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+50 "ApisSmart" \
    -fill '#FFFFFF' -pointsize 26 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+105 "GESTÃO INTELIGENTE DE APIÁRIOS" \
    -fill '#94A3B8' -pointsize 18 -font "DejaVu-Sans" -gravity North -annotate +0+140 "Software Completo para Apicultura e Meliponicultura" \
    /tmp/folder_frente_base.png

# Composite Phone on Folder Frente
convert /tmp/folder_frente_base.png \
    /tmp/mockups/phone_dashboard.png -gravity Center -geometry +0-40 -composite \
    -fill '#059669' -draw "roundrectangle 100,1230,420,1310,12,12" \
    -fill '#FFFFFF' -pointsize 18 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +120+1248 "✓ 100% OFFLINE NO CAMPO" \
    -fill '#94A3B8' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +120+1278 "Funciona sem internet no pasto" \
    \
    -fill '#D97706' -draw "roundrectangle 440,1230,760,1310,12,12" \
    -fill '#FFFFFF' -pointsize 18 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +460+1248 "🎙️ DITADO POR VOZ & IA" \
    -fill '#94A3B8' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +460+1278 "Manejo com luvas sem digitar" \
    \
    -fill '#2563EB' -draw "roundrectangle 780,1230,1100,1310,12,12" \
    -fill '#FFFFFF' -pointsize 18 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +800+1248 "🍯 CONTROLE DE MEL" \
    -fill '#94A3B8' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +800+1278 "Melgueiras, lotes e pesagem" \
    \
    -fill '#1E293B' -stroke '#D97706' -strokewidth 2 -draw "roundrectangle 80,1350,1120,1540,16,16" \
    -fill '#FBBF24' -pointsize 22 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1375 "🎁 TESTE GRÁTIS POR 14 DIAS COM TODAS AS FUNÇÕES!" \
    -fill '#FFFFFF' -pointsize 17 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1415 "Ativação direta sem cartão • Suporte técnico especializado" \
    -fill '#10B981' -pointsize 26 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1465 "📱 WhatsApp / Telefone: (51) 99677-6214" \
    -fill '#CBD5E1' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1505 "Fale com o desenvolvedor: Christian Dahmer" \
    release/marketing_real/folder_promocional_frente_real.jpg

echo "4. Generating FOLDER VERSO (1200x1600 - 3:4) with REAL Feature Screenshots..."
convert -size 1200x1600 \
    gradient:'#0B1120-#040812' \
    -fill '#D97706' -draw "rectangle 0,0,1200,12" \
    -fill '#F59E0B' -pointsize 34 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+40 "RECURSOS AVANÇADOS DO SISTEMA" \
    -fill '#94A3B8' -pointsize 18 -font "DejaVu-Sans" -gravity North -annotate +0+85 "Desenvolvido sob medida para a rotina do produtor de mel" \
    /tmp/folder_verso_base.png

# Composite 2 phones (Colheita & Financeiro) side-by-side
convert /tmp/folder_verso_base.png \
    /tmp/mockups/phone_colheita.png -gravity NorthWest -geometry +100+140 -composite \
    /tmp/mockups/phone_financeiro.png -gravity NorthEast -geometry +100+140 -composite \
    \
    -fill '#1E293B' -stroke '#334155' -strokewidth 1 -draw "roundrectangle 80,1070,1120,1370,14,14" \
    -fill '#FBBF24' -pointsize 20 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1095 "📊 VISÃO INTEGRADA DA SUA PRODUÇÃO" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1135 "• Mapeamento GPS dos Apiários, Colmeias e Caixas-Isca em satélite" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1165 "• Gráficos de evolução de colheita e rendimento médio por colmeia" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1195 "• Fluxo de caixa apícola: controle de custos, vendas e preço do kg de mel" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1225 "• Calculadora nutricional de xarope 1:1 e 2:1 para épocas de entressafra" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1255 "• Emissão de relatórios e laudos zootécnicos em PDF e planilhas CSV" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1285 "• Suporte completo a Abelhas Sem Ferrão (ASF) e Apis Mellifera" \
    -fill '#FFFFFF' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1315 "• Sistema de identificação por QR Code com impressão de etiquetas" \
    \
    -fill '#0F172A' -stroke '#D97706' -strokewidth 2 -draw "roundrectangle 80,1400,1120,1540,14,14" \
    -fill '#10B981' -pointsize 24 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1425 "📞 Peça sua demonstração: (51) 99677-6214" \
    -fill '#E2E8F0' -pointsize 16 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1465 "Atendimento direto com Christian Dahmer • Instalação e Suporte" \
    -fill '#FBBF24' -pointsize 15 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1498 "🌐 Licença vitalícia ou anual disponível para produtores e associações" \
    release/marketing_real/folder_promocional_verso_real.jpg

echo "5. Generating ANÚNCIO FEED (1080x1080 - 1:1) with REAL Dashboard Screenshot..."
convert -size 1080x1080 \
    gradient:'#0F172A-#030712' \
    -fill '#D97706' -draw "rectangle 0,0,1080,10" \
    -fill '#F59E0B' -pointsize 32 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+35 "ApisSmart: Gestão de Apiários" \
    -fill '#FFFFFF' -pointsize 22 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+75 "CONTROLE TOTAL DAS SUAS COLMEIAS 🐝" \
    -fill '#94A3B8' -pointsize 15 -font "DejaVu-Sans" -gravity North -annotate +0+105 "100% Offline • Inspeção por Voz com IA • Gráficos de Produção" \
    /tmp/feed_base.png

# Composite Dashboard Phone Mockup on Feed
convert /tmp/feed_base.png \
    /tmp/mockups/phone_dashboard.png -gravity Center -geometry +180+20 -composite \
    \
    -fill '#059669' -draw "roundrectangle 60,180,480,250,12,12" \
    -fill '#FFFFFF' -pointsize 17 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+195 "✓ 100% SEM INTERNET" \
    -fill '#D1FAE5' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+220 "Funciona dentro do mato e pasto" \
    \
    -fill '#D97706' -draw "roundrectangle 60,280,480,350,12,12" \
    -fill '#FFFFFF' -pointsize 17 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+295 "🎙️ INSPEÇÃO POR VOZ" \
    -fill '#FEF3C7' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+320 "Dite o relato sem tirar as luvas" \
    \
    -fill '#2563EB' -draw "roundrectangle 60,380,480,450,12,12" \
    -fill '#FFFFFF' -pointsize 17 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+395 "🗺️ MAPA SATÉLITE & GPS" \
    -fill '#DBEAFE' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+420 "Apiários, colmeias e caixas-isca" \
    \
    -fill '#7C3AED' -draw "roundrectangle 60,480,480,550,12,12" \
    -fill '#FFFFFF' -pointsize 17 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+495 "💰 GESTÃO FINANCEIRA" \
    -fill '#EDE9FE' -pointsize 13 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+520 "Custo/kg de mel e fluxo de caixa" \
    \
    -fill '#1E293B' -stroke '#F59E0B' -strokewidth 2 -draw "roundrectangle 60,590,480,720,12,12" \
    -fill '#FBBF24' -pointsize 18 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+610 "🎁 14 DIAS GRÁTIS" \
    -fill '#CBD5E1' -pointsize 14 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+638 "Sem cadastro de cartão." \
    -fill '#CBD5E1' -pointsize 14 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+662 "Liberação instantânea." \
    \
    -fill '#111827' -stroke '#10B981' -strokewidth 2 -draw "roundrectangle 50,940,1030,1040,14,14" \
    -fill '#10B981' -pointsize 24 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+960 "📱 WHATSAPP: (51) 99677-6214" \
    -fill '#FBBF24' -pointsize 15 -font "DejaVu-Sans" -gravity NorthWest -annotate +80+998 "Chame agora e comece a usar nas suas colmeias!" \
    release/marketing_real/anuncio_feed_1x1_real.jpg

echo "6. Generating ANÚNCIO STORIES (1080x1920 - 9:16) with REAL Screenshots..."
convert -size 1080x1920 \
    gradient:'#0B132B-#020617' \
    -fill '#D97706' -draw "rectangle 0,0,1080,14" \
    -fill '#F59E0B' -pointsize 38 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+70 "ApisSmart" \
    -fill '#FFFFFF' -pointsize 26 -font "DejaVu-Sans-Bold" -gravity North -annotate +0+125 "GESTÃO PROFISSIONAL DE APIÁRIOS" \
    -fill '#94A3B8' -pointsize 18 -font "DejaVu-Sans" -gravity North -annotate +0+165 "Prints Reais da Aplicação em Operação" \
    /tmp/stories_base.png

# Composite 2 phones (Dashboard and Colheita)
convert /tmp/stories_base.png \
    /tmp/mockups/phone_dashboard.png -gravity North -geometry +0+220 -composite \
    \
    -fill '#059669' -draw "roundrectangle 80,1400,1000,1470,12,12" \
    -fill '#FFFFFF' -pointsize 20 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1422 "✓ 100% OFFLINE • DITADO POR VOZ COM IA" \
    \
    -fill '#1E293B' -stroke '#F59E0B' -strokewidth 2 -draw "roundrectangle 80,1500,1000,1650,14,14" \
    -fill '#FBBF24' -pointsize 22 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +110+1525 "🎁 TESTE GRÁTIS POR 14 DIAS!" \
    -fill '#E2E8F0' -pointsize 16 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1560 "Melhore a produtividade e controle do seu apiário" \
    -fill '#CBD5E1' -pointsize 14 -font "DejaVu-Sans" -gravity NorthWest -annotate +110+1595 "Apis Mellifera e Abelhas Sem Ferrão (Meliponíneos)" \
    \
    -fill '#064E3B' -stroke '#10B981' -strokewidth 2 -draw "roundrectangle 60,1690,1020,1860,16,16" \
    -fill '#10B981' -pointsize 28 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +90+1720 "💬 WHATSAPP: (51) 99677-6214" \
    -fill '#FFFFFF' -pointsize 18 -font "DejaVu-Sans" -gravity NorthWest -annotate +90+1768 "Fale com o desenvolvedor Christian Dahmer" \
    -fill '#FBBF24' -pointsize 15 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +90+1805 "Toque para enviar mensagem e solicitar seu acesso" \
    release/marketing_real/anuncio_stories_9x16_real.jpg

echo "7. Generating BANNER PANORÂMICO (1920x1080 - 16:9) with REAL Screenshots..."
convert -size 1920x1080 \
    gradient:'#0F172A-#030712' \
    -fill '#D97706' -draw "rectangle 0,0,1920,10" \
    -fill '#F59E0B' -pointsize 42 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+60 "ApisSmart: Gestão Inteligente de Apiários" \
    -fill '#FFFFFF' -pointsize 22 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +80+115 "A Tecnologia Definitiva para Produtores de Mel e Meliponicultores" \
    /tmp/banner_base.png

# Composite 3 mockups (Dashboard, Colheita, Financeiro)
convert /tmp/banner_base.png \
    /tmp/mockups/phone_dashboard.png -gravity NorthWest -geometry +60+170 -composite \
    /tmp/mockups/phone_colheita.png -gravity North -geometry +0+170 -composite \
    /tmp/mockups/phone_financeiro.png -gravity NorthEast -geometry +60+170 -composite \
    \
    -fill '#0F172A' -stroke '#10B981' -strokewidth 2 -draw "roundrectangle 60,940,1860,1040,14,14" \
    -fill '#10B981' -pointsize 24 -font "DejaVu-Sans-Bold" -gravity NorthWest -annotate +100+962 "📱 CONTATO / WHATSAPP: (51) 99677-6214 (Christian Dahmer)" \
    -fill '#FBBF24' -pointsize 18 -font "DejaVu-Sans-Bold" -gravity NorthEast -annotate +100+965 "🎁 14 DIAS GRÁTIS COM TODAS AS FUNÇÕES LIBERADAS" \
    -fill '#CBD5E1' -pointsize 14 -font "DejaVu-Sans" -gravity NorthWest -annotate +100+998 "100% Offline • Mapeamento Satélite • Inspeção por Voz com IA • Relatórios Zootécnicos" \
    release/marketing_real/banner_horizontal_16x9_real.jpg

echo "8. Copying all generated real marketing pieces to public folder and packaging zip..."
cp -r release/marketing_real/* public/marketing_real/
mkdir -p release/marketing public/marketing
cp release/marketing_real/* release/marketing/
cp release/marketing_real/* public/marketing/

zip -j release/material_promocional_prints_reais_apissmart.zip \
    release/marketing_real/*.jpg \
    screenshots/dark/*.png \
    docs/MATERIAL_PROMOCIONAL.md

cp release/material_promocional_prints_reais_apissmart.zip public/
cp release/material_promocional_prints_reais_apissmart.zip .

echo "=== REAL MARKETING COMPOSITION COMPLETED SUCCESSFULLY ==="
