package com.example.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.example.task.DataAbout.Companion.num
import com.example.task.DataAbout.Companion.sel
import com.example.task.DataAbout.Companion.work
import com.example.task.DateActivity.Companion.sdate
import com.example.task.MainActivity.Companion.spinner
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*

class MainActivity:AppCompatActivity() {

    lateinit var adapter:MyAdapter

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        spinner=findViewById(R.id.spinner)
        spinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent:AdapterView<*>,view:View,position:Int,id:Long
            ) {
                sel=spinner.getSelectedItem().toString()
                num=position
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent:AdapterView<*>) {
                spinner.setSelection(0)
            }
        }

        work.load(this)
        adapter=MyAdapter(work.main,this)
        val list:ListView=findViewById(R.id.mainView)
        list.adapter=adapter

        val add:FloatingActionButton=findViewById(R.id.add)
        add.setOnClickListener {
            edit(it,-1)
        }

        val more:TextView=findViewById(R.id.more)
        more.setOnClickListener {
            val intent=Intent(this,DateActivity::class.java)
            startActivity(this,intent,null)
        }
    }

    override fun onStart() {
        super.onStart()
        mode=false
        work.uniq(this)
        spinner.setSelection(num)
        adapter.notifyDataSetChanged()
    }

    companion object {
        lateinit var spinner:Spinner
        var mode=false
        fun edit(view:View,pos:Int) {
            val intent=Intent(view.context,EditActivity::class.java)
            intent.putExtra("pos",pos)
            startActivity(view.context,intent,null)
        }
    }

    class MyAdapter(val main:MutableList<TaskData>,val context:Context):BaseAdapter() {

        override fun getCount():Int {
            return main.size
        }

        override fun getItem(p0:Int):Any {
            return main[p0]
        }

        override fun getItemId(p0:Int):Long {
            return p0.toLong()
        }

        override fun getView(p0:Int,p1:View?,p2:ViewGroup?):View {
            val view:View?
            val task=getItem(p0) as TaskData
            if (mode) {
                if (task.time=="never"||task.time.substring(0..9)!=sdate) {
                    view=LayoutInflater.from(context).inflate(R.layout.none_list,p2,false)
                    return view
                }
                else {
                    view=LayoutInflater.from(context).inflate(R.layout.task_list,p2,false)
                }
            }
            else {
                if (sel!="ALL"&&task.group!=sel) {
                    view=LayoutInflater.from(context).inflate(R.layout.none_list,p2,false)
                    return view
                }
                else {
                    view=LayoutInflater.from(context).inflate(R.layout.task_list,p2,false)
                }
            }

            val uitem=view!!.findViewById(R.id.item) as TextView
            val uinfo=view.findViewById(R.id.info) as TextView
            val ugroup=view.findViewById(R.id.group) as TextView
            val ulevel=view.findViewById(R.id.level) as TextView
            val utime=view.findViewById(R.id.time) as TextView
            val udone=view.findViewById(R.id.done) as CheckBox
            val umore=view.findViewById(R.id.more) as TextView
            val unone=view.findViewById(R.id.none) as TextView

            uitem.text=task.item
            uinfo.text=task.info
            ugroup.text=task.group
            ulevel.text=task.level.toString()
            utime.text=task.time
            udone.isChecked=task.done
            udone.setOnClickListener {
                val builder=AlertDialog.Builder(it.context)
                if (!main[p0].done) {
                    builder.setMessage("确定完成？").setCancelable(false)
                        .setPositiveButton("Yes") {dialog,id->
                            main[p0].done=true
                            work.main.sort()
                            work.save(it.context)
                            notifyDataSetChanged()
                        }.setNegativeButton("No") {dialog,id->
                            udone.isChecked=false
                            dialog.dismiss()
                        }
                }
                else {
                    builder.setMessage("确定取消？").setCancelable(false)
                        .setPositiveButton("Yes") {dialog,id->
                            main[p0].done=false
                            work.main.sort()
                            work.save(it.context)
                            notifyDataSetChanged()
                        }.setNegativeButton("No") {dialog,id->
                            udone.isChecked=true
                            dialog.dismiss()
                        }
                }
                val alert=builder.create()
                alert.show()
            }
            umore.setOnClickListener {
                edit(it,p0)
            }
            unone.setOnClickListener {
                val builder=AlertDialog.Builder(it.context)
                builder.setMessage("确定删除？").setCancelable(false)
                    .setPositiveButton("Yes") {dialog,id->
                        main.removeAt(p0)
                        work.main.sort()
                        work.save(it.context)
                        work.uniq(it.context)
                        spinner.setSelection(num)
                        notifyDataSetChanged()
                    }.setNegativeButton("No") {dialog,id->
                        dialog.dismiss()
                    }
                val alert=builder.create()
                alert.show()
            }
            return view
        }

    }

}

