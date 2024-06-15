package com.example.readya

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale

class MyApplication:Application() {
    override fun onCreate() {
        super.onCreate()
    }

    companion object{
        fun formatTimeStamp(timestamp: Long) : String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String?, pdfTitle: String, sizeTv: TextView) {
            val TAG = "PDF_SIZE_TAG"
            if (pdfUrl.isNullOrEmpty()) {
                Log.e(TAG, "URL is null or empty")
                return
            }

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetaData ->
                    Log.d(TAG, "loadPdfSize: Metadatos recibidos")
                    val bytes = storageMetaData.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize: Size bytes $bytes")

                    // Convert bytes to KB/MB
                    val kb = bytes / 1024
                    val mb = kb / 1024
                    if (mb >= 1) {
                        sizeTv.text = String.format("%.2f MB", mb)
                    } else if (kb >= 1) {
                        sizeTv.text = String.format("%.2f KB", kb)
                    } else {
                        sizeTv.text = String.format("%.2f bytes", bytes)
                    }
                }
                .addOnFailureListener {e ->
                    Log.d(TAG, "loadPdfSize: ${e.message}")
                }
        }

        fun loadPdfFromUrlSinglePage(pdfUrl: String?, pdfTitle: String, pdfView: PDFView, progressBar: ProgressBar, pagesTv: TextView?) {
            val TAG = "PDF_THUMBNAIL_TAG"
            if (pdfUrl.isNullOrEmpty()) {
                Log.e(TAG, "URL is null or empty")
                return
            }

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTES_PDF)
                .addOnSuccessListener {bytes ->
                    Log.d(TAG, "loadPdfFromUrlSinglePage: Size bytes $bytes")

                    // Set to pdfview
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError{t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onPageError{page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message}")
                        }
                        .onLoad{nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: Pages: $nbPages")
                            progressBar.visibility = View.INVISIBLE
                            pagesTv?.text = "$nbPages"
                        }
                        .load()
                }
                .addOnFailureListener {e ->
                    Log.e(TAG, "Failed to load PDF", e)
                    Log.d(TAG, "loadPdfFromUrlSinglePage: ${e.message}")
                }
        }

        fun loadCategory(categoryId: String, categoryTv: TextView) {
            // load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //Get Category
                        val category = "${snapshot.child("category").value}"

                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MyApplication", "Error loading category", error.toException())
                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookUrl: String, bookTitle: String){
            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Borrar...")

            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Por favor espera")
            progressDialog.setMessage("Borrando $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: Borrando del bd...")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Borrado del almacenamiento")
                    Log.d(TAG, "deleteBook: Borrado de la base de datos ahora...")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener { 
                            progressDialog.dismiss()
                            Toast.makeText(context, "Borrado exitosamente...", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "deleteBook: Borrado de la bd también...")
                        }
                }
                .addOnFailureListener{e->
                    Log.d(TAG, "deleteBook: Falló en borrar del almacenamiento de ${e.message}")
                    Toast.makeText(context, "Falló en borrar del almacenamiento de ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementBookViewCount(bookId: String) {
            //1) Get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if (viewsCount == "" || viewsCount=="null") {
                            viewsCount = "0"
                        }

                        //2 increment views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }
}
