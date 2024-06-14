package com.example.readya

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.readya.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class CategoryAddActivity : AppCompatActivity() {


    //view binding
    private lateinit var binding: ActivityCategoryAddBinding
    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()

        // Configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor, espera...")
        progressDialog.setCanceledOnTouchOutside(false)


        // Manejar clic en el botón de retroceso
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Manejar clic en el botón de enviar
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var category = ""

    private fun validateData(){
        //validate data

        category = binding.categoryEt.text.toString().trim()

        if (category.isEmpty()){
            Toast.makeText(this, "Ingresa la categoría", Toast.LENGTH_SHORT).show()
        }
        else {
            addCategoryFirebase()
        }
    }

    private fun addCategoryFirebase() {

        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        // Configurar datos para agregar en Firebase DB
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = timestamp.toString() // Convertir timestamp a String para usarlo como ID
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        // Obtener referencia a la instancia de Firebase Database y la ruta "Categories"
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        // Agregar datos a Firebase DB bajo la ruta con el timestamp como clave
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Añadido correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "No se ha podido añadir debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

}