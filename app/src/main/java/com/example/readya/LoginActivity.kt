package com.example.readya
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.readya.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LoginActivity : AppCompatActivity() {

    private var email = ""
    private var password = ""

    //viewBinding
    private lateinit var  binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el TextView con SpannableString
        val appNameTextView = findViewById<TextView>(R.id.appName)
        val text = "Readya"
        val spannableString = SpannableString(text)

        // Establecer "Readya" en bold
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Cambiar el color de "ya" a naranja
        val orangeColor: Int = Color.parseColor("#FFA500")
        spannableString.setSpan(
            ForegroundColorSpan(orangeColor),
            4,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        appNameTextView.text = spannableString

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while login user

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espera")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, not have account, goto register screen
        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //begin login
        binding.loginBtn.setOnClickListener {
            /*
            * 1) Input data
            * 2) Validate data
            * 3) Login - Firebase Auth
            * 4) Check user type - Firebase Auth
            *       If user - Move to user dashboard
            *       If admin - Move to admin dashboard
            *  */
            email = binding.emailEt.text.toString().trim()
            password = binding.passwordEt.text.toString().trim()

            validateData(email, password)
        }
    }

    private fun validateData(email: String, password: String) {
        when {
            email.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu correo electrónico...", Toast.LENGTH_SHORT).show()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                Toast.makeText(this, "Ingresa un correo electrónico válido...", Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Ingresa tu contraseña...", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Si todos los campos son válidos, procede con el registro
                loginUser()
            }
        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //login success
                checkUser()
            }
            .addOnFailureListener { e->
                //failed login
                progressDialog.dismiss()
                Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
            }
    }

    private  fun checkUser() {

        progressDialog.setMessage("Verificando usuario...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    //get user type
                    val userType = snapshot.child("userType").value

                    if (userType == "user") {
                        //open user dashboard
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                        finish()
                    }
                    else if (userType == "admin") {
                        //open admin dashboard
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}