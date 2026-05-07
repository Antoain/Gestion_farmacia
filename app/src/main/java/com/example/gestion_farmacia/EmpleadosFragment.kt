package com.example.gestion_farmacia

import android.app.AlertDialog
import android.os.Bundle
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EmpleadosFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: EmpleadoAdapter
    private val listaEmpleados = mutableListOf<Empleado>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_empleados, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db   = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val recycler    = view.findViewById<RecyclerView>(R.id.recyclerEmpleados)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val btnAgregar  = view.findViewById<Button>(R.id.btnAgregarEmpleado)

        adapter = EmpleadoAdapter(listaEmpleados) { empleado, nuevoEstado ->
            toggleActivoEmpleado(empleado, nuevoEstado)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnAgregar.setOnClickListener {
            mostrarDialogAgregarEmpleado()
        }

        cargarEmpleados(progressBar)
    }

    override fun onResume() {
        super.onResume()
        view?.let { cargarEmpleados(it.findViewById(R.id.progressBar)) }
    }

    private fun cargarEmpleados(progressBar: ProgressBar) {
        progressBar.visibility = View.VISIBLE
        db.collection("usuarios")
            .whereEqualTo("rol", "empleado")
            .get()
            .addOnSuccessListener { result ->
                progressBar.visibility = View.GONE
                listaEmpleados.clear()
                for (doc in result) {
                    listaEmpleados.add(
                        Empleado(
                            uid    = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            email  = doc.getString("email")  ?: "",
                            activo = doc.getBoolean("activo") ?: true
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Error al cargar empleados", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleActivoEmpleado(empleado: Empleado, activo: Boolean) {
        db.collection("usuarios").document(empleado.uid)
            .update("activo", activo)
            .addOnSuccessListener {
                val msg = if (activo) "Empleado activado" else "Empleado desactivado"
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                // Actualizar la lista local
                val idx = listaEmpleados.indexOfFirst { it.uid == empleado.uid }
                if (idx >= 0) {
                    listaEmpleados[idx] = listaEmpleados[idx].copy(activo = activo)
                    adapter.notifyItemChanged(idx)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun mostrarDialogAgregarEmpleado() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_agregar_empleado, null)

        val etNombre   = dialogView.findViewById<EditText>(R.id.etNombreEmpleado)
        val etEmail    = dialogView.findViewById<EditText>(R.id.etEmailEmpleado)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPasswordEmpleado)

        AlertDialog.Builder(requireContext())
            .setTitle("Agregar Empleado")
            .setView(dialogView)
            .setPositiveButton("Agregar") { dialog, _ ->
                val nombre   = etNombre.text.toString().trim()
                val email    = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (password.length < 6) {
                    Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                crearEmpleado(nombre, email, password)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun crearEmpleado(nombre: String, email: String, password: String) {
        // Guardar admin actual para re-autenticarse después
        val adminEmail    = auth.currentUser?.email ?: return

        // Crear usuario en Firebase Auth
        // NOTA: createUserWithEmailAndPassword cierra la sesión del admin actual.
        // Por eso guardamos el email admin y pedimos re-login después,
        // o bien usamos Firebase Admin SDK (backend). La solución más simple
        // para una app de curso es usar un segundo FirebaseApp instance.
        // Aquí usamos la solución directa con re-login.

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                val datos = hashMapOf(
                    "nombre" to nombre,
                    "email"  to email,
                    "rol"    to "empleado",
                    "activo" to true
                )

                db.collection("usuarios").document(uid)
                    .set(datos)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Empleado creado exitosamente", Toast.LENGTH_SHORT).show()
                        // El admin queda deslogueado por createUser; hay que re-loguear
                        // Mostramos un Toast informativo y redirigimos al Login
                        Toast.makeText(
                            requireContext(),
                            "Por seguridad, vuelve a iniciar sesión como administrador.",
                            Toast.LENGTH_LONG
                        ).show()
                        auth.signOut()
                        SessionManager(requireContext()).cerrarSesion()
                        startActivity(
                            android.content.Intent(requireContext(), LoginActivity::class.java)
                                .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        )
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al guardar en BD: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al crear usuario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
