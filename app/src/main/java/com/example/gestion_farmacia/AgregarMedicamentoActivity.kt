package com.example.gestion_farmacia

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class AgregarMedicamentoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    private val categorias = listOf(
        "Antibiótico",
        "Analgésico",
        "Antihistamínico",
        "Gastrointestinal",
        "Antidiabético",
        "Cardiovascular",
        "Respiratorio",
        "Suplemento",
        "Antiinflamatorio",
        "Dermatológico",
        "Neurológico",
        "Oftalmológico",
        "Vitaminas",
        "Otro"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_medicamento)

        db = FirebaseFirestore.getInstance()

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val actvCategoria = findViewById<AutoCompleteTextView>(R.id.actvCategoria)
        val etPrecio = findViewById<TextInputEditText>(R.id.etPrecio)
        val etStock = findViewById<TextInputEditText>(R.id.etStock)
        val etDescripcion = findViewById<TextInputEditText>(R.id.etDescripcion)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Configurar dropdown de categorías
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
        actvCategoria.setAdapter(adapter)
        actvCategoria.setOnClickListener { actvCategoria.showDropDown() }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val categoria = actvCategoria.text.toString().trim()
            val precioStr = etPrecio.text.toString().trim()
            val stockStr = etStock.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (nombre.isEmpty() || categoria.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = precioStr.toDoubleOrNull() ?: 0.0
            val stock = stockStr.toIntOrNull() ?: 0

            val medicamento = hashMapOf(
                "nombre" to nombre,
                "categoria" to categoria,
                "precio" to precio,
                "stock" to stock,
                "descripcion" to descripcion
            )

            progressBar.visibility = View.VISIBLE
            btnGuardar.isEnabled = false

            db.collection("medicamentos")
                .add(medicamento)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Medicamento guardado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnGuardar.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}