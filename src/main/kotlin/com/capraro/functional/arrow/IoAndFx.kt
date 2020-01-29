package com.capraro.functional.arrow

import arrow.core.Either
import arrow.core.extensions.fx
import arrow.core.flatMap
import arrow.core.right
import arrow.fx.IO
import arrow.fx.extensions.fx
import kotlinx.coroutines.runBlocking

typealias UserId = String

data class User(val id: String, val name: String)

data class Json(val body: String)

data class JsonException(val message: String)

data class ApiError(val message: String)

fun parseUser(json: Json): Either<JsonException, User> {
    //always work
    return Either.right(User("1", "Richard"))
}

suspend fun parseUserCo(json: Either<ApiError, Json>): Either<JsonException, User> {
    //always work
    return Either.right(User("1", "Richard"))
}

interface Api {
    fun query(q: String): Either<ApiError, Json>
    suspend fun queryCo(q: String): Either<ApiError, Json>
    suspend fun queryIO(q: String): IO<Either<ApiError, Json>>
}

class MyApi : Api {
    override fun query(q: String): Either<ApiError, Json> {
        return Json("{user: 1}").right()
    }

    override suspend fun queryCo(q: String): Either<ApiError, Json> {
        return Json("{user: 1}").right()
    }

    override suspend fun queryIO(q: String): IO<Either<ApiError, Json>> = IO {
        Json("{user: 1}").right()
    }

    // classic flatmap on Either
    fun getUserFlatMap(id: UserId): Either<ApiError, User> =
            query("SELECT * FROM users WHERE id = $id")
                    .flatMap {
                        parseUser(it)
                                .mapLeft { jsonException -> ApiError("Failed to parse: exception $jsonException") }
                    }

    //Monad comprehension with Either.fx and bind()
    suspend fun getUserFx(id: UserId): Either<ApiError, User> = Either.fx {
        val result = query("SELECT * FROM users WHERE id = $id").bind()
        parseUser(result).mapLeft { ApiError("Failed to parse") }.bind()
    }

    //Lift in IO
    suspend fun getUserIoAndFx(id: UserId) = IO.fx {
        val userRequest = effect { queryCo("123") }.bind()
        effect { println("Seen $userRequest") }.bind()
        effect { parseUserCo(userRequest) }.bind()
    }
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