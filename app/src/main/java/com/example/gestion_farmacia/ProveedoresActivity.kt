package com.example.gestion_farmacia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ProveedoresActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProveedorAdapter
    private val listaProveedores = mutableListOf<Proveedor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proveedores)

        db = FirebaseFirestore.getInstance()

        val recycler = findViewById<RecyclerView>(R.id.recyclerProveedores)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        adapter = ProveedorAdapter(listaProveedores) { proveedor ->
            AlertDialog.Builder(this)
                .setTitle("Eliminar proveedor")
                .setMessage("¿Eliminar a ${proveedor.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    db.collection("proveedores").document(proveedor.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Proveedor eliminado", Toast.LENGTH_SHORT).show()
                            cargarProveedores(progressBar)
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<Button>(R.id.btnAgregarProveedor).setOnClickListener {
            mostrarDialogoAgregar(progressBar)
        }

        cargarProveedores(progressBar)
    }

    private fun mostrarDialogoAgregar(progressBar: ProgressBar) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_proveedor, null)

        AlertDialog.Builder(this)
            .setTitle("🏭 Nuevo Proveedor")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreDialog)
                    .text.toString().trim()
                val telefono = dialogView.findViewById<TextInputEditText>(R.id.etTelefonoDialog)
                    .text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Recoger checkboxes seleccionados
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
                    .filter { dialogView.findViewById<CheckBox>(it.first).isChecked }
                    .joinToString(", ") { it.second }

                val proveedor = hashMapOf(
                    "nombre" to nombre,
                    "telefono" to telefono,
                    "productos" to productosSeleccionados
                )

                db.collection("proveedores").add(proveedor)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Proveedor agregado", Toast.LENGTH_SHORT).show()
                        cargarProveedores(progressBar)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarProveedores(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("proveedores")
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                listaProveedores.clear()
                for (doc in result) {
                    listaProveedores.add(
                        Proveedor(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            telefono = doc.getString("telefono") ?: "",
                            productos = doc.getString("productos") ?: ""
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar proveedores", Toast.LENGTH_SHORT).show()
            }
    }
}