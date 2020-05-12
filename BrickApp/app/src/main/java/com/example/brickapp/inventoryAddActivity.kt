package com.example.brickapp

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_inventory_add.*
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.net.HttpURLConnection
import java.net.URL
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory


class inventoryAddActivity : AppCompatActivity() {

    var urlPrefix: String? = null

    private inner class MyTask : AsyncTask<String, Void, Int>() {

        override fun onPreExecute() {

        }

        override fun doInBackground(vararg p0: String?): Int {
            if (p0.size == 1) {
                try {
                    HttpURLConnection.setFollowRedirects(false)
                    val con = URL(p0[0]).openConnection() as HttpURLConnection
                    con.requestMethod = "GET"
                    val toReturn: Int
                    toReturn = if (con.responseCode == HttpURLConnection.HTTP_OK) 1
                    else 0
                    return toReturn
                } catch (e: Exception) {
                    e.printStackTrace()
                    return -1
                }
            }
            else {
                val dbHandler = MyDBHandler(this@inventoryAddActivity, null, null, 1)
                if (dbHandler.checkIfProjectExists(p0[1])) return -2
                try {
                    val url = URL(p0[0])
                    val dbf: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
                    val db: DocumentBuilder = dbf.newDocumentBuilder()
                    val doc: Document = db.parse(InputSource(url.openStream()))
                    doc.documentElement.normalize()
                    val items = doc.getElementsByTagName("ITEM")
                    dbHandler.addProject(p0[1])
                    for (i in 0 until items.length) {
                        val node = items.item(i)
                        dbHandler.addInventoryPart(node.childNodes, p0[1])
                    }
                    return 2
                } catch (e: Exception) {
                    e.printStackTrace()
                    return -1
                }
            }
        }

        override fun onPostExecute(result: Int) {
            val message: String
            if (result < 2) {
                if (result == 1) message = "Given Inventory id is correct"
                else if (result == -2) message = "Project with this name already exists"
                else message = "Given Inventory id or URL prefix is not correct (or maybe you have problems with internet connection)"
                Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
            }
            if (result == 2) {
                val intent = Intent(this@inventoryAddActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_add)
        urlPrefix = PreferenceManager.getDefaultSharedPreferences(this).getString("urlPrefix", "http://fcds.cs.put.poznan.pl/MyWeb/BL/")
    }

    fun checkId(v: View) {
        val id = inventoryId.text.toString()
        val url = urlPrefix + id + ".xml";
        val task = MyTask()
        task.execute(url)
    }

    fun addProject(v: View) {
        val projectName = projectName.text.toString()
        val id = inventoryId.text.toString()
        val url = urlPrefix + id + ".xml";
        val task = MyTask()
        task.execute(url, projectName)
    }
}
