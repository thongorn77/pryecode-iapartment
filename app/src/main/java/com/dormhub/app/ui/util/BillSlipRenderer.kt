package com.dormhub.app.ui.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillItem
import com.dormhub.app.data.entity.PropertySettings
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BillSlipRenderer {

    private const val W = 540
    private const val PADDING = 20f
    private const val COLOR_HEADER = 0xFF1A237E.toInt()
    private const val COLOR_SECTION = 0xFF1565C0.toInt()
    private const val COLOR_TOTAL_BG = 0xFFE8EAF6.toInt()
    private const val COLOR_DIVIDER = 0xFFBBBBBB.toInt()
    private const val COLOR_GRAY = 0xFF757575.toInt()

    fun renderAndShare(
        context: Context,
        bill: Bill,
        items: List<BillItem>,
        settings: PropertySettings,
    ): Uri {
        val bitmap = render(bill, items, settings)
        val file = saveToCache(context, bitmap, bill.billNumber)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun render(bill: Bill, items: List<BillItem>, settings: PropertySettings): Bitmap {
        val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("th"))

        // Estimate height
        val extraRows = items.size
        val hasQr = settings.promptPayId.isNotBlank()
        val estimatedH = 780 + extraRows * 28 + if (hasQr) 230 else 0

        val bmp = Bitmap.createBitmap(W, estimatedH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.WHITE)

        // ── Paints ──────────────────────────────────────────────
        fun paint(size: Float = 14f, color: Int = Color.BLACK, bold: Boolean = false,
                  align: Paint.Align = Paint.Align.LEFT) = Paint().apply {
            this.isAntiAlias = true; this.textSize = size; this.color = color
            this.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            this.textAlign = align
        }

        val pHeaderTitle  = paint(19f, Color.WHITE, bold = true, align = Paint.Align.CENTER)
        val pHeaderSub    = paint(11f, Color.WHITE, align = Paint.Align.CENTER)
        val pSectionLabel = paint(11f, COLOR_SECTION)
        val pNormal       = paint(13f)
        val pBold         = paint(13f, bold = true)
        val pRight        = paint(13f, align = Paint.Align.RIGHT)
        val pBoldRight    = paint(13f, bold = true, align = Paint.Align.RIGHT)
        val pSmall        = paint(10f, COLOR_GRAY)
        val pSmallCenter  = paint(10f, COLOR_GRAY, align = Paint.Align.CENTER)
        val pTotalAmount  = paint(18f, COLOR_SECTION, bold = true, align = Paint.Align.RIGHT)
        val pDivider      = Paint().apply { color = COLOR_DIVIDER; strokeWidth = 1f }

        fun textPaint(size: Float, color: Int = Color.BLACK, bold: Boolean = false) = TextPaint().apply {
            isAntiAlias = true; textSize = size; this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        var y = 0f

        // ── Header ─────────────────────────────────────────────
        canvas.drawRect(0f, 0f, W.toFloat(), 85f, Paint().apply { color = COLOR_HEADER })
        val propName = settings.name.ifBlank { "หอพัก" }
        canvas.drawText(propName, W / 2f, 28f, pHeaderTitle)
        if (settings.address.isNotBlank()) {
            val addr = settings.address.replace("\n", " ").take(70)
            canvas.drawText(addr, W / 2f, 48f, pHeaderSub)
        }
        val contactLine = buildString {
            if (settings.phone.isNotBlank()) append("โทร: ${settings.phone}")
            if (settings.bankName.isNotBlank() && settings.bankAccountNumber.isNotBlank()) {
                if (isNotEmpty()) append("  |  ")
                append("${settings.bankName} ${settings.bankAccountNumber}")
            }
        }
        if (contactLine.isNotBlank()) {
            canvas.drawText(contactLine.take(72), W / 2f, 65f, pHeaderSub)
        }
        y = 93f

        // ── Bill Title ─────────────────────────────────────────
        val monthNames = arrayOf("ม.ค.","ก.พ.","มี.ค.","เม.ย.","พ.ค.","มิ.ย.",
                                  "ก.ค.","ส.ค.","ก.ย.","ต.ค.","พ.ย.","ธ.ค.")
        val yr = bill.yearMonth / 100; val mo = bill.yearMonth % 100
        val monthLabel = if (mo in 1..12) monthNames[mo - 1] else "$mo"
        val titlePaint = paint(16f, bold = true, align = Paint.Align.CENTER)
        canvas.drawText("ใบแจ้งค่าเช่า ประจำเดือน $monthLabel $yr", W / 2f, y + 18f, titlePaint)
        y += 28f

        canvas.drawText("เลขที่: ${bill.billNumber}", PADDING, y + 15f, pNormal)
        canvas.drawText("ครบกำหนด: ${sdf.format(Date(bill.dueDate))}", W - PADDING, y + 15f, pRight)
        y += 25f

        canvas.drawLine(PADDING, y, W - PADDING, y, pDivider); y += 10f

        // ── Tenant info ────────────────────────────────────────
        canvas.drawText("ห้อง ${bill.roomNumber}  |  ผู้เช่า: ${bill.tenantName}", PADDING, y + 15f, pBold)
        y += 22f
        if (bill.tenantPhone.isNotBlank()) {
            canvas.drawText("โทร: ${bill.tenantPhone}", PADDING, y + 13f, pSmall)
            y += 18f
        }
        y += 4f; canvas.drawLine(PADDING, y, W - PADDING, y, pDivider); y += 10f

        // ── Line items helper ──────────────────────────────────
        fun row(label: String, amountSatang: Long, leftBold: Boolean = false) {
            val lp = if (leftBold) pBold else pNormal
            canvas.drawText(label, PADDING, y + 15f, lp)
            canvas.drawText("฿${thb.format(amountSatang / 100)}", W - PADDING, y + 15f,
                            if (leftBold) pBoldRight else pRight)
            y += 24f
        }

        fun sectionTitle(title: String) {
            canvas.drawText(title, PADDING, y + 12f, pSectionLabel); y += 18f
        }

        // Rent
        sectionTitle("ค่าเช่า")
        row("ค่าเช่าห้อง", bill.rent)

        // Utilities
        y += 2f; sectionTitle("ค่าสาธารณูปโภค")
        val elecLabel = "ค่าไฟ (${bill.electricityPrevious}→${bill.electricityCurrent}, ${bill.electricityUnits} หน่วย)"
        row(elecLabel, bill.electricityAmount)
        val waterLabel = "ค่าน้ำ (${bill.waterPrevious}→${bill.waterCurrent}, ${bill.waterUnits} หน่วย)"
        row(waterLabel, bill.waterAmount)

        // Fixed fees
        if (bill.fixedFeesTotal > 0) {
            y += 2f; sectionTitle("ค่าบริการ")
            row("ค่าบริการรายเดือน", bill.fixedFeesTotal)
        }

        // Extra items
        if (items.isNotEmpty()) {
            y += 2f; sectionTitle("รายการเพิ่มเติม")
            for (item in items) {
                val prefix = if (item.amount < 0) "(ส่วนลด) " else ""
                row("$prefix${item.name}", item.amount)
            }
        }

        // Late fee / previous balance
        if (bill.previousBalance > 0) { y += 2f; row("ยอดค้างเดือนก่อน", bill.previousBalance, true) }
        if (bill.lateFee > 0) { row("ค่าปรับ", bill.lateFee) }

        // ── Total ──────────────────────────────────────────────
        y += 4f; canvas.drawLine(PADDING, y, W - PADDING, y, pDivider); y += 4f
        canvas.drawRect(0f, y, W.toFloat(), y + 46f, Paint().apply { color = COLOR_TOTAL_BG })
        canvas.drawText("ยอดรวมทั้งหมด:", PADDING, y + 29f, pBold)
        canvas.drawText("฿${thb.format(bill.total / 100)} บาท", W - PADDING, y + 29f, pTotalAmount)
        y += 50f

        if (bill.paidAmount > 0) {
            canvas.drawText("ชำระแล้ว", PADDING, y + 15f, pNormal)
            canvas.drawText("฿${thb.format(bill.paidAmount / 100)}", W - PADDING, y + 15f, pRight)
            y += 22f
            val remColor = if (bill.remainingAmount > 0) Color.RED else Color.parseColor("#2E7D32")
            val remPaint = paint(13f, remColor, bold = true); val remRight = paint(13f, remColor, bold = true, align = Paint.Align.RIGHT)
            canvas.drawText("ยอดคงเหลือ", PADDING, y + 15f, remPaint)
            canvas.drawText("฿${thb.format(bill.remainingAmount / 100)}", W - PADDING, y + 15f, remRight)
            y += 22f
        }

        // ── PromptPay QR ───────────────────────────────────────
        if (settings.promptPayId.isNotBlank()) {
            y += 8f; canvas.drawLine(PADDING, y, W - PADDING, y, pDivider); y += 12f
            val pCenter = paint(13f, COLOR_SECTION, bold = true, align = Paint.Align.CENTER)
            canvas.drawText("ชำระเงินด้วย PromptPay", W / 2f, y + 14f, pCenter); y += 22f

            val qrSize = 180
            val amount = if (bill.remainingAmount > 0) bill.remainingAmount else bill.total
            val qrPayload = generatePromptPayPayload(settings.promptPayId, amount)
            val qrBmp = generateQrBitmap(qrPayload, qrSize)
            if (qrBmp != null) {
                val left = (W - qrSize) / 2f
                canvas.drawBitmap(qrBmp, left, y, null)
                y += (qrSize + 6).toFloat()
            }
            canvas.drawText("เลขพร้อมเพย์: ${settings.promptPayId}", W / 2f, y + 14f, pSmallCenter)
            y += 22f
        }

        // ── Footer ─────────────────────────────────────────────
        y += 10f
        if (settings.billFooterNote.isNotBlank()) {
            canvas.drawText(settings.billFooterNote.take(80), W / 2f, y + 12f, pSmallCenter)
            y += 18f
        }
        val watermark = paint(9f, Color.LTGRAY, align = Paint.Align.CENTER)
        canvas.drawText("สร้างโดย iApartment by Pryecode", W / 2f, y + 10f, watermark)

        // Crop to actual content height
        val finalH = (y + 20f).toInt().coerceAtMost(estimatedH)
        return if (finalH < estimatedH) Bitmap.createBitmap(bmp, 0, 0, W, finalH) else bmp
    }

    private fun saveToCache(context: Context, bitmap: Bitmap, billNumber: String): File {
        val dir = File(context.cacheDir, "images").also { it.mkdirs() }
        val file = File(dir, "bill_${billNumber.replace("/", "-").replace(":", "-")}.png")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 90, out) }
        return file
    }

    private fun generateQrBitmap(content: String, size: Int): Bitmap? = try {
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also { bmp ->
            for (x in 0 until size) for (y in 0 until size) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    } catch (e: Exception) { null }

    // ── PromptPay QR payload (EMVCo / BOT standard) ───────────
    private fun generatePromptPayPayload(promptPayId: String, amountSatang: Long): String {
        val id = promptPayId.trim().replace("-", "").replace(" ", "")
        val account = when {
            id.length == 10 && id.startsWith("0") -> "0066${id.drop(1)}" // mobile
            id.length == 13 && id.all { it.isDigit() } -> id               // national ID
            id.startsWith("+66") -> "0066${id.drop(3)}"
            else -> id
        }
        val sub00 = "0016A000000677010111"
        val sub01 = "01${"%02d".format(account.length)}$account"
        val merchantInfo = sub00 + sub01
        val tag29 = "29${"%02d".format(merchantInfo.length)}$merchantInfo"
        val amountStr = "%.2f".format(amountSatang / 100.0)
        val tag54 = "54${"%02d".format(amountStr.length)}$amountStr"
        val body = "000201010212${tag29}5802TH5303764${tag54}6304"
        return "$body${"%04X".format(crc16(body))}"
    }

    private fun crc16(data: String): Int {
        var crc = 0xFFFF
        for (c in data) {
            crc = crc xor (c.code shl 8)
            repeat(8) { crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1 }
        }
        return crc and 0xFFFF
    }
}
