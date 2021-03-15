package com.abtasty.flagshipqa.ui.context

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abtasty.flagshipqa.R
import kotlinx.android.synthetic.main.fragment_config.view.edit_visitor_context
import kotlinx.android.synthetic.main.fragment_context.view.*
import org.json.JSONObject


class ContextFragment : Fragment() {

    private lateinit var contextViewModel: ContextViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        contextViewModel = ViewModelProvider(this).get(ContextViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_context, container, false)

        contextViewModel.visitorContext.observe(viewLifecycleOwner, Observer {
            try {
                root.edit_visitor_context.setText(JSONObject(it.toString()).toString(4))
            } catch (e: Exception) {
                root.edit_visitor_context.setText("{\n\n}")
            }
        })

        root.synchronize.setOnClickListener {
            contextViewModel.visitorContext.value =  root.edit_visitor_context.text.toString()
            contextViewModel.synchronize(
                { message ->
                    activity?.runOnUiThread {
                        Toast.makeText(context!!, message, Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    activity?.runOnUiThread {
                        Toast.makeText(context!!, error, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        return root
    }
}