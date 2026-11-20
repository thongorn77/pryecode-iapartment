/*
 * BillListScreen.kt — หน้ารายการบิลประจำเดือน
 * ============================================================
 * แสดงบิลทั้งหมด พร้อม status chip
 * ปุ่ม "สร้างบิล" สร้าง DRAFT จากมิเตอร์ที่จดแล้ว
 */
package com.dormhub.app.ui.bills

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.data.entity.Bill
import com.dormhub.app.data.entity.BillStatus
import com.dormhub.app.ui.navigation.Screen
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillListScreen(
    yearMonth: Int,
    navController: NavController,
    viewModel: BillListViewModel = hiltViewModel(),
) {
    val state       by viewModel.uiState.collectAsStateWithLifecycle()
    val generating  by viewModel.isGenerating.collectAsStateWithLifecycle()
    val genError    by viewModel.generateError.collectAsStateWithLifecycle()
    val year  = yearMonth / 100
    val month = yearMonth % 100
    val monthLabel = "%04d/%02d".format(year, month)
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))
    var showIssueAllDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (showIssueAllDialog) {
        AlertDialog(
            onDismissRequest = { showIssueAllDialog = false },
            title = { Text("ออกบิลทั้งหมด") },
            text  = { Text("ออกบิล DRAFT ทั้งหมด ${state.bills.count { it.status == BillStatus.DRAFT }} ใบ?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.issueAllDrafts()
                    showIssueAllDialog = false
                }) { Text("ยืนยัน") }
            },
            dismissButton = {
                TextButton(onClick = { showIssueAllDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    genError?.let { err ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("ไม่สามารถสร้างบิล") },
            text  = { Text(err) },
            confirmButton = {
                TextButton(onClick = { /* clear handled by VM */ }) { Text("ตกลง") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("บิล $monthLabel") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "กลับ")
                    }
                },
                actions = {
                    if (state.bills.any { it.status == BillStatus.DRAFT }) {
                        TextButton(onClick = { showIssueAllDialog = true }) {
                            Text("ออกบิลทั้งหมด")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (state.canGenerate) viewModel.generateBills() },
                icon = {
                    if (generating) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Default.Add, null)
                },
                text = { Text("สร้างบิล") },
                containerColor = if (state.canGenerate)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val filteredBills = remember(state.bills, searchQuery) {
            if (searchQuery.isBlank()) state.bills
            else state.bills.filter { bill ->
                bill.roomNumber.contains(searchQuery, ignoreCase = true) ||
                bill.tenantName.contains(searchQuery, ignoreCase = true) ||
                bill.billNumber.contains(searchQuery, ignoreCase = true)
            }
        }

        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ค้นหาห้อง / ผู้เช่า / เลขบิล") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "ล้าง")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // banner วันออกบิล
            if (!state.canGenerate) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "⏰ ออกบิลได้ตั้งแต่วันที่ ${state.billingDay} ของเดือน",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            if (filteredBills.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (searchQuery.isBlank()) "ยังไม่มีบิลเดือนนี้\nกดปุ่ม + เพื่อสร้างบิลจากมิเตอร์"
                            else "ไม่พบบิลที่ตรงกับ \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredBills, key = { it.id }) { bill ->
                    BillListItem(
                        bill = bill,
                        thb = thb,
                        onClick = {
                            navController.navigate(Screen.BillDetail.withIds(yearMonth, bill.id))
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
        } // end Column
    }
}

@Composable
private fun BillListItem(
    bill: Bill,
    thb: NumberFormat,
    onClick: () -> Unit,
) {
    val (statusLabel, statusColor) = when (bill.status) {
        BillStatus.DRAFT   -> "ร่าง"      to Color(0xFF9E9E9E)
        BillStatus.ISSUED  -> "รอชำระ"   to Color(0xFF1565C0)
        BillStatus.PARTIAL -> "บางส่วน"  to Color(0xFFF57F17)
        BillStatus.PAID    -> "ชำระแล้ว" to Color(0xFF2E7D32)
        BillStatus.VOID    -> "ยกเลิก"   to Color(0xFFB71C1C)
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(bill.billNumber, fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            Text("ห้อง ${bill.roomNumber}  •  ${bill.tenantName}")
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "฿${thb.format(bill.total / 100)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                SuggestionChip(
                    onClick = {},
                    label = { Text(statusLabel, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.12f),
                        labelColor = statusColor
                    )
                )
            }
        }
    )
}
