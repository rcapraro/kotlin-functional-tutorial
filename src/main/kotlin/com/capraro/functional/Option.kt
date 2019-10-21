package com.capraro.functional

sealed class Option<out T> {
    companion object
}

object None : Option<Nothing>() {
    override fun toString(): String {
        return "None"
    }
}

data class Some<out T>(val value: T) : Option<T>()

//Functor
/*interface Functor<C<_>> { //Invalid Kotlin code
    fun <A,B> map(ca: C<A>, transform: (A) -> B): C<B>
}*/
fun <T, R> Option<T>.map(transform: (T) -> R) = when (this) {
    None -> None
    is Some -> Some(transform(this.value))
}

//Monad
/*interface Monad<C<_>>: Functor<C> { //Invalid Kotlin code
    fun <A, B> flatMap(ca:C<A>, fm:(A) -> C<B>): C<B>
}*/
fun <T, R> Option<T>.flatMap(fm: (T) -> Option<R>) = when (this) {
    None -> None
    is Some -> fm(this.value)
}

//Map rewritten with flatMap
fun <T, R> Option<T>.map2(transform: (T) -> R) = flatMap { Some(transform(it)) }

//Applicative
/*interface Applicative<C<_>>: Functor<C> { //Invalid Kotlin code
    fun <A> just(a:A): C<A>

    fun <A, B> ap(ca:C<A>, fab: C<(A) -> B>): C<B>
}*/
fun <T> Option.Companion.just(t: T): Option<T> = Some(t)

fun <T, R> Option<T>.ap(fab: Option<(T) -> R>): Option<R> = fab.flatMap { f -> map(f) }

infix fun <T, R> Option<(T) -> R>.applyTo(o: Option<T>): Option<R> = flatMap { f: (T) -> R -> o.map(f) }

fun half(a: Int) = when {
    a % 2 == 0 -> Some(a / 2)
    else -> None
}


fun main() {

    println(Some("Hello world").map(String::toUpperCase))

    println(None.map(String::reversed))

    println(Some(2).map { it.div(5.0) })

    println(calculateDiscount(Some(100_000.0)))

    //flatMap can be nested !
    println(Some(5).flatMap { f ->
        Some(2).flatMap { t ->
            Some(f + t)
        }
    })

    //another example
    println(
        Some(20)
            .flatMap(::half)  //10
            .flatMap(::half)  //5
            .map { t -> t * 10 } //50
    )

    //A shorter version with map
    println(Option.just(5).flatMap { f ->
        Option.just(2).map { t ->
            (f + t)
        }
    })

    //and with ap
    println(Option.just(5).ap(Option.just(2).map { f ->
        { t: Int -> f + t }
    }))

    //with the apply function
    println(Option.just { f: Int -> { t: Int -> f + t } } applyTo Option.just(5) applyTo Option.just(2))// Some(value=7)

}

fun calculateDiscount(price: Option<Double>): Option<Double> {

    return price.flatMap {
        when {
            it > 50_000.0 -> Some(0.2 * it)
            it > 5_000.0 -> Some(0.1 * it)
            else -> None
        }
    }
}