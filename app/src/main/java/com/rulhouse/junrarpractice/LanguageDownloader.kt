package com.rulhouse.junrarpractice

import android.content.Context
import android.net.ConnectivityManager
import android.os.Environment
import android.util.Log
import com.github.junrar.Junrar
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

class LanguageDownloader {
    private var onDownloadVoiceListener: OnDownloadVoiceListener? = null

    private var isStop = false

    interface OnDownloadVoiceListener {
        fun onStart()
        fun onSuccess()
        fun onNetworkError()
        fun onProgressUpdate(nowSize: Long, size: Long)
        fun onStop()
    }

    fun setOnDownloadListener(onDownloadVoiceListener: OnDownloadVoiceListener) {
        this.onDownloadVoiceListener = onDownloadVoiceListener
    }

    fun stop() {
        isStop = true
    }

    fun extractFromAFileToADirectory(fileName: String, context: Context) {
        try {
            onDownloadVoiceListener?.onStart()
            if (isStop) {
                onDownloadVoiceListener?.onStop()
                return
            }
            downloadRar(fileName = fileName, context = context)
            if (isStop) {
                onDownloadVoiceListener?.onStop()
                return
            }
            extractRar(fileName = fileName, context = context)
            if (isStop) {
                onDownloadVoiceListener?.onStop()
                return
            } else {
                onDownloadVoiceListener?.onSuccess()
            }
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

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun downloadRar(fileName: String, context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = cm.activeNetworkInfo
        val tempDir = getRarFileDir(context)
        createFolder(tempDir)
        val url = URL("https://oucarevoice.s3.amazonaws.com/$fileName")
        val size = getFileSize(url)
        val fileDir = tempDir

        val input = BufferedInputStream(url.openStream())
        val output = FileOutputStream("$fileDir/$fileName")

        val data = ByteArray(1024)
        var count: Int
        var nowSize: Long = 0

        while (input.read(data, 0, 1024).also {
                count = it
                nowSize += it
            } != -1) {
            onDownloadVoiceListener?.onProgressUpdate(size = size, nowSize = nowSize)
            if (!ni!!.isConnected) {
                output.close()
                input.close()
                break
            }
            if (isStop) break
            output.write(data, 0, count)
        }

        output.flush()
        output.close()
        input.close()
    }

    private fun extractRar(fileName: String, context: Context) {
        val saveDir =
            context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.path
        if (saveDir != null) {
            createFolder(saveDir)
        }
        Junrar.extract("${getRarFileDir(context)}/$fileName", saveDir)
    }

    private fun stopEvent() {
        onDownloadVoiceListener?.onStop()
    }

    private fun getFileSize(url: URL): Long {
        val connection: URLConnection = url.openConnection()
        connection.connect()

        return connection.contentLengthLong
    }
}