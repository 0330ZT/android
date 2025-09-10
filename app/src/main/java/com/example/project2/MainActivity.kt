package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.project2.ui.theme.Project2Theme
import android.util.Log
import com.example.project2.data.AppDatabase
import com.example.project2.data.User
import com.example.project2.data.GameRule
import com.example.project2.data.UserStats
import com.example.project2.data.GameRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 获取从LoginActivity传递的用户信息
        val username = intent.getStringExtra("username") ?: "Android"
        val isAdmin = intent.getBooleanExtra("isAdmin", false)

        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = username,
                        isAdmin = isAdmin,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    isAdmin: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textState = remember { mutableStateOf("") }
    var showGameModeDialog by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDevelopmentDialog by remember { mutableStateOf(false) } // 新增待开发弹窗

    // 动画状态
    var isAnimating by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    // 添加数据库初始化
    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(context)
        Log.d("Database", "Database initialized: ${database.isOpen}")

        // 初始化游戏规则
        val existingRule = database.gameRuleDao().getGameRule()
        if (existingRule == null) {
            val defaultRule = GameRule(
                id = 1,
                boardSize = 15,
                winCount = 5,
                allowUndo = true,
                hasForbiddenMoves = false,
                maxMoves = 225,
                ruleDescription = "标准五子棋规则：15x15棋盘，连成5子获胜"
            )
            database.gameRuleDao().insertGameRule(defaultRule)
            Log.d("Database", "Default game rule inserted")
        }

        // 测试数据库操作
        try {
            val testUser = User(
                username = "test_user",
                password = "test_pass",
                isAdmin = false,
                nickname = "测试用户"
            )
            database.userDao().insertUser(testUser)
            Log.d("Database", "Test user inserted successfully")
        } catch (e: Exception) {
            Log.e("Database", "Error inserting test user: ${e.message}")
        }
    }

    // 主容器 - 使用渐变背景
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFBBDEFB),
                        Color(0xFF90CAF9)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 用户信息卡片
            item {
                UserInfoCard(
                    name = name,
                    isAdmin = isAdmin,
                    isAnimating = isAnimating,
                    onAnimationToggle = { isAnimating = !isAnimating }
                )
            }

            // 游戏统计概览卡片 (直接显示在主界面)
            item {
                GameStatsOverviewCard(username = name)
            }

            // 功能按钮网格
            item {
                FeatureButtonsGrid(
                    onGameClick = { showGameModeDialog = true },
                    onStatsClick = { showDevelopmentDialog = true }, // 改为显示待开发弹窗
                    onSettingsClick = { showSettingsDialog = true },
                    onTextInputClick = {
                        val inputText = textState.value
                        if (inputText.isNotEmpty()) {
                            Toast.makeText(context, "输入的内容：$inputText", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "请输入一些内容", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            // 文本输入区域
            item {
                TextInputCard(
                    textState = textState,
                    onTextChange = { textState.value = it }
                )
            }

            // 快速操作卡片
            item {
                QuickActionsCard(
                    isAdmin = isAdmin,
                    onUndoClick = {
                        Toast.makeText(context, "悔棋功能", Toast.LENGTH_SHORT).show()
                    },
                    onRestartClick = {
                        Toast.makeText(context, "重新开始", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    // 对战模式选择对话框
    if (showGameModeDialog) {
        GameModeDialog(
            onDismiss = { showGameModeDialog = false },
            onModeSelected = { mode, opponent ->
                val intent = Intent(context, GameActivity::class.java)
                intent.putExtra("username", name)
                intent.putExtra("isAdmin", isAdmin)
                intent.putExtra("gameMode", mode)
                intent.putExtra("opponent", opponent)
                intent.putExtra("opponentNickname", opponent)
                context.startActivity(intent)
                showGameModeDialog = false
            }
        )
    }

    // 统计对话框
    if (showStatsDialog) {
        StatsDialog(
            onDismiss = { showStatsDialog = false },
            username = name
        )
    }

    // 设置对话框
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            isAdmin = isAdmin
        )
    }

    // 待开发功能弹窗
    if (showDevelopmentDialog) {
        DevelopmentDialog(
            onDismiss = { showDevelopmentDialog = false },
            title = "游戏统计",
            description = "详细统计功能正在开发中，敬请期待！"
        )
    }
}

@Composable
fun UserInfoCard(
    name: String,
    isAdmin: Boolean,
    isAnimating: Boolean,
    onAnimationToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAnimationToggle() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像 - 带旋转动画
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF4FC3F7),
                                Color(0xFF29B6F6)
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        color = if (isAdmin) Color.Red else Color.Green,
                        shape = CircleShape
                    )
                    .rotate(if (isAnimating) 360f else 0f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.aaa),
                    contentDescription = "用户头像",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 用户名
            Text(
                text = "Hello $name!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 用户类型徽章
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.Settings else Icons.Default.Person,
                    contentDescription = "用户类型",
                    tint = if (isAdmin) Color.Red else Color.Green,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAdmin) "管理员用户" else "普通用户",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isAdmin) Color.Red else Color.Green
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 学号
            Text(
                text = "22010169",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
fun FeatureButtonsGrid(
    onGameClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTextInputClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "功能菜单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 主要功能按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureButton(
                    icon = Icons.Default.PlayArrow,
                    text = "开始游戏",
                    onClick = onGameClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF4CAF50)
                )

                FeatureButton(
                    icon = Icons.Default.Info,
                    text = "游戏统计",
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF2196F3)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureButton(
                    icon = Icons.Default.Settings,
                    text = "设置",
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFFF9800)
                )

                FeatureButton(
                    icon = Icons.Default.Edit,
                    text = "文本输入",
                    onClick = onTextInputClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF9C27B0)
                )
            }
        }
    }
}

