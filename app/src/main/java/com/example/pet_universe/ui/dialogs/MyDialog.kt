package com.example.pet_universe.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.pet_universe.R

class MyDialog(
    context: Context,
    private val title: String,
    private val options: List<String>,
    private val onOptionSelected: (String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_options)

        setTitle(title)
        val listView: ListView = findViewById(R.id.optionsListView)
        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, options)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            onOptionSelected(options[position])
            dismiss()
        }
    }
}
