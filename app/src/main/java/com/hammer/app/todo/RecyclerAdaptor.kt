package com.hammer.app.todo

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.google.gson.Gson
import com.hammer.app.todo.data.Item
import com.hammer.app.todo.main.MainActivity

class MyRecyclerAdapter(private val activity: MainActivity) : RecyclerView.Adapter<MyRecyclerViewHolder>() {
    private val preference: SharedPreferences by lazy { activity.getSharedPreferences("ToDo", Context.MODE_PRIVATE) }
    private val gson = Gson()
    private val list: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyRecyclerViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_main, parent, false)
        return MyRecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: MyRecyclerViewHolder, position: Int) {
        val listItem = list[position].content
        val isChecked = list[position].isChecked
        val checkBox = holder.v.findViewById<CheckBox>(R.id.checkBox)
        holder.v.findViewById<TextView>(R.id.list_item).text = listItem
        checkBox.isChecked = isChecked

        //list内容が変更されたらSharedPreferenceに反映する
        fun itemChange() {
            val e = preference.edit()
            e.remove(list[position].date.toString())
            e.putString(list[position].date.toString(), gson.toJson(list[position]))
            e.apply()
        }

        //RecyclerView itemのcheckboxがクリックされた時の処理
        fun clickCheckBox() {
            val clear = activity.findViewById<Button>(R.id.clear)
            list[position].isChecked = !list[position].isChecked
            if (list.any { it.isChecked }) clear.visibility = View.VISIBLE else clear.visibility = View.INVISIBLE
            var count = 0
            for (i in 0 until itemCount) {
                if (!list[i].isChecked) count++
            }
            val left = "$count items left"
            activity.findViewById<TextView>(R.id.leftNum).text = left
        }

        //RecyclerView itemクリック時
        holder.v.setOnClickListener {
            clickCheckBox()
            checkBox.isChecked = !checkBox.isChecked
            itemChange()
            activity.filter()
        }

        //RecyclerView itemのcheckboxクリック時
        checkBox.setOnClickListener {
            clickCheckBox()
            itemChange()
            activity.filter()
        }
    }

    //Filter.ALL時のlist更新
    fun load() {
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
    }

    //Filter.ACTIVE時のlist更新
    fun activeLoad() {
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
        list.removeAll { it.isChecked }
    }

    //Filter.COMPLETED時のlist更新
    fun completedLoad() {
        list.clear()
        list.addAll(preference.all.values.filterIsInstance(String::class.java).map { value ->
            gson.fromJson<Item>(value, Item::class.java)
        })
        list.sortBy { it.date }
        list.retainAll { it.isChecked }
    }

    //RecyclerView itemの削除処理
    fun clear(position: Int) {
        load()
        val e = preference.edit()
        e.remove(list[position].date.toString())
        e.apply()
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }
}

class MyRecyclerViewHolder(val v: View) : RecyclerView.ViewHolder(v)

