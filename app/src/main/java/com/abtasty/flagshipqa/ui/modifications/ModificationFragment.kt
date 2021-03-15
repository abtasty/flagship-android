package com.abtasty.flagshipqa.ui.modifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abtasty.flagshipqa.R
import kotlinx.android.synthetic.main.fragment_config.view.edit_visitor_context
import kotlinx.android.synthetic.main.fragment_context.view.*
import kotlinx.android.synthetic.main.fragment_modifications.*
import kotlinx.android.synthetic.main.fragment_modifications.view.*
import org.json.JSONObject


class ModificationFragment : Fragment() {

    private lateinit var modificationViewModel: ModificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        modificationViewModel = ViewModelProvider(this).get(ModificationViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_modifications, container, false)
        root.edit_modifications.keyListener = null
        modificationViewModel.modifications.observe(viewLifecycleOwner, Observer {
            try {
                root.edit_modifications.setText(JSONObject(it.toString()).toString(4))
            } catch (e: Exception) {
                root.edit_modifications.setText("{\n\n}")
            }
        })

        root.switch_view.setOnClickListener {
            toggleView()
        }

        root.spinner.adapter = ArrayAdapter(requireContext(), R.layout.spinner_elem, modificationViewModel.types)

        root.get_modification_value.setOnClickListener {
            modificationViewModel.getModification(
                root.edit_text_key.text.toString(),
                root.edit_text_default.text.toString(),
                root.spinner.selectedItem.toString()
            )
        }

        modificationViewModel.value.observe(viewLifecycleOwner, Observer {
            root.edit_text_result_value.setText(it.toString())
        })

        modificationViewModel.info.observe(viewLifecycleOwner, Observer {
//            root.edit_text_result_campaign.setText(it.optString("campaignId", "unknown"))
//            root.edit_text_result_group.setText(it.optString("variationGroupId", "unknown"))
//            root.edit_text_result_variation.setText(it.optString("variationId", "unknown"))
            root.edit_text_info.setText(it.toString(4))
        })

        root.activate.setOnClickListener {
            modificationViewModel.activate(root.edit_text_key.text.toString())
        }

        return root
    }

    fun toggleView() {
        get_modifications.visibility = if (get_modifications.visibility == View.INVISIBLE) View.VISIBLE else View.INVISIBLE
        edit_modifications.visibility = if (edit_modifications.visibility == View.INVISIBLE) View.VISIBLE else View.INVISIBLE
        switch_view.setText(if (edit_modifications.visibility == View.VISIBLE) R.string.fragment_modifications_view_compute else R.string.fragment_modifications_view_json)
    }
}