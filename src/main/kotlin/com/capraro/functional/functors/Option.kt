package com.capraro.functional.functors

sealed class Option<out T> {

    object None : Option<Nothing>() {
        override fun toString(): String {
            return "None"
        }
    }

    data class Some<out T>(val value: T) : Option<T>()

    companion object
}

//Functor
/*interface Functor<C<_>> { //Invalid Kotlin code
    fun <A,B> map(ca: C<A>, transform: (A) -> B): C<B>
}*/
fun <T, R> Option<T>.map(transform: (T) -> R) = when (this) {
    Option.None -> Option.None
    is Option.Some -> Option.Some(transform(this.value))
}

//Monad
/*interface Monad<C<_>>: Functor<C> { //Invalid Kotlin code
    fun <A, B> flatMap(ca:C<A>, fm:(A) -> C<B>): C<B>
}*/
fun <T, R> Option<T>.flatMap(fm: (T) -> Option<R>) = when (this) {
    Option.None -> Option.None
    is Option.Some -> fm(this.value)
}

//Map rewritten with flatMap
fun <T, R> Option<T>.map2(transform: (T) -> R) = flatMap { Option.Some(transform(it)) }

//Applicative
/*interface Applicative<C<_>>: Functor<C> { //Invalid Kotlin code
    fun <A> pure(a:A): C<A>

    fun <A, B> ap(ca:C<A>, fab: C<(A) -> B>): C<B>
}*/
fun <T> Option.Companion.pure(t: T): Option<T> = Option.Some(t)

fun <T, R> Option<T>.ap(fab: Option<(T) -> R>): Option<R> = fab.flatMap { f -> map(f) }

fun main() {

    println(Option.Some("Hello world").map(String::toUpperCase))

    val newOption = Option.Some(2).map { it.div(5.0) }
    println(newOption)

    println(calculateDiscount(Option.Some(100_000.0)))

    //flatMap can be nested !
    println(Option.Some(5).flatMap { f ->
        Option.Some(2).flatMap { t ->
            Option.Some(f + t)
        }
    })

    //A shorter version with map
    println(Option.Some(5).flatMap { f ->
        Option.Some(2).map { t ->
            (f + t)
        }
    })

    //and with ap
    println(Option.Some(5).ap(Option.Some(2).map { f ->
        { t: Int -> f + t }
    }))

    Option.None.map(String::reversed)
}

fun calculateDiscount(price: Option<Double>): Option<Double> {

    return price.flatMap {
        when {
            it > 50_000.0 -> Option.Some(0.2 * it)
            it > 5_000.0 -> Option.Some(0.1 * it)
            else -> Option.None
        }
    }
}