class TaskData(
    var item:String="",
    var info:String="",
    var group:String="",
    var level:Int=1,
    var time:String="",
    var done:Boolean=false
):Comparable<TaskData> {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun compareTo(other:TaskData):Int {
        if (done>other.done) {
            return 1
        }
        else if (done<other.done) {
            return -1
        }
        if (level>other.level) {
            return -1
        }
        else if (level<other.level) {
            return 1
        }
        return this.compare(other.time)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun compare(time2:String):Int {
        if (time=="never") {
            if (time2=="never") {
                return 0
            }
            else {
                return 1
            }
        }
        else if (time2=="never") {
            return -1
        }
        val format=SimpleDateFormat("yyyy/MM/dd HH:mm")
        val date:Date=format.parse(time)
        val date2:Date=format.parse(time2)
        return date.compareTo(date2)
    }
}

class DataAbout:AppCompatActivity() {

    companion object {
        val work=DataAbout()
        var sel="ALL"
        var num=0
    }

    val main=mutableListOf<TaskData>()

    fun uniq(context:Context) {
        var typ=mutableListOf("ALL")
        for (i in main) {
            typ.add(i.group)
        }
        typ=typ.distinct().toMutableList()
        num=typ.indexOf(sel)
        if (num==-1) {
            num=0
        }
        val adapter=ArrayAdapter(
            context,R.layout.menu_show,typ
        )
        adapter.setDropDownViewResource(R.layout.item_show)
        spinner.adapter=adapter
    }

    fun load(context:Context) {
        main.clear()
        val data=read(context)
        var use=TaskData()
        var how=0
        var tmp=""
        for (i in 0 until data.length) {
            if (data[i]=='$') {
                when (how) {
                    0->use.item=tmp
                    1->use.info=tmp
                    2->use.group=tmp
                    3->use.level=tmp.toInt()
                    4->use.time=tmp
                    5-> {
                        use.done=tmp=="Y"
                        main.add(use)
                        use=TaskData()
                        how=-1
                    }
                }
                tmp=""
                how++
            }
            else {
                tmp+=data[i]
            }
        }
        uniq(context)
    }

    fun save(context:Context) {
        var data=""
        for (i in 0 until main.size) {
            data+=main[i].item+"$"+main[i].info+"$"+main[i].group+"$"+main[i].level.toString()+"$"
            data+=main[i].time+"$"+when (main[i].done) {
                true->"Y"
                else->"N"
            }+"$"
        }
        rite(context,data)
    }

    fun read(context:Context):String {
        val fileInputStream:FileInputStream
        try {
            fileInputStream=context.openFileInput("task.dat")
        }
        catch (e:Exception) {
            e.printStackTrace()
            return ""
        }
        val bufferedReader=BufferedReader(InputStreamReader(fileInputStream))
        val stringBuilder:StringBuilder=StringBuilder()
        var text:String?=null
        while ({text=bufferedReader.readLine(); text}()!=null) {
            stringBuilder.append(text+"\n")
        }
        return stringBuilder.toString().dropLast(1)
    }

    fun rite(context:Context,data:String) {
        val fileOutputStream:FileOutputStream
        try {
            fileOutputStream=context.openFileOutput("task.dat",Context.MODE_PRIVATE)
            fileOutputStream.write(data.toByteArray())
        }
        catch (e:Exception) {
            e.printStackTrace()
        }
    }

}

class DateTime(val textView:TextView) {

    init {
        textView.setOnClickListener {
            onDateSet()
        }
    }

    var date:String=""
    var time:String=""

    fun onDateSet() {
        val calendar:Calendar=Calendar.getInstance()
        val day=calendar.get(Calendar.DAY_OF_MONTH)
        val month=calendar.get(Calendar.MONTH)
        val year=calendar.get(Calendar.YEAR)
        val datePickerDialog=
            DatePickerDialog(textView.context,object:DatePickerDialog.OnDateSetListener {
                override fun onDateSet(view:DatePicker?,year:Int,month:Int,dayOfMonth:Int) {
                    date=String.format("%d/%02d/%02d",year,month+1,dayOfMonth)
                    onTimeSet()
                }
            },year,month,day)
        datePickerDialog.setButton(
            DatePickerDialog.BUTTON_NEGATIVE,null,null as DialogInterface.OnClickListener?
        )
        datePickerDialog.show()
    }

    fun onTimeSet() {
        val calendar:Calendar=Calendar.getInstance()
        val hour=calendar.get(Calendar.HOUR_OF_DAY)
        val minute=calendar.get(Calendar.MINUTE)
        val timePickerDialog=
            TimePickerDialog(textView.context,object:TimePickerDialog.OnTimeSetListener {
                override fun onTimeSet(view:TimePicker?,hourOfDay:Int,minute:Int) {
                    time=String.format("%02d:%02d",hourOfDay,minute)
                    onValueSet()
                }
            },hour,minute,true)
        timePickerDialog.setButton(
            TimePickerDialog.BUTTON_NEGATIVE,null,null as DialogInterface.OnClickListener?
        )
        timePickerDialog.show()
    }

    fun onValueSet() {
        textView.text="$date $time"
    }
}