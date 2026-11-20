/*
 * SettingsScreen.kt — หน้าตั้งค่า
 * ============================================================
 * แสดงและแก้ไข:
 *   - ข้อมูลหอพัก (ชื่อ, ที่อยู่, โทรศัพท์)
 *   - อัตราค่าไฟ-น้ำปัจจุบัน
 *   - ค่าบริการรายเดือน
 *   - PromptPay / บัญชีธนาคาร
 * การแก้ไขอัตราค่าไฟ-น้ำจะสร้าง RateVersion ใหม่ (ไม่แก้อันเก่า)
 */
package com.dormhub.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.R
import com.dormhub.app.data.entity.FixedFee
import com.dormhub.app.data.entity.PropertySettings
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPropertyDialog by remember { mutableStateOf(false) }
    var showRateDialog     by remember { mutableStateOf(false) }
    var showFeeDialog      by remember { mutableStateOf(false) }
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))

    if (showPropertyDialog) {
        PropertyEditDialog(
            settings = state.settings,
            onDismiss = { showPropertyDialog = false },
            onSave = { viewModel.savePropertySettings(it); showPropertyDialog = false }
        )
    }

    if (showRateDialog) {
        RateEditDialog(
            rate = state.latestRate,
            onDismiss = { showRateDialog = false },
            onSave = { e, em, w, wm ->
                viewModel.saveRate(e, em, w, wm)
                showRateDialog = false
            }
        )
    }

    if (showFeeDialog) {
        FeeAddDialog(
            onDismiss = { showFeeDialog = false },
            onSave = { name, amt -> viewModel.saveFixedFee(name, amt); showFeeDialog = false }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ข้อมูลหอพัก
            item {
                SettingsSection(
                    title = stringResource(R.string.section_property_info),
                    onEdit = { showPropertyDialog = true }
                ) {
                    SettingsRow("ชื่อหอพัก", state.settings.name.ifBlank { "-" })
                    SettingsRow("ที่อยู่", state.settings.address.ifBlank { "-" })
                    SettingsRow("โทรศัพท์", state.settings.phone.ifBlank { "-" })
                    SettingsRow("PromptPay", state.settings.promptPayId.ifBlank { "-" })
                    SettingsRow("บัญชีธนาคาร",
                        if (state.settings.bankName.isNotBlank())
                            "${state.settings.bankName} ${state.settings.bankAccountNumber}"
                        else "-")
                }
            }

            // อัตราค่าไฟ-น้ำ
            item {
                SettingsSection(
                    title = "อัตราค่าไฟ-น้ำ (ปัจจุบัน)",
                    onEdit = { showRateDialog = true }
                ) {
                    val r = state.latestRate
                    if (r != null) {
                        SettingsRow("ค่าไฟ", "฿${thb.format(r.electricityRatePerUnit / 100)}/หน่วย (ขั้นต่ำ ฿${thb.format(r.electricityMinimum / 100)})")
                        SettingsRow("ค่าน้ำ", "฿${thb.format(r.waterRatePerUnit / 100)}/หน่วย (ขั้นต่ำ ฿${thb.format(r.waterMinimum / 100)})")
                    } else {
                        Text("ยังไม่ได้ตั้งค่า", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ค่าบริการรายเดือน
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("ค่าบริการรายเดือน", style = MaterialTheme.typography.titleSmall,
                         fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showFeeDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "เพิ่ม")
                    }
                }
            }
            if (state.fixedFees.isEmpty()) {
                item { Text("ยังไม่มีค่าบริการ", color = MaterialTheme.colorScheme.onSurfaceVariant,
                             style = MaterialTheme.typography.bodySmall) }
            }
            items(state.fixedFees) { fee ->
                FeeRow(fee = fee, onDelete = { viewModel.deleteFixedFee(fee.id) })
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, onEdit: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall,
                     fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "แก้ไข") }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, modifier = Modifier.width(100.dp),
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FeeRow(fee: FixedFee, onDelete: () -> Unit) {
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    ListItem(
        headlineContent = { Text(fee.name) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("฿${thb.format(fee.amount / 100)}")
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "ลบ",
                         tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
    HorizontalDivider()
}

// ─── Dialogs ───────────────────────────────────────────────

@Composable
private fun PropertyEditDialog(
    settings: PropertySettings,
    onDismiss: () -> Unit,
    onSave: (PropertySettings) -> Unit
) {
    var name       by remember { mutableStateOf(settings.name) }
    var address    by remember { mutableStateOf(settings.address) }
    var phone      by remember { mutableStateOf(settings.phone) }
    var promptPay  by remember { mutableStateOf(settings.promptPayId) }
    var bankName   by remember { mutableStateOf(settings.bankName) }
    var bankAccName  by remember { mutableStateOf(settings.bankAccountName) }
    var bankAccNo  by remember { mutableStateOf(settings.bankAccountNumber) }
    var billingDay by remember { mutableStateOf(settings.billingDay.toString()) }
    var dueDay     by remember { mutableStateOf(settings.dueDay.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("แก้ไขข้อมูลหอพัก") },
        text = {
            Column(
                modifier = Modifier.verticalScrollable(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("ชื่อหอพัก") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it },
                    label = { Text("ที่อยู่") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                OutlinedTextField(value = phone, onValueChange = { phone = it },
                    label = { Text("โทรศัพท์") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                OutlinedTextField(value = promptPay, onValueChange = { promptPay = it },
                    label = { Text("PromptPay") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankName, onValueChange = { bankName = it },
                    label = { Text("ชื่อธนาคาร") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankAccName, onValueChange = { bankAccName = it },
                    label = { Text("ชื่อบัญชี") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankAccNo, onValueChange = { bankAccNo = it },
                    label = { Text("เลขบัญชี") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = billingDay, onValueChange = { billingDay = it },
                        label = { Text("วันออกบิล") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = dueDay, onValueChange = { dueDay = it },
                        label = { Text("วันครบกำหนด") }, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(settings.copy(
                    name             = name,
                    address          = address,
                    phone            = phone,
                    promptPayId      = promptPay,
                    bankName         = bankName,
                    bankAccountName  = bankAccName,
                    bankAccountNumber = bankAccNo,
                    billingDay       = billingDay.toIntOrNull() ?: 1,
                    dueDay           = dueDay.toIntOrNull() ?: 15,
                ))
            }) { Text("บันทึก") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ยกเลิก") } }
    )
}

@Composable
private fun RateEditDialog(
    rate: com.dormhub.app.data.entity.RateVersion?,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double, Double) -> Unit
) {
    var elecRate by remember { mutableStateOf((rate?.electricityRatePerUnit?.div(100.0))?.toString() ?: "5.00") }
    var elecMin  by remember { mutableStateOf((rate?.electricityMinimum?.div(100.0))?.toString() ?: "50.00") }
    var waterRate by remember { mutableStateOf((rate?.waterRatePerUnit?.div(100.0))?.toString() ?: "18.00") }
    var waterMin  by remember { mutableStateOf((rate?.waterMinimum?.div(100.0))?.toString() ?: "50.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("แก้ไขอัตราค่าไฟ-น้ำ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("หมายเหตุ: จะสร้างอัตราใหม่สำหรับเดือนนี้เป็นต้นไป",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(value = elecRate, onValueChange = { elecRate = it },
                    label = { Text("ค่าไฟ (บาท/หน่วย)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = elecMin, onValueChange = { elecMin = it },
                    label = { Text("ขั้นต่ำค่าไฟ (บาท)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = waterRate, onValueChange = { waterRate = it },
                    label = { Text("ค่าน้ำ (บาท/หน่วย)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                OutlinedTextField(value = waterMin, onValueChange = { waterMin = it },
                    label = { Text("ขั้นต่ำค่าน้ำ (บาท)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    elecRate.toDoubleOrNull() ?: 5.0,
                    elecMin.toDoubleOrNull() ?: 50.0,
                    waterRate.toDoubleOrNull() ?: 18.0,
                    waterMin.toDoubleOrNull() ?: 50.0,
                )
            }) { Text("บันทึก") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ยกเลิก") } }
    )
}

@Composable
private fun FeeAddDialog(onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("เพิ่มค่าบริการ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("ชื่อค่าบริการ เช่น ค่าอินเทอร์เน็ต") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it },
                    label = { Text("จำนวนเงิน (บาท)") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, amount.toDoubleOrNull() ?: 0.0) },
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) { Text("เพิ่ม") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ยกเลิก") } }
    )
}

@Composable
private fun Modifier.verticalScrollable(): Modifier =
    this.heightIn(max = 400.dp).then(
        Modifier.verticalScroll(rememberScrollState())
    )

