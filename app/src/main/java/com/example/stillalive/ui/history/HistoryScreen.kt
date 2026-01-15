package com.example.stillalive.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stillalive.utils.DateUtils

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import com.example.stillalive.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
) {
    val signRecords by viewModel.signRecords.collectAsState(initial = emptyList())
    val smsRecords by viewModel.smsRecords.collectAsState(initial = emptyList())
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("签到记录", "短信记录")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "呀呐YANA", 
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily(Font(R.font.zcoolkuaile))
                    ) 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NavigationBarDefaults.containerColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(signRecords) { record ->
                            val containerColor = if (record.type == "AUTO") 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else 
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                                
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = containerColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                ListItem(
                                    headlineContent = { 
                                        Text(
                                            record.date, 
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        ) 
                                    },
                                    supportingContent = { 
                                        Text(
                                            DateUtils.formatDateTime(record.timestamp),
                                            style = MaterialTheme.typography.bodyMedium
                                        ) 
                                    },
                                    trailingContent = {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                            shape = MaterialTheme.shapes.small,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ) {
                                            Text(
                                                text = if (record.type == "AUTO") "自动" else "手动",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(smsRecords) { record ->
                            val containerColor = if (record.status == "SENT") 
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                            else 
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = containerColor
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = MaterialTheme.shapes.large
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "发送给: ${record.contactName}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (record.status == "SENT") "发送成功" else "发送失败",
                                            color = if (record.status == "SENT") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = DateUtils.formatDateTime(record.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = record.content,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
