package com.example.moonapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.view.View
import android.widget.LinearLayout
import java.lang.Exception
import java.util.*
import java.util.Calendar.*

class YearActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_year)
        updateView(getInstance().get(YEAR))
        val yearInput = findViewById<TextView>(R.id.yearInput)
        yearInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                val year = getYearFromField()
                updateView(year)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    fun getYearFromField() :Int {
        val yearInput = findViewById<TextView>(R.id.yearInput)
        var year: Int
        try {
            year = yearInput.text.toString().toInt()
        }
        catch (e: Exception) {
            return -1
        }
        return year
    }

    fun incrementYear(v: View) {
        val yearInput = findViewById<TextView>(R.id.yearInput)
        var year = getYearFromField()
        year++
        yearInput.text = year.toString()
        updateView(year)
    }

    fun decrementYear(v: View) {
        val yearInput = findViewById<TextView>(R.id.yearInput)
        var year = getYearFromField()
        year--
        yearInput.text = year.toString()
        updateView(year)
    }

    fun updateView(year: Int) {
        if (year < 1900 || year > 2200) return

        var date = getInstance()
        date.set(YEAR, year)
        date.set(DAY_OF_MONTH, 30)
        date.set(MONTH, 0)
        var fullMoonDate = getInstance()
        for (i in 0..11) {
            fullMoonDate.set(YEAR, date.get(YEAR))
            fullMoonDate.set(DAY_OF_MONTH, date.get(DAY_OF_MONTH))
            fullMoonDate.set(MONTH, date.get(MONTH))
            var phase = trigonometricalPhaseCalculation(date).toInt()
            if (phase < 15) fullMoonDate.add(DATE, -(15 + phase))
            else fullMoonDate.add(DATE, -(phase - 15))
            val index = i + 2
            val listItem = findViewById<TextView>(resources.getIdentifier("textView$index", "id", packageName))
            var month = (fullMoonDate.get(MONTH) + 1).toString()
            if (month.length == 1) month ="0$month"
            listItem.text = getString(R.string.listString, fullMoonDate.get(DAY_OF_MONTH), month, fullMoonDate.get(YEAR))
            date.add(DATE, 30)
        }
    }
}
