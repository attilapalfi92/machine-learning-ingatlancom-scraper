package com.attilapalfi.machinelearning.housingprices.ingatlancomscraper

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlatRepository : JpaRepository<Flat, String>