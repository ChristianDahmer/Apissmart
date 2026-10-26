package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entities.FinancialTransactionEntity
import com.example.receipt.ReceiptManager
import com.example.receipt.model.ReceiptColorTheme
import com.example.receipt.model.ReceiptItem
import com.example.receipt.model.ReceiptLayoutConfig
import com.example.receipt.model.ReceiptTemplateStyle
import com.example.receipt.model.ReceiptData
import com.example.receipt.util.CurrencyToWordsPtBr
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReceiptModuleTest {

    private lateinit var context: Context
    private lateinit var receiptManager: ReceiptManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        receiptManager = ReceiptManager(context)
    }

    @Test
    fun testCurrencyToWordsPtBr() {
        assertEquals("Zero reais", CurrencyToWordsPtBr.convert(0.0))
        assertEquals("Um real", CurrencyToWordsPtBr.convert(1.0))
        assertEquals("Cinquenta reais", CurrencyToWordsPtBr.convert(50.0))
        assertEquals("Cento e cinquenta reais", CurrencyToWordsPtBr.convert(150.0))
        val wordsWithCents = CurrencyToWordsPtBr.convert(145.50)
        assertTrue(wordsWithCents.contains("Cento e quarenta e cinco reais"))
        assertTrue(wordsWithCents.contains("cinquenta centavos"))
    }

    @Test
    fun testReceiptLayoutConfigSaveAndLoad() {
        val customConfig = ReceiptLayoutConfig(
            templateStyle = ReceiptTemplateStyle.ARTISANAL_CERTIFICATE,
            colorTheme = ReceiptColorTheme.FOREST_GREEN,
            businessName = "Apiário Teste Mantiqueira",
            ownerName = "Carlos Apicultor",
            document = "12.345.678/0001-99",
            phone = "(11) 99999-8888",
            cityState = "Campos do Jordão - SP",
            showPixKey = true,
            pixKey = "carlos@apiario.com"
        )

        receiptManager.saveLayoutConfig(customConfig)
        val loaded = receiptManager.loadLayoutConfig()

        assertEquals(ReceiptTemplateStyle.ARTISANAL_CERTIFICATE, loaded.templateStyle)
        assertEquals(ReceiptColorTheme.FOREST_GREEN, loaded.colorTheme)
        assertEquals("Apiário Teste Mantiqueira", loaded.businessName)
        assertEquals("carlos@apiario.com", loaded.pixKey)
    }

    @Test
    fun testCreateReceiptFromTransaction() {
        val tx = FinancialTransactionEntity(
            id = 101L,
            type = "RECEITA",
            category = "Venda de Mel Silvestre",
            amount = 225.0,
            dateMillis = System.currentTimeMillis(),
            description = "Entrega para Restaurante Sabor da Serra",
            paymentStatus = "PAGO",
            paymentMethod = "PIX",
            quantityKgOrUnits = 5.0
        )

        val receipt = receiptManager.createReceiptFromTransaction(tx, "Restaurante Sabor da Serra")

        assertEquals(101L, receipt.transactionId)
        assertEquals("Restaurante Sabor da Serra", receipt.clientName)
        assertEquals(225.0, receipt.totalAmount, 0.01)
        assertEquals("PIX", receipt.paymentMethod)
        assertEquals(1, receipt.items.size)
        assertEquals(5.0, receipt.items[0].quantity, 0.01)
        assertEquals(45.0, receipt.items[0].unitPrice, 0.01)
    }

    @Test
    fun testFormattedReceiptTextAndHtml() {
        val config = receiptManager.loadLayoutConfig()
        val receipt = ReceiptData(
            id = "REC-2026-TEST",
            receiptNumber = 99,
            dateMillis = System.currentTimeMillis(),
            clientName = "Cliente Teste",
            totalAmount = 100.0,
            paymentMethod = "PIX",
            items = listOf(ReceiptItem(description = "Mel 1kg", quantity = 2.0, unit = "kg", unitPrice = 50.0, totalPrice = 100.0))
        )

        val text = receiptManager.generateFormattedReceiptText(receipt, config)
        assertTrue(text.contains("REC-2026-TEST"))
        assertTrue(text.contains("R$ 100,00") || text.contains("100.00") || text.contains("100,00"))
        assertTrue(text.contains("Cem reais"))

        val html = receiptManager.generateHtmlReceipt(receipt, config)
        assertTrue(html.contains("<!DOCTYPE html>"))
        assertTrue(html.contains("REC-2026-TEST"))
        assertTrue(html.contains("Cliente Teste"))
    }
}
