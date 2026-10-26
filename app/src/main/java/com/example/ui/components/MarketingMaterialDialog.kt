package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.HoneyGoldPrimary
import com.example.ui.theme.MeadowGreenSecondary

@Composable
fun MarketingMaterialDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("📁 Localização", "🎨 Peças Gráficas", "📝 Textos & Copy", "📦 Pacotes ZIP")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HoneyGoldPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(HoneyGoldPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = HoneyGoldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Material de Marketing & Divulgação",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kit comercial completo, prints das telas, folders e copys",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 12.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (selectedTab == index) HoneyGoldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                HorizontalDivider()

                // Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    when (selectedTab) {
                        0 -> LocationGuideTab(context)
                        1 -> CreativePiecesTab(context)
                        2 -> CopyAndTextsTab(context)
                        3 -> ZipPackagesTab(context)
                    }
                }

                HorizontalDivider()

                // Bottom Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText(
                                "Pastas do Material de Marketing ApisSmart",
                                "Pastas no projeto:\n- /release/marketing/\n- /public/marketing/\n- /docs/MATERIAL_PROMOCIONAL.md\n- /docs/MATERIAL_COMPLETO_APISSMART.txt\n- material_promocional_prints_reais_apissmart.zip"
                            )
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Caminhos copiados para a área de transferência!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar Caminhos", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            try {
                                val url = "https://api.whatsapp.com/send?phone=5551996776214&text=Ol%C3%A1%20Christian,%20estou%20com%20o%20ApisSmart%20e%20gostaria%20de%20informa%C3%A7%C3%B5es%20sobre%20o%20material%20de%20marketing!"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "WhatsApp: (51) 99677-6214", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MeadowGreenSecondary)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Suporte / Vendas (51) 99677-6214", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LocationGuideTab(context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = HoneyGoldPrimary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Folder, contentDescription = null, tint = HoneyGoldPrimary)
                    Column {
                        Text(
                            text = "Onde encontrar no sistema de arquivos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Todo o material promocional gerado está salvo nas pastas raiz e públicas do projeto para download imediato.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        val locations = listOf(
            Triple(
                "📁 /release/marketing/",
                "Imagens em alta definição das peças promocionais prontas para postagem e impressão gráfica.",
                listOf(
                    "folder_promocional_frente_real.jpg (1200x1600 - 3:4)",
                    "folder_promocional_verso_real.jpg (1200x1600 - 3:4)",
                    "anuncio_feed_1x1_real.jpg (1080x1080 - 1:1)",
                    "anuncio_stories_9x16_real.jpg (1080x1920 - 9:16)",
                    "banner_horizontal_16x9_real.jpg (1920x1080 - 16:9)",
                    "anuncio_feed_whatsapp_51996776214.jpg",
                    "anuncio_stories_whatsapp_51996776214.jpg",
                    "folder_flyer_whatsapp_51996776214.jpg"
                )
            ),
            Triple(
                "📁 /public/marketing/",
                "Cópia espelho acessível para download direto através do servidor web e embutidos públicos.",
                listOf(
                    "Mesmos arquivos da pasta release/marketing/",
                    "material_promocional_prints_reais_apissmart.zip",
                    "screenshots_dark_e_marketing.zip"
                )
            ),
            Triple(
                "📄 /docs/",
                "Documentos estratégicos de copywriting, vendas, roteiros de vídeos e listagem da Play Store.",
                listOf(
                    "MATERIAL_PROMOCIONAL.md (Guia comercial e argumentos de venda tela a tela)",
                    "MATERIAL_COMPLETO_APISSMART.txt (Dossiê completo para vendedores e revenda)",
                    "GOOGLE_PLAY_STORE_LISTING.md (Textos prontos para publicação na Play Store)",
                    "ROTEIRO_VIDEOS_IA.md (Roteiros passo a passo para reels e shorts publicitários)"
                )
            ),
            Triple(
                "📸 /screenshots/ e /screenshots/dark/",
                "Capturas de tela reais do aplicativo em modo claro e escuro em alta resolução (1078x2399).",
                listOf(
                    "01_dashboard_screen.png / 01_dashboard_screen_dark.png",
                    "02_apiarios_e_colmeias.png / 02_apiarios_e_colmeias_dark.png",
                    "03_inspecoes_sanitarias.png / 03_inspecoes_sanitarias_dark.png",
                    "04_colheita_e_producao.png / 04_colheita_e_producao_dark.png",
                    "05_estoque_e_insumos.png / 05_estoque_e_insumos_dark.png",
                    "06_cronograma_manejos.png / 06_cronograma_manejos_dark.png",
                    "07_gestao_financeira.png / 07_gestao_financeira_dark.png",
                    "08_assistente_tecnico_ia.png / 08_assistente_tecnico_ia_dark.png",
                    "09_administracao_licenca.png / 09_administracao_licenca_dark.png"
                )
            )
        )

        items(locations) { (title, subtitle, files) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HoneyGoldPrimary
                        )
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Caminho", title.replace("📁 ", "").replace("📄 ", "").replace("📸 ", ""))
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Caminho copiado!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    files.forEach { file ->
                        Text(
                            text = "• $file",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreativePiecesTab(context: Context) {
    val pieces = listOf(
        CreativeItem(
            title = "Folder Frente — Visão Geral & Benefícios",
            format = "1200 x 1600 (Proporção 3:4)",
            target = "Impressão gráfica frente/verso e folhetos PDF",
            file = "release/marketing/folder_promocional_frente_real.jpg",
            description = "Mockup real do celular com a tela Dashboard em Dark Mode, destaques de funcionamento 100% offline, comando de voz e WhatsApp de atendimento."
        ),
        CreativeItem(
            title = "Folder Verso — Recursos Zootécnicos & Gráficos",
            format = "1200 x 1600 (Proporção 3:4)",
            target = "Impressão gráfica verso e material técnico",
            file = "release/marketing/folder_promocional_verso_real.jpg",
            description = "Mockups lado a lado da Colheita e Gestão Financeira, checklist de funcionalidades, mapas GPS de satélite e QR Code."
        ),
        CreativeItem(
            title = "Anúncio Feed Instagram & Facebook",
            format = "1080 x 1080 (Proporção 1:1)",
            target = "Feed de redes sociais, grupos e catálogos",
            file = "release/marketing/anuncio_feed_1x1_real.jpg",
            description = "Enquadramento quadrado balanceado com mockups de alta visibilidade e oferta de 30 dias grátis de teste."
        ),
        CreativeItem(
            title = "Anúncio Stories, WhatsApp Status & Reels",
            format = "1080 x 1920 (Proporção 9:16)",
            target = "Stories Instagram, Status do WhatsApp e Shorts",
            file = "release/marketing/anuncio_stories_9x16_real.jpg",
            description = "Vertical de tela inteira com botões em destaque e chamada direta para conversar no WhatsApp."
        ),
        CreativeItem(
            title = "Banner Horizontal Panorâmico",
            format = "1920 x 1080 (Proporção 16:9)",
            target = "Site, canal do YouTube, palestras e feiras",
            file = "release/marketing/banner_horizontal_16x9_real.jpg",
            description = "Composição ampla com 3 telas reais em molduras de titânio (Dashboard, Colheita e Financeiro)."
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pieces) { piece ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = piece.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${piece.format} • ${piece.target}",
                                fontSize = 11.sp,
                                color = HoneyGoldPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Arquivo", piece.file)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Caminho do arquivo copiado!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = piece.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Arquivo: ${piece.file}",
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun CopyAndTextsTab(context: Context) {
    val copies = listOf(
        CopyItem(
            title = "Mensagem Rápida de Apresentação (WhatsApp)",
            copy = """
🐝 *Olá! Conheça o ApisSmart: O Aplicativo Definitivo para o Apicultor e Meliponicultor Moderno!*

Cansado de cadernos de campo molhados e anotações perdidas no pasto?

✅ *100% OFFLINE:* Registre tudo no apiário mesmo sem internet!
🎙️ *MODO MÃOS LIVRES:* Dite as inspeções com as luvas sujas de própolis e a IA diagnostica a sanidade!
🗺️ *MAPA POR SATÉLITE & GPS:* Localize apiários, colmeias e caixas-isca com raio de voo.
🍯 *CONTROLE DE MELGUEIRAS:* Rastreie lote, florada e rendimento por colmeia.
📊 *GESTÃO FINANCEIRA:* Saiba o custo exato por kg de mel e seu lucro real.

🎁 *TESTE GRÁTIS POR 30 DIAS!*
Fale comigo agora pelo WhatsApp: *(51) 99677-6214* (Christian Dahmer).
            """.trimIndent()
        ),
        CopyItem(
            title = "Texto para Postagem no Instagram / Facebook",
            copy = """
🐝 Já pensou em inspecionar suas colmeias falando, sem tirar as luvas cheias de própolis?

O ApisSmart é a tecnologia completa desenvolvida para quem vive da apicultura e meliponicultura:

🌿 Mapeamento GPS em imagens de satélite de alta definição
📋 Diagnóstico sanitário com Inteligência Artificial para Varroa, traças e postura
⚖️ Controle minucioso de melgueiras colhidas e pesagem líquida
📦 Estoque inteligente de cera, caixas e alimentação
💰 Balanço financeiro de receitas, despesas e margem de lucro

Disponível para celulares Android com funcionamento 100% offline no campo!

👉 Chame no WhatsApp (51) 99677-6214 e ative seus 30 dias de teste gratuito sem burocracia.

#apicultura #meliponicultura #apicultor #abelhas #apismellifera #mel #gestaoapicola #agrotech #apissmart
            """.trimIndent()
        ),
        CopyItem(
            title = "Proposta Comercial para Associações e Cooperativas",
            copy = """
Prezada Diretoria e Colegas Apicultores,

Apresentamos o ApisSmart como solução integrada de zootecnia e rastreabilidade para os associados da entidade:

1. Padronização de laudos técnicos e inspeções fitossanitárias.
2. Controle rigoroso de qualidade do mel por apiário, umidade e florada.
3. Rastreabilidade com geração de QR Code para impressão de etiquetas nas caixas.
4. Exportação unificada de relatórios em PDF e planilhas CSV para órgãos fiscalizadores e auditorias de selos (Orgânico, ARTE, IG).

Condições especiais de licença coletiva e suporte técnico dedicado:
Contato: (51) 99677-6214 • Christian Dahmer • Desenvolvedor ApisSmart.
            """.trimIndent()
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(copies) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = HoneyGoldPrimary
                        )

                        Row {
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText(item.title, item.copy)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Texto copiado para área de transferência!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, item.copy)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar texto promocional")
                                    context.startActivity(shareIntent)
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartilhar", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = item.copy,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZipPackagesTab(context: Context) {
    val zips = listOf(
        ZipItem(
            name = "material_promocional_prints_reais_apissmart.zip",
            size = "2.6 MB",
            locations = "Raiz do projeto, /release/ e /public/",
            description = "Kit oficial gerado a partir de capturas reais em Dark Mode com molduras de smartphone, folders e guia comercial."
        ),
        ZipItem(
            name = "screenshots_dark_e_marketing.zip",
            size = "12 MB",
            locations = "Raiz do projeto, /release/ e /public/",
            description = "Pacote completo e unificado: todas as peças publicitárias em JPG, todos os prints em Dark Mode e documentação comercial."
        ),
        ZipItem(
            name = "screenshots_dark_apissmart.zip",
            size = "1.5 MB",
            locations = "Raiz do projeto, /release/ e /public/",
            description = "Todas as 9 telas do aplicativo em modo escuro nativo (Dashboard, Apiários, Inspeções, Colheita, etc.) sem molduras."
        ),
        ZipItem(
            name = "screenshots_apissmart.zip",
            size = "1.3 MB",
            locations = "Raiz do projeto, /release/ e /public/",
            description = "Todas as telas do aplicativo em modo claro nativo em alta definição (1078x2399)."
        )
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MeadowGreenSecondary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = MeadowGreenSecondary)
                    Column {
                        Text(
                            text = "Pacotes Prontos para Download",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Arquivos .ZIP compactados contendo imagens, ilustrações e textos organizados para envio rápido.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(zips) { zip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = zip.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = HoneyGoldPrimary
                            )
                            Text(
                                text = "Tamanho: ${zip.size} • ${zip.locations}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(28.dp),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Arquivo Zip", zip.name)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Nome do pacote copiado!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = zip.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private data class CreativeItem(
    val title: String,
    val format: String,
    val target: String,
    val file: String,
    val description: String
)

private data class CopyItem(
    val title: String,
    val copy: String
)

private data class ZipItem(
    val name: String,
    val size: String,
    val locations: String,
    val description: String
)
