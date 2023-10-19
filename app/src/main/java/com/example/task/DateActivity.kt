package com.example.task

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.CalendarView
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.task.MainActivity.Companion.mode
import java.util.*

class DateActivity:AppCompatActivity() {

    companion object {
        var sdate:String=""
    }

    var adapter=MainActivity.MyAdapter(DataAbout.work.main,this)

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date)

        mode=true
        val list:ListView=findViewById(R.id.dateView)
        list.adapter=adapter

        val calendar:CalendarView=findViewById(R.id.calendar)
        calendar.setOnDateChangeListener {calendarView,i,i1,i2->
            sdate=String.format("%d/%02d/%02d",i,i1+1,i2)
            adapter.notifyDataSetChanged()
        }

        val uquit:Button=findViewById(R.id.quit)
        uquit.setOnClickListener {finish()}

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onStart() {
        super.onStart()
        sdate=SimpleDateFormat("yyyy/MM/dd").format(Date())
        adapter.notifyDataSetChanged()
    }

}