package com.example.gestion_farmacia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class AgregarProveedorActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_proveedor)

        db = FirebaseFirestore.getInstance()

        val headerProductos = findViewById<LinearLayout>(R.id.headerProductos)
        val contenedorCheckboxes = findViewById<LinearLayout>(R.id.contenedorCheckboxes)
        val tvToggle = findViewById<TextView>(R.id.tvToggle)

        headerProductos.setOnClickListener {
            if (contenedorCheckboxes.visibility == View.GONE) {
                contenedorCheckboxes.visibility = View.VISIBLE
                tvToggle.text = "▲ Ocultar"
            } else {
                contenedorCheckboxes.visibility = View.GONE
                tvToggle.text = "▼ Ver"
            }
        }

        val etNombre = findViewById<TextInputEditText>(R.id.etNombre)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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

            val productosSeleccionados = checkboxes
                .filter { findViewById<CheckBox>(it.first).isChecked }
                .joinToString(", ") { it.second }

            progressBar.visibility = View.VISIBLE
            btnGuardar.isEnabled = false

            val proveedor = hashMapOf(
                "nombre" to nombre,
                "telefono" to telefono,
                "productos" to productosSeleccionados
            )

            db.collection("proveedores").add(proveedor)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Proveedor agregado exitosamente", Toast.LENGTH_SHORT).show()
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