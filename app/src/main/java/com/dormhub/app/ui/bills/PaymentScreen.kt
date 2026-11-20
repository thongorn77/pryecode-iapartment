/*
 * PaymentScreen.kt — หน้าบันทึกการชำระเงิน
 * ============================================================
 * กรอกจำนวนเงิน เลือกวิธีชำระ บันทึก
 * รองรับ partial payment (จ่ายบางส่วน)
 */
package com.dormhub.app.ui.bills

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.data.entity.PaymentMethod
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    yearMonth: Int,
    billId: String,
    navController: NavController,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) navController.popBackStack()
    }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("ข้อผิดพลาด") },
            text  = { Text(err) },
            confirmButton = { TextButton(onClick = { }) { Text("ตกลง") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("บันทึกชำระเงิน") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bill summary
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(bill.billNumber,
                         style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.Bold)
                    Text("ห้อง ${bill.roomNumber}  •  ${bill.tenantName}",
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth()) {
                        Text("ยอดรวม", Modifier.weight(1f))
                        Text("฿${thb.format(bill.total / 100)}", fontWeight = FontWeight.Medium)
                    }
                    if (bill.paidAmount > 0) {
                        Row(Modifier.fillMaxWidth()) {
                            Text("ชำระแล้ว", Modifier.weight(1f),
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("฿${thb.format(bill.paidAmount / 100)}",
                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Text("คงเหลือ", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        Text("฿${thb.format(bill.remainingAmount / 100)}",
                             fontWeight = FontWeight.Bold,
                             color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Amount input
            OutlinedTextField(
                value = state.amount,
                onValueChange = viewModel::setAmount,
                label = { Text("จำนวนเงิน (บาท)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                prefix = { Text("฿") },
            )

            // Method selector
            Text("วิธีชำระเงิน", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    val label = when (method) {
                        PaymentMethod.CASH      -> "เงินสด"
                        PaymentMethod.TRANSFER  -> "โอนเงิน"
                        PaymentMethod.PROMPTPAY -> "PromptPay"
                    }
                    FilterChip(
                        selected = state.method == method,
                        onClick  = { viewModel.setMethod(method) },
                        label    = { Text(label) }
                    )
                }
            }

            // Note
            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::setNote,
                label = { Text("หมายเหตุ (ไม่บังคับ)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.amount.isNotEmpty()
            ) {
                Text("บันทึกการชำระเงิน")
            }
        }
    }
}
