package com.example.readya

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.readya.databinding.ActivityDashboardAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class DashboardAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    private lateinit var adapterCategory: AdapterCategory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()
        loadCategories()
        //handle click, logout
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //search
        binding.searchEt.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //called as and when user type anything

                try {
                    adapterCategory.filter.filter(s)
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }

        })

        //handle click, start add category page
        binding.addCategoryBtn.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            //not logged in, goto main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            //logged in, get and show user info
            val email = firebaseUser.email
            //set to text view of toolbar
            binding.subTitleTv.text = email
        }
    }

    private fun loadCategories() {
        // Inicializar el ArrayList
        categoryArrayList = ArrayList()

        // Obtener todas las categorías desde Firebase Database
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Limpiar la lista antes de agregar datos nuevos
                categoryArrayList.clear()

                // Iterar a través de los hijos (categorías) en el snapshot
                for (ds in snapshot.children) {
                    // Obtener datos y convertir a modelo (ModelCategory)
                    val model = ds.getValue(ModelCategory::class.java)
                        // Agregar el modelo a la lista
                        categoryArrayList.add(model!!)
                    }
                // Inicializar o actualizar el adaptador con los datos cargados
                adapterCategory = AdapterCategory(this@DashboardAdminActivity, categoryArrayList)
                binding.categoriesRv.adapter = adapterCategory
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

}
