package com.example

import android.app.Application
import android.util.Log

class ApisSmartApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashHandler()
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("ApisSmartCrash", "Uncaught exception on thread ${thread.name}", throwable)
                val prefs = getSharedPreferences("apis_smart_crash_logs", MODE_PRIVATE)
                val time = System.currentTimeMillis()
                val log = "[$time] ${throwable.javaClass.simpleName}: ${throwable.message}\n${Log.getStackTraceString(throwable)}"
                prefs.edit().putString("last_crash_log", log).putLong("last_crash_time", time).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
