package com.pdg.colourselector.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.pdg.colourselector.R
import com.pdg.colourselector.model.Colour

internal class CustomListAdapter(val context: Context, val arrayValues: Array<Colour>) : BaseAdapter() {

    val TAG = "Colour_app"

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return arrayValues.size
    }

    override fun getItem(position: Int): Any? {
        return arrayValues[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val rowView: View

        val inflater = LayoutInflater.from(context)
        rowView = inflater.inflate(R.layout.list_item, parent, false)

        val titleTV = rowView.findViewById<TextView>(R.id.colourTitle)
        titleTV.text = arrayValues[position].name

        val colourView = rowView.findViewById<ImageView>(R.id.colourIV)
        colourView.setBackground(context.resources.getDrawable(arrayValues[position].resID, null))

        return rowView
    }
}
