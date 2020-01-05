package com.capraro.functional.arrow

import arrow.Kind
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.io.async.async
import arrow.fx.fix
import arrow.fx.rx2.ObservableK
import arrow.fx.rx2.extensions.observablek.async.async
import arrow.fx.rx2.fix
import arrow.fx.typeclasses.Async
import arrow.typeclasses.Applicative
import arrow.typeclasses.ApplicativeError
import arrow.typeclasses.Functor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET

private val data = mapOf("John" to 0.2f, "Richard" to 0.5f)

fun <F> getBitCoinForUser(username: String, AE: ApplicativeError<F, Throwable>): Kind<F, Float> {
    val dataFromMap = data[username]
    return if (dataFromMap != null) {
        AE.just(dataFromMap)
    } else {
        AE.raiseError(RuntimeException("No user found"))
    }
}

data class InternalResponse(
    val bpi: Bpi,
    val disclaimer: String,
    val time: Time
)

data class Bpi(
    val EUR: EUR,
    val USD: USD
)

data class EUR(
    val code: String,
    val description: String,
    val rate: String,
    val rate_float: Float
)

data class USD(
    val code: String,
    val description: String,
    val rate: String,
    val rate_float: Double
)

data class Time(
    val updated: String,
    val updatedISO: String,
    val updateduk: String
)

fun initApi(): BitcoinApi {

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.coindesk.com/v1/bpi/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    return retrofit.create(BitcoinApi::class.java)

}

interface BitcoinApi {
    @GET("currentprice/EUR.json")
    fun getCurrentEURPrice(): Call<InternalResponse>
}


fun <F> getPriceInEUR(api: BitcoinApi, async: Async<F>): Kind<F, InternalResponse> {
    return async.async { callback: (Either<Throwable, InternalResponse>) -> Unit ->
        api.getCurrentEURPrice().enqueue(
            object : Callback<InternalResponse> {
                override fun onResponse(call: Call<InternalResponse>, response: Response<InternalResponse>) {
                    callback(response.body()!!.right())
                }

                override fun onFailure(call: Call<InternalResponse>, t: Throwable) {
                    callback(t.left())
                }
            }
        )
    }
}

fun <F> parseResponse(response: Kind<F, InternalResponse>, functor: Functor<F>): Kind<F, Float> {
    return functor.run { response.map { it.bpi.EUR.rate_float } }
}

fun <F> calculate(quantity: Kind<F, Float>, price: Kind<F, Float>, applicative: Applicative<F>): Kind<F, Float> {
    return applicative.map(quantity, price) { it.a * it.b }
}

fun <F> useCase(username: String, api: BitcoinApi, A: Async<F>): Kind<F, Float> {
    val bitcoinForUser = getBitCoinForUser(username, A)
    val priceResponse = getPriceInEUR(api, A)
    val price = parseResponse(priceResponse, A)
    return calculate(bitcoinForUser, price, A)
}

fun main() {

    //Polymorphism using IO or Rx

    val api = initApi()
    useCase("Richard", api, IO.async()).fix().unsafeRunAsync { result ->
        result.fold(
            { println("Error: $it") },
            { println("Success: $it") }
        )
    }

    useCase("Richard", api, ObservableK.async()).fix().observable.subscribe(
        { println("OnNext: $it") },
        { println("OnError: $it") }
    )
}
