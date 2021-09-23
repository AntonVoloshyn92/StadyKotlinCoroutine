package com.anton.stadykotlincoroutine

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.*

class JobActivity : AppCompatActivity() {

    private val PROGRESS_MAX = 100
    private val PROGRESS_START = 0
    private val TIME_JOB = 4_000
    private lateinit var job: CompletableJob
    private lateinit var jobButton: Button
    private lateinit var jobProgressBar: ProgressBar
    private lateinit var jobCompleteText: TextView
    private val TAG = "Job Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job)
        initUI()

        jobButton.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }
            jobProgressBar.startJobOrCancel(job)
        }
    }

    private fun initUI() {
        jobButton = findViewById(R.id.job_button)
        jobProgressBar = findViewById(R.id.job_progress_bar)
        jobCompleteText = findViewById(R.id.job_complete_text)
    }

    fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            Log.d(TAG, "${job} is already active. Cancelling...")
            jobButton.text = getString(R.string.start_job)
            resetJob()
        } else {
            jobButton.text = getString(R.string.cancel_job)
            CoroutineScope(Dispatchers.IO + job).launch {
                Log.d(TAG, "coroutin ${this} is activaited with job")

                for (i in PROGRESS_START..PROGRESS_MAX) {
                    delay((TIME_JOB / PROGRESS_MAX).toLong())
                    this@startJobOrCancel.progress = i
                }

                updateJobCompleteTextView("Job is complete")
            }
        }
    }

    private fun updateJobCompleteTextView(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            jobCompleteText.text = text
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted) {
            job.cancel(CancellationException("Resetting Job"))
        }
        initJob()
    }

    private fun initJob() {
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "Unknown cancellation error"
                }
                Log.d(TAG, "${job} was cancelled. Reason: $msg")
                showToast(msg)
            }
        }
        jobProgressBar.max = PROGRESS_MAX
        jobProgressBar.progress = PROGRESS_START
    }

    private fun showToast(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@JobActivity, text, Toast.LENGTH_LONG).show()
        }
    }
}