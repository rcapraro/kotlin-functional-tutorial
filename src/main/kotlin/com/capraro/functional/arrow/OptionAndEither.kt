package com.capraro.functional.arrow

import arrow.core.Either
import arrow.core.Option
import arrow.core.identity
import arrow.syntax.function.pipe
import kotlin.math.absoluteValue

object UserService {

    fun findAge(user: String): Either<String, Option<Int>> {
        return when (user) {
            "Brigitte" -> Either.right(Option.just(66))
            "Emmanuel" -> Either.right(Option.just(41))
            "Alexandre" -> Either.left("Elysée Error")
            else -> Either.right(Option.empty())
        }
    }
}

fun main() {

    val brigitteAge: Either<String, Option<Int>> = UserService.findAge("Brigitte")

    brigitteAge.fold(::identity) { op ->
        op.fold({ "Not found" }, Int::toString)
    } pipe ::println


    val emmanuelAge: Either<String, Option<Int>> = UserService.findAge("Emmanuel")

    val difference: Either<String, Option<Either<String, Option<Int>>>> = brigitteAge.map { bOp ->
        bOp.map { a ->
            emmanuelAge.map { eOp ->
                eOp.map { p ->
                    (a - p).absoluteValue
                }
            }
        }
    }

    difference.fold(::identity) { op1 ->
        op1.fold({ "Not Found" }, { either ->
            either.fold(::identity) { op2 ->
                op2.fold({ "Not Found" }, Int::toString)
            }
        })
    } pipe ::println


}

