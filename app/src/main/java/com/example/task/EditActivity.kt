package com.example.task

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.task.DataAbout.Companion.work

class EditActivity:AppCompatActivity() {

    var now=TaskData()

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        val pos=intent.getIntExtra("pos",-1)

        val uitem:EditText=findViewById(R.id.vitem)
        val uinfo:EditText=findViewById(R.id.vinfo)
        val ugroup:EditText=findViewById(R.id.vgroup)
        val ulevel:EditText=findViewById(R.id.vlevel)
        val utime:TextView=findViewById(R.id.vtime)
        val udone:Switch=findViewById(R.id.vdone)
        val unull:Button=findViewById(R.id.vnull)
        val uback:Button=findViewById(R.id.back)
        val unext:Button=findViewById(R.id.next)

        DateTime(utime)

        if (pos!=-1) {
            now=work.main[pos]
            uitem.setText(now.item)
            uinfo.setText(now.info)
            ugroup.setText(now.group)
            ulevel.setText(now.level.toString())
            utime.text=now.time
            udone.isChecked=now.done
        }

        uback.setOnClickListener {finish()}
        unull.setOnClickListener {
            utime.text="never"
        }
        unext.setOnClickListener {

            fun check():String {

                now.item=uitem.text.toString()
                now.info=uinfo.text.toString()
                now.group=ugroup.text.toString()
                now.time=utime.text.toString()
                now.done=udone.isChecked

                if (now.item=="") {
                    return "'项目'不能为空！"
                }
                if (now.group=="") {
                    return "'分类'不能为空！"
                }
                try {
                    now.level=ulevel.text.toString().toInt()
                }
                catch (e:Exception) {
                    return "'等级'格式错误！"
                }
                if (now.level<1) {
                    return "'等级'非正整数！"
                }
                return ""
            }

            val tmp=check()
            if (tmp!="") {
                Toast.makeText(this,tmp,Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pos==-1) {
                work.main.add(now)
            }
            else {
                work.main[pos]=now
            }
            work.main.sort()
            work.save(this)
            finish()
        }
    }
}