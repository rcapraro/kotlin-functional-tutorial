package com.capraro.functional.arrow

import arrow.core.Either
import arrow.fx.IO
import arrow.fx.handleError
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

fun allCitiesToVisitFor(speaker: Speaker): List<City> {
    val result = mutableListOf<City>()

    for (talk in speaker.getTalks())
        for (conf in talk.getConferences())
            for (city in conf.getCities())
                result.add(city)

    return result
}

fun allCitiesToVisitFlatMap(speaker: Speaker): List<City> {
    return speaker
            .getTalks()
            .flatMap { talk -> talk.getConferences() }
            .flatMap { conference -> conference.getCities() }
}

class SpeakerService {
    fun loadAllSpeakers(raiseError: Boolean = false): List<Speaker> {
        if (raiseError) throw RuntimeException("Something gets wrong !")
        else return listOf(Speaker("Richard capraro", "Saagie"), Speaker("John DOE", "Acme"))
    }
}

class ApiClientEither(private val service: SpeakerService) {
    suspend fun getSpeakers(raiseError: Boolean = false): Either<Throwable, List<Speaker>> = Either.catch {
        service.loadAllSpeakers(raiseError)
    }
}

suspend fun getSpeakersEither(raiseError: Boolean = false) {
    ApiClientEither(SpeakerService())
            .getSpeakers(raiseError)
            .map { speakers ->
                speakers.map { it.name }
            }.fold(
                    {
                        it.printStackTrace()
                    },
                    {
                        println(it)
                    })
}

class ApiClientIO(private val service: SpeakerService) {
    fun getSpeakers(raiseError: Boolean = false): IO<List<Speaker>> = IO {
        service.loadAllSpeakers(raiseError)
    }

}

fun getSpeakersIO(raiseError: Boolean = false): IO<List<String>> {
    return ApiClientIO(SpeakerService())
            .getSpeakers(raiseError)
            .map { speakers -> speakers.map { it.name } }
            .handleError { error -> listOf(error.message!!) }
}


suspend fun main() {
    getSpeakersEither()

    val speakersIO = getSpeakersIO()

    println(speakersIO.unsafeRunSync())

    println("-".repeat(20))

    getSpeakersEither(raiseError = true)

    val speakersIOError = getSpeakersIO(raiseError = true)

    println(speakersIOError.unsafeRunSync())

}

