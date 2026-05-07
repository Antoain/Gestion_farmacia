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

        val esAdmin     = intent.getBooleanExtra("esAdmin", false)

        val etNombre      = findViewById<TextInputEditText>(R.id.etNombre)
        val etCategoria   = findViewById<TextInputEditText>(R.id.etCategoria)
        val etPrecio      = findViewById<TextInputEditText>(R.id.etPrecio)
        val etStock       = findViewById<TextInputEditText>(R.id.etStock)
        val etDescripcion = findViewById<TextInputEditText>(R.id.etDescripcion)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnEliminar   = findViewById<Button>(R.id.btnEliminar)
        val progressBar   = findViewById<ProgressBar>(R.id.progressBar)

        // Cargar datos
        etNombre.setText(intent.getStringExtra("nombre"))
        etCategoria.setText(intent.getStringExtra("categoria"))
        etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
        etStock.setText(intent.getIntExtra("stock", 0).toString())
        etDescripcion.setText(intent.getStringExtra("descripcion"))

        // --- Restricciones según rol ---
        if (!esAdmin) {
            // Empleado: solo puede editar stock
            etNombre.isEnabled      = false
            etCategoria.isEnabled   = false
            etPrecio.isEnabled      = false
            etDescripcion.isEnabled = false
            btnEliminar.visibility  = View.GONE
            btnActualizar.text      = "Actualizar Stock"
        }

        // ACTUALIZAR
        btnActualizar.setOnClickListener {
            val stock = etStock.text.toString().toIntOrNull()
            if (stock == null || stock < 0) {
                Toast.makeText(this, "Ingresa un stock válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnActualizar.isEnabled = false

            if (esAdmin) {
                // Admin actualiza todo
                val nombre      = etNombre.text.toString().trim()
                val categoria   = etCategoria.text.toString().trim()
                val precio      = etPrecio.text.toString().toDoubleOrNull() ?: 0.0
                val descripcion = etDescripcion.text.toString().trim()

                if (nombre.isEmpty() || categoria.isEmpty()) {
                    progressBar.visibility = View.GONE
                    btnActualizar.isEnabled = true
                    Toast.makeText(this, "Nombre y categoría son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val datos = mapOf(
                    "nombre"      to nombre,
                    "categoria"   to categoria,
                    "precio"      to precio,
                    "stock"       to stock,
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
            } else {
                // Empleado solo actualiza stock
                db.collection("medicamentos").document(medicamentoId)
                    .update("stock", stock)
                    .addOnSuccessListener {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, "Stock actualizado correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        btnActualizar.isEnabled = true
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }

        // ELIMINAR (solo admin)
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
