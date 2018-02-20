package com.attilapalfi.machinelearning.housingprices.ingatlancomscraper


data class Flat(val price: String = "", val subType: String = "", val size: String = "", val rooms: String = "",
                val settlement: String = "", val settlementSub: String = "", val buildingMaterial: String = "",
                val buildingLevels: String = "", val floor: String = "", val condition: String = "", val buildYear: String = "",
                val barrierFree: String = "", val comfort: String = "", val gardenConnected: String = "",
                val toilet: String = "", val energyCert: String = "", val attic: String = "",
                val heating: String = "", val parking: String = "", val ac: String = "")