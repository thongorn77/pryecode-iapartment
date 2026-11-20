/*
 * BillDetailScreen.kt — รายละเอียดบิล พร้อม LINE share
 * ============================================================
 * แสดงรายการค่าใช้จ่ายครบถ้วน
 * share LINE ด้วย Intent.ACTION_SEND
 */
package com.dormhub.app.ui.bills

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.Payment
import com.dormhub.app.data.entity.PaymentMethod
import com.dormhub.app.ui.navigation.Screen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillDetailScreen(
    yearMonth: Int,
    billId: String,
    navController: NavController,
    viewModel: BillDetailViewModel = hiltViewModel(),
) {
    val state   by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("th")) }
    var showVoidDialog by remember { mutableStateOf(false) }
    var voidReason     by remember { mutableStateOf("") }

    if (showVoidDialog) {
        AlertDialog(
            onDismissRequest = { showVoidDialog = false },
            title = { Text("ยกเลิกบิล") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("บิลที่ยกเลิกแล้วไม่สามารถแก้ไขได้")
                    OutlinedTextField(
                        value = voidReason,
                        onValueChange = { voidReason = it },
                        label = { Text("เหตุผล") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.voidBill(voidReason)
                        showVoidDialog = false
                    },
                    enabled = voidReason.isNotBlank()
                ) { Text("ยืนยัน", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showVoidDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.bill?.billNumber ?: "บิล") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
                    }
                },
                actions = {
                    state.bill?.let { bill ->
                        if (bill.status != BillStatus.VOID) {
                            IconButton(onClick = {
                                val text = buildBillShareText(bill, thb, sdf)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(intent, "ส่งบิลผ่าน LINE"))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "แชร์ LINE")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading || state.bill == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val bill = state.bill!!

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                BillHeaderCard(bill = bill, thb = thb, sdf = sdf)
            }

            // Meter details
            item {
                BillMeterCard(bill = bill, thb = thb)
            }

            // Totals
            item {
                BillTotalCard(bill = bill, thb = thb)
            }

            // Actions
            item {
                BillActionRow(
                    bill = bill,
                    yearMonth = yearMonth,
                    navController = navController,
                    onIssue = { viewModel.issueBill() },
                    onVoid = { showVoidDialog = true }
                )
            }

            // Payment history
            if (state.payments.isNotEmpty()) {
                item {
                    Text("ประวัติการชำระ",
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold)
                }
                items(state.payments) { payment ->
                    PaymentHistoryItem(payment = payment, thb = thb, sdf = sdf)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun BillHeaderCard(bill: Bill, thb: NumberFormat, sdf: SimpleDateFormat) {
    val (statusLabel, statusColor) = when (bill.status) {
        BillStatus.DRAFT   -> "ร่าง"      to Color(0xFF9E9E9E)
        BillStatus.ISSUED  -> "รอชำระ"   to Color(0xFF1565C0)
        BillStatus.PARTIAL -> "บางส่วน"  to Color(0xFFF57F17)
        BillStatus.PAID    -> "ชำระแล้ว" to Color(0xFF2E7D32)
        BillStatus.VOID    -> "ยกเลิก"   to Color(0xFFB71C1C)
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(bill.billNumber,
                     style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold,
                     modifier = Modifier.weight(1f))
                SuggestionChip(
                    onClick = {},
                    label = { Text(statusLabel) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }
            HorizontalDivider()
            BillInfoRow("ห้อง", bill.roomNumber)
            BillInfoRow("ผู้เช่า", bill.tenantName)
            if (bill.tenantPhone.isNotBlank()) BillInfoRow("โทร", bill.tenantPhone)
            BillInfoRow("ครบกำหนด", sdf.format(Date(bill.dueDate)))
        }
    }
}

@Composable
private fun BillMeterCard(bill: Bill, thb: NumberFormat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("รายละเอียดค่าใช้จ่าย",
                 style = MaterialTheme.typography.titleSmall,
                 fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            BillAmountRow("ค่าเช่า", bill.rent)
            BillAmountRow(
                "ค่าไฟ (${bill.electricityPrevious}→${bill.electricityCurrent}, ${bill.electricityUnits} หน่วย)",
                bill.electricityAmount
            )
            BillAmountRow(
                "ค่าน้ำ (${bill.waterPrevious}→${bill.waterCurrent}, ${bill.waterUnits} หน่วย)",
                bill.waterAmount
            )
            if (bill.fixedFeesTotal > 0) BillAmountRow("ค่าบริการรายเดือน", bill.fixedFeesTotal)
            if (bill.extrasTotal != 0L) BillAmountRow("รายการเพิ่ม/ลด", bill.extrasTotal)
            if (bill.previousBalance > 0) BillAmountRow("ยอดค้างเดือนก่อน", bill.previousBalance)
            if (bill.lateFee > 0) BillAmountRow("ค่าปรับ", bill.lateFee)
        }
    }
}

@Composable
private fun BillTotalCard(bill: Bill, thb: NumberFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("รวมทั้งหมด",
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold,
                     modifier = Modifier.weight(1f))
                Text("฿${thb.format(bill.total / 100)}",
                     style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold)
            }
            if (bill.paidAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("ชำระแล้ว", modifier = Modifier.weight(1f),
                         color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("฿${thb.format(bill.paidAmount / 100)}",
                         color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text("คงเหลือ", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("฿${thb.format(bill.remainingAmount / 100)}",
                         fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BillActionRow(
    bill: Bill,
    yearMonth: Int,
    navController: NavController,
    onIssue: () -> Unit,
    onVoid: () -> Unit,
) {
    when (bill.status) {
        BillStatus.DRAFT -> {
            Button(onClick = onIssue, modifier = Modifier.fillMaxWidth()) {
                Text("ออกบิล")
            }
        }
        BillStatus.ISSUED, BillStatus.PARTIAL -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { navController.navigate(Screen.Payment.withIds(yearMonth, bill.id)) },
                    modifier = Modifier.weight(1f)
                ) { Text("บันทึกชำระเงิน") }
                OutlinedButton(
                    onClick = onVoid,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("ยกเลิกบิล") }
            }
        }
        BillStatus.PAID  -> {
            OutlinedButton(onClick = onVoid, modifier = Modifier.fillMaxWidth(),
                           colors = ButtonDefaults.outlinedButtonColors(
                               contentColor = MaterialTheme.colorScheme.error)) {
                Text("ยกเลิกบิล")
            }
        }
        BillStatus.VOID  -> { /* no actions */ }
    }
}

@Composable
private fun PaymentHistoryItem(payment: Payment, thb: NumberFormat, sdf: SimpleDateFormat) {
    val methodLabel = when (payment.method) {
        PaymentMethod.CASH      -> "เงินสด"
        PaymentMethod.TRANSFER  -> "โอนเงิน"
        PaymentMethod.PROMPTPAY -> "PromptPay"
    }
    ListItem(
        headlineContent = { Text(payment.receiptNumber) },
        supportingContent = { Text("${sdf.format(Date(payment.paymentDate))}  •  $methodLabel") },
        trailingContent  = { Text("฿${thb.format(payment.amount / 100)}",
                                  fontWeight = FontWeight.Medium) }
    )
}

@Composable
private fun BillInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(80.dp),
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BillAmountRow(label: String, amountSatang: Long) {
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(1f),
             style = MaterialTheme.typography.bodyMedium)
        Text("฿${thb.format(amountSatang / 100)}",
             style = MaterialTheme.typography.bodyMedium)
    }
}

private fun buildBillShareText(bill: Bill, thb: NumberFormat, sdf: SimpleDateFormat): String {
    val year  = bill.yearMonth / 100
    val month = bill.yearMonth % 100
    return buildString {
        appendLine("📋 ใบแจ้งค่าเช่า ${"%04d/%02d".format(year, month)}")
        appendLine("เลขที่: ${bill.billNumber}")
        appendLine("ห้อง: ${bill.roomNumber}")
        appendLine("ผู้เช่า: ${bill.tenantName}")
        appendLine()
        appendLine("💰 รายการ:")
        appendLine("  ค่าเช่า: ฿${thb.format(bill.rent / 100)}")
        appendLine("  ค่าไฟ (${bill.electricityUnits} หน่วย): ฿${thb.format(bill.electricityAmount / 100)}")
        appendLine("  ค่าน้ำ (${bill.waterUnits} หน่วย): ฿${thb.format(bill.waterAmount / 100)}")
        if (bill.fixedFeesTotal > 0)
            appendLine("  ค่าบริการ: ฿${thb.format(bill.fixedFeesTotal / 100)}")
        appendLine()
        appendLine("📌 รวม: ฿${thb.format(bill.total / 100)}")
        appendLine("⏰ ชำระภายใน: ${sdf.format(Date(bill.dueDate))}")
        if (bill.remainingAmount < bill.total)
            appendLine("✅ ชำระแล้ว: ฿${thb.format(bill.paidAmount / 100)}")
    }
}
