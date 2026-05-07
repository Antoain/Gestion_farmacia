package com.example.gestion_farmacia

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmpleadoAdapter(
    private val lista: List<Empleado>,
    private val onToggleActivo: (Empleado, Boolean) -> Unit
) : RecyclerView.Adapter<EmpleadoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre:  TextView = view.findViewById(R.id.tvNombreEmpleado)
        val tvEmail:   TextView = view.findViewById(R.id.tvEmailEmpleado)
        val tvEstado:  TextView = view.findViewById(R.id.tvEstadoEmpleado)
        val swActivo:  Switch   = view.findViewById(R.id.swActivo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emp = lista[position]
        holder.tvNombre.text = emp.nombre
        holder.tvEmail.text  = emp.email

        if (emp.activo) {
            holder.tvEstado.text      = "● Activo"
            holder.tvEstado.setTextColor(Color.parseColor("#28C76F"))
        } else {
            holder.tvEstado.text      = "● Inactivo"
            holder.tvEstado.setTextColor(Color.parseColor("#EA5455"))
        }

        // Evitar disparar el listener al hacer notifyDataSetChanged
        holder.swActivo.setOnCheckedChangeListener(null)
        holder.swActivo.isChecked = emp.activo
        holder.swActivo.setOnCheckedChangeListener { _, isChecked ->
            onToggleActivo(emp, isChecked)
        }
    }

    override fun getItemCount() = lista.size
}
