package com.example.mitaller

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class HomeProfesorActivity : AppCompatActivity() {

    private lateinit var txtNombre: TextView
    private lateinit var txtDni: TextView
    private lateinit var txtRol: TextView
    private lateinit var imgFoto: ImageView
    private lateinit var listMaquinas: LinearLayout
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnEscanearQR: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val REQUEST_CODE_SCAN_QR = 5678
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_profesor)

        // Vincular vistas
        txtNombre = findViewById(R.id.txtNombreProfesor)
        txtDni = findViewById(R.id.txtDniProfesor)
        txtRol = findViewById(R.id.txtRolProfesor)
        imgFoto = findViewById(R.id.imgFotoProfesor)
        listMaquinas = findViewById(R.id.listMaquinasProfesor)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionProfesor)
        btnEscanearQR = findViewById(R.id.btnEscanearQRProfesor)

        cargarDatosProfesor()

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnEscanearQR.setOnClickListener {
            abrirEscanerQR()
        }
    }

    private fun cargarDatosProfesor() {
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = user.uid
        val userRef = db.collection("users").document(userId)

        userRef.get().addOnSuccessListener { doc ->
            if (doc != null && doc.exists()) {
                val nombre = doc.getString("fullName") ?: ""
                val dni = doc.getString("dni") ?: ""
                val rol = doc.getString("role") ?: ""
                val fotoUrl = doc.getString("foto") ?: ""

                txtNombre.text = nombre
                txtDni.text = "DNI: $dni"
                txtRol.text = "Rol: $rol"

                if (fotoUrl.isNotEmpty()) {
                    Glide.with(this).load(fotoUrl).into(imgFoto)
                } else {
                    imgFoto.setImageResource(R.drawable.prof) // Drawable por defecto
                }

                cargarMaquinas()
            } else {
                Toast.makeText(this, "No se encontraron datos del profesor", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_LONG).show()
        }
    }

    private fun cargarMaquinas() {
        listMaquinas.removeAllViews()

        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault())

        db.collection("maquinas")
            .whereEqualTo("profesorId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val tv = TextView(this)
                    tv.text = "No hay máquinas asignadas."
                    listMaquinas.addView(tv)
                } else {
                    for (doc in result) {
                        val nombre = doc.getString("Nombre") ?: "Máquina sin nombre"
                        val estado = doc.getString("estado") ?: "Desconocido"
                        val condicion = doc.getString("condicion") ?: "Desconocido"
                        val justificativo = doc.getString("justificativo") ?: "Sin justificativo"
                        val timestamp = doc.getTimestamp("ultima_actualizacion")
                        val ultimaActualizacion = timestamp?.toDate()?.let { sdf.format(it) } ?: "No disponible"

                        val tv = TextView(this)
                        tv.text = "Máquina: $nombre\nEstado: $estado\nCondición: $condicion\nJustificativo: $justificativo\nÚltima actualización: $ultimaActualizacion"
                        tv.setPadding(0, 0, 0, 24)
                        listMaquinas.addView(tv)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar máquinas", Toast.LENGTH_LONG).show()
            }
    }


    private fun abrirEscanerQR() {
        val intent = Intent(this, ScanQrActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN_QR && resultCode == RESULT_OK) {
            val rawQr = data?.getStringExtra("QR_RESULT") ?: return
            Toast.makeText(this, "QR escaneado: $rawQr", Toast.LENGTH_LONG).show()

            try {
                val uri = android.net.Uri.parse(rawQr)
                val ip = uri.host ?: ""
                val maquinaId = uri.getQueryParameter("rele") ?: ""

                if (ip.isNotEmpty() && maquinaId.isNotEmpty()) {
                    val intent = Intent(this, ActualizarEstadoActivity::class.java)
                    intent.putExtra("maquinaId", maquinaId)
                    intent.putExtra("ipMaquina", ip)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "QR inválido. Falta IP o parámetro 'rele'", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar QR", Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        cargarMaquinas()
    }
}
