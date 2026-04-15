package com.example.mitaller

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class ActualizarEstadoActivity : AppCompatActivity() {

    private lateinit var tvNombreMaquina: TextView
    private lateinit var rgEstado: RadioGroup
    private lateinit var etJustificativo: EditText
    private lateinit var btnGuardar: Button

    private val db = FirebaseFirestore.getInstance()
    private var maquinaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar_estado)

        tvNombreMaquina = findViewById(R.id.tvNombreMaquina)
        rgEstado = findViewById(R.id.rgEstado)
        etJustificativo = findViewById(R.id.etJustificativo)
        btnGuardar = findViewById(R.id.btnGuardarEstado)

        maquinaId = intent.getStringExtra("maquinaId")

        if (maquinaId == null) {
            Toast.makeText(this, "No se recibió el ID de la máquina", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        cargarDatosMaquina(maquinaId!!)

        btnGuardar.setOnClickListener {
            guardarEstado()
        }
    }

    private fun cargarDatosMaquina(id: String) {
        db.collection("maquinas").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("nombre") ?: "Máquina sin nombre"
                    val estado = doc.getString("estado") ?: ""

                    tvNombreMaquina.text = nombre

                    // Seleccionar el radio button según estado actual
                    when (estado.lowercase()) {
                        "funciona" -> rgEstado.check(R.id.rbFunciona)
                        "no funciona" -> rgEstado.check(R.id.rbNoFunciona)
                        else -> rgEstado.clearCheck()
                    }
                } else {
                    Toast.makeText(this, "No se encontró la máquina", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun guardarEstado() {
        val selectedRadioId = rgEstado.checkedRadioButtonId
        if (selectedRadioId == -1) {
            Toast.makeText(this, "Por favor seleccioná un estado", Toast.LENGTH_SHORT).show()
            return
        }
        val estado = when (selectedRadioId) {
            R.id.rbFunciona -> "funciona"
            R.id.rbNoFunciona -> "no funciona"
            else -> ""
        }

        val justificativo = etJustificativo.text.toString().trim()

        val updateMap = hashMapOf<String, Any>(
            "estado" to estado,
            "justificativo" to justificativo,
            "ultima_actualizacion" to com.google.firebase.Timestamp.now()
        )

        maquinaId?.let { id ->
            db.collection("maquinas").document(id).update(updateMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar el estado", Toast.LENGTH_LONG).show()
                }
        }
    }
}
