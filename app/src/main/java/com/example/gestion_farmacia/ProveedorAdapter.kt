package com.example.gestion_farmacia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProveedorAdapter(
    private val lista: List<Proveedor>,
    private val onEliminar: (Proveedor) -> Unit
) : RecyclerView.Adapter<ProveedorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProveedor)
        val tvTelefono: TextView = view.findViewById(R.id.tvTelefono)
        val tvProductos: TextView = view.findViewById(R.id.tvProductos)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarProveedor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proveedor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val prov = lista[position]
        holder.tvNombre.text = prov.nombre
        holder.tvTelefono.text = "📞 ${prov.telefono}"
        holder.tvProductos.text = "Productos: ${prov.productos}"
        holder.btnEliminar.setOnClickListener { onEliminar(prov) }
    }

    override fun getItemCount() = lista.size
}