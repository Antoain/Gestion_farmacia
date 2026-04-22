package com.example.gestion_farmacia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class MedicamentosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MedicamentoAdapter
    private val listaMedicamentos = mutableListOf<Medicamento>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicamentos)

        db = FirebaseFirestore.getInstance()

        val recycler = findViewById<RecyclerView>(R.id.recyclerMedicamentos)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        adapter = MedicamentoAdapter(listaMedicamentos) { medicamento ->
            val intent = Intent(this, DetalleMedicamentoActivity::class.java)
            intent.putExtra("id", medicamento.id)
            intent.putExtra("nombre", medicamento.nombre)
            intent.putExtra("categoria", medicamento.categoria)
            intent.putExtra("precio", medicamento.precio)
            intent.putExtra("stock", medicamento.stock)
            intent.putExtra("descripcion", medicamento.descripcion)
            startActivity(intent)
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        findViewById<android.widget.Button>(R.id.btnAgregar).setOnClickListener {
            startActivity(Intent(this, AgregarMedicamentoActivity::class.java))
        }

        cargarMedicamentos(progressBar)
    }

    override fun onResume() {
        super.onResume()
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        cargarMedicamentos(progressBar)
    }

    private fun cargarMedicamentos(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("medicamentos")
            .get()
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
                    listaMedicamentos.add(med)
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }
}