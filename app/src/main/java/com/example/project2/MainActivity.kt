package com.example.project2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.ui.theme.Project2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Project2Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val textState = remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hello $name!",
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 添加显示"22010169"的TextView
        Text(
            text = "22010169",
            fontSize = 24.sp,// 字体大小，单位sp
            fontWeight = FontWeight.Bold,//字体粗体
            color = Color.Blue,//文本颜色蓝色
            textAlign = TextAlign.Center,//居中对齐
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
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Project2Theme {
        Greeting("Android")
    }
}