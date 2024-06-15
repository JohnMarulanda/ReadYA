package com.example.readya

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.readya.databinding.ActivityPdfAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    // Binding activity
    private lateinit var binding: ActivityPdfAddBinding

    // Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    // Arraylist to hold pdf categories
    private lateinit var categoryArrayList: ArrayList<ModelCategory>

    // Uri of picked pfd
    private var pdfUri: Uri? = null

    // Tag
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        // Setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Por favor espere")
        progressDialog.setCanceledOnTouchOutside(false)

        // handle click, show category pick dialog
        binding.categoryTv.setOnClickListener{
            categoryPickDialog()
        }

        // handle click, pick pdf intent
        binding.attachPdfBtn.setOnClickListener{
            pdfPickIntent()
        }

        //handle click, start uploading pdf/book
        binding.submitBtn.setOnClickListener{
            validateData()
        }

        // Handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private var title = ""
    private var description = ""
    private var category = ""

    private fun validateData() {
        Log.d(TAG, "validateData: Validando información")

        // Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        category = binding.categoryTv.text.toString().trim()

        // Validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Ingresa un título", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Ingresa una descripción", Toast.LENGTH_SHORT).show()
        }
        else if (category.isEmpty()){
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
        }
        else if (pdfUri == null) {
            Toast.makeText(this, "Selecciona un PDF", Toast.LENGTH_SHORT).show()
        }
        else {
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: Subiendo archivo")

        // Progress dialog
        progressDialog.setMessage("Subiendo PDF")
        progressDialog.show()

        // Timestamp
        val timestamp = System.currentTimeMillis()

        // Path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"

        // Storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener {taskSnapshot ->
                Log.d(TAG, "uploadPdfToStorage: Obteniendo URL del archivo subido")
                val uriTask:Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"
                
                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)
            }
            .addOnFailureListener{e ->
                Log.d(TAG, "uploadPdfToStorage: Fallo al subir debido a ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo subir el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        Log.d(TAG, "uploadPdfInfoToDb: Guardando información en la base de datos")
        progressDialog.setMessage("Subiendo información del PDF...")

        // uid of user
        val uid = firebaseAuth.uid

        // setup data to upload
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId
        hashMap["url"] = uploadedPdfUrl
        hashMap["timestamp"] = timestamp
        hashMap["viewsCount"] = 0
        hashMap["downloadsCount"] = 0

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "uploadPdfInfoToDb: Archivo subido a la base de datos")
                progressDialog.dismiss()
                Toast.makeText(this, "Cargado con éxito", Toast.LENGTH_SHORT).show()
                pdfUri = null
                onBackPressed()
            }
            .addOnFailureListener{e ->
                Log.d(TAG, "uploadPdfInfoToDb: Fallo al subir debido a ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "No se pudo subir el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: Cargando categorías")
        // init arraylist
        categoryArrayList = ArrayList()

        //db reference to load categoríes DF > Categories
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list before adding data
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(ModelCategory::class.java)
                    // Add to arraylist
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: Mostrando cuadro de dialogo de elegir categorías")

        // Get string array of categories from arraylist
        val categoriesArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoriesArray[i] = categoryArrayList[i].category
        }

        // alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una categoría")
            .setItems(categoriesArray) {dialog, which ->
                // handle item click

                // get clicked item
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryId = categoryArrayList[which].id

                binding.categoryTv.text = selectedCategoryTitle

                Log.d(TAG, "categoryPickDialog: Id de categoría seleccionada: $selectedCategoryId")
                Log.d(TAG, "categoryPickDialog: Título de categoría seleccionada: $selectedCategoryTitle")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Selección de pdf iniciada")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if (result.resultCode == RESULT_OK) {
                Log.d(TAG, "PDF seleccionado")
                pdfUri = result.data!!.data
            }
            else {
                Log.d(TAG, "Selección de PDF cancelada")
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )
}