package com.example.readya

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.readya.databinding.RowCategoryBinding
import com.google.firebase.database.FirebaseDatabase

class AdapterCategory :RecyclerView.Adapter<AdapterCategory.HolderCategory>, Filterable{

    private val context: Context
    public var categoryArrayList: ArrayList<ModelCategory>
    private var filterList: ArrayList<ModelCategory>

    private var filter: FilterCategory? = null

    private lateinit var binding: RowCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<ModelCategory>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderCategory {
        // Inflar el layout usando ViewBinding
        binding = RowCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HolderCategory(binding.root)
    }


    override fun onBindViewHolder(holder: HolderCategory, position: Int) {
        // Obtener el modelo de datos en la posición específica
        val model = categoryArrayList[position]

        // Obtener datos específicos del modelo
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        // Establecer los datos en las vistas del ViewHolder
        holder.categoryTv.text = category

        // Manejar clic en el botón de eliminar categoría
        holder.deleteBtn.setOnClickListener {
            // Mostrar un diálogo de confirmación antes de eliminar
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Borrar")
                .setMessage("¿Estas seguro que deseas borrar esta categoría?")
                .setPositiveButton("Confirmar") { a, d ->
                    Toast.makeText(holder.itemView.context, "Borrando...", Toast.LENGTH_SHORT).show()
                    // Aquí deberías llamar a una función o método para eliminar la categoría
                    deleteCategory(model, holder) // Suponiendo que tienes una función para eliminar categorías
                }
                .setNegativeButton("Cancelar") { a, d ->
                    a.dismiss()
                }
                .show()
        }
    }

    // Función para eliminar la categoría (debes implementarla según tu lógica)
    private fun deleteCategory(model: ModelCategory, holder: HolderCategory) {
        val id = model.id

        // Obtener referencia a la instancia de Firebase Database y la ruta "Categories"
        val ref = FirebaseDatabase.getInstance().getReference("Categories")

        // Eliminar la categoría de la base de datos
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                // Éxito al eliminar la categoría
                Toast.makeText(context, "Categoria borrada correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Error al eliminar la categoría
                Toast.makeText(context, "Fallo al eliminar la categoria: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getItemCount(): Int {
       return categoryArrayList.size
    }



    inner class HolderCategory(itemView: View): RecyclerView.ViewHolder(itemView){
       var categoryTv:TextView = binding.categoryTv
        var deleteBtn:ImageButton = binding.deleteBtn
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterCategory(filterList, this)
        }
        return filter as FilterCategory
    }


}