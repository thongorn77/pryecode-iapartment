/*
 * RoomDetailScreen.kt — หน้าข้อมูลห้องพัก
 * ============================================================
 * แสดง:
 *   - หมายเลขห้อง, สถานะ, ค่าเช่า
 *   - ข้อมูลผู้เช่าปัจจุบัน (ถ้ามี)
 *   - ปุ่ม "เพิ่มผู้เช่า" (ถ้าว่าง)
 *   - ปุ่ม "ย้ายออก" (ถ้ามีผู้เช่า)
 *   - ประวัติบิลย้อนหลัง
 */
package com.dormhub.app.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.R
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.ui.navigation.Screen
import com.dormhub.app.ui.theme.ColorMaintenance
import com.dormhub.app.ui.theme.ColorOccupied
import com.dormhub.app.ui.theme.ColorVacant
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    navController: NavController,
    viewModel: RoomDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showMoveOutDialog by remember { mutableStateOf(false) }
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale("th")) }

    if (showMoveOutDialog) {
        AlertDialog(
            onDismissRequest = { showMoveOutDialog = false },
            title   = { Text("ยืนยันการย้ายออก") },
            text    = { Text("ต้องการบันทึกการย้ายออกของ ${state.tenant?.name} ใช่ไหม?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.moveOut()
                    showMoveOutDialog = false
                }) { Text("ยืนยัน", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showMoveOutDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ห้อง ${state.room?.number ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.room?.status == RoomStatus.VACANT) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddTenant.withRoomId(roomId)) },
                    icon = { Icon(Icons.Default.PersonAdd, null) },
                    text = { Text(stringResource(R.string.btn_add_tenant)) }
                )
            }
        }
    ) { padding ->
        if (state.isLoading || state.room == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val room = state.room!!
        val statusColor = when (room.status) {
            RoomStatus.OCCUPIED    -> ColorOccupied
            RoomStatus.VACANT      -> ColorVacant
            RoomStatus.MAINTENANCE -> ColorMaintenance
        }
        val statusLabel = when (room.status) {
            RoomStatus.OCCUPIED    -> "มีผู้เช่า"
            RoomStatus.VACANT      -> "ว่าง"
            RoomStatus.MAINTENANCE -> "ซ่อมบำรุง"
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ข้อมูลห้อง
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ห้อง ${room.number}", style = MaterialTheme.typography.titleLarge,
                                 fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            SuggestionChip(
                                onClick = {},
                                label = { Text(statusLabel) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = statusColor.copy(alpha = 0.15f),
                                    labelColor = statusColor
                                )
                            )
                        }
                        HorizontalDivider()
                        InfoRow("ค่าเช่า", "฿${thb.format(room.monthlyRent / 100)} / เดือน")
                    }
                }
            }

            // ผู้เช่าปัจจุบัน
            state.tenant?.let { tenant ->
                item {
                    Text("ผู้เช่าปัจจุบัน", style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            InfoRow("ชื่อ", tenant.name)
                            if (tenant.phone.isNotBlank()) InfoRow("โทรศัพท์", tenant.phone)
                            if (tenant.lineName.isNotBlank()) InfoRow("LINE", tenant.lineName)
                            InfoRow("เข้าพักวันที่", sdf.format(Date(tenant.moveInDate)))
                            InfoRow("เงินประกัน", "฿${thb.format(tenant.deposit / 100)}")
                            HorizontalDivider()
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate(Screen.EditTenant.withIds(roomId, tenant.id))
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text("แก้ไขข้อมูล") }
                                OutlinedButton(
                                    onClick = { showMoveOutDialog = true },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("ย้ายออก") }
                            }
                        }
                    }
                }
            }

            // ประวัติบิล
            if (state.recentBills.isNotEmpty()) {
                item {
                    Text("บิลล่าสุด", style = MaterialTheme.typography.titleMedium,
                         fontWeight = FontWeight.SemiBold)
                }
                items(state.recentBills) { bill ->
                    val ym = bill.yearMonth
                    val monthStr = "${ym / 100}/${ym % 100}"
                    val billStatusLabel = when (bill.status) {
                        BillStatus.DRAFT   -> "ร่าง"
                        BillStatus.ISSUED  -> "รอชำระ"
                        BillStatus.PARTIAL -> "ชำระบางส่วน"
                        BillStatus.PAID    -> "ชำระแล้ว"
                        BillStatus.VOID    -> "ยกเลิก"
                    }
                    ListItem(
                        headlineContent = { Text("${bill.billNumber}  ($monthStr)") },
                        supportingContent = { Text("฿${thb.format(bill.total / 100)}") },
                        trailingContent = { Text(billStatusLabel) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.width(90.dp),
             color = MaterialTheme.colorScheme.onSurfaceVariant,
             style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
