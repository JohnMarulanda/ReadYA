package com.example.readya

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.readya.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityPdfEditBinding

    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }

    // Book id from intent started from AdapterPdfAdmin
    private var bookId = ""

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    // ArrayList to hold category titles
    private lateinit var categoryTitleArrayList: ArrayList<String>

    // ArrayList to hold category ids
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ArrayLists
        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        // Get book id to edit the book info
        bookId = intent.getStringExtra("bookId")!!

        // Setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espera por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        // Handle click, go back
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        // Handle click, pick category
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        // Handle click, begin update
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Cargando informacion del libro")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // Get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    // Set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    // Load book category info using categoryID
                    Log.d(TAG, "onDataChange: Cargando categoria del libro")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                // Get category
                                val category = snapshot.child("category").value
                                // Set to TextView
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle error
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    private var title = ""
    private var description = ""
    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""

    private fun validateData() {
        // Get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        // Validate data
        if (title.isEmpty()) {
            Toast.makeText(this, "Ingresar titulo", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Ingresar description", Toast.LENGTH_SHORT).show()
        } else if (selectedCategoryId.isEmpty()) {
            Toast.makeText(this, "Selecciona categoria", Toast.LENGTH_SHORT).show()
        } else {
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Starting updating pdf info...")

        // Show progress
        progressDialog.setMessage("Actualizando informacion del libro...")
        progressDialog.show()

        // Setup data to update to db, spellings of keys must be same as in firebase
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = title
        hashMap["description"] = description
        hashMap["categoryId"] = selectedCategoryId

        // Start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "updatePdf: Updated successfully...")
                Toast.makeText(this, "Updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun categoryDialog() {
        // Show dialog to pick the category of pdf/book we already got the categories
        if (::categoryTitleArrayList.isInitialized) {
            val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
            for (i in categoryTitleArrayList.indices) {
                categoriesArray[i] = categoryTitleArrayList[i]
            }

            // Alert dialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose Category")
                .setItems(categoriesArray) { dialog, position ->
                    // Handle click, save clicked category id and title
                    selectedCategoryId = categoryIdArrayList[position]
                    selectedCategoryTitle = categoryTitleArrayList[position]

                    // Set to TextView
                    binding.categoryTv.text = selectedCategoryTitle
                }
                .show()
        } else {
            Toast.makeText(this, "Categories not loaded yet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories")

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear list before starting adding data into them
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {
                    val id = ds.child("id").value.toString()
                    val category = ds.child("category").value.toString()

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category Id $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
