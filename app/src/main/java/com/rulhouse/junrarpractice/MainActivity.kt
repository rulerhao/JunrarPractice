package com.rulhouse.junrarpractice

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.rulhouse.junrarpractice.ui.theme.JunrarPracticeTheme
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JunrarPracticeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }
}

val languageDownloader = LanguageDownloader()

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun Greeting(name: String) {
    val context = LocalContext.current

    val downloadState = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Button(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                onClick = {
                    languageDownloader.stop()
                }
            ) {
                Text("Stop")
            }
            Row(
            ) {
                Text(text = "State: ")
                Text(text = downloadState.value)
            }
        }
    }

    val onDownloadVoiceListener = object :
        LanguageDownloader.OnDownloadVoiceListener {
        override fun onStart() {
            Log.d("TestDownload", "onStart")
            downloadState.value = "onStart"
        }

        override fun onSuccess() {
            Log.d("TestDownload", "onSuccess")
            downloadState.value = "onSuccess"
        }

        override fun onNetworkError() {
            Log.d("TestDownload", "onNetworkError")
            downloadState.value = "onNetworkError"
        }

        override fun onProgressUpdate(nowSize: Long, size: Long) {
            Log.d("TestDownload", "onProgressUpdate")
            downloadState.value = "$nowSize / $size"
        }

        override fun onStop() {
            Log.d("TestDownload", "onStop")
            downloadState.value = "onStop"
        }
    }

    LaunchedEffect(key1 = true) {
        GlobalScope.launch {
            val fileName = "zhs_sichuan.rar"
            languageDownloader.setOnDownloadListener(onDownloadVoiceListener)
            languageDownloader.extractFromAFileToADirectory(fileName, context)
        }
    }
}