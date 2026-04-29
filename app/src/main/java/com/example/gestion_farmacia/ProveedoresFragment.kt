package com.example.gestion_farmacia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore

class ProveedoresFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ProveedorAdapter
    private val listaProveedores = mutableListOf<Proveedor>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_proveedores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerProveedores)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        adapter = ProveedorAdapter(listaProveedores) { proveedor ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar proveedor")
                .setMessage("¿Eliminar a ${proveedor.nombre}?")
                .setPositiveButton("Eliminar") { _, _ ->
                    db.collection("proveedores").document(proveedor.id)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Proveedor eliminado", Toast.LENGTH_SHORT).show()
                            cargarProveedores(progressBar)
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<Button>(R.id.btnAgregarProveedor).setOnClickListener {
            mostrarDialogoAgregar(progressBar)
        }

        cargarProveedores(progressBar)
    }

    override fun onResume() {
        super.onResume()
        view?.let { cargarProveedores(it.findViewById(R.id.progressBar)) }
    }

    private fun mostrarDialogoAgregar(progressBar: ProgressBar) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_agregar_proveedor, null)

        AlertDialog.Builder(requireContext())
            .setTitle("🏭 Nuevo Proveedor")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = dialogView.findViewById<TextInputEditText>(R.id.etNombreDialog)
                    .text.toString().trim()
                val telefono = dialogView.findViewById<TextInputEditText>(R.id.etTelefonoDialog)
                    .text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
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
                    .filter { dialogView.findViewById<CheckBox>(it.first).isChecked }
                    .joinToString(", ") { it.second }

                val proveedor = hashMapOf(
                    "nombre" to nombre,
                    "telefono" to telefono,
                    "productos" to productosSeleccionados
                )

                db.collection("proveedores").add(proveedor)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Proveedor agregado", Toast.LENGTH_SHORT).show()
                        cargarProveedores(progressBar)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarProveedores(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("proveedores").get()
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
                Toast.makeText(requireContext(), "Error al cargar proveedores", Toast.LENGTH_SHORT).show()
            }
    }
}