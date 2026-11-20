/*
 * TenantFormScreen.kt — ฟอร์มเพิ่ม / แก้ไขผู้เช่า
 * ============================================================
 * ใช้สำหรับทั้ง Add (tenantId = null) และ Edit (tenantId = ...)
 * หลังบันทึกสำเร็จจะ navigate กลับ
 */
package com.dormhub.app.ui.tenants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.R
import com.dormhub.app.data.entity.BillLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantFormScreen(
    roomId: String,
    tenantId: String?,
    navController: NavController,
    viewModel: TenantViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) navController.popBackStack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (tenantId == null) stringResource(R.string.btn_add_tenant) else "แก้ไขข้อมูลผู้เช่า") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("ชื่อ-นามสกุล *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.phone,
                onValueChange = viewModel::updatePhone,
                label = { Text("โทรศัพท์") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = state.lineName,
                onValueChange = viewModel::updateLineName,
                label = { Text("ชื่อ LINE") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.moveInDate,
                onValueChange = viewModel::updateMoveInDate,
                label = { Text("วันที่เข้าพัก (dd/MM/yyyy)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = state.contractEnd,
                onValueChange = viewModel::updateContractEnd,
                label = { Text("วันหมดสัญญา (dd/MM/yyyy)") },
                placeholder = { Text("เว้นว่างถ้าไม่กำหนด") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.deposit,
                    onValueChange = viewModel::updateDeposit,
                    label = { Text("เงินประกัน (บาท)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = state.advanceRent,
                    onValueChange = viewModel::updateAdvanceRent,
                    label = { Text("ค่าเช่าล่วงหน้า (บาท)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            // ภาษาบิล
            Text("ภาษาบิล", style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.billLanguage == BillLanguage.TH,
                    onClick  = { viewModel.updateBillLanguage(BillLanguage.TH) },
                    label    = { Text("ภาษาไทย") }
                )
                FilterChip(
                    selected = state.billLanguage == BillLanguage.EN,
                    onClick  = { viewModel.updateBillLanguage(BillLanguage.EN) },
                    label    = { Text("English") }
                )
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::updateNote,
                label = { Text("หมายเหตุ") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            state.error?.let { err ->
                Text(err, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick  = { viewModel.save() },
                enabled  = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.btn_save))
                }
            }
        }
    }
}
