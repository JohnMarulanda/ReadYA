package com.example.readya

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.readya.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private var username = ""
    private var email = ""
    private var password = ""

    //view binging
    private lateinit var binding:ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espera")
        progressDialog.setCanceledOnTouchOutside(false)

        //hanlde back button click, previous screen
        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        //handle click, begin register
        binding.registerBtn.setOnClickListener{

            username = binding.nameEt.text.toString().trim()
            email = binding.emailEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()
            val cPassword = binding.cPasswordEt.text.toString().trim()
            validateData(username, email, password, cPassword)
        }

    }

    private fun validateData(username: String, email: String, password: String, cPassword: String) {
        when {
            email.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu correo electrónico...", Toast.LENGTH_SHORT).show()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Ingresa un correo electrónico válido...", Toast.LENGTH_SHORT).show()
            }
            username.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu usuario...", Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu contraseña...", Toast.LENGTH_SHORT).show()
            }
            password.length < 6 -> {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres...", Toast.LENGTH_SHORT).show()
            }
            cPassword.isEmpty() -> {
                Toast.makeText(this, "Confirma la contraseña...", Toast.LENGTH_SHORT).show()
            }
            password != cPassword -> {
                Toast.makeText(this, "Las contraseñas no coinciden...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Si todos los campos son válidos, procede con el registro
                createUserAccount()
            }
        }
    }

    private fun createUserAccount() {
        //show progress
        progressDialog.setMessage("Creando cuenta...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo()
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al crear la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private  fun updateUserInfo() {
        progressDialog.setMessage("Guardando información del usuario")

        //timestamp

        val timestamp = System.currentTimeMillis()

        //get current user uid

        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["profileImage"] = "" //add empty, will be edited in profile
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        //set data to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Cuenta creada...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Error guardando la información del usuario debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

}