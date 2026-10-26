package com.example.receipt.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.receipt.model.ReceiptData
import com.example.receipt.model.ReceiptLayoutConfig
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPdfGenerator {

    /**
     * Generates a complete A4 PDF document for the given receipt and layout config.
     */
    fun generatePdfFile(context: Context, receipt: ReceiptData, config: ReceiptLayoutConfig): File {
        val document = PdfDocument()

        // Standard A4 page size in points (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val primaryColorInt = config.colorTheme.primaryColor.toInt()
        val darkAccentInt = config.colorTheme.darkAccentColor.toInt()

        // Paints
        val paintText = Paint().apply { isAntiAlias = true }
        val paintFill = Paint().apply { isAntiAlias = true }
        val paintStroke = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
        }

        var currentY = 30f
        val startX = 30f
        val contentWidth = pageWidth - 60f

        // 1. Header Banner
        paintFill.color = primaryColorInt
        canvas.drawRoundRect(RectF(startX, currentY, startX + contentWidth, currentY + 70f), 8f, 8f, paintFill)

        // Business Name & Title inside Header
        paintText.color = Color.WHITE
        paintText.textSize = 18f
        paintText.isFakeBoldText = true
        canvas.drawText(config.businessName.uppercase(Locale.getDefault()), startX + 16f, currentY + 28f, paintText)

        paintText.textSize = 11f
        paintText.isFakeBoldText = false
        canvas.drawText(config.receiptTitle, startX + 16f, currentY + 46f, paintText)

        // Receipt ID Pill in Header Right
        paintFill.color = Color.WHITE
        canvas.drawRoundRect(RectF(startX + contentWidth - 140f, currentY + 16f, startX + contentWidth - 16f, currentY + 54f), 6f, 6f, paintFill)

        paintText.color = primaryColorInt
        paintText.textSize = 12f
        paintText.isFakeBoldText = true
        canvas.drawText(receipt.id, startX + contentWidth - 128f, currentY + 38f, paintText)

        currentY += 80f

        // 2. Issuer Info & Metadata Row
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date(receipt.dateMillis))

        paintText.color = Color.DKGRAY
        paintText.textSize = 10f
        paintText.isFakeBoldText = false
        canvas.drawText("Emissor: ${config.ownerName} • CNPJ/CPF: ${config.document}", startX, currentY + 10f, paintText)
        canvas.drawText("${config.address} • ${config.cityState} • Tel: ${config.phone}", startX, currentY + 24f, paintText)

        paintText.color = Color.BLACK
        paintText.isFakeBoldText = true
        canvas.drawText("Data: $dateStr  |  Forma: ${receipt.paymentMethod}", startX + contentWidth - 210f, currentY + 10f, paintText)

        currentY += 38f

        // 3. Amount Box (VALOR PAGO DESCRITO POR EXTENSO)
        paintFill.color = Color.parseColor("#F8FAFC")
        paintStroke.color = primaryColorInt
        paintStroke.strokeWidth = 1.5f

        val amountBox = RectF(startX, currentY, startX + contentWidth, currentY + 65f)
        canvas.drawRoundRect(amountBox, 8f, 8f, paintFill)
        canvas.drawRoundRect(amountBox, 8f, 8f, paintStroke)

        val amountStr = "R$ %,.2f".format(Locale.getDefault(), receipt.totalAmount)
        val amountInWords = CurrencyToWordsPtBr.convert(receipt.totalAmount)

        paintText.color = darkAccentInt
        paintText.textSize = 10f
        paintText.isFakeBoldText = true
        canvas.drawText("VALOR TOTAL RECEBIDO E QUITADO:", startX + 14f, currentY + 18f, paintText)

        paintText.color = primaryColorInt
        paintText.textSize = 20f
        paintText.isFakeBoldText = true
        canvas.drawText(amountStr, startX + contentWidth - 160f, currentY + 26f, paintText)

        paintText.color = Color.parseColor("#334155")
        paintText.textSize = 10f
        paintText.isFakeBoldText = false
        val wordsTrimmed = if (amountInWords.length > 70) amountInWords.take(67) + "..." else amountInWords
        canvas.drawText("($wordsTrimmed)", startX + 14f, currentY + 48f, paintText)

        currentY += 75f

        // 4. Customer Data Box
        paintFill.color = Color.parseColor("#FAFAFA")
        paintStroke.color = Color.parseColor("#CBD5E1")
        paintStroke.strokeWidth = 1f

        val custBox = RectF(startX, currentY, startX + contentWidth, currentY + 60f)
        canvas.drawRoundRect(custBox, 6f, 6f, paintFill)
        canvas.drawRoundRect(custBox, 6f, 6f, paintStroke)

        paintText.color = primaryColorInt
        paintText.textSize = 10f
        paintText.isFakeBoldText = true
        canvas.drawText("DADOS DO CLIENTE / PAGADOR", startX + 12f, currentY + 16f, paintText)

        paintText.color = Color.BLACK
        paintText.textSize = 11f
        paintText.isFakeBoldText = true
        canvas.drawText("Nome: ${receipt.clientName.ifBlank { "Consumidor Final" }}", startX + 12f, currentY + 34f, paintText)

        paintText.textSize = 10f
        paintText.isFakeBoldText = false
        val docStr = if (receipt.clientDocument.isNotBlank()) "CPF/CNPJ: ${receipt.clientDocument}" else ""
        val phoneStr = if (receipt.clientPhone.isNotBlank()) "Tel: ${receipt.clientPhone}" else ""
        val addrStr = if (receipt.clientAddress.isNotBlank()) "Endereço: ${receipt.clientAddress}" else ""
        canvas.drawText(listOf(docStr, phoneStr, addrStr).filter { it.isNotBlank() }.joinToString(" • "), startX + 12f, currentY + 48f, paintText)

        currentY += 70f

        // 5. Products / Services Table
        if (receipt.items.isNotEmpty()) {
            paintText.color = primaryColorInt
            paintText.textSize = 10f
            paintText.isFakeBoldText = true
            canvas.drawText("PRODUTOS / SERVIÇOS APÍCOLAS DISCRIMINADOS", startX, currentY + 10f, paintText)
            currentY += 16f

            // Table Header
            paintFill.color = Color.parseColor("#E2E8F0")
            canvas.drawRect(startX, currentY, startX + contentWidth, currentY + 20f, paintFill)

            paintText.color = Color.parseColor("#0F172A")
            paintText.textSize = 9f
            paintText.isFakeBoldText = true
            canvas.drawText("Item / Descrição do Produto", startX + 8f, currentY + 14f, paintText)
            canvas.drawText("Qtd", startX + 310f, currentY + 14f, paintText)
            canvas.drawText("Unitario", startX + 390f, currentY + 14f, paintText)
            canvas.drawText("Total", startX + 470f, currentY + 14f, paintText)

            currentY += 20f

            paintText.isFakeBoldText = false
            for (idx in receipt.items.indices) {
                val item = receipt.items[idx]
                val rowHeight = 22f

                if (idx % 2 == 1) {
                    paintFill.color = Color.parseColor("#F8FAFC")
                    canvas.drawRect(startX, currentY, startX + contentWidth, currentY + rowHeight, paintFill)
                }

                paintText.color = Color.BLACK
                val desc = if (item.description.length > 45) item.description.take(42) + "..." else item.description
                canvas.drawText(desc, startX + 8f, currentY + 15f, paintText)
                canvas.drawText("${item.quantity} ${item.unit}", startX + 310f, currentY + 15f, paintText)
                canvas.drawText("R$ %,.2f".format(Locale.getDefault(), item.unitPrice), startX + 390f, currentY + 15f, paintText)

                paintText.isFakeBoldText = true
                paintText.color = primaryColorInt
                canvas.drawText("R$ %,.2f".format(Locale.getDefault(), item.totalPrice), startX + 470f, currentY + 15f, paintText)
                paintText.isFakeBoldText = false

                currentY += rowHeight
            }

            // Bottom line for table
            paintStroke.color = Color.parseColor("#CBD5E1")
            canvas.drawLine(startX, currentY, startX + contentWidth, currentY, paintStroke)
            currentY += 12f
        }

        // 6. PIX Info (if enabled)
        if (config.showPixKey && config.pixKey.isNotBlank()) {
            paintFill.color = Color.parseColor("#F0FDF4")
            paintStroke.color = Color.parseColor("#BBF7D0")

            val pixBox = RectF(startX, currentY, startX + contentWidth, currentY + 32f)
            canvas.drawRoundRect(pixBox, 6f, 6f, paintFill)
            canvas.drawRoundRect(pixBox, 6f, 6f, paintStroke)

            paintText.color = Color.parseColor("#15803D")
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = true
            canvas.drawText("🔑 Chave PIX (${config.pixKeyType}): ${config.pixKey} | Titular: ${config.pixReceiverName} (${config.pixBank})", startX + 10f, currentY + 20f, paintText)

            currentY += 40f
        }

        // 7. Legal Term Text
        paintText.color = Color.parseColor("#475569")
        paintText.textSize = 8.5f
        paintText.isFakeBoldText = false
        val legalLines = wrapText(config.legalTerm, 90)
        for (line in legalLines) {
            canvas.drawText(line, startX, currentY, paintText)
            currentY += 11f
        }

        currentY += 15f

        // 8. Signatures Row with Vector Signature Drawings!
        val sigWidth = 200f
        val sigHeight = 45f

        // Issuer Signature
        if (config.showIssuerSignature) {
            val issuerSigSvg = receipt.issuerSignatureSvg ?: config.savedIssuerSignatureSvg
            val issuerX = startX + 20f

            if (!issuerSigSvg.isNullOrBlank()) {
                val sigBounds = RectF(issuerX, currentY, issuerX + sigWidth, currentY + sigHeight)
                ReceiptSignatureHelper.drawSignatureOnAndroidCanvas(canvas, issuerSigSvg, sigBounds, strokeColor = primaryColorInt)
            }

            val lineY = currentY + sigHeight + 4f
            paintStroke.color = Color.parseColor("#64748B")
            paintStroke.strokeWidth = 1f
            canvas.drawLine(issuerX, lineY, issuerX + sigWidth, lineY, paintStroke)

            paintText.color = Color.BLACK
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = true
            canvas.drawText(config.ownerName, issuerX + 20f, lineY + 14f, paintText)

            paintText.color = Color.GRAY
            paintText.textSize = 8.5f
            paintText.isFakeBoldText = false
            canvas.drawText("Emissor / Apicultor Responsavel", issuerX + 12f, lineY + 26f, paintText)
        }

        // Customer Signature
        if (config.showCustomerSignature) {
            val customerSigSvg = receipt.customerSignatureSvg
            val customerX = startX + contentWidth - sigWidth - 20f

            if (!customerSigSvg.isNullOrBlank()) {
                val sigBounds = RectF(customerX, currentY, customerX + sigWidth, currentY + sigHeight)
                ReceiptSignatureHelper.drawSignatureOnAndroidCanvas(canvas, customerSigSvg, sigBounds, strokeColor = 0xFF0F172A.toInt())
            }

            val lineY = currentY + sigHeight + 4f
            paintStroke.color = Color.parseColor("#64748B")
            paintStroke.strokeWidth = 1f
            canvas.drawLine(customerX, lineY, customerX + sigWidth, lineY, paintStroke)

            paintText.color = Color.BLACK
            paintText.textSize = 9.5f
            paintText.isFakeBoldText = true
            canvas.drawText(receipt.clientName.ifBlank { "Cliente / Pagador" }, customerX + 20f, lineY + 14f, paintText)

            paintText.color = Color.GRAY
            paintText.textSize = 8.5f
            paintText.isFakeBoldText = false
            canvas.drawText("Assinatura do Recebedor", customerX + 30f, lineY + 26f, paintText)
        }

        currentY += sigHeight + 45f

        // 9. Footer
        paintStroke.color = Color.parseColor("#E2E8F0")
        canvas.drawLine(startX, currentY, startX + contentWidth, currentY, paintStroke)

        currentY += 14f
        paintText.color = primaryColorInt
        paintText.textSize = 9f
        paintText.isFakeBoldText = true
        canvas.drawText("⭐ ${config.qualitySealText}", startX + 110f, currentY, paintText)

        currentY += 12f
        paintText.color = Color.GRAY
        paintText.textSize = 8f
        paintText.isFakeBoldText = false
        canvas.drawText(config.footerGreeting, startX + 60f, currentY, paintText)

        document.finishPage(page)

        // Write PDF file to app internal/cache receipts folder
        val receiptsDir = File(context.cacheDir, "receipts")
        if (!receiptsDir.exists()) receiptsDir.mkdirs()

        val pdfFile = File(receiptsDir, "Recibo_${receipt.id.replace("[^A-Za-z0-9_-]".toRegex(), "_")}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.close()
        document.close()

        return pdfFile
    }

    /**
     * Saves a copy of the receipt PDF to the user's Downloads directory.
     */
    fun savePdfToDownloads(context: Context, receipt: ReceiptData, config: ReceiptLayoutConfig): File? {
        return try {
            val pdfFile = generatePdfFile(context, receipt, config)

            val fileName = "Recibo_${receipt.id.replace("[^A-Za-z0-9_-]".toRegex(), "_")}.pdf"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val publicFile = File(downloadsDir, fileName)

            pdfFile.copyTo(publicFile, overwrite = true)
            Toast.makeText(
                context,
                "📄 PDF Salvo com sucesso em Downloads:\n$fileName",
                Toast.LENGTH_LONG
            ).show()
            publicFile
        } catch (e: Exception) {
            Toast.makeText(context, "⚠️ Não foi possível salvar em Downloads. PDF gerado em cache da aplicação.", Toast.LENGTH_SHORT).show()
            generatePdfFile(context, receipt, config)
        }
    }

    /**
     * Gets a FileProvider content URI for sharing the PDF file.
     */
    fun getShareablePdfUri(context: Context, receipt: ReceiptData, config: ReceiptLayoutConfig): Uri {
        val file = generatePdfFile(context, receipt, config)
        val authority = "${context.packageName}.provider"
        return FileProvider.getUriForFile(context, authority, file)
    }

    /**
     * Triggers PDF share intent via WhatsApp or standard system chooser.
     */
    fun sharePdf(context: Context, receipt: ReceiptData, config: ReceiptLayoutConfig, targetWhatsApp: Boolean = false) {
        try {
            val pdfUri = getShareablePdfUri(context, receipt, config)
            val textSummary = "Sua via do ${config.receiptTitle} - Nº ${receipt.id}\nEmitido por: ${config.businessName}"

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "${config.receiptTitle} - ${receipt.id}")
                putExtra(Intent.EXTRA_TEXT, textSummary)
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            if (targetWhatsApp) {
                // Check if WhatsApp or WhatsApp Business is installed
                val pm = context.packageManager
                val isWhatsAppInstalled = isPackageInstalled("com.whatsapp", pm) || isPackageInstalled("com.whatsapp.w4b", pm)

                if (isWhatsAppInstalled) {
                    val pkg = if (isPackageInstalled("com.whatsapp", pm)) "com.whatsapp" else "com.whatsapp.w4b"
                    intent.setPackage(pkg)
                    context.startActivity(intent)
                } else {
                    Toast.makeText(context, "WhatsApp não encontrado. Abrindo lista de compartilhamento...", Toast.LENGTH_SHORT).show()
                    val chooser = Intent.createChooser(intent, "Compartilhar Recibo em PDF via:")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            } else {
                val chooser = Intent.createChooser(intent, "Compartilhar Recibo em PDF:")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao preparar compartilhamento do PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: android.content.pm.PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun wrapText(text: String, maxCharsPerLine: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            if (currentLine.length + word.length + 1 > maxCharsPerLine) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
