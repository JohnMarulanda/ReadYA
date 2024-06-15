package com.example.readya

import android.widget.Filter

class FilterPdfUser : Filter {
    // ArrayList in which we want to search
    private var filterList: ArrayList<ModelPdf>
    // Adapter in which filter needs to be implemented
    private var adapterPdfUser: AdapterPdfUser

    // Constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            //val searchQuery = constraint.toString().lowercase()

            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()

            for (i in filterList.indices) {
                //validate if match
                if (filterList[i].title.uppercase().contains(constraint)) {
                    //searched value matched with title, add to list
                    filteredModels.add(filterList[i])
                }

                //return filtered list and size
                results.count = filteredModels.size
                results.values = filteredModels
            }

            // Set the filtered result
            results.values = filteredModels
            results.count = filteredModels.size
        } else {
            // If the search query is empty, return the original list
            results.values = filterList
            results.count = filterList.size
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        //apply filter changes
        if (results != null) {
            adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>
        }

        //notify changes
        adapterPdfUser.notifyDataSetChanged()
    }
}
