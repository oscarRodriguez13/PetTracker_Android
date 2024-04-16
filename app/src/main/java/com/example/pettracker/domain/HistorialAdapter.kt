package com.example.pettracker.domain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.pettracker.R

class HistorialAdapter(context: Context, historialList: List<HistorialItem>) :
    ArrayAdapter<HistorialItem>(context, 0, historialList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.historial_adapter, parent, false)
        }

        val currentItem = getItem(position)

        val tvMascota = itemView?.findViewById<TextView>(R.id.idMascota)
        val tvFecha = itemView?.findViewById<TextView>(R.id.idFecha)
        val tvPaseador = itemView?.findViewById<TextView>(R.id.idPaseador)
        val tvPrecio = itemView?.findViewById<TextView>(R.id.idPrecio)

        tvMascota?.text = currentItem?.nombreMascota
        tvFecha?.text = currentItem?.fecha
        tvPaseador?.text = currentItem?.nombrePaseador
        tvPrecio?.text = currentItem?.precio

        return itemView!!
    }
}
