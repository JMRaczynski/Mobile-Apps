package com.example.moonapp

var usedAlgorithm = ::simplePhaseCalculation
var algorithmButtonId = ""
var chosenHemisphere = ""
var hemisphereButtonId = ""

fun setSettings(algorithm: String, hemisphere: String) {
    when (algorithm) {
        "Prosty" -> {
            usedAlgorithm = ::simplePhaseCalculation
            algorithmButtonId = "radioButton"
        }
        "Conway" -> {
            usedAlgorithm = ::Conway
            algorithmButtonId = "radioButton2"
        }
        "Trygonometryczny 1" -> {
            usedAlgorithm = ::trigonometricalPhaseCalculation
            algorithmButtonId = "radioButton3"
        }
        "Trygonometryczny 2" -> {
            usedAlgorithm = ::trigonometricalPhaseCalculationv2
            algorithmButtonId = "radioButton4"
        }
    }
    when (hemisphere) {
        "Północna" -> {
            chosenHemisphere = "n"
            hemisphereButtonId = "radioButton5"
        }
        "Południowa" -> {
            chosenHemisphere = "s"
            hemisphereButtonId = "radioButton6"
        }
    }
}