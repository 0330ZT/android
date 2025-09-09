package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.data.AppDatabase
import com.example.project2.data.User
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onLoginSuccess = { user ->
                            // 登录成功后跳转到MainActivity
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            intent.putExtra("username", user.username)
                            intent.putExtra("isAdmin", user.isAdmin)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (User) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 状态管理
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var loginMode by remember { mutableStateOf("user") } // "user" 或 "admin"
    var showRegistrationDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // 初始化数据库数据
    LaunchedEffect(Unit) {
        scope.launch {
            val database = AppDatabase.getDatabase(context)
            
            // 检查是否已有数据，如果没有则添加默认数据
            val userCount = database.userDao().getAllUsers().size
            if (userCount == 0) {
                // 添加默认管理员账户
                val adminUser = User(
                    username = "admin",
                    password = "123456",
                    isAdmin = true,
                    nickname = "管理员"
                )
                database.userDao().insertUser(adminUser)
                
                // 添加默认普通用户账户
                val normalUser = User(
                    username = "user",
                    password = "123456",
                    isAdmin = false,
                    nickname = "普通用户"
                )
                database.userDao().insertUser(normalUser)
                
                // 添加一些示例好友关系
                val friend1 = com.example.project2.data.Friend(
                    username = "user",
                    friendUsername = "admin",
                    friendNickname = "管理员",
                    friendAvatar = ""
                )
                val friend2 = com.example.project2.data.Friend(
                    username = "admin",
                    friendUsername = "user",
                    friendNickname = "普通用户",
                    friendAvatar = ""
                )
                database.userDao().insertFriend(friend1)
                database.userDao().insertFriend(friend2)
            }
        }
    }
    
    // 处理登录成功的情况
    LaunchedEffect(isLoading) {
        if (isLoading) {
            // 模拟进度条动画
            for (i in 0..100) {
                progress = i.toFloat()
                delay(10) // 每10ms更新一次进度
            }
            
            // 1秒后检查登录结果
            delay(1000)
            
            scope.launch {
                val database = AppDatabase.getDatabase(context)
                val user = database.userDao().getUser(username, password)
                
                if (user != null) {
                    // 检查登录模式匹配
                    if ((loginMode == "admin" && user.isAdmin) || 
                        (loginMode == "user" && !user.isAdmin)) {
                        onLoginSuccess(user)
                    } else {
                        isLoading = false
                        progress = 0f
                        errorMessage = "登录模式不匹配，请选择正确的登录模式"
                        showErrorDialog = true
                    }
                } else {
                    isLoading = false
                    progress = 0f
                    errorMessage = "用户名或密码错误"
                    showErrorDialog = true
                }
            }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 应用图标
        Image(
            painter = painterResource(id = R.drawable.aaa),
            contentDescription = "应用图标",
            modifier = Modifier
                .size(100.dp)
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 标题
        Text(
            text = "用户登录",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 登录模式选择
        Text(
            text = "选择登录模式",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 普通用户模式
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (loginMode == "user"),
                        onClick = { loginMode = "user" }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (loginMode == "user"),
                    onClick = { loginMode = "user" }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("普通用户")
            }
            
            // 管理员模式
            Row(
                modifier = Modifier
                    .selectable(
                        selected = (loginMode == "admin"),
                        onClick = { loginMode = "admin" }
                    )
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (loginMode == "admin"),
                    onClick = { loginMode = "admin" }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("管理员")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 用户名输入框
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("用户名") },
            placeholder = { Text("请输入用户名") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 密码输入框
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密码") },
            placeholder = { Text("请输入密码") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true,
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 按钮行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 注册按钮
            OutlinedButton(
                onClick = { showRegistrationDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Text("注册")
            }
            
            // 登录按钮
            Button(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        isLoading = true
                    } else {
                        Toast.makeText(context, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "登录中...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "登录",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // 可视化进度条（当isLoading为true时显示）
        if (isLoading) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 线性进度条
                LinearProgressIndicator(
                    progress = progress / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 进度百分比文本
                Text(
                    text = "${progress.toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 状态文本
                Text(
                    text = if (progress < 100) "正在验证登录信息..." else "登录成功，即将跳转...",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 提示信息
        Text(
            text = "测试账号：\n管理员：admin / 123456\n普通用户：user / 123456",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
    
    // 注册对话框
    if (showRegistrationDialog) {
        RegistrationDialog(
            onDismiss = { showRegistrationDialog = false },
            onRegister = { newUser ->
                scope.launch {
                    val database = AppDatabase.getDatabase(context)
                    database.userDao().insertUser(newUser)
                    Toast.makeText(context, "注册成功！", Toast.LENGTH_SHORT).show()
                    showRegistrationDialog = false
                }
            }
        )
    }
    
    // 错误弹窗
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "登录失败",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showErrorDialog = false
                        username = ""
                        password = ""
                    }
                ) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun RegistrationDialog(
    onDismiss: () -> Unit,
    onRegister: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "用户注册",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("确认密码") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("昵称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAdmin,
                        onCheckedChange = { isAdmin = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("管理员账户")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isNotEmpty() && password.isNotEmpty() && 
                        password == confirmPassword) {
                        val newUser = User(
                            username = username,
                            password = password,
                            isAdmin = isAdmin,
                            nickname = nickname.ifEmpty { username }
                        )
                        onRegister(newUser)
                    }
                }
            ) {
                Text("注册")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Project2Theme {
        LoginScreen()
    }
}