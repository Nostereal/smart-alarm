package com.tema.smartalarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.tema.smartalarm.utils.getTimeFromText
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class
MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private val travelMode: List<String> = listOf("Driving", "Walking", "Transit")

    private val FILE_NAME = "data"

//    private var job: Job? = null

//    private val uiScope = CoroutineScope(Dispatchers.Main)

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

        val departAddress = departAddressEditText.text.toString()
        val destAddress = destAddressEditText.text.toString()
        val departTime = departureTimePicker.text.toString()

        val broadcastIntent = Intent(this, Receiver::class.java)
        with(broadcastIntent) {
            putExtra("mode", mode)
            putExtra("departAddress", departAddress)
            putExtra("destAddress", destAddress)
            putExtra("departTime", departTime)
            putExtra("alarmTime", alarmTimeTV.text.toString())
        }
        val pIntent = PendingIntent.getBroadcast(
            this,
            0,
            broadcastIntent,
            0
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        departureTimePicker.setOnClickListener {
            timePicker(this, it as TextView)
        }

        alarmTimeTV.setOnClickListener {
            timePicker(this, it as TextView)
        }

        doneButton.setOnClickListener {
            val standardAlarmInMillis = getTimeFromText(alarmTimeTV.text.toString().split(": ")[1]).timeInMillis
            val hourInMillis = 1000L * 60 * 60
            val dayInMillis = hourInMillis * 24L

            alarmManager.setInexactRepeating(
                AlarmManager.RTC,
                standardAlarmInMillis - hourInMillis,
                dayInMillis,
                pIntent
            )

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
//        job?.cancel()
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


}
