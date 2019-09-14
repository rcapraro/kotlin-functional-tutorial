package com.capraro.functional

//Functor...forward function composition
fun <A, B, C> ((A) -> B).map(transform: (B) -> C): (A) -> C = { t -> transform(this(t)) }

fun <A, B, C> ((A) -> B).flatMap(fm: (B) -> (A) -> C): (A) -> C = { t -> fm(this(t))(t) }

fun <A, B, C> ((A) -> B).ap(fab: (A) -> (B) -> C): (A) -> C = fab.flatMap { f -> map(f) }

fun main() {

    val add3AndMultiplyBy2: (Int) -> Int = { i: Int -> i + 3 }.map { j: Int -> j * 2 }

    println(add3AndMultiplyBy2(2)) //10

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


    val add4AndMultiplyBy3: (Int) -> Int = { i: Int -> i + 4 }.ap { { j: Int -> j * 3 } }
    println(add4AndMultiplyBy3(2)) //18

    //ap can access the original parameter
    val add5AndMultiplyBy4: (Int) -> Pair<Int, Int> = { i: Int -> i + 5 }.ap { original -> { j: Int -> original to (j * 4) } }
    println(add5AndMultiplyBy4(0)) //(0, 20)
    println(add5AndMultiplyBy4(1)) //(1, 24)
    println(add5AndMultiplyBy4(2)) //(2, 28)

}


