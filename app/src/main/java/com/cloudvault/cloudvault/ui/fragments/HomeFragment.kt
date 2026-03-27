package com.cloudvault.cloudvault.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudvault.cloudvault.R
import com.cloudvault.cloudvault.adapter.FileAdapter

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dummyFiles = listOf("Document1.pdf", "Image.jpg", "Spreadsheet.xlsx", "Presentation.pptx")
        val adapter = FileAdapter(dummyFiles)
        recyclerView.adapter = adapter

        return view
    }
}
