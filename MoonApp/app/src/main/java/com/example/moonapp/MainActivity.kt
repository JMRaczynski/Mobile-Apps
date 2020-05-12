package com.example.moonapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.util.*
import java.util.Calendar.*
import kotlin.math.floor


class MainActivity : AppCompatActivity() {

    private fun setMoonImage(imgName: String) {
        val moonPic = findViewById<ImageView>(R.id.moonPic)
        val resId = resources.getIdentifier(imgName, "drawable", "com.example.moonapp")
        moonPic.setImageResource(resId)
    }

    private fun setDateString(date: Calendar, view: TextView, stringId: Int) {
        var lastNewMoonMonth = (date.get(MONTH) + 1).toString()
        lastNewMoonMonth = if (lastNewMoonMonth.length != 1) lastNewMoonMonth else "0$lastNewMoonMonth"
        view.text = getString(stringId, date.get(DAY_OF_MONTH), lastNewMoonMonth, date.get(YEAR))
    }

    fun switchToYearActivity(v: View) {
        val intent = Intent(this, YearActivity::class.java)
        startActivity(intent)
    }

    fun switchToSettingsActivity(v: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun refreshActivity() {
        val todayDate = getInstance()
        val phaseDay = usedAlgorithm(
            todayDate
        ).toInt()
        setMoonImage("$chosenHemisphere$phaseDay")
        val phasePercent = floor(phaseDay / 29.0 * 100).toInt()
        val lastNewMoonDate = getInstance()
        lastNewMoonDate.add(DATE, -phaseDay)
        val nextFullMoonDate = getInstance()
        if (phaseDay < 15) nextFullMoonDate.add(DATE, 14 - phaseDay)
        else nextFullMoonDate.add(DATE, 44 - phaseDay)
        val todayStatus = findViewById<TextView>(R.id.todayStatus)
        todayStatus.text = getString(R.string.statusText, phasePercent)
        setDateString(lastNewMoonDate, previousNewMoon, R.string.previousNewMoonText)
        setDateString(nextFullMoonDate, nextFullMoon, R.string.nextFullMoonText)
    }

    fun readSettingsFile() {
        try {
            val file = InputStreamReader(openFileInput("settings.txt"))
            val br = BufferedReader(file)
            val alg = br.readLine()
            val hemisphere = br.readLine()
            setSettings(alg, hemisphere)
        } catch (e: FileNotFoundException) {
            setSettings("Trygonometryczny 1", "Północna")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_main)
        readSettingsFile()
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        refreshActivity()
    }
}
