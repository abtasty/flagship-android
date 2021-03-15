package com.abtasty.flagshipqa.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abtasty.flagshipqa.R
import kotlinx.android.synthetic.main.fragment_config.*
import kotlinx.android.synthetic.main.fragment_config.view.*
import org.json.JSONObject


class ConfigFragment : Fragment() {

    private lateinit var dashboardViewModel: ConfigViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel = ViewModelProvider(this).get(ConfigViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_config, container, false)

        dashboardViewModel.api_key.observe(viewLifecycleOwner, Observer {
            root.edit_text_api_key.setText(it)
        })
        dashboardViewModel.env_id.observe(viewLifecycleOwner, Observer {
            root.edit_text_env_id.setText(it)
        })
        dashboardViewModel.useBucketing.observe(viewLifecycleOwner, Observer {
            root.toggleButton.isChecked = it
        })
        dashboardViewModel.useBucketing.observe(viewLifecycleOwner, Observer {
            root.toggleButton.isChecked = it
        })
        dashboardViewModel.timeout.observe(viewLifecycleOwner, Observer {
            root.edit_text_timeout.setText(it.toString())
        })

        dashboardViewModel.visitorId.observe(viewLifecycleOwner, Observer {
            root.edit_text_visitorId.setText(it.toString())
        })

        dashboardViewModel.visitorContext.observe(viewLifecycleOwner, Observer {
            try {
                root.edit_visitor_context.setText(JSONObject(it.toString()).toString(4))
            } catch (e: Exception) {
                root.edit_visitor_context.setText("{\n\n}")
            }
        })

        root.start.setOnClickListener {
            dashboardViewModel.env_id.value = root.edit_text_env_id.text.toString()
            dashboardViewModel.useBucketing.value = root.toggleButton.isChecked
            dashboardViewModel.timeout.value = if (root.edit_text_timeout.text.toString()
                    .isNotEmpty()
            ) root.edit_text_timeout.text.toString().toInt() else 0
            dashboardViewModel.api_key.value = root.edit_text_api_key.text.toString()
            dashboardViewModel.visitorId.value = root.edit_text_visitorId.text.toString()
            dashboardViewModel.visitorContext.value = root.edit_visitor_context.text.toString()
            dashboardViewModel.saveLastConf()
            startFlagship()
        }

        root.add.setOnClickListener { v ->
            showPopupMenu(v)
        }
        return root
    }

    fun showPopupMenu(v: View) {
        val popup = PopupMenu(context!!, v)
        var i = 0
        for (e in dashboardViewModel.env_ids) {
            popup.menu.add(0, i, i, e.key + " - " + e.value)
            i++
        }
        popup.setOnMenuItemClickListener { item ->
            val split = item.title.split(" - ")
            if (split.size > 1)
                edit_text_env_id.setText(split[1])
            true
        }
        popup.show();
    }

    fun startFlagship() {
        dashboardViewModel.startFlagship(
            {
                activity?.runOnUiThread {
                    Toast.makeText(context!!, "Started", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(context!!, "Error : $error", Toast.LENGTH_SHORT).show()
            })
    }
}