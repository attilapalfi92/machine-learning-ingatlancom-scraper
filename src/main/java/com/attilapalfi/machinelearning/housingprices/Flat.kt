package com.attilapalfi.machinelearning.housingprices

data class Flat(val price: String = "", val subType: String = "", val size: String = "", val rooms: String = "",
                val settlement: String = "", val settlementSub1: String = "", val settlementSub2: String = "",
                val settlementSub3: String = "", val settlementSub4: String = "", val buildingMaterial: String = "",
                val buildingLevels: Int = 0, val floor: Int = 0, val condition: String = "", val buildYear: String = "",
                val barrierFree: String = "", val comfort: String = "", val gardenConnected: String = "",
                val toilet: String = "", val energyCert: String = "", val attic: String = "", val cellar: String = "",
                val heating: String = "", val parking: String = "", val ac: String = "")