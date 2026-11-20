/*
 * OnboardingScreen.kt — หน้า Wizard ตั้งค่าครั้งแรก
 * ============================================================
 * ประกอบด้วย 5 ขั้นตอน แสดงทีละขั้น:
 *   Step 0: ข้อมูลหอพัก
 *   Step 1: อาคารและชั้น
 *   Step 2: สร้างห้องพักแบบ bulk
 *   Step 3: อัตราค่าไฟ-น้ำ และค่าบริการ
 *   Step 4: บัญชีรับเงิน + PromptPay
 */
package com.dormhub.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.dormhub.app.R

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (state.step + 1) / 5f },
            modifier = Modifier.fillMaxWidth()
        )

        // Step content
        Box(modifier = Modifier.weight(1f)) {
            when (state.step) {
                0 -> Step1PropertyInfo(state, viewModel)
                1 -> Step2Buildings(state, viewModel)
                2 -> Step3Rooms(state, viewModel)
                3 -> Step4Rates(state, viewModel)
                4 -> Step5Payment(state, viewModel)
            }
        }

        // Error
        state.error?.let { err ->
            Text(text = err, color = MaterialTheme.colorScheme.error,
                 modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.step > 0) {
                OutlinedButton(onClick = { viewModel.prevStep() }) {
                    Text(stringResource(R.string.btn_back))
                }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            if (state.step < 4) {
                Button(onClick = {
                    if (state.step == 1) viewModel.buildRoomInputs()
                    viewModel.nextStep()
                }) {
                    Text(stringResource(R.string.btn_next))
                }
            } else {
                Button(
                    onClick = { viewModel.completeOnboarding() },
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.btn_finish))
                    }
                }
            }
        }
    }
}

