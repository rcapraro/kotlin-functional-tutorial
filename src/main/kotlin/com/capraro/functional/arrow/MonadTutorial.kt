package com.capraro.functional.arrow

import arrow.core.Either
import java.time.LocalDate

data class Speaker(val name: String, val company: String) {
    fun getTalks(): List<Talk> = listOf(Talk("Spring for Dummies"), Talk("Kubernetes in production"))
}

data class Talk(val name: String) {
    fun getConferences(): List<Conference> = listOf(
            Conference("Devoxx", LocalDate.of(2019, 11, 15)),
            Conference("Spring io", LocalDate.of(2020, 5, 25))
    )
}

data class Conference(val name: String, val date: LocalDate) {
    fun getCities(): List<City> = listOf(City("Paris"), City("Barcelona"))
}

data class City(val name: String)

class SpeakerService {

    fun loadAllSpeakers(): List<Speaker> {

        if (Math.random() > 0.5) throw RuntimeException("Something gets wrong !")
        else return listOf(Speaker("Richard capraro", "Saagie"), Speaker("John DOE", "Acme"))
    }
}

class ApiClient(private val service: SpeakerService) {
    suspend fun getSpeakers(): Either<Throwable, List<Speaker>> = Either.catch {
        service.loadAllSpeakers()
    }
}

suspend fun getSpeakers() {

    ApiClient(SpeakerService())
            .getSpeakers()
            .map { speakers: List<Speaker> ->
                speakers.map { it.name }
            }.fold(
                    {
                        it.printStackTrace()
                    },
                    {
                        println(it)
                    })

}

fun allCitiesToVisit(speaker: Speaker): List<City> {
    val result = mutableListOf<City>()

    for (talk in speaker.getTalks())
        for (conf in talk.getConferences())
            for (city in conf.getCities())
                result.add(city)

    return result
}

fun allCitiesToVisit2(speaker: Speaker): List<City> {

    return speaker
            .getTalks()
            .flatMap { talk -> talk.getConferences() }
            .flatMap { conference -> conference.getCities() }
}

suspend fun main() {
    getSpeakers()
}

