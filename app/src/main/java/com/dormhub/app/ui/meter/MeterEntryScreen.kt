/*
 * MeterEntryScreen.kt — หน้าจดมิเตอร์ไฟ-น้ำ รายเดือน
 * ============================================================
 * แสดงรายการห้องทั้งหมด ให้กรอกค่ามิเตอร์ปัจจุบัน
 * ค่ามิเตอร์ก่อนหน้าถูก pre-fill จากเดือนก่อน
 */
package com.dormhub.app.ui.meter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterEntryScreen(
    yearMonth: Int,
    navController: NavController,
    viewModel: MeterEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val year  = yearMonth / 100
    val month = yearMonth % 100
    val monthLabel = "%04d/%02d".format(year, month)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("จดมิเตอร์ $monthLabel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.rooms.isEmpty()) {
                item {
                    Text(
                        "ไม่มีห้องพัก",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }

            items(state.rooms, key = { it.room.id }) { roomState ->
                MeterRoomCard(
                    roomState = roomState,
                    onElecCurrentChange = { viewModel.updateElecCurrent(roomState.room.id, it) },
                    onWaterCurrentChange = { viewModel.updateWaterCurrent(roomState.room.id, it) },
                    onSave = { viewModel.saveReading(roomState.room.id) }
                )
            }
        }
    }
}

@Composable
private fun MeterRoomCard(
    roomState: RoomMeterState,
    onElecCurrentChange: (String) -> Unit,
    onWaterCurrentChange: (String) -> Unit,
    onSave: () -> Unit,
) {
    val elecUnits = (roomState.electricityCurrent.toLongOrNull() ?: 0L) -
                   (roomState.electricityPrevious.toLongOrNull() ?: 0L)
    val waterUnits = (roomState.waterCurrent.toLongOrNull() ?: 0L) -
                    (roomState.waterPrevious.toLongOrNull() ?: 0L)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "ห้อง ${roomState.room.number}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (roomState.tenant != null) {
                    Text(
                        roomState.tenant.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (roomState.isSaved) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "บันทึกแล้ว",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider()

            // ไฟฟ้า
            Text("⚡ มิเตอร์ไฟฟ้า", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomState.electricityPrevious,
                    onValueChange = {},
                    label = { Text("ก่อนหน้า") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = roomState.electricityCurrent,
                    onValueChange = onElecCurrentChange,
                    label = { Text("ปัจจุบัน") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            if (elecUnits >= 0) {
                Text("ใช้ ${elecUnits} หน่วย", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(4.dp))

            // น้ำประปา
            Text("💧 มิเตอร์น้ำ", style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomState.waterPrevious,
                    onValueChange = {},
                    label = { Text("ก่อนหน้า") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = roomState.waterCurrent,
                    onValueChange = onWaterCurrentChange,
                    label = { Text("ปัจจุบัน") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            if (waterUnits >= 0) {
                Text("ใช้ ${waterUnits} หน่วย", style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = roomState.electricityCurrent.isNotEmpty() && roomState.waterCurrent.isNotEmpty()
            ) {
                Text(if (roomState.isSaved) "อัปเดตข้อมูล" else "บันทึกมิเตอร์")
            }
        }
    }
}
