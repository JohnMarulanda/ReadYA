package com.example.readya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.readya.databinding.ActivityPdfDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfDetailActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding:ActivityPdfDetailBinding

    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //book id
    private var bookId = ""
    // get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    // boolean to store if its in favorites
    private var isInMyFavorite = false

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get  book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser != null) {
            //user is logged in, check if book is in fav or not
            checkIsFavorite()
        }


        //increment book view count, whenever this page starts
        MyApplication.incrementBookViewCount(bookId)
        loadBookDetails()

        //hanlde backbutton click, goback
        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        //handle click, open pdf view activity
        binding.readBookBtn.setOnClickListener{
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener{
            if(firebaseAuth.currentUser == null){
                Toast.makeText(this, "No estás logueado", Toast.LENGTH_SHORT).show()
            }
            else{
                if (isInMyFavorite) {
                    removeFromFavorite()
                }
                else {
                    addToFavorite()
                }
            }
        }
    }

    private fun loadBookDetails() {
        //Books > bookId > Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("url").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)
                    //load pdf thumbnail, pages count
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", binding.pdfView, binding.progressBar, binding.pagesTv)
                    //load pdf size
                    MyApplication.loadPdfSize("$url", "$title", binding.sizeTv)

                    //set data
                    binding.titleTv.text = title
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Verificando si el libro está en favoritos")
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        Log.d(TAG, "onDataChange: Añadido a favoritos")
                        binding.favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_filled_white, 0, 0)
                        binding.favoriteBtn.text = "Quitar favorito"
                    }
                    else {
                        Log.d(TAG, "onDataChange: Quitado de favoritos")
                        binding.favoriteBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_favorite_border_white, 0, 0)
                        binding.favoriteBtn.text = "Añadir a favoritos"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Añadiendo a favoritos")
        val timestamp = System.currentTimeMillis()

        // setup data to add in db
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: Añadido a favoritos")
                Toast.makeText(this, "Añadido a favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener{e ->
                Log.d(TAG, "addToFavorite: Error al añadir: ${e.message}")
                Toast.makeText(this, "Error al añadir: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeFromFavorite() {
        Log.d(TAG, "removeFromFavorite: Quitando de favoritos")

        // database ref
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavorite: Quitado de favoritos")
                Toast.makeText(this, "Quitado de favoritos", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {e ->
                Log.d(TAG, "removeFromFavorite: Error al quitar de favoritos: ${e.message}")
                Toast.makeText(this, "Error al quitar de favoritos: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}