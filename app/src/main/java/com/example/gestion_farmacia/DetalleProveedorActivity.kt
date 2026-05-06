package com.example.gestion_farmacia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class DetalleProveedorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var proveedorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_proveedor)

        db = FirebaseFirestore.getInstance()

        proveedorId = intent.getStringExtra("id") ?: ""
        val nombre = intent.getStringExtra("nombre") ?: ""
        val telefono = intent.getStringExtra("telefono") ?: ""
        val productos = intent.getStringExtra("productos") ?: ""

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val btnActualizar = findViewById<Button>(R.id.btnActualizar)
        val btnEliminar = findViewById<Button>(R.id.btnEliminar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val headerProductos = findViewById<LinearLayout>(R.id.headerProductos)
        val contenedorCheckboxes = findViewById<LinearLayout>(R.id.contenedorCheckboxes)
        val tvToggle = findViewById<TextView>(R.id.tvToggle)

        // Rellenar campos
        etNombre.setText(nombre)
        etTelefono.setText(telefono)

        // Toggle esconder/mostrar checkboxes
        headerProductos.setOnClickListener {
            if (contenedorCheckboxes.visibility == View.GONE) {
                contenedorCheckboxes.visibility = View.VISIBLE
                tvToggle.text = "▲ Ocultar"
            } else {
                contenedorCheckboxes.visibility = View.GONE
                tvToggle.text = "▼ Ver"
            }
        }

        val checkboxes = listOf(
            Pair(R.id.cbAntibioticos, "Antibióticos"),
            Pair(R.id.cbAnalgesicos, "Analgésicos"),
            Pair(R.id.cbAntihistaminicos, "Antihistamínicos"),
            Pair(R.id.cbGastrointestinales, "Gastrointestinales"),
            Pair(R.id.cbAntidiabeticos, "Antidiabéticos"),
            Pair(R.id.cbCardiovasculares, "Cardiovasculares"),
            Pair(R.id.cbRespiratorios, "Respiratorios"),
            Pair(R.id.cbSuplementos, "Suplementos y Vitaminas"),
            Pair(R.id.cbDermatologicos, "Dermatológicos"),
            Pair(R.id.cbNeurologicos, "Neurológicos")
        )

        // Marcar checkboxes según productos guardados
        checkboxes.forEach { (id, label) ->
            findViewById<CheckBox>(id).isChecked = productos.contains(label)
        }

        // Actualizar
        btnActualizar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevoTelefono = etTelefono.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val productosSeleccionados = checkboxes
                .filter { findViewById<CheckBox>(it.first).isChecked }
                .joinToString(", ") { it.second }

            progressBar.visibility = View.VISIBLE
            btnActualizar.isEnabled = false

            db.collection("proveedores").document(proveedorId)
                .update(
                    "nombre", nuevoNombre,
                    "telefono", nuevoTelefono,
                    "productos", productosSeleccionados
                )
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Proveedor actualizado", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    btnActualizar.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        // Eliminar
        btnEliminar.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar proveedor")
                .setMessage("¿Estás seguro que deseas eliminar a $nombre?")
                .setPositiveButton("Eliminar") { _, _ ->
                    progressBar.visibility = View.VISIBLE
                    db.collection("proveedores").document(proveedorId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Proveedor eliminado", Toast.LENGTH_SHORT).show()
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