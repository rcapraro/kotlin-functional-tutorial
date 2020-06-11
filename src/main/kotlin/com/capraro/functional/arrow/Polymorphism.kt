package com.capraro.functional.arrow

import arrow.Kind
import arrow.core.ListK
import arrow.core.Option
import arrow.core.extensions.listk.functor.functor
import arrow.core.extensions.monoid
import arrow.core.extensions.option.applicative.applicative
import arrow.core.extensions.option.functor.functor
import arrow.core.extensions.semigroup
import arrow.core.fix
import arrow.core.k
import arrow.typeclasses.Applicative
import arrow.typeclasses.Functor

//use a functor to map() a Kind<F, String> to a Kind<F, Int>
fun <F> parseInt(value: Kind<F, String>, functor: Functor<F>): Kind<F, Int> =
    functor.run { value.map { it.toInt() } }

fun <F> multiply(amount: Kind<F, Float>, multiplier: Kind<F, Float>, applicative: Applicative<F>): Kind<F, Float> =
    applicative.mapN(amount, multiplier) { it.a * it.b }

fun main() {

    println(Int.semigroup().run { 1.combine(2) })

    println(String.monoid().run { listOf("H", "e", "l", "l", "o").combineAll() })

    println(parseInt(Option.just("42"), Option.functor()).fix())

    val value: ListK<String> = listOf("1", "2", "3").k()
    println(parseInt(value, ListK.functor()).fix())

    println(multiply(Option.just(42f), Option.just(2f), Option.applicative()).fix())

}
