package com.dormhub.app.ui.bills

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillItem
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.Payment
import com.dormhub.app.data.entity.PaymentMethod
import com.dormhub.app.ui.navigation.Screen
import com.dormhub.app.ui.util.BillSlipRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val scope   = rememberCoroutineScope()
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("th")) }

    var showVoidDialog   by remember { mutableStateOf(false) }
    var voidReason       by remember { mutableStateOf("") }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var isSharingPng     by remember { mutableStateOf(false) }

    // ── Void dialog ──────────────────────────────────────────
    if (showVoidDialog) {
        AlertDialog(
            onDismissRequest = { showVoidDialog = false },
            title = { Text("ยกเลิกบิล") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("บิลที่ยกเลิกแล้วไม่สามารถแก้ไขได้")
                    OutlinedTextField(
                        value = voidReason, onValueChange = { voidReason = it },
                        label = { Text("เหตุผล") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.voidBill(voidReason); showVoidDialog = false },
                    enabled = voidReason.isNotBlank()
                ) { Text("ยืนยัน", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showVoidDialog = false }) { Text("ยกเลิก") } }
        )
    }

    // ── Add item dialog ──────────────────────────────────────
    if (showAddItemDialog) {
        AddBillItemDialog(
            onDismiss = { showAddItemDialog = false },
            onSave = { name, amount -> viewModel.addItem(name, amount); showAddItemDialog = false }
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
                            // PNG share
                            if (isSharingPng) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp).padding(end = 8.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                IconButton(onClick = {
                                    val settings = state.settings ?: return@IconButton
                                    scope.launch {
                                        isSharingPng = true
                                        try {
                                            val uri = withContext(Dispatchers.IO) {
                                                BillSlipRenderer.renderAndShare(
                                                    context, bill, state.billItems, settings
                                                )
                                            }
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "image/png"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(Intent.createChooser(intent, "ส่งบิลผ่าน LINE"))
                                        } finally {
                                            isSharingPng = false
                                        }
                                    }
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "แชร์ PNG")
                                }
                            }
                            // Add item (only for editable statuses)
                            if (bill.status == BillStatus.DRAFT || bill.status == BillStatus.ISSUED) {
                                IconButton(onClick = { showAddItemDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "เพิ่มรายการ")
                                }
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
        val canEdit = bill.status == BillStatus.DRAFT || bill.status == BillStatus.ISSUED

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BillHeaderCard(bill = bill, thb = thb, sdf = sdf) }
            item { BillMeterCard(bill = bill, thb = thb) }

            // Extra BillItems
            if (state.billItems.isNotEmpty()) {
                item {
                    Text("รายการเพิ่มเติม",
                         style = MaterialTheme.typography.titleSmall,
                         fontWeight = FontWeight.SemiBold)
                }
                items(state.billItems, key = { it.id }) { item ->
                    BillItemRow(
                        item = item,
                        thb = thb,
                        canDelete = canEdit,
                        onDelete = { viewModel.removeItem(item.id) }
                    )
                }
            }

            item { BillTotalCard(bill = bill, thb = thb) }
            item {
                BillActionRow(
                    bill = bill, yearMonth = yearMonth, navController = navController,
                    onIssue = { viewModel.issueBill() }, onVoid = { showVoidDialog = true }
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

// ── BillItemRow ─────────────────────────────────────────────

@Composable
private fun BillItemRow(
    item: BillItem,
    thb: NumberFormat,
    canDelete: Boolean,
    onDelete: () -> Unit,
) {
    val amtColor = if (item.amount < 0) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = { Text(item.name) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${if (item.amount < 0) "-" else "+"}฿${thb.format(kotlin.math.abs(item.amount / 100L))}",
                    color = amtColor, fontWeight = FontWeight.Medium
                )
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "ลบ",
                             tint = MaterialTheme.colorScheme.error,
                             modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    )
    HorizontalDivider()
}

// ── AddBillItemDialog ────────────────────────────────────────

@Composable
private fun AddBillItemDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name   by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isDiscount by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("เพิ่มรายการ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Quick presets
                Text("รายการด่วน", style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("ค่าอินเทอร์เน็ต", "ค่าจอดรถ", "ค่าทำความสะอาด").forEach { preset ->
                        FilterChip(
                            selected = name == preset,
                            onClick = { name = preset },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("ชื่อรายการ") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("จำนวนเงิน (บาท)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    prefix = { Text("฿") }
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDiscount, onCheckedChange = { isDiscount = it })
                    Text("เป็นส่วนลด (หักออกจากยอด)",
                         style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amt = (amount.toDoubleOrNull() ?: 0.0) * (if (isDiscount) -1.0 else 1.0)
                    onSave(name, amt)
                },
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("เพิ่ม") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ยกเลิก") } }
    )
}

// ── Existing composables (unchanged) ────────────────────────

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
                Text(bill.billNumber, style = MaterialTheme.typography.titleLarge,
                     fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                SuggestionChip(onClick = {}, label = { Text(statusLabel) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f), labelColor = statusColor))
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
                 style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            HorizontalDivider()
            BillAmountRow("ค่าเช่า", bill.rent)
            BillAmountRow(
                "ค่าไฟ (${bill.electricityPrevious}→${bill.electricityCurrent}, ${bill.electricityUnits} หน่วย)",
                bill.electricityAmount)
            BillAmountRow(
                "ค่าน้ำ (${bill.waterPrevious}→${bill.waterCurrent}, ${bill.waterUnits} หน่วย)",
                bill.waterAmount)
            if (bill.fixedFeesTotal > 0) BillAmountRow("ค่าบริการรายเดือน", bill.fixedFeesTotal)
            if (bill.previousBalance > 0) BillAmountRow("ยอดค้างเดือนก่อน", bill.previousBalance)
            if (bill.lateFee > 0) BillAmountRow("ค่าปรับ", bill.lateFee)
        }
    }
}

@Composable
private fun BillTotalCard(bill: Bill, thb: NumberFormat) {
    Card(modifier = Modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("รวมทั้งหมด", style = MaterialTheme.typography.titleMedium,
                     fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("฿${thb.format(bill.total / 100)}",
                     style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    Text("฿${thb.format(bill.remainingAmount / 100)}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BillActionRow(
    bill: Bill, yearMonth: Int, navController: NavController,
    onIssue: () -> Unit, onVoid: () -> Unit,
) {
    when (bill.status) {
        BillStatus.DRAFT -> {
            Button(onClick = onIssue, modifier = Modifier.fillMaxWidth()) { Text("ออกบิล") }
        }
        BillStatus.ISSUED, BillStatus.PARTIAL -> {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { navController.navigate(Screen.Payment.withIds(yearMonth, bill.id)) },
                    modifier = Modifier.weight(1f)
                ) { Text("บันทึกชำระเงิน") }
                OutlinedButton(
                    onClick = onVoid, modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("ยกเลิกบิล") }
            }
        }
        BillStatus.PAID -> {
            OutlinedButton(onClick = onVoid, modifier = Modifier.fillMaxWidth(),
                           colors = ButtonDefaults.outlinedButtonColors(
                               contentColor = MaterialTheme.colorScheme.error)) {
                Text("ยกเลิกบิล")
            }
        }
        BillStatus.VOID -> { /* no actions */ }
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
        trailingContent = { Text("฿${thb.format(payment.amount / 100)}", fontWeight = FontWeight.Medium) }
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
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("฿${thb.format(amountSatang / 100)}", style = MaterialTheme.typography.bodyMedium)
    }
}
