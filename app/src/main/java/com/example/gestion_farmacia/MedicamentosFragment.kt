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

class MedicamentosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MedicamentoAdapter
    private val listaMedicamentos = mutableListOf<Medicamento>()
    private var soloStockBajo = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medicamentos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        soloStockBajo = arguments?.getBoolean("soloStockBajo", false) ?: false

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerMedicamentos)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        adapter = MedicamentoAdapter(listaMedicamentos) { medicamento ->
            val intent = Intent(requireContext(), DetalleMedicamentoActivity::class.java)
            intent.putExtra("id", medicamento.id)
            intent.putExtra("nombre", medicamento.nombre)
            intent.putExtra("categoria", medicamento.categoria)
            intent.putExtra("precio", medicamento.precio)
            intent.putExtra("stock", medicamento.stock)
            intent.putExtra("descripcion", medicamento.descripcion)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        view.findViewById<Button>(R.id.btnAgregar).setOnClickListener {
            startActivity(Intent(requireContext(), AgregarMedicamentoActivity::class.java))
        }

        cargarMedicamentos(progressBar)
    }

    override fun onResume() {
        super.onResume()
        // Al volver, quitar el filtro de stock bajo
        soloStockBajo = false
        view?.let { cargarMedicamentos(it.findViewById(R.id.progressBar)) }
    }

    private fun cargarMedicamentos(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("medicamentos").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                listaMedicamentos.clear()
                for (doc in result) {
                    val med = Medicamento(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        categoria = doc.getString("categoria") ?: "",
                        precio = doc.getDouble("precio") ?: 0.0,
                        stock = doc.getLong("stock")?.toInt() ?: 0,
                        descripcion = doc.getString("descripcion") ?: ""
                    )
                    // Si viene el filtro, solo agrega los de stock bajo
                    if (!soloStockBajo || med.stock < 10) {
                        listaMedicamentos.add(med)
                    }
                }
                adapter.notifyDataSetChanged()

                if (soloStockBajo) {
                    Toast.makeText(requireContext(), "Mostrando medicamentos con stock bajo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }
}