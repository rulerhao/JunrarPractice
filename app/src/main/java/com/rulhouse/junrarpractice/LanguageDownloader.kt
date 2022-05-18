package com.rulhouse.junrarpractice

import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import com.github.junrar.Junrar
import com.github.junrar.exception.RarException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class LanguageDownloader {
    private var onDownloadVoiceListener: OnDownloadVoiceListener? = null

    interface OnDownloadVoiceListener {
        fun onStart()
        fun onSuccess()
        fun onNetworkError()
    }

    fun setOnDownloadListener(onDownloadVoiceListener: OnDownloadVoiceListener) {
        this.onDownloadVoiceListener = onDownloadVoiceListener
    }

    suspend fun extractFromAFileToADirectory(fileName: String, context: Context) {
        try {
            onDownloadVoiceListener?.onStart()
            downloadRar(fileName = fileName, context = context)
            extractRar(fileName = fileName, context = context)
            onDownloadVoiceListener?.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("TestDownload", "Download Error")
            onDownloadVoiceListener?.onNetworkError()
        }
    }

    private fun getRarFileDir(context: Context): String {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path + "/RarFolder"
    }

    private fun createFolder(dir: String) {
        val files = File(dir)
        if (files.exists() && files.isDirectory) {
            Log.d("TestDownload", "Folder exist")
        } else {
            if (!files.exists()) {
                Log.w("TestDownload", "Folder not exists ")
            }
            if (!files.mkdirs()) {
                Log.e("TestDownload", "Folder could not create ")
            }
        }
    }

    private suspend fun downloadRar(fileName: String, context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        val tempDir = getRarFileDir(context)
        createFolder(tempDir)
        val url = URL("https://oucarevoice.s3.amazonaws.com/$fileName")
        val fileDir = tempDir

        val input = BufferedInputStream(url.openStream())
        val output = FileOutputStream("$fileDir/$fileName")

        val data = ByteArray(1024)
        var count: Int
        while (input.read(data, 0, 1024).also {
                count = it
            } != -1) {
            if (!ni!!.isConnected) {
                output.close()
                input.close()
                break
            }
            output.write(data, 0, count)
        }

        output.flush()
        output.close()
        input.close()
    }

    private suspend fun extractRar(fileName: String, context: Context) {
        val saveDir =
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.path
        if (saveDir != null) {
            createFolder(saveDir)
        }
        Junrar.extract("${getRarFileDir(context)}/$fileName", saveDir)
    }
}