@Composable
fun FeatureButton(
    icon: ImageVector? = null,  // 让图标可选
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun TextInputCard(
    textState: MutableState<String>,
    onTextChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "文本输入区域",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = textState.value,
                onValueChange = onTextChange,
                label = { Text("请输入文本") },
                placeholder = { Text("最多两行文本") },
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "编辑")
                },
                trailingIcon = {
                    if (textState.value.isNotEmpty()) {
                        IconButton(onClick = { onTextChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GameStatsCard(
    username: String,
    isAdmin: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "统计",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "游戏统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem("总游戏", "0", Icons.Default.PlayArrow, Color(0xFF2196F3))
                StatItem("胜利", "0", Icons.Default.Star, Color(0xFF4CAF50))
                StatItem("胜率", "0%", Icons.Default.Info, Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun QuickActionsCard(
    isAdmin: Boolean,
    onUndoClick: () -> Unit,
    onRestartClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "快速操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isAdmin) {
                    ActionButton(
                        icon = Icons.Default.ArrowBack,
                        text = "悔棋",
                        onClick = onUndoClick,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFF9800)
                    )
                }

                ActionButton(
                    icon = Icons.Default.Refresh,
                    text = "重新开始",
                    onClick = onRestartClick,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            brush = Brush.horizontalGradient(listOf(color, color))
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

// 新增：游戏统计概览卡片
@Composable
fun GameStatsOverviewCard(username: String) {
    val context = LocalContext.current
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 修复数据加载逻辑 - 只统计人机对战
    LaunchedEffect(username) {
        isLoading = true
        try {
            val database = AppDatabase.getDatabase(context)
            
            // 直接获取游戏记录，不使用 Flow
            val allRecords = database.gameStatsDao().getUserGameRecords(username)
            
            // 使用 first() 获取第一个值，避免一直等待
            val records = allRecords.first()
            
            // 筛选出人机对战的记录
            val aiGameRecords = records.filter { record ->
                record.gameMode == "human_vs_ai" || 
                record.player2 == "AI"
            }
            
            Log.d("GameStatsOverview", "Found ${aiGameRecords.size} AI game records")
            aiGameRecords.forEach { record ->
                Log.d("GameStatsOverview", "Record: winner=${record.winner}, player1=${record.player1}, player2=${record.player2}, gameMode=${record.gameMode}")
            }
            
            // 计算人机对战的统计 - 修复胜负判断逻辑
            val totalGames = aiGameRecords.size
            val wins = aiGameRecords.count { record ->
                // 修复胜负判断逻辑 - 根据棋子颜色判断
                when {
                    record.winner == "black" && record.player1 == username -> true  // 黑棋胜利且用户是黑棋
                    record.winner == "white" && record.player1 == username -> false // 白棋胜利但用户是黑棋
                    record.winner == "black" && record.player2 == username -> false // 黑棋胜利但用户是白棋
                    record.winner == "white" && record.player2 == username -> true  // 白棋胜利且用户是白棋
                    record.winner == null -> false     // 平局算失败
                    else -> false
                }
            }
            val losses = aiGameRecords.count { record ->
                when {
                    record.winner == "black" && record.player1 == username -> false // 黑棋胜利且用户是黑棋
                    record.winner == "white" && record.player1 == username -> true  // 白棋胜利但用户是黑棋
                    record.winner == "black" && record.player2 == username -> true  // 黑棋胜利但用户是白棋
                    record.winner == "white" && record.player2 == username -> false // 白棋胜利且用户是白棋
                    record.winner == null -> true      // 平局算失败
                    else -> true
                }
            }
            val draws = aiGameRecords.count { record ->
                record.winner == null
            }
            val winRate = if (totalGames > 0) wins.toFloat() / totalGames else 0f
            
            Log.d("GameStatsOverview", "Calculated stats: totalGames=$totalGames, wins=$wins, losses=$losses, draws=$draws, winRate=$winRate")
            
            // 创建人机对战统计
            val aiStats = UserStats(
                username = username,
                totalGames = totalGames,
                wins = wins,
                losses = losses,
                draws = draws,
                winRate = winRate,
                totalPlayTime = aiGameRecords.sumOf { (it.endTime ?: 0L) - it.startTime },
                averageGameTime = if (totalGames > 0) aiGameRecords.sumOf { (it.endTime ?: 0L) - it.startTime } / totalGames else 0L,
                longestWinStreak = calculateWinStreak(aiGameRecords, username),
                currentWinStreak = calculateCurrentWinStreak(aiGameRecords, username),
                bestMoveCount = 0, // 暂时设为0，因为GameRecord没有moveCount字段
                lastPlayTime = aiGameRecords.maxOfOrNull { it.startTime } ?: 0L
            )
            
            userStats = aiStats
            Log.d("GameStatsOverview", "Final AI game stats for $username: $aiStats")
        } catch (e: Exception) {
            Log.e("GameStatsOverview", "Error loading AI game stats: ${e.message}")
            userStats = UserStats(username = username) // 使用默认值
        } finally {
            isLoading = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "人机对战统计",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "人机对战统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else {
                val stats = userStats ?: UserStats(username = username)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "总游戏",
                        value = "${stats.totalGames}",
                        icon = Icons.Default.PlayArrow,
                        color = Color(0xFF2196F3)
                    )
                    StatItem(
                        label = "胜利",
                        value = "${stats.wins}",
                        icon = Icons.Default.Star,
                        color = Color(0xFF4CAF50)
                    )
                    StatItem(
                        label = "胜率",
                        value = "${(stats.winRate * 100).toInt()}%",
                        icon = Icons.Default.Info,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

// 修复后的连胜计算
private fun calculateWinStreak(records: List<GameRecord>, username: String): Int {
    var maxStreak = 0
    var currentStreak = 0
    
    // 按时间排序（最新的在前）
    val sortedRecords = records.sortedByDescending { it.startTime }
    
    for (record in sortedRecords) {
        val isWin = when {
            record.winner == "black" && record.player1 == username -> true  // 黑棋胜利且用户是黑棋
            record.winner == "white" && record.player1 == username -> false // 白棋胜利但用户是黑棋
            record.winner == "black" && record.player2 == username -> false // 黑棋胜利但用户是白棋
            record.winner == "white" && record.player2 == username -> true  // 白棋胜利且用户是白棋
            record.winner == null -> false
            else -> false
        }
        
        if (isWin) {
            currentStreak++
            maxStreak = maxOf(maxStreak, currentStreak)
        } else {
            currentStreak = 0
        }
    }
    
    return maxStreak
}

// 修复后的当前连胜计算
private fun calculateCurrentWinStreak(records: List<GameRecord>, username: String): Int {
    var currentStreak = 0
    
    // 按时间排序（最新的在前）
    val sortedRecords = records.sortedByDescending { it.startTime }
    
    for (record in sortedRecords) {
        val isWin = when {
            record.winner == "black" && record.player1 == username -> true  // 黑棋胜利且用户是黑棋
            record.winner == "white" && record.player1 == username -> false // 白棋胜利但用户是黑棋
            record.winner == "black" && record.player2 == username -> false // 黑棋胜利但用户是白棋
            record.winner == "white" && record.player2 == username -> true  // 白棋胜利且用户是白棋
            record.winner == null -> false
            else -> false
        }
        
        if (isWin) {
            currentStreak++
        } else {
            break // 遇到失败就停止计算
        }
    }
    
    return currentStreak
}

// 修复后的 StatsDialog
@Composable
fun StatsDialog(
    onDismiss: () -> Unit,
    username: String
) {
    // 暂时显示待开发信息
    DevelopmentDialog(
        onDismiss = onDismiss,
        title = "游戏统计",
        description = "详细统计功能正在开发中，敬请期待！"
    )
}

@Composable
fun UserStatsOverview(
    username: String,
    stats: UserStats?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "用户",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "玩家：$username",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "总游戏",
                    value = "${stats?.totalGames ?: 0}",
                    icon = Icons.Default.PlayArrow,
                    color = Color(0xFF2196F3)
                )
                StatItem(
                    label = "胜利",
                    value = "${stats?.wins ?: 0}",
                    icon = Icons.Default.Star,
                    color = Color(0xFF4CAF50)
                )
                StatItem(
                    label = "失败",
                    value = "${stats?.losses ?: 0}",
                    icon = Icons.Default.Close,
                    color = Color(0xFFF44336)
                )
                StatItem(
                    label = "平局",
                    value = "${stats?.draws ?: 0}",
                    icon = Icons.Default.Info,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun DetailedStatsCard(
    stats: UserStats?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "详细统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format("%.1f", stats?.winRate ?: 0f)}%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "胜率",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats?.currentWinStreak ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                    Text(
                        text = "当前连胜",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats?.longestWinStreak ?: 0}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF9C27B0)
                    )
                    Text(
                        text = "最长连胜",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats?.averageGameTime ?: 0}秒",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    Text(
                        text = "平均游戏时间",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${stats?.bestMoveCount ?: 0}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "最佳步数",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RecentGamesCard(
    gameRecords: List<GameRecord>,
    username: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "最近游戏",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (gameRecords.isEmpty()) {
                Text(
                    text = "暂无游戏记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gameRecords.take(5)) { record ->
                        GameRecordItem(
                            record = record,
                            username = username
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameRecordItem(
    record: GameRecord,
    username: String
) {
    val isWin = record.winner == username
    val opponent = if (record.player1 == username) record.player2 else record.player1
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "vs $opponent",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "游戏时间：${record.startTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isWin) Icons.Default.Star else Icons.Default.Close,
                    contentDescription = if (isWin) "胜利" else "失败",
                    tint = if (isWin) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isWin) "胜利" else "失败",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isWin) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun AchievementsCard(
    stats: UserStats?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "成就系统",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val achievements = listOf(
                Achievement(
                    name = "初出茅庐",
                    description = "完成第一场游戏",
                    icon = Icons.Default.PlayArrow,
                    isUnlocked = (stats?.totalGames ?: 0) >= 1,
                    color = Color(0xFF4CAF50)
                ),
                Achievement(
                    name = "连胜达人",
                    description = "获得5连胜",
                    icon = Icons.Default.Star,
                    isUnlocked = (stats?.longestWinStreak ?: 0) >= 5,
                    color = Color(0xFFFF9800)
                ),
                Achievement(
                    name = "常胜将军",
                    description = "获得10连胜",
                    icon = Icons.Default.Star,
                    isUnlocked = (stats?.longestWinStreak ?: 0) >= 10,
                    color = Color(0xFF9C27B0)
                ),
                Achievement(
                    name = "游戏达人",
                    description = "完成50场游戏",
                    icon = Icons.Default.PlayArrow,
                    isUnlocked = (stats?.totalGames ?: 0) >= 50,
                    color = Color(0xFF2196F3)
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(achievements) { achievement ->
                    AchievementItem(achievement = achievement)
                }
            }
        }
    }
}

@Composable
fun AchievementItem(
    achievement: Achievement
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = achievement.icon,
            contentDescription = achievement.name,
            tint = if (achievement.isUnlocked) achievement.color else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (achievement.isUnlocked) Color.Black else Color.Gray
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (achievement.isUnlocked) Color.Gray else Color.LightGray
            )
        }
        
        if (achievement.isUnlocked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已解锁",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 数据类
data class Achievement(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean,
    val color: Color
)

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    isAdmin: Boolean
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "设置",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "设置功能开发中...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
fun GameModeDialog(
    onDismiss: () -> Unit,
    onModeSelected: (String, String) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择对战模式",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 人机对战模式
                Button(
                    onClick = { onModeSelected("human_vs_ai", "AI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "AI",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("人机对战")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 双人对战模式（与管理员对战）
                Button(
                    onClick = { onModeSelected("human_vs_admin", "管理员") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "管理员",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("与管理员对战")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 取消按钮
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }
}

@Composable
fun DevelopmentDialog(
    onDismiss: () -> Unit,
    title: String,
    description: String
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 图标 - 使用确实存在的图标
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "开发中",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 描述
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("知道了")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2Theme {
        Greeting("Android", false)
    }
}