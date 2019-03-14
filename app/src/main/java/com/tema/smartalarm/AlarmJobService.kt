package com.tema.smartalarm

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AlarmJobService : JobService() {

    companion object {
        private const val TAG = "AlarmJobService"
    }

    private var jobCancelled: Boolean = false
    private lateinit var job: Job

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started")
        doBackgroundWork(params)

        return true
    }

    private fun doBackgroundWork(params: JobParameters?) {
        job = GlobalScope.launch {
            if (jobCancelled)
                return@launch

            // TODO: start program before alarm and change alarm time if needed

            Log.d(TAG, "Job finished")
            jobFinished(params, true)
        }
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job cancelled before completion")
        jobCancelled = true
        job.cancel()

        return true
    }
}