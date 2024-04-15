package com.example.pettracker.domain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.pettracker.R

class ProgramarPaseoAdapter(context: Context, paseosList: List<ProgramarPaseoItem>) :
    ArrayAdapter<ProgramarPaseoItem>(context, 0, paseosList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.programar_paseo_adapter, parent, false)
        }

        val currentItem = getItem(position)


        val tvDuenho = itemView?.findViewById<TextView>(R.id.nombre_duenho)
        val tvDuracion = itemView?.findViewById<TextView>(R.id.duracion_paseo)
        val tvCantidad = itemView?.findViewById<TextView>(R.id.cantidad_mascotas)


        tvDuenho?.text = currentItem?.nombre
        tvDuracion?.text = currentItem?.duracion
        tvCantidad?.text = currentItem?.cantidad

        return itemView!!
    }
}