/*
 * MeterEntryScreen.kt — หน้าจดมิเตอร์ไฟ-น้ำ รายเดือน
 * ============================================================
 * แสดงรายการห้องทั้งหมด ให้กรอกค่ามิเตอร์ปัจจุบัน
 * ค่ามิเตอร์ก่อนหน้าถูก pre-fill จากเดือนก่อน
 */
package com.dormhub.app.ui.meter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.dormhub.app.ui.util.isWideScreen

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
    val wide = isWideScreen()

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

        if (state.rooms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("ไม่มีห้องพัก", style = MaterialTheme.typography.bodyLarge,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(if (wide) 2 else 1),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.rooms, key = { it.room.id }) { roomState ->
                    MeterRoomCard(
                        roomState = roomState,
                        onElecPreviousChange = { viewModel.updateElecPrevious(roomState.room.id, it) },
                        onElecCurrentChange  = { viewModel.updateElecCurrent(roomState.room.id, it) },
                        onWaterPreviousChange = { viewModel.updateWaterPrevious(roomState.room.id, it) },
                        onWaterCurrentChange = { viewModel.updateWaterCurrent(roomState.room.id, it) },
                        onSave = { viewModel.saveReading(roomState.room.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MeterRoomCard(
    roomState: RoomMeterState,
    onElecPreviousChange: (String) -> Unit,
    onElecCurrentChange: (String) -> Unit,
    onWaterPreviousChange: (String) -> Unit,
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
            if (roomState.isFirstEntry) {
                Text("✏️ ใส่ค่าเริ่มต้นได้เลย",
                     style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.tertiary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = roomState.electricityPrevious,
                    onValueChange = if (roomState.isFirstEntry) onElecPreviousChange else { _ -> },
                    label = { Text(if (roomState.isFirstEntry) "ค่าเริ่มต้น" else "ก่อนหน้า") },
                    readOnly = !roomState.isFirstEntry,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    onValueChange = if (roomState.isFirstEntry) onWaterPreviousChange else { _ -> },
                    label = { Text(if (roomState.isFirstEntry) "ค่าเริ่มต้น" else "ก่อนหน้า") },
                    readOnly = !roomState.isFirstEntry,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
