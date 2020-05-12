package com.example.brickapp

import android.Manifest
import android.app.AlertDialog
import android.app.ListActivity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.settings_activity.*
import java.lang.Exception
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    var listItems = ArrayList<String>()
    var adapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listItems)
        val root = window.decorView.findViewById<View>(android.R.id.content) as ViewGroup
        val linearLayout = root.getChildAt(0) as ViewGroup
        val listView = linearLayout.getChildAt(1) as ListView
        listView.adapter = adapter
        listView.setOnItemClickListener { parent, view, position, id ->
            val target = view as TextView
            val intent = Intent(this, InventoryViewActivity::class.java)
            val bundle = Bundle()
            bundle.putString("projectName", target.text.toString())
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        listItems.clear()
        val dbHandler = MyDBHandler(this, null, null, 1)
        try {
            val newList = dbHandler.getInventoriesNames(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("showArchived", false))
            for (i in newList.indices) {
                listItems.add(newList.get(i))
            }
        }
        catch (e: Exception) {
            displayErrorMessage()
        }
        adapter?.notifyDataSetChanged()
    }

    fun displayErrorMessage() {
        val builder: AlertDialog.Builder? = this.let {
            AlertDialog.Builder(it)
        }

// 2. Chain together various setter methods to set the dialog characteristics
        builder?.setMessage("Please install the database in directory: data/data/com.example.brickapp/databases")
            ?.setTitle("ERROR WHILE RETRIEVING DATA")
        builder?.apply {
            setPositiveButton(
                "OK"
            ) { _, _ ->
                finish()
            }
            val dialog: AlertDialog? = builder.create()
            dialog?.show()
        }
    }

    fun addProject(v: View) {
        val intent = Intent(this, inventoryAddActivity::class.java)
        startActivity(intent)
    }

    fun goToSettings(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
}
