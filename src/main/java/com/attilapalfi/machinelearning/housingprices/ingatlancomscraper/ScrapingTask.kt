package com.attilapalfi.machinelearning.housingprices.ingatlancomscraper

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ScrapingTask(private val flatRepository: FlatRepository) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(this.javaClass)

    private val sleepMillis = 2000L

    private val maxRetries = 10

    private val startPage = 961

    override fun run(vararg args: String?) {
        var pageNumber = 1
        do {
            log.info("Scraping page $pageNumber")
            val page = tryDownloadingPage(1, "https://ingatlan.com/lista/elado+lakas?page=$pageNumber")
            if (pageNumber >= startPage) {
                val flatList = getFlatList(page)
                flatList.map { it.select("a[title=\"Részletek\"]").attr("href") }
                        .parallelStream()
                        .map { getFlat(it) }
                        .forEach { tryStoringFlat(it) }
            }
            pageNumber++
        } while (hasNextPage(page))
    }

    private fun hasNextPage(page: Document): Boolean = page.body().text().contains("Következő oldal")

    private fun getFlatList(page: Document): List<Element> {
        val houseList = page.select("div.resultspage__listings")
        return houseList[0].children().filter { e -> e.hasAttr("data-id") }
    }

    private fun getFlat(href: String): Flat {
        val flatPage: Document = tryDownloadingPage(1, "https://ingatlan.com$href")
        val size = flatPage.select("div.parameter-area-size").select("span.parameter-value").text().trim()
        val rooms = flatPage.select("div.parameter-room").select("span.parameter-value").text().trim()
        val price = flatPage.select("div.parameter-price").select("span.parameter-value").text().trim()
        val subType = flatPage.select("div.listing-subtype").text().trim()
        val settlementArray = flatPage.select("h1.js-listing-title").text().trim().split(",")
        val settlement = if (settlementArray.isNotEmpty()) {
            settlementArray[0].trim()
        } else {
            "nincs megadva"
        }
        val settlementSub = if (settlementArray.size > 1) {
            settlementArray[1].trim()
        } else {
            "nincs megadva"
        }

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

    private fun tryDownloadingPage(tries: Int, url: String): Document {
        return try {
            getPage(url)
        } catch (e: Throwable) {
            log.warn("[Page Download] Exception occurred at try number $tries. ${e.javaClass.name}: ${e.message}")
            if (tries <= maxRetries) {
                log.info("[Page Download] Sleeping for $sleepMillis ms")
                Thread.sleep(sleepMillis)
                tryDownloadingPage(tries + 1, url)
            } else {
                log.error("[Page Download] Tried $maxRetries times, out of options. :(")
                throw e
            }
        }
    }

    private fun getPage(url: String): Document = Jsoup.connect(url).get()


    private fun getParameter(elements: List<Element>, paramName: String): String {
        val index = elements.indexOfFirst { it.text().contains(paramName) }
        if (index == -1) {
            return "nincs megadva"
        }
        return elements[index + 1].text()
    }

    private fun tryStoringFlat(flat: Flat) {
        storeFlat(1, flat)
    }

    private fun storeFlat(tries: Int, flat: Flat) {
        try {
            flatRepository.save(flat)
        } catch (e: Throwable) {
            log.warn("[Flat save] Exception occurred at try number $tries. ${e.javaClass.name}: ${e.message}")
            if (tries <= maxRetries) {
                log.info("[Flat save] Sleeping for $sleepMillis ms")
                Thread.sleep(sleepMillis)
                storeFlat(tries + 1, flat)
            } else {
                log.error("[Flat save] Tried $maxRetries times, out of options. :(")
                throw e
            }
        }
    }
}