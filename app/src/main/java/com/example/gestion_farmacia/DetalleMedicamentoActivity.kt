package com.example.gestion_farmacia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class DetalleMedicamentoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var medicamentoId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_medicamento)

        db = FirebaseFirestore.getInstance()
        medicamentoId = intent.getStringExtra("id") ?: ""

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etCategoria = findViewById<TextInputEditText>(R.id.etCategoria)
        val etPrecio = findViewById<TextInputEditText>(R.id.etPrecio)
        val etStock = findViewById<TextInputEditText>(R.id.etStock)
        val etDescripcion = findViewById<TextInputEditText>(R.id.etDescripcion)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // Cargar datos recibidos
        etNombre.setText(intent.getStringExtra("nombre"))
        etCategoria.setText(intent.getStringExtra("categoria"))
        etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
        etStock.setText(intent.getIntExtra("stock", 0).toString())
        etDescripcion.setText(intent.getStringExtra("descripcion"))

        // ACTUALIZAR
        btnActualizar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val categoria = etCategoria.text.toString().trim()
            val precio = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
            val stock = etStock.text.toString().toIntOrNull() ?: 0
            val descripcion = etDescripcion.text.toString().trim()

            if (nombre.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnActualizar.isEnabled = false

            val datos = mapOf(
                "nombre" to nombre,
                "categoria" to categoria,
                "precio" to precio,
                "stock" to stock,
                "descripcion" to descripcion
            )

            db.collection("medicamentos").document(medicamentoId)
                .update(datos)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Medicamento actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnActualizar.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // ELIMINAR
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar medicamento")
                .setMessage("¿Estás seguro de que deseas eliminar este medicamento?")
                .setPositiveButton("Eliminar") { _, _ ->
                    progressBar.visibility = View.VISIBLE
                    db.collection("medicamentos").document(medicamentoId)
                        .delete()
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Medicamento eliminado", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}