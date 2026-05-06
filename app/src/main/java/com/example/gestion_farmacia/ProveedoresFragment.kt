package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
            val intent = Intent(requireContext(), DetalleProveedorActivity::class.java)
            intent.putExtra("id", proveedor.id)
            intent.putExtra("nombre", proveedor.nombre)
            intent.putExtra("telefono", proveedor.telefono)
            intent.putExtra("productos", proveedor.productos)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<Button>(R.id.btnAgregarProveedor).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarProveedorActivity::class.java))
        }

        cargarProveedores(progressBar)
    }

    override fun onResume() {
        super.onResume()
        view?.let { cargarProveedores(it.findViewById(R.id.progressBar)) }
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