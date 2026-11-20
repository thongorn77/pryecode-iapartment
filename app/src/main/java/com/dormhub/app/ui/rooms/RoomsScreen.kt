/*
 * RoomsScreen.kt — หน้าแสดงห้องพักทั้งหมด
 * ============================================================
 * แสดง grid ของห้องพัก จัดกลุ่มตามอาคารและชั้น
 * สีของ chip บ่งบอกสถานะ:
 *   เขียว = มีผู้เช่า, เทา = ว่าง, ส้ม = ซ่อมบำรุง
 * แตะห้องไหน → ไปหน้า RoomDetailScreen
 */
package com.dormhub.app.ui.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.dormhub.app.R
import com.dormhub.app.data.entity.Room
import com.dormhub.app.data.entity.RoomStatus
import com.dormhub.app.ui.navigation.Screen
import com.dormhub.app.ui.theme.ColorMaintenance
import com.dormhub.app.ui.theme.ColorOccupied
import com.dormhub.app.ui.theme.ColorVacant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomsScreen(
    navController: NavController,
    viewModel: RoomsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.nav_rooms)) })
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (state.grouped.isEmpty()) {
            EmptyRoomsPlaceholder()
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.grouped.forEach { buildingGroup ->
                item {
                    Text(buildingGroup.building.name,
                         style = MaterialTheme.typography.titleMedium,
                         color = MaterialTheme.colorScheme.primary)
                }
                buildingGroup.floors.forEach { floorGroup ->
                    item {
                        Text("  ชั้น ${floorGroup.floor.number}",
                             style = MaterialTheme.typography.labelLarge,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        RoomGrid(
                            rooms = floorGroup.rooms,
                            onRoomClick = { room ->
                                navController.navigate(Screen.RoomDetail.withId(room.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoomGrid(rooms: List<Room>, onRoomClick: (Room) -> Unit) {
    // ใช้ FlowRow เพื่อ wrap อัตโนมัติ
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rooms.forEach { room ->
            RoomChip(room = room, onClick = { onRoomClick(room) })
        }
    }
}

@Composable
private fun RoomChip(room: Room, onClick: () -> Unit) {
    val bgColor = when (room.status) {
        RoomStatus.OCCUPIED    -> ColorOccupied
        RoomStatus.VACANT      -> ColorVacant
        RoomStatus.MAINTENANCE -> ColorMaintenance
    }
    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = room.number,
                style = MaterialTheme.typography.labelLarge,
                color = bgColor,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    // Simple FlowRow implementation using Row wrapping
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = horizontalArrangement,
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun EmptyRoomsPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ยังไม่มีห้องพัก", style = MaterialTheme.typography.titleMedium)
            Text("ไปที่ ตั้งค่า → อาคาร เพื่อเพิ่มห้อง",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
