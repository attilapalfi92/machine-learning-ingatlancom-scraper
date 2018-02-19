package com.attilapalfi.machinelearning.housingprices

data class House(val price: Int, val houseType: String, val subType: String, val size: Int,
                 val lotSize: String, val settlement: String, val settlementSub1: String,
                 val settlementSub2: String, val settlementSub3: String, val settlementSub4: String,
                 val buildingMaterial: String, val buildingLevels: Int, val floor: Int,
                 val condition: String, val buildYear: Int, val barrierFree: String, val comfort: String,
                 val toilet: String, val energyCert: String, val attic: String, val cellar: String,
                 val heating: String, val parking: String, val ac: String)