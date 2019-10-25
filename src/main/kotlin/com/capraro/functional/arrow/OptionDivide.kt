package com.capraro.functional.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.extensions.fx
import arrow.core.toOption

fun divide(num: Int, den: Int): Int? {
    return if (num % den != 0) {
        null
    } else {
        num / den
    }
}

//return the pair of the two number divided by den if both are divisible by den
fun division(a: Int, b: Int, den: Int): Pair<Int, Int>? {
    return when (val aDiv = divide(a, den)) {
        is Int -> {
            when (val bDiv = divide(b, den)) {
                is Int -> aDiv to bDiv
                else -> null
            }
        }
        else -> null
    }
}

fun optionDivide(num: Int, den: Int): Option<Int> = divide(num, den).toOption()

fun optionDivision(a: Int, b: Int, den: Int): Option<Pair<Int, Int>> {
    return when (val aDiv = optionDivide(a, den)) {
        is Some -> {
            when (val bDiv = optionDivide(b, den)) {
                is Some -> Some(aDiv.t to bDiv.t)
                else -> None
            }
        }
        else -> None
    }
}

fun flatMapDivision(a: Int, b: Int, den: Int): Option<Pair<Int, Int>> {
    return optionDivide(a, den).flatMap { aDiv: Int ->
        optionDivide(b, den).flatMap { bDiv: Int ->
            Some(aDiv to bDiv)
        }
    }
}

fun comprehensionDivision(a: Int, b: Int, den: Int): Option<Pair<Int, Int>> {
    return Option.fx {
        val aDiv: Int = optionDivide(a, den).bind()
        val bDiv: Int = optionDivide(b, den).bind()
        aDiv to bDiv
    }
}

fun main() {
    println(divide(10, 2)) //5
    println(divide(10, 3)) //null

    println(division(15, 20, 5)) //3, 4
    println(division(15, 22, 5)) //null
    println(division(14, 20, 5)) //null

    println(optionDivision(15, 20, 5)) //Some(3, 4)
    println(optionDivision(15, 22, 5)) //None
    println(optionDivision(14, 20, 5)) //None

    println(flatMapDivision(15, 20, 5)) //Some(3, 4)
    println(flatMapDivision(15, 22, 5)) //None
    println(flatMapDivision(14, 20, 5)) //None

    println(comprehensionDivision(15, 20, 5)) //Some(3, 4)
    println(comprehensionDivision(15, 22, 5)) //None
    println(comprehensionDivision(14, 20, 5)) //None
}