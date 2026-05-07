package com.example.gestion_farmacia

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicamentoAdapter(
    private val lista: List<Medicamento>,
    private val onClick: (Medicamento) -> Unit
) : RecyclerView.Adapter<MedicamentoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre:    TextView = view.findViewById(R.id.tvNombre)
        val tvCategoria: TextView = view.findViewById(R.id.tvCategoria)
        val tvStock:     TextView = view.findViewById(R.id.tvStock)
        val tvPrecio:    TextView = view.findViewById(R.id.tvPrecio)
        val tvStockBadge: TextView = view.findViewById(R.id.tvStockBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicamento, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = lista[position]
        holder.tvNombre.text    = med.nombre
        holder.tvCategoria.text = med.categoria
        holder.tvStock.text     = "Stock: ${med.stock} unidades"
        holder.tvPrecio.text    = "$${String.format("%.2f", med.precio)}"

        // Badge de stock bajo
        if (med.stock < 10) {
            holder.tvStockBadge.visibility = View.VISIBLE
            holder.tvStock.setTextColor(Color.parseColor("#EA5455"))
        } else {
            holder.tvStockBadge.visibility = View.GONE
            holder.tvStock.setTextColor(Color.parseColor("#1F7A5C"))
        }

        holder.itemView.setOnClickListener { onClick(med) }
    }

    override fun getItemCount() = lista.size
}
