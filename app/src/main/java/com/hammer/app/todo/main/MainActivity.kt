package com.hammer.app.todo.main

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.hammer.app.todo.R
import com.hammer.app.todo.MyRecyclerAdapter
import com.hammer.app.todo.data.Filter
import com.hammer.app.todo.data.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val preference: SharedPreferences by lazy { getSharedPreferences("ToDo", Context.MODE_PRIVATE) }
    val gson = Gson()
    private val list: MutableList<Item> = mutableListOf()
    private val recyclerAdaptor = MyRecyclerAdapter(this)
    var filter = Filter.ALL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listRefresh()
        recyclerViewRefresh()

        count()

        if (list.any { it.isChecked }) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE

        add.setOnClickListener {
            if (editText.text.isNotBlank() && editText.text.isNotEmpty()) {
                val item = Item(editText.text.toString(), false, Date())
                editText.text.clear()
                val e = preference.edit()
                e.putString(item.date.toString(), gson.toJson(item))
                e.apply()
                filter()
                if (list.any { it.isChecked }) clear.visibility = View.VISIBLE
                else {
                    clear.visibility = View.INVISIBLE
                    allCheck.isChecked = false
                }
                count()
            }
        }

        allCheck.setOnClickListener {
            listRefresh()
            recyclerViewRefresh()
            if (allCheck.isChecked) list.forEach { it.isChecked = true } else list.forEach { it.isChecked = false }
            val e = preference.edit()
            for (i in 0 until list.size) {
                e.putString(list[i].date.toString(), gson.toJson(list[i]))
            }
            e.apply()
            filter()
            if (allCheck.isChecked) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE
            count()
        }

        all.setOnClickListener {
            all.setBackgroundColor(Color.parseColor("#dcdcdc"))
            active.setBackgroundColor(Color.parseColor("#ffffff"))
            completed.setBackgroundColor(Color.parseColor("#ffffff"))
            recyclerViewRefresh()
            filter = Filter.ALL
        }

        active.setOnClickListener {
            all.setBackgroundColor(Color.parseColor("#ffffff"))
            active.setBackgroundColor(Color.parseColor("#dcdcdc"))
            completed.setBackgroundColor(Color.parseColor("#ffffff"))
            list.removeAll { it.isChecked }
            activeRecyclerViewRefresh()
            filter = Filter.ACTIVE
        }

        completed.setOnClickListener {
            all.setBackgroundColor(Color.parseColor("#ffffff"))
            active.setBackgroundColor(Color.parseColor("#ffffff"))
            completed.setBackgroundColor(Color.parseColor("#dcdcdc"))
            list.retainAll { it.isChecked }
            completedRecyclerViewRefresh()
            filter = Filter.COMPLETED
        }

        clear.setOnClickListener {
            var checkedList:MutableList<Int> = mutableListOf()
            listRefresh()
            for(i in 0 until list.size) {
                if(list[i].isChecked){
                    checkedList.add(i)
                }
            }
            checkedList.sortDescending()
            checkedList.forEach { recyclerAdaptor.clear(it) }
            listRefresh()
            recyclerViewRefresh()
            if (list.any { it.isChecked }) clear.visibility = View.VISIBLE
            else {
                clear.visibility = View.INVISIBLE
                allCheck.isChecked = false
            }
            count()
        }
    }

    private fun recyclerViewRefresh() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = recyclerAdaptor
        recyclerAdaptor.load()
        recycler_view.adapter!!.notifyDataSetChanged()
    }

    private fun activeRecyclerViewRefresh() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = recyclerAdaptor
        recyclerAdaptor.activeLoad()
        recycler_view.adapter!!.notifyDataSetChanged()
    }

    private fun completedRecyclerViewRefresh() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = recyclerAdaptor
        recyclerAdaptor.completedLoad()
        recycler_view.adapter!!.notifyDataSetChanged()
    }

    private fun listRefresh() {
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
    }

    private fun count() {
        var count = 0
        listRefresh()
        for (i in 0 until list.size) {
            if (!list[i].isChecked) count++
        }
        val left = "$count items left"
        leftNum.text = left
    }

    fun filter(){
        when (filter) {
            Filter.ALL -> {
                listRefresh()
                recyclerViewRefresh()
            }
            Filter.ACTIVE -> {
                listRefresh()
                list.removeAll { it.isChecked }
                activeRecyclerViewRefresh()
            }
            Filter.COMPLETED -> {
                listRefresh()
                list.retainAll { it.isChecked }
                completedRecyclerViewRefresh()
            }
        }
    }
}
