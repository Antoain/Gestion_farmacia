package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MedicamentosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var session: SessionManager
    private lateinit var adapter: MedicamentoAdapter

    private val listaTodos      = mutableListOf<Medicamento>()   // lista completa de Firestore
    private val listaFiltrada   = mutableListOf<Medicamento>()   // lista que muestra el adapter
    private var soloStockBajo   = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_medicamentos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db      = FirebaseFirestore.getInstance()
        session = SessionManager(requireContext())
        soloStockBajo = arguments?.getBoolean("soloStockBajo", false) ?: false

        val recycler    = view.findViewById<RecyclerView>(R.id.recyclerMedicamentos)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnAgregar  = view.findViewById<Button>(R.id.btnAgregar)
        val etBuscar    = view.findViewById<EditText>(R.id.etBuscar)

        // El botón Agregar solo es visible para el admin
        btnAgregar.visibility = if (session.esAdmin()) View.VISIBLE else View.GONE
        btnAgregar.setOnClickListener {
            startActivity(Intent(requireContext(), AgregarMedicamentoActivity::class.java))
        }

        // Adapter: al tocar un medicamento, abrir detalle pasando el rol
        adapter = MedicamentoAdapter(listaFiltrada) { medicamento ->
            val intent = Intent(requireContext(), DetalleMedicamentoActivity::class.java).apply {
                putExtra("id",          medicamento.id)
                putExtra("nombre",      medicamento.nombre)
                putExtra("categoria",   medicamento.categoria)
                putExtra("precio",      medicamento.precio)
                putExtra("stock",       medicamento.stock)
                putExtra("descripcion", medicamento.descripcion)
                putExtra("esAdmin",     session.esAdmin())   // <-- nuevo
            }
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Búsqueda en tiempo real
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filtrar(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        cargarMedicamentos(progressBar)
    }

    override fun onResume() {
        super.onResume()
        soloStockBajo = false
        view?.let { cargarMedicamentos(it.findViewById(R.id.progressBar)) }
    }

    private fun cargarMedicamentos(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("medicamentos").get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                listaTodos.clear()
                for (doc in result) {
                    val med = Medicamento(
                        id          = doc.id,
                        nombre      = doc.getString("nombre")      ?: "",
                        categoria   = doc.getString("categoria")   ?: "",
                        precio      = doc.getDouble("precio")      ?: 0.0,
                        stock       = doc.getLong("stock")?.toInt() ?: 0,
                        descripcion = doc.getString("descripcion") ?: ""
                    )
                    if (!soloStockBajo || med.stock < 10) {
                        listaTodos.add(med)
                    }
                }
                // Aplicar filtro actual del buscador
                val query = view?.findViewById<EditText>(R.id.etBuscar)?.text.toString()
                filtrar(query)

                if (soloStockBajo) {
                    Toast.makeText(requireContext(), "Mostrando medicamentos con stock bajo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrar(query: String) {
        listaFiltrada.clear()
        if (query.isEmpty()) {
            listaFiltrada.addAll(listaTodos)
        } else {
            val q = query.lowercase()
            listaFiltrada.addAll(
                listaTodos.filter {
                    it.nombre.lowercase().contains(q) ||
                            it.categoria.lowercase().contains(q)
                }
            )
        }
        adapter.notifyDataSetChanged()
    }
}
