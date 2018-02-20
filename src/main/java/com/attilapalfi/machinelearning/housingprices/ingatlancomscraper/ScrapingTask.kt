package com.attilapalfi.machinelearning.housingprices.ingatlancomscraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ScrapingTask(private val flatRepository: FlatRepository) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(this.javaClass)

    override fun run(vararg args: String?) {
        var pageNumber = 1
        do {
            val page = Jsoup.connect("https://ingatlan.com/lista/elado+lakas?page=$pageNumber").get()
            log.info("Scraping page $pageNumber")
            val flatList = getFlatList(page)
            flatList.map { it.select("a[title=\"Részletek\"]").attr("href") }
                    .parallelStream()
                    .map { getFlat(it) }
                    .forEach { storeFlat(it) }
            pageNumber++
        } while (hasNextPage(page))
    }

    private fun hasNextPage(page: Document): Boolean = page.body().text().contains("Következő oldal")

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
        val settlementArray = flatPage.select("h1.js-listing-title").text().trim().split(",")
        val settlement = if (settlementArray.isNotEmpty()) { settlementArray[0].trim() } else { "nincs megadva" }
        val settlementSub = if (settlementArray.size > 1) { settlementArray[1].trim() } else { "nincs megadva" }

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

    private fun storeFlat(flat: Flat) {
        flatRepository.save(flat)
    }
}