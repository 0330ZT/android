package com.example.project2

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2.ui.theme.Project2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() //让应用内容延伸到状态栏和导航栏区域
        setContent {
            Project2Theme { //应用主题  
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding -> //用于避开系统UI
                    Greeting(
                        name = "Android", //传递给Greeting组件的参数
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable     //构建UI
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val textState = remember { mutableStateOf("") }
    
    Column(
        modifier = modifier.fillMaxSize(),//填充父容器的全部可用空间
        horizontalAlignment = Alignment.CenterHorizontally,//水平居中
        verticalArrangement = Arrangement.Center  //垂直居中
    ) {
        // 添加ImageView组件，显示本地图片
        Image(
            painter = painterResource(id = R.drawable.aaa), // 图片放到drawable目录下.名字是实际的图片名字，去掉后缀
            contentDescription = "微信头像",
            modifier = Modifier
                .size(120.dp)   //图片的尺寸
                .padding(16.dp),  //图片的内边距
            contentScale = ContentScale.Fit  //保持图片比例
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
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