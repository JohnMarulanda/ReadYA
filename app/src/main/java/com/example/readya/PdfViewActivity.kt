package com.example.readya

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.readya.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfViewActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPdfViewBinding

    private companion object {
        const val TAG = "PDF_VIEW_TAG"
    }

    // Book id
    var bookId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get book id from intent
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }

    private fun loadBookDetails() {
        Log.d(TAG, "LoadBookDetails: Se consigio el pdf desde la bd")

        // Database Reference para obtener los detalles del libro, por ejemplo, obtener la URL del libro usando el ID del libro
        // Paso (1) Obtener la URL del libro usando el ID del libro
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Obtener la URL del libro
                val pdfUrl = snapshot.child("url").value
                Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                // Paso (2) Cargar el PDF usando la URL desde Firebase Storage
                loadBookFromUrl("$pdfUrl")
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar el error de cancelación si es necesario
                Log.e(TAG, "Error al cargar detalles del libro: ${error.message}")
            }
        })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        Log.d(TAG, "LoadBookFromUrl: Get Pdf from firebase storage using URL")

        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(Constants.MAX_BYTES_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "LoadBookFromUrl: PDF got from URL")

                // Cargar el PDF en la vista PDFView
                binding.pdfView.fromBytes(bytes)
                    .swipeHorizontal(false) // Deshabilitar desplazamiento horizontal para desplazarse verticalmente
                    .onPageChange { page, pageCount ->
                        // Establecer las páginas actual y total en el subtítulo de la barra de herramientas
                        val currentPage = page + 1 // Las páginas empiezan desde 0, así que sumamos 1 para mostrar la página actual
                        binding.toolbarSubtitleTv.text = "$currentPage/$pageCount"
                        Log.d(TAG, "LoadBookFromUrl: $currentPage/$pageCount")
                    }
                    .onError { t ->
                        Log.d(TAG, "LoadBookFromUrl: ${t.message}")
                    }
                    .onPageError { page, t ->
                        Log.d(TAG, "LoadBookFromUrl: ${t.message}")
                    }
                    .load()

                // Ocultar la barra de progreso después de cargar el PDF
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "LoadBookFromUrl: Failed to get PDF due to ${e.message}")
                // Manejar el fallo de carga del PDF aquí si es necesario
            }
    }

}