package com.example.readya

import android.app.Application
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
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
    }
}
