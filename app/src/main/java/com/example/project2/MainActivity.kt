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
import kotlinx.coroutines.delay

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

            // 功能按钮网格
            item {
                FeatureButtonsGrid(
                    onGameClick = { showGameModeDialog = true },
                    onStatsClick = { showStatsDialog = true },
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

            // 游戏统计卡片
            item {
                GameStatsCard(
                    username = name,
                    isAdmin = isAdmin
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
                StatItem("总游戏", "0", Icons.Default.PlayArrow)
                StatItem("胜利", "0", Icons.Default.Star)
                StatItem("胜率", "0%", Icons.Default.Info)
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF4CAF50),
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

@Composable
fun StatsDialog(
    onDismiss: () -> Unit,
    username: String
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
                    text = "游戏统计",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "统计功能开发中...",
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2Theme {
        Greeting("Android", false)
    }
}