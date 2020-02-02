package com.capraro.functional.arrow

import arrow.core.*
import arrow.core.extensions.fx
import arrow.fx.ForIO
import arrow.fx.IO
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.monad.monad
import arrow.fx.fix
import arrow.fx.handleError
import arrow.mtl.EitherT
import arrow.mtl.extensions.eithert.monad.monad
import arrow.mtl.value
import kotlinx.coroutines.runBlocking

typealias UserId = String

data class User(val id: String, val name: String)

data class Json(val body: String)

data class JsonException(val message: String)

data class ApiError(val message: String)

fun parseUser(json: Json): Either<ApiError, User> {
    //always work
    return Right(User("1", "Richard"))
}

suspend fun parseUserCo(json: Json): Either<ApiError, User> {
    //always work
    return Right(User("1", "Richard"))
}

fun parseUserIO(json: Json): IO<Either<ApiError, User>> = IO {
    //always work
    Right(User("1", "Richard"))
}

interface Api {
    fun query(q: String): Either<ApiError, Json>
    suspend fun queryCo(q: String): Either<ApiError, Json>
    fun queryIO(q: String): IO<Either<ApiError, Json>>
}

class MyApi : Api {
    override fun query(q: String): Either<ApiError, Json> {
        return Json("{user: 1}").right()
    }

    override suspend fun queryCo(q: String): Either<ApiError, Json> {
        return Json("{user: 1}").right()
    }

    // equivalent to the preceding function because in Arrow FX,
    // a suspend function returning a value type (e.g Option<String>) can be considered the same
    // as an IO returning the same value type (IO<Option<String>>)
    override fun queryIO(q: String): IO<Either<ApiError, Json>> = IO {
        if (q.isEmpty()) {
            throw IllegalArgumentException()
        } else Json("{user: 1}").right()
    }.handleError { Left(ApiError("Error")) }

    // classic flatmap on Either
    fun getUserFlatMap(id: UserId): Either<ApiError, User> =
        query("SELECT * FROM users WHERE id = $id")
            .flatMap {
                parseUser(it)
            }

    //Monad comprehension with Either.fx and bind() (or '!' which is equivalent)
    suspend fun getUserFx(id: UserId): Either<ApiError, User> = Either.fx {
        val result = !query("SELECT * FROM users WHERE id = $id")
        println("JSON result is $result")
        !parseUser(result)
    }

    //Lift in IO
    suspend fun getUserIoAndFx(id: UserId) = EitherT.monad<ForIO, ApiError>(IO.monad()).fx.monad() {
        val userRequest = !EitherT(queryIO("SELECT * FROM users WHERE id = $id"))
        val user = !EitherT(parseUserIO(userRequest))
        user
    }.value().fix()
}

fun sayHello(): Unit =
    println("Hello World")

fun sayGoodBye(): Unit =
    println("Good bye World!")

// Apply the effect immediately
fun greetEffect(): IO<Unit> = IO.fx {
    !effect { sayHello() }
    !effect { sayGoodBye() }
}

fun main() {

    val api = MyApi()
    println(api.getUserFlatMap("1"))

    runBlocking {
        println(api.getUserFx("1"))
        println(api.getUserIoAndFx("1").unsafeRunSync())
    }

    runBlocking { greetEffect().unsafeRunSync() }
}