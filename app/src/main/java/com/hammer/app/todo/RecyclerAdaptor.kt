package com.hammer.app.todo

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.google.gson.Gson
import com.hammer.app.todo.data.Item
import com.hammer.app.todo.main.MainActivity

class MyRecyclerAdapter(val activity: MainActivity): RecyclerView.Adapter<MyRecyclerViewHolder>() {
    private val preference: SharedPreferences by lazy { activity.getSharedPreferences("ToDo", Context.MODE_PRIVATE)}
    val gson = Gson()
    val list: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_main, parent,false)
        return MyRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyRecyclerViewHolder, position: Int) {
        val listItem = "${list[position].content}"
        val isChecked = list[position].isChecked
        val checkBox = holder.v.findViewById<CheckBox>(R.id.checkBox)
        holder.v.findViewById<TextView>(R.id.list_item).text = listItem
        checkBox.isChecked = isChecked

        holder.v.setOnClickListener{
            checkBox.isChecked =  !checkBox.isChecked
            val deleteBtn = holder.v.findViewById<Button>(R.id.deleteButton)
            if (checkBox.isChecked) deleteBtn.visibility = View.VISIBLE else deleteBtn.visibility = View.INVISIBLE
        }

        holder.v.findViewById<Button>(R.id.deleteButton).setOnClickListener {
            val e = preference.edit()
            e.remove(list[position].date.toString())
            e.apply()
            list.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, list.size)
        }

    }

    fun load(){
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
    }
}

class MyRecyclerViewHolder(val v: View): RecyclerView.ViewHolder(v)

