package com.attilapalfi.machinelearning.housingprices

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

// https://ingatlan.com/xvi-ker/elado+haz/ikerhaz/budapest+16+kerulet+vagas+utca/25367897
// /vii-ker/elado+lakas/tegla-epitesu-lakas/budapest+7+kerulet/25534997

fun main(args: Array<String>) {
    val propertyPage = getPropertyPage(1)
    propertyPage.map { it.select("a[title=\"Részletek\"]").attr("href") }
            .parallelStream()
            .forEach{ getProperty(it) }
}

private fun getProperty(href: String) {
    val flatPage = Jsoup.connect("https://ingatlan.com$href").get()
    val size = flatPage.select("div.parameter-area-size").select("span.parameter-value").text()
    val rooms = flatPage.select("div.parameter-room").select("span.parameter-value").text()
    val price = flatPage.select("div.parameter-price").select("span.parameter-value").text()
    val subType = flatPage.select("div.listing-subtype").text()
    val location = getLocation(flatPage)
    Flat(
            price = price,
            subType = subType,
            size = size,
            rooms = rooms,
            buildingMaterial = subType
    )
}

fun getLocation(flatPage: Document): Location {
    val locationDiv = flatPage.select("#map-nav-links")
    return Location()
}


private fun getPropertyPage(page: Int): List<Element> {
    val doc = Jsoup.connect("https://ingatlan.com/lista/elado+lakas?page=$page").get()
    val houseList = doc.select("div.resultspage__listings")
    return houseList[0].children().filter { e -> e.hasAttr("data-id") }
}