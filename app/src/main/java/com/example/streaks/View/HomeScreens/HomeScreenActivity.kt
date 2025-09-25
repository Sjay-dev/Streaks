@file:OptIn(ExperimentalFoundationApi::class)

package com.example.streaks.View.HomeScreens


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.example.streaks.Model.StreakModel
import com.example.streaks.View.NotificationScreens.NotificationScreen
import com.example.streaks.View.SettingsScreens.SettingsScreen
import com.example.streaks.ViewModel.NotificationViewModel
import com.example.streaks.ViewModel.StreakViewModel
import com.example.streaks.ui.theme.StreaksTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

const val CHANNEL = "CHANNEL"

@AndroidEntryPoint
class HomeScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) // system default notification sound
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Streak reminders"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, attributes) // ✅ system default notification sound
            }

            val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        setContent {
            val viewModel: StreakViewModel = hiltViewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            val systemUiController = rememberSystemUiController()
            systemUiController.setSystemBarsColor(
                Color.Transparent,
                darkIcons = !isDarkMode
            )
            StreaksTheme(darkTheme = isDarkMode) {
                scaffoldScreen()

            }




        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scaffoldScreen(
    viewModel: StreakViewModel = hiltViewModel()
) {


    rememberNavController()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

    val selectedStreaks by viewModel.selectedStreaks.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }



    val userName = "Sj"
    var greetings by remember { mutableStateOf("") }
    val context = LocalContext.current

    val intent : Intent = Intent(context, HomeScreenActivity::class.java)
    val openPage = intent?.getIntExtra("OPEN_PAGE_INDEX", 0) ?: 0

    val notificationViewModel : NotificationViewModel = hiltViewModel()
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

    //Scroll to Notification page
    LaunchedEffect(Unit) {
        pagerState.scrollToPage(openPage)
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        val deleteMessage = if (selectedStreaks.size == 1) {
            "Are you sure you want to delete this streak?"
        } else {
            "Are you sure you want to delete ${selectedStreaks.size} streaks?"
        }

        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text(deleteMessage) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }



    Scaffold(
        topBar = {
            when (pagerState.currentPage) {
                0 -> {
                    if (selectedStreaks.isNotEmpty()) {
                        TopAppBar(
                            title = { Text("${selectedStreaks.size} selected") },
                            actions = {
                                IconButton(onClick = { showDeleteDialog = true }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                                IconButton(onClick = { viewModel.clearSelection() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            }
                        )
                    }
                    else {
                        Surface(
                            modifier = Modifier
                                .statusBarsPadding()
                                .fillMaxWidth(),
                            color = Color.Transparent,
                            tonalElevation = 15.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "$greetings, $userName",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(2)
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings" ,
                                    )
                                }
                            }
                        }
                    }
                }

                1 -> {
                    Surface(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth(),
                        color = Color.Transparent,
                        tonalElevation = 15.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Notification",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                2 -> {
                    Surface(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth(),
                        color = Color.Transparent,
                        tonalElevation = 15.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Settings",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },

        floatingActionButton = {
            if (selectedStreaks.isEmpty()) {
                when (pagerState.currentPage) {
                    0 -> {
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
                            Icon(Icons.Default.Add, "Add streak")
                        }
                    }

                    1 ->{
                        FloatingActionButton(
                            onClick = {
                                notificationViewModel.clearAll()
                            },
                            containerColor = Color(0xFF4CAF50),
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.padding(end = 15.dp)
                        ) {
                            Icon(Icons.Default.Check, "Check")
                        }
                    }
                }
            }
        },

        bottomBar = {
            BottomNavigationBar(
                pagerState = pagerState,
                onTabSelected = { index ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> HomeScreen(paddingValues , viewModel)
                1 -> NotificationScreen(paddingValues)
                2 -> SettingsScreen(paddingValues)
            }
        }

    } }


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
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "No Streak created yet!",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color =MaterialTheme.colorScheme.background
            )
        }
    }
    else {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
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
    val context = LocalContext.current
    val streakColor = Color(streak.streakColor.toULong())

    // Gradient for border
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            streakColor.copy(alpha = 0.9f),
            streakColor.copy(alpha = 0.4f),
            streakColor.copy(alpha = 0.9f)
        ),
        start = Offset(0f, 0f),
        end = Offset(500f, 500f) // diagonal gradient
    )

    Surface(
        color = if (isSelected) streakColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(
            width = 2.dp,
            brush = gradientBrush
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
                        context.startActivity(
                            Intent(context, StreakDetailsActivity::class.java).apply {
                                putExtra("streakId", streak.streakId)
                            }
                        )
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
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        streak.streakName,
                        modifier = Modifier.padding(bottom = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = streakColor
                    )

                    Text(
                        viewModel.nextCount(streak),
                        modifier = Modifier.padding(bottom = 12.dp),
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                }

                Box(
                    modifier = Modifier
                        .size(55.dp)
                        .clip(CircleShape)
                        .border(
                            width = 3.dp,
                            brush = gradientBrush, // 👈 Gradient border for circle too
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        viewModel.calculateStreakCount(streak).toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = streakColor
                    )
                }
            }
        }
    }
}



@Composable
fun BottomNavigationBar(
    pagerState: PagerState,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("Home", "Notifications", "Settings")
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Notifications,
        Icons.Default.Settings
    )

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        items.forEachIndexed { index, label ->
            NavigationBarItem(
                selected = pagerState.currentPage == index,
                onClick = { onTabSelected(index) },
                icon = {
                        Icon(icons[index] as ImageVector, contentDescription = label)
                },
                label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}








