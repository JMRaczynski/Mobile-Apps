package com.example.brickapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_inventory_view.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL
import android.Manifest
import android.view.Gravity
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class InventoryViewActivity : AppCompatActivity() {

    private var projectName = ""
    private var itemData = mutableListOf(mutableListOf<String>())
    val dbHandler = MyDBHandler(this, null, null, 1)

    private inner class MyTask : AsyncTask<String, Void, Pair<Int, Drawable>?>() {
        override fun onPreExecute() {
        }

        override fun doInBackground(vararg p0: String?): Pair<Int, Drawable>? {

            try {
                val inputStream: InputStream = URL("https://www.lego.com/service/bricks/5/2/" + p0[0]).getContent() as InputStream
                val pair = Pair(Integer.parseInt(p0[1]), resize(Drawable.createFromStream(inputStream, "src name")))
                inputStream.close()
                return pair
            } catch (e: FileNotFoundException) {
                try {
                    val inputStream: InputStream
                    if (p0.size == 2) {
                        inputStream = URL("https://www.bricklink.com/PL/" + p0[0] + ".jpg").getContent() as InputStream
                    }
                    else {
                        inputStream = URL("http://img.bricklink.com/P/" + p0[2] + "/" + p0[0] + ".jpg").getContent() as InputStream
                    }
                    val pair = Pair(Integer.parseInt(p0[1]), resize(Drawable.createFromStream(inputStream, "src name")))
                    inputStream.close()
                    return pair
                } catch (e: Exception) {
                    return null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        override fun onPostExecute(result: Pair<Int, Drawable>?) {
            if (result != null) {
                val imgView = findViewById<ImageView>(result.first)
                imgView.setImageDrawable(result.second)
            }
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_view)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        projectName = intent.extras.getString("projectName")
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        populateActivity()
    }

    override fun finish() {
        super.finish()
        for (i in 0 until itemData.size) {
            val ll = itemList.getChildAt(i * 2) as ViewGroup
            val description = ll.getChildAt(1) as TextView
            val text = description.text.toString()
            val words = text.split("\\n+".toRegex())
            val numbers = words[1].split(" ")
            dbHandler.updatePart(projectName, Integer.parseInt(numbers[0]), itemData[2][i], itemData[3][i])
        }
    }

    fun populateActivity() {
        dbHandler.updateLastModified(projectName)
        itemData = dbHandler.getItemInfo(projectName)
        val numberOfItems = dbHandler.getNumberOfInventoryParts(projectName)
        println(numberOfItems)
        println(itemData[0].size)
        val difference = numberOfItems - itemData[0].size
        if (itemData[0].size != numberOfItems) Toast.makeText(applicationContext, "Information about $difference bricks not found in database", Toast.LENGTH_LONG).show()
        val list = itemList
        for (i in 0 until itemData[0].size) {
            val ll1 = LinearLayout(this)
            list.addView(ll1)
            ll1.orientation = LinearLayout.HORIZONTAL
            val itemDescription = TextView(this)
            itemDescription.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            itemDescription.text = itemData[4][i] + " " + itemData[3][i] + " [" + itemData[2][i] + "]\n\n" + itemData[1][i] + " of " + itemData[0][i]
            val params = itemDescription.layoutParams as LinearLayout.LayoutParams
            params.setMargins(50, 30, 30, 30) //substitute parameters for left, top, right, bottom
            itemDescription.layoutParams = params
            val itemImage = ImageView(this)
            itemImage.id = i + 1000
            itemImage.minimumHeight = 250
            itemImage.minimumWidth = 250
            itemImage.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val imgParams = itemImage.layoutParams as LinearLayout.LayoutParams
            imgParams.setMargins(30, 30, 50, 30)
            itemImage.layoutParams = imgParams
            var itemCode = dbHandler.getItemCode(itemData[2][i], itemData[3][i]) // first arg is code, second is color name
            var itemColorId: String?
            val task = MyTask()
            if (itemCode == null) {
                itemCode = itemData[2][i]
            }
            else {
                //itemColorId = dbHandler.getColorId(itemData[3][i])
            }
            //println(itemData[4][i])
            println(itemData[3][i])
            //println(itemCode)
            //task.execute(itemCode, itemImage.id.toString())
            itemColorId = dbHandler.getColorId(itemData[3][i])
            //println(itemColorId)
            task.execute(itemCode, itemImage.id.toString(), itemColorId)
            ll1.addView(itemImage)
            ll1.addView(itemDescription)
            createButtonLayout(list, itemDescription, itemData[1][i] == itemData[0][i], itemData[1][i] == "0")
            if (itemData[1][i] == itemData[0][i]) ll1.setBackgroundColor(Color.parseColor("#77FF77"))
        }
    }

    fun createButtonLayout(list: LinearLayout, description: TextView, allCollected: Boolean, noneCollected: Boolean) {
        val ll2 = LinearLayout(this)
        ll2.weightSum = 2f
        list.addView(ll2)
        val minusButton = createButton("-")
        val plusButton = createButton("+")
        minusButton.setOnClickListener {
                v -> plusButtonHandler(v, description, plusButton)
        }
        plusButton.setOnClickListener {
                v -> plusButtonHandler(v, description, minusButton)
        }
        if (allCollected) {
            plusButton.isEnabled = false
            ll2.setBackgroundColor(Color.parseColor("#77FF77"))
        }
        if (noneCollected) minusButton.isEnabled = false
        ll2.addView(minusButton)
        ll2.addView(plusButton)
    }

    fun createButton(label: String): Button {
        val button = Button(this)
        button.text = label
        button.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        button.textSize = 25F
        button.setBackgroundColor(Color.parseColor("#FFFFFF"))
        return button
    }

    private fun plusButtonHandler(v: View, description: TextView, oppositeButton: Button) {
        val button = v as Button
        val text = description.text.toString()
        val words = text.split("\\n+".toRegex()).toMutableList()
        val numbers = words[1].split(" ").toMutableList()
        val row = description.parent as LinearLayout
        var num = Integer.parseInt(numbers[0])
        if (v.text == "+") {
            if (num == 0) oppositeButton.isEnabled = true
            num++
            if (num == Integer.parseInt(numbers[2])) {
                button.isEnabled = false
                row.setBackgroundColor(Color.parseColor("#77FF77"))
            }
        }
        else {
            if (num == Integer.parseInt(numbers[2])) {
                oppositeButton.isEnabled = true
                row.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }
            num--
            if (num == 0) button.isEnabled = false
        }
        numbers[0] = num.toString()
        words[1] = numbers.joinToString(separator = " ")
        description.text = words.joinToString(separator = "\n\n")
    }

    private fun resize(image: Drawable?): Drawable {
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 250, 250, false)
        return BitmapDrawable(resources, bitmapResized)
    }

    fun exportProject(v: View) {
        dbHandler.markAsArchived(projectName)
        writeXml()
        finish()
        Toast.makeText(applicationContext, "Project has been saved in file " + projectName + ".xml", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun writeXml() {
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()
        val rootElement: Element = doc.createElement("INVENTORY")
        for (i in 0 until itemData[0].size) {
            val itemElement = doc.createElement("ITEM")
            val itemType = dbHandler.getItemType(itemData[5][i])
            val typeNode = doc.createElement("ITEMTYPE")
            typeNode.textContent = itemType
            itemElement.appendChild(typeNode)
            val idNode = doc.createElement("ITEMID")
            idNode.textContent = itemData[2][i]
            itemElement.appendChild(idNode)
            val colorNode = doc.createElement("COLOR")
            colorNode.textContent = dbHandler.getColorId(itemData[3][i])
            itemElement.appendChild(colorNode)
            val quantityNode = doc.createElement("QTYFILLED")
            val ll = itemList.getChildAt(i * 2) as ViewGroup
            val description = ll.getChildAt(1) as TextView
            val text = description.text.toString()
            val words = text.split("\\n+".toRegex())
            val numbers = words[1].split(" ")
            quantityNode.textContent = (Integer.parseInt(numbers[numbers.size - 1]) - Integer.parseInt(numbers[0])).toString()
            itemElement.appendChild(quantityNode)
            rootElement.appendChild(itemElement)
        }
        doc.appendChild(rootElement)
        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        //val path = this.filesDir
        val file = File(Environment.getExternalStorageDirectory(), "$projectName.xml")
        transformer.transform(DOMSource(doc), StreamResult(file))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_DENIED ||
                grantResults[1] == PackageManager.PERMISSION_DENIED)) {
            archiveButton.isEnabled = false
            populateActivity()
        }
    }
}
