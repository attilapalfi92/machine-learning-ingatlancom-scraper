package com.attilapalfi.machinelearning.housingprices

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

// https://ingatlan.com/xvi-ker/elado+haz/ikerhaz/budapest+16+kerulet+vagas+utca/25367897
// /vii-ker/elado+lakas/tegla-epitesu-lakas/budapest+7+kerulet/25534997

fun main(args: Array<String>) {
    val housePage = getHousePage(1)
    housePage.map { it.select("a[title=\"Részletek\"]").attr("href") }
            .parallelStream()
            .map { processHouse(it) }
}

private fun processHouse(href: String) {
    val housePage = Jsoup.connect("https://ingatlan.com$href").get()
    println(housePage)
}


private fun getHousePage(page: Int): List<Element> {
    val doc = Jsoup.connect("https://ingatlan.com/lista/elado+lakas?page=$page").get()
    val houseList = doc.select("div.resultspage__listings")
    return houseList[0].children().filter { e -> e.hasAttr("data-id") }
}