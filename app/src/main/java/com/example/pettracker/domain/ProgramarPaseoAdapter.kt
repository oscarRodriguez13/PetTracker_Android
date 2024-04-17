package com.example.pettracker.domain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pettracker.R
import de.hdodenhof.circleimageview.CircleImageView

class ProgramarPaseoAdapter(
    private val profiles: List<ProgramarPaseoItem>,
    private val onClick: (ProgramarPaseoItem) -> Unit // Lambda para manejar clics
) : RecyclerView.Adapter<ProgramarPaseoAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: CircleImageView = itemView.findViewById(R.id.profile_image)
        val nombre: TextView = itemView.findViewById(R.id.nombre_duenho)
        val duracion: TextView = itemView.findViewById(R.id.duracion_paseo)
        val cantidad: TextView = itemView.findViewById(R.id.cantidad_mascotas)

        fun bind(item: ProgramarPaseoItem) {
            image.setImageResource(item.image)
            nombre.text = item.nombre
            duracion.text = item.duracion
            cantidad.text = item.cantidad

            // Manejar el clic en el elemento del RecyclerView
            itemView.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.programar_paseo_adapter, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile) // Llamar a la función bind para configurar el elemento
    }

    override fun getItemCount(): Int {
        return profiles.size
    }
}