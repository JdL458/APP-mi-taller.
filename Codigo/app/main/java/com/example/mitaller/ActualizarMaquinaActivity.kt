package com.example.mitaller

import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mitaller.databinding.ActivityActualizarMaquinaBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ActualizarMaquinaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActualizarMaquinaBinding
    private lateinit var db: FirebaseFirestore
    private var maquinaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActualizarMaquinaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Recibir datos de la máquina desde el intent
        val nombreMaquina = intent.getStringExtra("nombreMaquina") ?: ""
        maquinaId = intent.getStringExtra("maquinaId") // ID del documento en Firestore

        binding.tvNombreMaquina.text = nombreMaquina

        // Mostrar u ocultar el justificativo si elige "No funciona"
        binding.rgEstado.setOnCheckedChangeListener { _, checkedId ->
            val rbNoFunciona = findViewById<RadioButton>(R.id.rbNoFunciona)
            binding.justificacionLayout.visibility =
                if (checkedId == rbNoFunciona.id) View.VISIBLE else View.GONE
        }

        binding.btnGuardarEstado.setOnClickListener {
            guardarCambios()
        }
    }

    private fun guardarCambios() {
        val estado = when (binding.rgEstado.checkedRadioButtonId) {
            R.id.rbFunciona -> "Funciona"
            R.id.rbNoFunciona -> "No funciona"
            else -> ""
        }

        if (estado.isEmpty()) {
            Toast.makeText(this, "Seleccioná un estado", Toast.LENGTH_SHORT).show()
            return
        }

        val justificativo = binding.etJustificativo.text.toString().trim()
        if (estado == "No funciona" && justificativo.isEmpty()) {
            Toast.makeText(this, "Ingresá un justificativo", Toast.LENGTH_SHORT).show()
            return
        }

        val fechaActual = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val datosActualizados = hashMapOf(
            "estado" to estado,
            "justificativo" to justificativo,
            "ultimaActualizacion" to fechaActual
        )

        maquinaId?.let { id ->
            db.collection("maquinas").document(id)
                .update(datosActualizados as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(this, "Error: ID de máquina no encontrado", Toast.LENGTH_SHORT).show()
        }
    }
}
