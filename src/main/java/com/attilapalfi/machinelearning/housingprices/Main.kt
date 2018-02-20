package com.attilapalfi.machinelearning.housingprices

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.concurrent.ConcurrentHashMap

val flats = ConcurrentHashMap.newKeySet<Flat>()

fun main(args: Array<String>) {
    var pageNumber = 1
    do {
        val page = Jsoup.connect("https://ingatlan.com/lista/elado+lakas?page=$pageNumber").get()
        val flatList = getFlatList(page)
        flatList.map { it.select("a[title=\"Részletek\"]").attr("href") }
                .parallelStream()
                .map { getFlat(it) }
                .forEach { storeFlat(it) }
        pageNumber++
    } while (hasNextPage(page))
}

fun hasNextPage(page: Document): Boolean = page.body().text().contains("Következő oldal")

private fun getFlatList(page: Document): List<Element> {
    val houseList = page.select("div.resultspage__listings")
    return houseList[0].children().filter { e -> e.hasAttr("data-id") }
}

private fun getFlat(href: String): Flat {
    val flatPage: Document = Jsoup.connect("https://ingatlan.com$href").get()
    val size = flatPage.select("div.parameter-area-size").select("span.parameter-value").text().trim()
    val rooms = flatPage.select("div.parameter-room").select("span.parameter-value").text().trim()
    val price = flatPage.select("div.parameter-price").select("span.parameter-value").text().trim()
    val subType = flatPage.select("div.listing-subtype").text().trim()
    val settlementString = flatPage.select("h1.js-listing-title").text().trim()
    val settlement = settlementString.split(",")[0].trim()
    val settlementSub = settlementString.split(",")[1].trim()

    val params = flatPage.select("div.paramterers")
    val elements = params[0].allElements.filter { it.`is`("td") }

    return Flat(
            price = price,
            subType = subType,
            size = size,
            rooms = rooms,
            buildingMaterial = subType,
            settlement = settlement,
            settlementSub = settlementSub,
            condition = getParameter(elements, "Ingatlan állapota"),
            buildingLevels = getParameter(elements, "Épület szintjei"),
            floor = getParameter(elements, "Emelet"),
            buildYear = getParameter(elements, "Építés éve"),
            barrierFree = getParameter(elements, "Akadálymentesített"),
            comfort = getParameter(elements, "Komfort"),
            gardenConnected = getParameter(elements, "Kertkapcsolatos"),
            toilet = getParameter(elements, "Fürdő és WC"),
            energyCert = getParameter(elements, "Energiatanúsítvány"),
            attic = getParameter(elements, "Tetőtér"),
            heating = getParameter(elements, "Fűtés"),
            parking = getParameter(elements, "Parkolás"),
            ac = getParameter(elements, "Légkondicionáló")
    )
}

private fun getParameter(elements: List<Element>, paramName: String): String {
    val index = elements.indexOfFirst { it.text().contains(paramName) }
    if (index == -1) {
        return "nincs megadva"
    }
    return elements[index + 1].text()
}

fun storeFlat(flat: Flat) {
    flats.add(flat)
}