package com.example.moonapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import java.io.OutputStreamWriter

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        setRadioButtons()
        super.onResume()
    }

    override fun finish() {
        writeFile()
        super.finish()
    }

    fun writeFile() {
        val filename = "settings.txt"
        val file = OutputStreamWriter(openFileOutput(filename, Context.MODE_PRIVATE))
        var radioGroup = findViewById<RadioGroup>(R.id.algorithms)
        val chosenAlgorithm = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        file.write(chosenAlgorithm.text.toString() + "\n")
        radioGroup = findViewById(R.id.hemispheres)
        val chosenHemisphere = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
        file.write(chosenHemisphere.text.toString())
        file.flush()
        file.close()
        Toast.makeText(this, "Ustawienia zostały zapisane", Toast.LENGTH_LONG).show()
        setSettings(
            chosenAlgorithm.text as String,
            chosenHemisphere.text as String
        )
    }

    fun setRadioButtons() {
        var button = findViewById<RadioButton>(resources.getIdentifier(algorithmButtonId, "id", packageName))
        button.isChecked = true
        button = findViewById(resources.getIdentifier(hemisphereButtonId, "id", packageName))
        button.isChecked = true
    }

}
