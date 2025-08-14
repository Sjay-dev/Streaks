@file:OptIn(ExperimentalFoundationApi::class)

package com.example.streaks.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.streaks.Model.Frequency
import com.example.streaks.Model.StreakModel
import com.example.streaks.R
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime




@AndroidEntryPoint
class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

                scaffoldScreen()

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scaffoldScreen(
    viewModel: StreakViewModel = hiltViewModel()
) {
    val selectedStreaks by viewModel.selectedStreaks.collectAsState()

    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        Color.Transparent,
        darkIcons = true
    )

    val userName = "Sj"
    var greetings by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Real-time greeting update
    LaunchedEffect(Unit) {
        while (true) {
            val timeNow = LocalTime.now().hour
            greetings = when (timeNow) {
                in 0..11 -> "Good Morning"
                in 12..16 -> "Good Afternoon"
                in 17..23 -> "Good Evening"
                else -> "Hello"
            }
            delay(60 * 1000)
        }
    }

    Scaffold(
        topBar = {
            if (selectedStreaks.isNotEmpty()) {
                TopAppBar(
                    title = { Text("${selectedStreaks.size} selected") },
                    actions = {
                        IconButton(onClick = { viewModel.deleteSelected() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    }
                )
            } else {
                Surface(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    color = Color.White,
                    tonalElevation = 15.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$greetings, $userName",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { TODO() }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings"
                            )
                        }
                    }
                }
            }
        },

        floatingActionButton = {
            if (selectedStreaks.isEmpty()) {
                FloatingActionButton(
                    onClick = {
                        context.startActivity(
                            Intent(context, CreateStreakActivity::class.java)
                        )
                    },
                    containerColor = Color.Blue,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(end = 15.dp)
                ) {
                    Icon(Icons.Default.Add, "Add tasks")
                }
            }
        },

        bottomBar = {
            val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
            val coroutineScope = rememberCoroutineScope()

            NavigationBar(containerColor = Color.White) {
                val items = listOf("Home", "Analysis", "Notifications", "Settings")
                val icons = listOf(
                    Icons.Default.Home,
                    painterResource(R.drawable.monitoring_24px),
                    Icons.Default.Notifications,
                    Icons.Default.Settings
                )

                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        icon = {
                            if (index == 1) {
                                Icon(painter = icons[index] as Painter, contentDescription = label)
                            } else {
                                Icon(
                                    imageVector = icons[index] as ImageVector,
                                    contentDescription = label
                                )
                            }
                        },
                        label = {
                            Text(
                                label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Blue
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        HomeScreen(paddingValues, viewModel)
    }
}

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    viewModel: StreakViewModel
) {
    val streaks by viewModel.streaks.collectAsState()
    val selectedStreaks by viewModel.selectedStreaks.collectAsState()

    if (streaks.isEmpty()) {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No Streak created yet!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Blue
            )
        }
    } else {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn {
                items(streaks) { streak ->
                    Streaks(
                        streak = streak,
                        viewModel = viewModel,
                        isSelected = selectedStreaks.contains(streak.streakId),
                        inSelectionMode = selectedStreaks.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
fun Streaks(
    streak: StreakModel,
    viewModel: StreakViewModel,
    isSelected: Boolean,
    inSelectionMode: Boolean
) {
    Surface(
        color = if (isSelected) Color(0xFFE0F7FA) else Color.White,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 1.dp,
        border = BorderStroke(
            1.dp,
            if (isSelected) Color.Blue else Color.LightGray
        ),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 10.dp)
            .combinedClickable(
                onClick = {
                    if (inSelectionMode) {
                        viewModel.toggleSelection(streak.streakId)
                    } else {
                        // Normal click action
                    }
                },
                onLongClick = {
                    viewModel.toggleSelection(streak.streakId)
                }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        streak.streakName,
                        Modifier.padding(bottom = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )

                    Text(
                        viewModel.nextCount(streak),
                        Modifier.padding(bottom = 12.dp),
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(shape = CircleShape)
                        .border(
                            width = 3.dp,
                            color = Color(streak.colorValue.toULong()),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        viewModel.calculateStreakCount(streak).toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color(streak.colorValue.toULong())
                    )
                }
            }
        }
    }
}






