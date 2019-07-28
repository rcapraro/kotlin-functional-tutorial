package com.capraro.functional.functors

//Functor...forward function composition
fun <A, B, C> ((A) -> B).map(transform: (B) -> C): (A) -> C = { t -> transform(this(t)) }

fun main() {

    val add3AndMultiplyBy2: (Int) -> Int = { i: Int -> i + 3 }.map { j: Int -> j * 2 }

    println(add3AndMultiplyBy2(2))
}
