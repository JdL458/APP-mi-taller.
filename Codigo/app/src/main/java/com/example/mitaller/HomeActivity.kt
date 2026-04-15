package com.example.mitaller

import android.content.Intent
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : AppCompatActivity() {

    private lateinit var txtNombreAlumno: TextView
    private lateinit var txtDni: TextView
    private lateinit var txtRol: TextView
    private lateinit var imgFotoAlumno: ImageView
    private lateinit var listMaquinas: LinearLayout
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnEscanearQR: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val REQUEST_CODE_SCAN_QR = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Vinculamos vistas
        txtNombreAlumno = findViewById(R.id.txtNombreAlumno)
        txtDni = findViewById(R.id.txtDni)
        txtRol = findViewById(R.id.txtRol)
        imgFotoAlumno = findViewById(R.id.imgFotoAlumno)
        listMaquinas = findViewById(R.id.listMaquinas)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        btnEscanearQR = findViewById(R.id.btnEscanearQR)

        cargarDatosUsuario()

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnEscanearQR.setOnClickListener {
            abrirEscanerQR()
        }
    }

    private fun cargarDatosUsuario() {
        val user = auth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val userId = user.uid
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("fullName") ?: ""
                    val dni = doc.getString("dni") ?: ""
                    val rol = doc.getString("role") ?: ""
                    val fotoUrl = doc.getString("foto") ?: ""
                    val maquinas = doc.get("maquinas") as? List<String> ?: emptyList()

                    txtNombreAlumno.text = nombre
                    txtDni.text = "DNI: $dni"
                    txtRol.text = "Rol: $rol"

                    if (fotoUrl.isNotEmpty()) {
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.placeholder_foto)
                            .error(R.drawable.placeholder_foto)
                            .into(imgFotoAlumno)

                        // Animación fade-in para la imagen cargada
                        imgFotoAlumno.alpha = 0f
                        imgFotoAlumno.animate().alpha(1f).setDuration(1000).start()
                    }

                    mostrarMaquinas(maquinas)
                } else {
                    Toast.makeText(this, "No se encontraron datos del usuario", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos del usuario", Toast.LENGTH_LONG).show()
            }
    }


    private fun mostrarMaquinas(maquinasUsuario: List<String>) {
        listMaquinas.removeAllViews()

        db.collection("maquinas").get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    val tv = TextView(this)
                    tv.text = "No hay máquinas disponibles."
                    listMaquinas.addView(tv)
                    return@addOnSuccessListener
                }
                for (doc in querySnapshot.documents) {
                    val nombre = doc.getString("Nombre") ?: "Máquina sin nombre"
                    val estado = doc.getString("Estado") ?: "Desconocido"
                    val condicion = doc.getString("condicion") ?: "Desconocido"

                    val tv = TextView(this)
                    tv.text = "Máquina: $nombre\nEstado: $estado\nCondición: $condicion"
                    tv.setPadding(0, 0, 0, 24)
                    listMaquinas.addView(tv)
                }
            }
            .addOnFailureListener {
                val tv = TextView(this)
                tv.text = "Error al cargar las máquinas."
                listMaquinas.addView(tv)
            }
    }

    private fun abrirEscanerQR() {
        val intent = Intent(this, ScanQrActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_SCAN_QR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN_QR && resultCode == RESULT_OK) {
            val urlCompleta = data?.getStringExtra("QR_RESULT") ?: return

            val maquinaId = try {
                val uri = android.net.Uri.parse(urlCompleta)
                uri.getQueryParameter("rele") ?: ""
            } catch (e: Exception) {
                ""
            }

            if (maquinaId.isNotEmpty()) {
                guardarQR(urlCompleta)
                controlarMaquina(urlCompleta, maquinaId)
            } else {
                Toast.makeText(this, "QR inválido. No contiene parámetro 'rele'", Toast.LENGTH_LONG).show()
            }
        }
    }





    private fun guardarQR(qr: String) {
        val user = auth.currentUser ?: return
        val userId = user.uid

        val userRef = db.collection("users").document(userId)

        userRef.update("maquinas", com.google.firebase.firestore.FieldValue.arrayUnion(qr))
            .addOnSuccessListener {
                Toast.makeText(this, "Máquina guardada: $qr", Toast.LENGTH_SHORT).show()
                cargarDatosUsuario()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar la máquina", Toast.LENGTH_SHORT).show()
            }
    }

    private fun controlarMaquina(urlToggleCompleta: String, maquinaId: String) {
        Thread {
            try {
                val urlToggle = URL(urlToggleCompleta)
                val connToggle = urlToggle.openConnection() as HttpURLConnection
                connToggle.requestMethod = "GET"
                connToggle.connectTimeout = 5000
                connToggle.readTimeout = 5000

                val responseCodeToggle = connToggle.responseCode
                if (responseCodeToggle == HttpURLConnection.HTTP_OK) {
                    val respuesta = BufferedReader(InputStreamReader(connToggle.inputStream)).readLine()?.trim() ?: "Sin respuesta"

                    val nuevaCondicion = if (respuesta.contains("Encendido", ignoreCase = true)) "encendido" else "apagado"
                    val db = FirebaseFirestore.getInstance()
                    db.collection("maquinas").document(maquinaId)
                        .update("condicion", nuevaCondicion)
                        .addOnSuccessListener {
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(this, "Máquina $nuevaCondicion y Firebase actualizado", Toast.LENGTH_LONG).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(this, "Error al actualizar Firebase: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(this, "Error en toggle: $responseCodeToggle", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Error al controlar la máquina: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }





}
