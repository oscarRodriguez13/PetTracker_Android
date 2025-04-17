package com.example.pettracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.domain.Profile
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class SolicitudPaseoAdapter(
    private val profiles: List<Profile>,
    private val onClick: (Profile) -> Unit // Lambda para manejar clics
) : RecyclerView.Adapter<SolicitudPaseoAdapter.ProfileViewHolder>() {

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.profile_image)
        val profileName: TextView = itemView.findViewById(R.id.profile_name)
        val profilePrice: TextView = itemView.findViewById(R.id.profile_price)

        fun bind(profile: Profile) {

            val profileRef = Firebase.storage.reference.child("Usuarios").child(profile.uid).child("profile")

            profileRef.downloadUrl.addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()

                // Cargar la imagen usando Glide
                Glide.with(itemView.context) // Utiliza el contexto del itemView
                    .load(profileImageUrl) // Utiliza la URL de la imagen
                    .into(profileImage)

            }.addOnFailureListener {
                profileImage.setImageResource(R.drawable.icn_labrador)
            }

            profileName.text = profile.name
            profilePrice.text = profile.price

            // Manejar el clic en el elemento del RecyclerView
            itemView.setOnClickListener { onClick(profile) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.solicitudes_paseo_adapter, parent, false)
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
