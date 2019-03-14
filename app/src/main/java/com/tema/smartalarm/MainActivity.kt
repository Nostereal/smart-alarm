package com.tema.smartalarm

import android.app.TimePickerDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val travelMode: List<String> = listOf("Driving", "Walking", "Transit")

    private val FILE_NAME = "data"

    private var job: Job? = null

    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

        departAddressEditText.setText(prefs.getString("Departure Address", "800-летия Москвы, 28к1"))
        destAddressEditText.setText(prefs.getString("Destination Address", "Прянишникова, 2а"))
        departureTimePicker.text = prefs.getString("Departure Time", "0:00")
        alarmTimeTV.text = prefs.getString("Basic Alarm", "Basic alarm: 7:30")
        val modeIndexForSpinner = prefs.getInt("Travel Mode Index", 2)

        var mode = travelMode[2]
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, travelMode)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        travelModeSpinner.adapter = adapter
        travelModeSpinner.setSelection(modeIndexForSpinner)
        travelModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                mode = travelMode[position]
            }

        }

        departureTimePicker.setOnClickListener {
            timePicker(this, it as TextView)
        }

        alarmTimeTV.setOnClickListener {
            timePicker(this, it as TextView)
        }

        doneButton.setOnClickListener {
            val departAddress = departAddressEditText.text.toString()
            val destAddress = destAddressEditText.text.toString()
            val departTime = departureTimePicker.text.toString()

            job = uiScope.launch {
                val coordsListDef = async(Dispatchers.IO) { GeocoderApi().getCoordsList(departAddress, destAddress) }
                val coordsList = coordsListDef.await()

                val tripDurationDef = async(Dispatchers.IO) { DistMatrixApi().getTripDuration(departTime, coordsList, mode) }
                val tripDuration = tripDurationDef.await()
                Log.d(TAG, "$tripDuration")
            }

            val editor = prefs.edit()
            with(editor) {
                putString("Departure Address", departAddressEditText.text.toString())
                putString("Destination Address", destAddressEditText.text.toString())
                putString("Departure Time", departureTimePicker.text.toString())
                putString("Basic Alarm", alarmTimeTV.text.toString())
                putInt("Travel Mode Index", travelMode.indexOf(mode))
                apply()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    private fun timePicker(context: Context, textView: TextView) {
        val calendar = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener {_: TimePicker?,
                                                                  hourOfDay: Int,
                                                                  minute: Int ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            textView.text = SimpleDateFormat("HH:mm").format(calendar.time)
        }
        TimePickerDialog(context, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
    }

    fun scheduleJob(v: View) {
        val componentName = ComponentName(this, AlarmJobService::class.java)
        val info = JobInfo.Builder(0, componentName)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NOT_ROAMING)
            .build()

        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val resultCode = scheduler.schedule(info)
        if (resultCode == JobScheduler.RESULT_SUCCESS)
            Log.d(TAG, "Job  scheduled")
        else
            Log.d(TAG, "Job scheduling failed")
    }

    fun cancelJob(v: View) {
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(0)
        Log.d(TAG, "Job cancelled")
    }
}