// ─── Step 1: ข้อมูลหอพัก ───────────────────────────────────
@Composable
private fun Step1PropertyInfo(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepTitle("1/5  ข้อมูลหอพัก")
        Text("กรอกข้อมูลหอพักของคุณ", style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = state.propertyName,
            onValueChange = vm::updatePropertyName,
            label = { Text(stringResource(R.string.label_property_name) + " *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.propertyAddress,
            onValueChange = vm::updatePropertyAddress,
            label = { Text(stringResource(R.string.label_address)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        OutlinedTextField(
            value = state.propertyPhone,
            onValueChange = vm::updatePropertyPhone,
            label = { Text(stringResource(R.string.label_phone)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        Text("* กรอกชื่อหอพักก็พอสำหรับตอนนี้ ข้อมูลอื่นเพิ่มได้ในภายหลัง",
             style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ─── Step 2: อาคารและชั้น ──────────────────────────────────
@Composable
private fun Step2Buildings(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepTitle("2/5  อาคารและชั้น")
        Text("กรอกจำนวนอาคารและชั้นในหอพักของคุณ",
             style = MaterialTheme.typography.bodyMedium,
             color = MaterialTheme.colorScheme.onSurfaceVariant)

        state.buildings.forEachIndexed { index, building ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("อาคาร ${index + 1}", fontWeight = FontWeight.SemiBold,
                             modifier = Modifier.weight(1f))
                        if (state.buildings.size > 1) {
                            IconButton(onClick = { vm.removeBuilding(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "ลบ")
                            }
                        }
                    }
                    OutlinedTextField(
                        value = building.name,
                        onValueChange = { vm.updateBuilding(index, it, building.floors) },
                        label = { Text("ชื่ออาคาร") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = building.floors.toString(),
                        onValueChange = { vm.updateBuilding(index, building.name, it.toIntOrNull() ?: 1) },
                        label = { Text("จำนวนชั้น") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = { vm.addBuilding() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("เพิ่มอาคาร")
        }
    }
}

// ─── Step 3: ห้องพัก ───────────────────────────────────────
@Composable
private fun Step3Rooms(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepTitle("3/5  สร้างห้องพัก")
        Text("ใส่เลขห้องและค่าเช่า เช่น \"101-110\" = ห้อง 101 ถึง 110\nหรือ \"101,102\" สำหรับห้องไม่ต่อเนื่อง",
             style = MaterialTheme.typography.bodySmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (state.roomInputs.isEmpty()) {
            Text("ไม่มีชั้นที่ต้องตั้งค่า กลับไปขั้นที่ 2 เพื่อเพิ่มอาคาร",
                 color = MaterialTheme.colorScheme.error)
        }

        state.roomInputs.forEachIndexed { index, roomInput ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${roomInput.buildingName}  ชั้น ${roomInput.floorNumber}",
                         fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = roomInput.pattern,
                            onValueChange = { vm.updateRoomInput(index, it, roomInput.rent) },
                            label = { Text("เลขห้อง") },
                            placeholder = { Text("เช่น 101-110") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                        )
                        OutlinedTextField(
                            value = roomInput.rent,
                            onValueChange = { vm.updateRoomInput(index, roomInput.pattern, it) },
                            label = { Text("ค่าเช่า (บาท)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }
    }
}

// ─── Step 4: อัตราค่าไฟ-น้ำ ────────────────────────────────
@Composable
private fun Step4Rates(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepTitle("4/5  อัตราค่าไฟ-น้ำ")

        SectionLabel("ค่าไฟฟ้า")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.electricityRate,
                onValueChange = vm::updateElectricityRate,
                label = { Text("อัตรา (บาท/หน่วย)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = state.electricityMin,
                onValueChange = vm::updateElectricityMin,
                label = { Text("ขั้นต่ำ (บาท)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        SectionLabel("ค่าน้ำ")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.waterRate,
                onValueChange = vm::updateWaterRate,
                label = { Text("อัตรา (บาท/หน่วย)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = state.waterMin,
                onValueChange = vm::updateWaterMin,
                label = { Text("ขั้นต่ำ (บาท)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        SectionLabel("ค่าบริการรายเดือน (ถ้ามี)")
        state.feeItems.forEachIndexed { i, (name, amt) ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { vm.updateFeeItem(i, it, amt) },
                    label = { Text("ชื่อค่าบริการ") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amt,
                    onValueChange = { vm.updateFeeItem(i, name, it) },
                    label = { Text("บาท") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                IconButton(onClick = { vm.removeFeeItem(i) }) {
                    Icon(Icons.Default.Delete, contentDescription = "ลบ")
                }
            }
        }
        OutlinedButton(onClick = { vm.addFeeItem() }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("เพิ่มค่าบริการ เช่น ค่าอินเทอร์เน็ต 200 บาท")
        }
    }
}

// ─── Step 5: บัญชีรับเงิน ──────────────────────────────────
@Composable
private fun Step5Payment(state: OnboardingUiState, vm: OnboardingViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepTitle("5/5  บัญชีรับเงิน")

        SectionLabel("PromptPay")
        OutlinedTextField(
            value = state.promptPayId,
            onValueChange = vm::updatePromptPayId,
            label = { Text("หมายเลข PromptPay (โทรศัพท์ / เลขบัตรประชาชน)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        SectionLabel("บัญชีธนาคาร")
        OutlinedTextField(
            value = state.bankName,
            onValueChange = vm::updateBankName,
            label = { Text("ชื่อธนาคาร") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.bankAccountName,
            onValueChange = vm::updateBankAccountName,
            label = { Text("ชื่อบัญชี") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.bankAccountNumber,
            onValueChange = vm::updateBankAccountNumber,
            label = { Text("เลขบัญชี") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        SectionLabel("วันออกบิล")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.billingDay,
                onValueChange = vm::updateBillingDay,
                label = { Text("วันออกบิล") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = state.dueDay,
                onValueChange = vm::updateDueDay,
                label = { Text("วันครบกำหนด") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

// ─── Helper composables ────────────────────────────────────
@Composable
private fun StepTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.primary,
         modifier = Modifier.padding(top = 4.dp))
}
