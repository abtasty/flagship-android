package com.abtasty.flagshipqa.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abtasty.flagshipqa.R
import kotlinx.android.synthetic.main.fragment_config.*
import kotlinx.android.synthetic.main.fragment_config.view.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import kotlinx.android.synthetic.main.fragment_modifications.view.*
import org.json.JSONObject


class EventFragment : Fragment() {

    private lateinit var eventViewModel: EventViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_event, container, false)
//        root.edit_text_api_key.setText(dashboardViewModel.api_key)
//        dashboardViewModel.data.observe(viewLifecycleOwner, Observer {
//
//        })
        root.spinner_event.adapter = ArrayAdapter(requireContext(), R.layout.spinner_elem, eventViewModel.actions)
        root.button_page.setOnClickListener {
            eventViewModel.sendScreen(root.edit_text_interface.text.toString())
        }
        root.button_event.setOnClickListener {
            eventViewModel.sendEvent(
                root.spinner_event.selectedItem.toString(),
                root.edit_text_event_action.text.toString()
            )
        }
        root.button_transaction.setOnClickListener {
            eventViewModel.sendTransaction(
                root.edit_text_item_transaction_id.text.toString(),
                root.edit_text_affiliation.text.toString()
            )
        }
        root.button_item.setOnClickListener {
            eventViewModel.sendItem(
                root.edit_text_item_transaction_id.text.toString(),
                root.edit_text_product_name.text.toString(),
                root.edit_text_product_sku.text.toString()
            )
        }
        return root
    }
}