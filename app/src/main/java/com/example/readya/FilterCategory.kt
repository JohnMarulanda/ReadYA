package com.example.readya

import android.view.Display.Mode
import android.widget.ArrayAdapter
import android.widget.Filter

class FilterCategory: Filter  {

    private var filterList: ArrayList<ModelCategory>
    private var adapterCategory: AdapterCategory

    constructor(filterList: ArrayList<ModelCategory>, adapterCategory: AdapterCategory) : super() {
        this.filterList = filterList
        this.adapterCategory = adapterCategory
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        // Si hay un texto de búsqueda
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<ModelCategory> = ArrayList()

            for (i in 0 until filterList.size) {

                // Convertir el nombre de la categoría a mayúsculas y comparar con el filtro
                if (filterList[i].category.uppercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels

        } else {
            // Si el texto de búsqueda está vacío, mostrar todos los elementos
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }


    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        adapterCategory.categoryArrayList = results.values as ArrayList<ModelCategory>

        adapterCategory.notifyDataSetChanged()
    }
}