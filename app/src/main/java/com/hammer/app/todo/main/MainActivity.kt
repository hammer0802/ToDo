package com.hammer.app.todo.main

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.google.gson.Gson
import com.hammer.app.todo.R
import com.hammer.app.todo.MyRecyclerAdapter
import com.hammer.app.todo.data.Item
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val preference: SharedPreferences by lazy { getSharedPreferences("ToDo", Context.MODE_PRIVATE) }
    val gson = Gson()
    private val list: MutableList<Item> = mutableListOf()
    private val recyclerAdaptor = MyRecyclerAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listRefresh()
        recyclerViewRefresh()

        var count = 0
        for (i in 0 until list.size) {
            if (!list[i].isChecked) count++
        }
        val left = "$count items left"
        leftNum.text = left

        if (list.any { it.isChecked }) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE

        add.setOnClickListener {
            if (editText.text.isNotBlank() && editText.text.isNotEmpty()) {
                val item = Item(editText.text.toString(), false, Date())
                editText.text.clear()
                val e = preference.edit()
                e.putString(item.date.toString(), gson.toJson(item))
                e.apply()
                listRefresh()
                recyclerViewRefresh()
                if (list.any { it.isChecked }) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE
            }
        }

        allCheck.setOnClickListener {
            if (allCheck.isChecked) list.forEach { it.isChecked = true } else list.forEach { it.isChecked = false }
            listRefresh()
            recyclerViewRefresh()
            if (list.any { it.isChecked }) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE
        }
    }

    private fun recyclerViewRefresh() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = recyclerAdaptor
        recyclerAdaptor.load()
        recycler_view.adapter!!.notifyDataSetChanged()
    }

    private fun listRefresh() {
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
    }
}
