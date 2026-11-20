/*
 * DashboardScreen.kt — หน้าหลัก (Dashboard)
 * ============================================================
 * แสดงสรุปสถานะของหอพักในเดือนปัจจุบัน
 * StatCard = กล่องตัวเลขสรุป (จำนวนห้อง, ยอดเงิน ฯลฯ)
 * ใน Phase 2 จะเพิ่มปุ่ม "จดมิเตอร์" และ "ออกบิล"
 */
package com.dormhub.app.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.R
import com.dormhub.app.ui.navigation.Screen
import com.dormhub.app.ui.util.isWideScreen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    val wide = isWideScreen()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.propertyName.ifBlank { stringResource(R.string.app_name) }) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Quick Actions
            if (state.currentYearMonth > 0) {
                item {
                    Text("การดำเนินการ",
                         style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    val actions = listOf(
                        Triple("จดมิเตอร์", Icons.Default.Speed,
                            { navController.navigate(Screen.MeterEntry.withYearMonth(state.currentYearMonth)) }),
                        Triple("รายการบิล", Icons.Default.Assignment,
                            { navController.navigate(Screen.BillList.withYearMonth(state.currentYearMonth)) }),
                    )
                    if (wide) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            actions.forEach { (label, icon, onClick) ->
                                ActionCard(label = label, icon = icon,
                                           modifier = Modifier.weight(1f), onClick = onClick)
                            }
                            // ช่องว่างให้สมดุลบน tablet landscape
                            Spacer(Modifier.weight(2f))
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            actions.forEach { (label, icon, onClick) ->
                                ActionCard(label = label, icon = icon,
                                           modifier = Modifier.weight(1f), onClick = onClick)
                            }
                        }
                    }
                }
            }

            // สรุปห้องพัก
            item {
                Text(stringResource(R.string.section_room_summary),
                     style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatCard(stringResource(R.string.label_total_rooms), state.totalRooms.toString(),
                             modifier = Modifier.weight(1f))
                    StatCard(stringResource(R.string.label_occupied), state.occupiedRooms.toString(),
                             color = MaterialTheme.colorScheme.primary,
                             modifier = Modifier.weight(1f))
                    StatCard(stringResource(R.string.label_vacant), state.vacantRooms.toString(),
                             modifier = Modifier.weight(1f))
                }
            }

            // สรุปการเงินเดือนนี้
            item {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.section_billing_summary),
                     style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BillingRow(stringResource(R.string.label_total_billed),
                               "฿${thb.format(state.totalBilled / 100)}")
                    BillingRow(stringResource(R.string.label_collected),
                               "฿${thb.format(state.totalCollected / 100)}",
                               valueColor = MaterialTheme.colorScheme.primary)
                    BillingRow(stringResource(R.string.label_outstanding),
                               "฿${thb.format((state.totalBilled - state.totalCollected) / 100)}",
                               valueColor = MaterialTheme.colorScheme.error)
                }
            }

            // สัญญาใกล้หมด
            if (state.expiringContracts.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null,
                             tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.section_expiring_contracts),
                             style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
                items(state.expiringContracts) { tenant ->
                    ListItem(
                        headlineContent = { Text(tenant.name) },
                        supportingContent = { Text("ห้อง: ${tenant.roomId}") }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null,
                 tint = MaterialTheme.colorScheme.onPrimaryContainer,
                 modifier = Modifier.size(28.dp))
            Text(label, style = MaterialTheme.typography.labelLarge,
                 color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleLarge,
                 fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun BillingRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium,
             fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
