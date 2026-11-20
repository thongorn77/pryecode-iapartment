package com.dormhub.app.ui.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.dormhub.app.data.dao.MonthSummary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    navController: NavController,
    viewModel: ReportViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val thb = NumberFormat.getNumberInstance(Locale("th", "TH"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("รายงานสรุป") },
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

        if (state.monthlySummaries.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("ยังไม่มีข้อมูลบิล", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        // Yearly totals
        val yearMap = state.monthlySummaries.groupBy { it.yearMonth / 100 }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            yearMap.entries.sortedByDescending { it.key }.forEach { (year, months) ->
                val yearBilled = months.sumOf { it.totalBilled }
                val yearPaid   = months.sumOf { it.totalPaid }

                item(key = "year_$year") {
                    YearSummaryCard(
                        year = year,
                        totalBilled = yearBilled,
                        totalPaid = yearPaid,
                        thb = thb
                    )
                }
                items(months.sortedByDescending { it.yearMonth }, key = { "month_${it.yearMonth}" }) { summary ->
                    MonthRow(summary = summary, thb = thb)
                }
            }
        }
    }
}

@Composable
private fun YearSummaryCard(year: Int, totalBilled: Long, totalPaid: Long, thb: NumberFormat) {
    val outstanding = totalBilled - totalPaid
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("สรุปปี $year", style = MaterialTheme.typography.titleMedium,
                 fontWeight = FontWeight.Bold)
            HorizontalDivider()
            Row(Modifier.fillMaxWidth()) {
                StatItem("ยอดแจ้งทั้งปี", "฿${thb.format(totalBilled / 100)}", Modifier.weight(1f))
                StatItem("รับชำระแล้ว", "฿${thb.format(totalPaid / 100)}", Modifier.weight(1f),
                         valueColor = MaterialTheme.colorScheme.primary)
                StatItem("ค้างชำระ", "฿${thb.format(outstanding / 100)}", Modifier.weight(1f),
                         valueColor = if (outstanding > 0) MaterialTheme.colorScheme.error
                                      else MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, modifier: Modifier = Modifier,
                     valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, color = valueColor,
             style = MaterialTheme.typography.bodyMedium)
        Text(label, style = MaterialTheme.typography.labelSmall,
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MonthRow(summary: MonthSummary, thb: NumberFormat) {
    val monthNames = arrayOf("ม.ค.","ก.พ.","มี.ค.","เม.ย.","พ.ค.","มิ.ย.",
                              "ก.ค.","ส.ค.","ก.ย.","ต.ค.","พ.ย.","ธ.ค.")
    val yr = summary.yearMonth / 100
    val mo = summary.yearMonth % 100
    val monthLabel = "${if (mo in 1..12) monthNames[mo - 1] else "$mo"} $yr"
    val outstanding = summary.totalBilled - summary.totalPaid
    val collectionRate = if (summary.totalBilled > 0)
        (summary.totalPaid * 100 / summary.totalBilled).toInt() else 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(monthLabel, fontWeight = FontWeight.SemiBold)
                Text("เก็บได้ $collectionRate%", style = MaterialTheme.typography.labelSmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("฿${thb.format(summary.totalBilled / 100)}",
                     fontWeight = FontWeight.Medium,
                     style = MaterialTheme.typography.bodyMedium)
                if (outstanding > 0) {
                    Text("ค้าง ฿${thb.format(outstanding / 100)}",
                         style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.error)
                } else {
                    Text("ครบ", style = MaterialTheme.typography.labelSmall,
                         color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        LinearProgressIndicator(
            progress = { collectionRate / 100f },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = if (collectionRate == 100) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
