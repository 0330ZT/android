package com.example.project2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.project2.ui.theme.Project2Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2Theme {
                LoginScreen(
                    onLoginSuccess = { username, isAdmin ->
                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra("username", username)
                            putExtra("isAdmin", isAdmin)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 状态管理
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var loginMode by remember { mutableStateOf("user") } // "user" or "admin"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // 登录界面图片
            Image(
                painter = painterResource(id = R.drawable.aaa),
                contentDescription = "登录界面图片",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 登录模式选择 (管理员/用户)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("登录模式:", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    Modifier
                        .selectable(
                            selected = (loginMode == "user"),
                            onClick = { loginMode = "user" }
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (loginMode == "user"),
                        onClick = null
                    )
                    Text(text = "用户")
                }
                Row(
                    Modifier
                        .selectable(
                            selected = (loginMode == "admin"),
                            onClick = { loginMode = "admin" }
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (loginMode == "admin"),
                        onClick = null
                    )
                    Text(text = "管理员")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 登录按钮
            Button(
                onClick = {
                    isLoading = true
                    progress = 0f
                    scope.launch {
                        var loginSuccess = false
                        var loggedInUsername = ""
                        var isAdmin = false

                        // 模拟进度条
                        val totalTime = 1000L // 1秒
                        val steps = 100
                        for (i in 1..steps) {
                            progress = i / steps.toFloat()
                            delay(totalTime / steps)
                        }

                        // 简单的登录验证
                        when {
                            loginMode == "admin" && username == "admin" && password == "123456" -> {
                                loginSuccess = true
                                loggedInUsername = username
                                isAdmin = true
                            }
                            loginMode == "user" && username == "user" && password == "123456" -> {
                                loginSuccess = true
                                loggedInUsername = username
                                isAdmin = false
                            }
                            else -> {
                                loginSuccess = false
                            }
                        }

                        isLoading = false
                        if (loginSuccess) {
                            Toast.makeText(context, "登录成功！", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(loggedInUsername, isAdmin)
                        } else {
                            showErrorDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("登录")
            }
        }

        // 登录进度条 (覆盖在内容上方)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(80.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 8.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "登录中...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // 错误弹窗
        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                title = { Text("登录失败") },
                text = { Text("用户名或密码错误，或者登录模式不匹配。\n\n默认账户：\n管理员：admin/123456\n用户：user/123456") },
                confirmButton = {
                    Button(onClick = { showErrorDialog = false }) {
                        Text("确定")
                    }
                }
            )
        }
    }
}