package com.capraro.functional.functors

//Functor...forward function composition
fun <A, B, C> ((A) -> B).map(transform: (B) -> C): (A) -> C = { t -> transform(this(t)) }

fun main() {

    val add3AndMultiplyBy2: (Int) -> Int = { i: Int -> i + 3 }.map { j: Int -> j * 2 }

    println(add3AndMultiplyBy2(2))

    println(
        listOf(1, 2, 3)
            .flatMap { i ->
                listOf(i + 10, i * 2)
            }.joinToString()
    )

    //Equivalent to: (to introduce Applicatives)
    val numbers = listOf(1, 2, 3)
    val functions = listOf<(Int) -> Int>({ i -> i + 10 }, { i -> i * 2 })
    println(numbers.flatMap { number ->
        functions.map { f ->
            f(number)
        }
    }.joinToString())

}


