package com.example.gestion_farmacia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val tvUsuario = view.findViewById<TextView>(R.id.tvUsuario)
        tvUsuario.text = auth.currentUser?.email ?: "Usuario"

        cargarEstadisticas(view)

        // Click en stock bajo → ir a Medicamentos filtrando stock bajo
        view.findViewById<CardView>(R.id.cardStockBajo).setOnClickListener {
            val bundle = Bundle().apply { putBoolean("soloStockBajo", true) }
            findNavController().navigate(R.id.medicamentosFragment, bundle)
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { cargarEstadisticas(it) }
    }

    private fun cargarEstadisticas(view: View) {
        val tvTotalMedicamentos = view.findViewById<TextView>(R.id.tvTotalMedicamentos)
        val tvTotalProveedores = view.findViewById<TextView>(R.id.tvTotalProveedores)
        val tvStockBajo = view.findViewById<TextView>(R.id.tvStockBajo)

        db.collection("medicamentos").get()
            .addOnSuccessListener { result ->
                tvTotalMedicamentos.text = result.size().toString()
                val stockBajo = result.count { doc ->
                    (doc.getLong("stock") ?: 0L) < 10
                }
                tvStockBajo.text = stockBajo.toString()
            }

        db.collection("proveedores").get()
            .addOnSuccessListener { result ->
                tvTotalProveedores.text = result.size().toString()
            }
    }
}