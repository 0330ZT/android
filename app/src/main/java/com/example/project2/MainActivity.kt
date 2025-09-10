package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 添加ImageView组件，显示本地图片
        Image(
            painter = painterResource(id = R.drawable.aaa),
            contentDescription = "微信头像",
            modifier = Modifier
                .size(120.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hello $name!",
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 显示用户类型
        Text(
            text = if (isAdmin) "管理员用户" else "普通用户",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAdmin) Color.Red else Color.Green,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 添加显示"22010169"的TextView
        Text(
            text = "22010169",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 添加EditText控件（使用OutlinedTextField）
        OutlinedTextField(
            value = textState.value,
            onValueChange = { textState.value = it },
            label = { Text("请输入文本") },
            placeholder = { Text("最多两行文本") },
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 添加Button控件
        Button(
            onClick = {
                val inputText = textState.value
                if (inputText.isNotEmpty()) {
                    Toast.makeText(context, "输入的内容：$inputText", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "请输入一些内容", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("显示输入内容")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 开始五子棋游戏按钮
        Button(
            onClick = {
                showGameModeDialog = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp)
        ) {
            Text("开始五子棋游戏")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 显示权限信息
        Text(
            text = if (isAdmin) "管理员权限：可以悔棋" else "普通用户：无法悔棋",
            fontSize = 12.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
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
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "选择对战模式",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 人机对战模式
                Button(
                    onClick = { onModeSelected("human_vs_ai", "AI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("人机对战")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 双人对战模式（与管理员对战）
                Button(
                    onClick = { onModeSelected("human_vs_admin", "管理员") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
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