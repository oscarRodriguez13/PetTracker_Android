package com.example.pettracker.customerActivities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pettracker.R
import com.example.pettracker.domain.Datos
import com.example.pettracker.adapter.MascotasAdapter
import com.example.pettracker.domain.PetAdapter
import com.example.pettracker.generalActivities.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView

class SettingsActivity : AppCompatActivity() {

    private var isNotified = false
    private lateinit var fotoPaseador: ImageView
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvPets: TextView
    private var photoURI: Uri? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        fotoPaseador = findViewById(R.id.icn_perfil)
        auth = FirebaseAuth.getInstance()

        setupImagePickers()
        setupButtons()
        setupRecyclerView()

        loadUserData()
    }

    private fun setupImagePickers() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                fotoPaseador.setImageURI(uri)
                uploadImageToFirebase(uri) // Subir la imagen a Firebase
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                photoURI?.let {
                    fotoPaseador.setImageURI(it)
                    uploadImageToFirebase(it) // Subir la imagen a Firebase
                }
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val storageRef = Firebase.storage.reference.child("Usuarios/$userId/profile")
            storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileImageUriInDatabase(uri.toString(), userId)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Fallo al subir imagen", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfileImageUriInDatabase(imageUrl: String, userId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("Usuarios/$userId")
        databaseRef.child("profileImageUrl").setValue(imageUrl).addOnSuccessListener {
            Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al actualizar la imagen de perfil en la base de datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {

        tvName = findViewById(R.id.txt_nombre)
        tvEmail = findViewById(R.id.txt_email)
        tvPets = findViewById(R.id.txt_Mascotas)

        findViewById<ImageButton>(R.id.agregarFotoView).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        findViewById<ImageButton>(R.id.tomarFotoView).setOnClickListener {
            handleCameraPermission()
        }

        findViewById<Button>(R.id.buttonOption2).setOnClickListener {
            navigateToHistorial()
        }

        findViewById<Button>(R.id.buttonOption1).setOnClickListener {
            navigateToHome()
        }

        findViewById<CircleImageView>(R.id.icn_logout).setOnClickListener {
            navigateToLogin()
        }

        findViewById<CircleImageView>(R.id.icn_notificacion).setOnClickListener {
            toggleNotification()
        }

        findViewById<ImageButton>(R.id.btn_agregar).setOnClickListener {
            val intent = Intent(applicationContext, AgregarMascotaActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, android.Manifest.permission.CAMERA
            ) -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Datos.MY_PERMISSION_REQUEST_CAMERA
                )
            }
            else -> {
                requestPermissions(
                    arrayOf(android.Manifest.permission.CAMERA),
                    Datos.MY_PERMISSION_REQUEST_CAMERA
                )
            }
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val petAdapters = mutableListOf<PetAdapter>()
        val adapter = MascotasAdapter(petAdapters) { pet ->
            abrirDetallePerfil(pet)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Firebase Database reference
        val database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val ref = database.child("Mascotas").child(userId)

            ref.addChildEventListener(object : ChildEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    // Cuando se agrega una nueva mascota
                    val name = dataSnapshot.child("nombre").getValue(String::class.java) ?: "Sin nombre"
                    val breed = dataSnapshot.child("raza").getValue(String::class.java) ?: "Sin raza"
                    val petAdapter = PetAdapter(userId, dataSnapshot.key.toString(), null, name, breed) // Usa una imagen por defecto
                    petAdapters.add(petAdapter)
                    adapter.notifyDataSetChanged()
                    tvPets.text = "Mascotas: ${petAdapters.size}"
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    // Cuando se modifica una mascota existente
                    val key = dataSnapshot.key
                    val index = petAdapters.indexOfFirst { it.petId == key }
                    if (index != -1) {
                        val name = dataSnapshot.child("nombre").getValue(String::class.java) ?: "Sin nombre"
                        val breed = dataSnapshot.child("raza").getValue(String::class.java) ?: "Sin raza"
                        petAdapters[index] = PetAdapter(userId, key.toString(), null, name, breed)
                        adapter.notifyItemChanged(index)
                    }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    // Cuando se elimina una mascota
                    val key = dataSnapshot.key
                    val index = petAdapters.indexOfFirst { it.petId == key }
                    if (index != -1) {
                        petAdapters.removeAt(index)
                        adapter.notifyItemRemoved(index)
                        tvPets.text = "Mascotas: ${petAdapters.size}"
                    }
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    // No se suele usar en la mayoría de los casos
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@SettingsActivity, "Error al cargar perfiles de mascotas", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Foto nueva")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Tomada desde la aplicacion del Taller 2")
        photoURI = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        photoURI?.let { uri ->
            takePictureLauncher.launch(uri)
        }
    }

    private fun navigateToHistorial() {
        val intent = Intent(applicationContext, HistorialActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToHome() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLogin() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun toggleNotification() {
        val notifyButton = findViewById<CircleImageView>(R.id.icn_notificacion)
        if (isNotified) {
            notifyButton.setImageResource(R.drawable.icn_notificacion_inactiva)
        } else {
            notifyButton.setImageResource(R.drawable.icn_notificacion)
        }
        isNotified = !isNotified
    }

    private fun abrirDetallePerfil(petAdapter: PetAdapter) {
        val intent = Intent(this, DetallesMascotaActivity::class.java)
        intent.putExtra("petId", petAdapter.petId)
        startActivity(intent)
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference("Usuarios").child(userId)

            val profileRef = Firebase.storage.reference.child("Usuarios").child(userId).child("profile")

            profileRef.downloadUrl.addOnSuccessListener { uri ->
                val profileImageUrl = uri.toString()

                // Cargar la imagen usando Glide
                Glide.with(this@SettingsActivity) // Utiliza el contexto del itemView
                    .load(profileImageUrl) // Utiliza la URL de la imagen
                    .into(fotoPaseador)

            }.addOnFailureListener {
                fotoPaseador.setImageResource(R.drawable.icn_labrador)
            }

            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("nombre").getValue(String::class.java)
                    val userEmail = auth.currentUser?.email


                    tvName.text = userName ?: "Nombre no disponible"
                    tvEmail.text = userEmail ?: "Correo no disponible"

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@SettingsActivity, "Error al cargar datos del usuario", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}
