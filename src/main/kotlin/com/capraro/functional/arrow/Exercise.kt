package com.capraro.functional.arrow

import arrow.core.Either
import arrow.core.flatMap


fun parse(s: String): Either<NumberFormatException, Int> =
    if (s.matches(Regex("-?[0-9]+"))) Either.right(s.toInt())
    else Either.left(NumberFormatException("$s is not an Integer"))

fun reciprocal(i: Int): Either<IllegalArgumentException, Double> =
    if (i == 0) Either.left(IllegalArgumentException("Cannot take reciprocal of 0."))
    else Either.right(1.0 / i)

fun stringify(d: Double): String = d.toString()

fun magic(s: String): Either<Exception, String> =
    parse(s)
        .flatMap { reciprocal(it) }
        .map {
            stringify(
                it
            )
        }

fun getListOfEither(): List<Either<String, Int>> = listOf(Either.right(2), Either.left("Error"))

fun getListOfResults(list: List<Either<String, Int>>): List<Int> = list.fold(
    initial = listOf(),
    operation = { acc, either ->
        either.fold(
            { acc },
            { acc + it }
        )
    }
)

fun main() {

    println(magic("2"))

    val right: Either<String, Int> = Either.right(5)
    val left: Either<String, Int> = Either.left("System error")
    val value = right.flatMap { Either.right(it + 1) }
    val error = left.flatMap { Either.right(it + 1) }

    println(value)
    println(error)
}