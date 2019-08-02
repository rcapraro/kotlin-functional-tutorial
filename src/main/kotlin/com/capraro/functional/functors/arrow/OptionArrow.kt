package com.capraro.functional.functors.arrow

import arrow.core.Option
import arrow.core.extensions.option.monad.binding

data class Country(val code: Option<String>)
data class Address(val id: Int, val country: Option<Country>)
data class Person(val name: String, val address: Option<Address>)

fun getCountryCode(maybePerson: Option<Person>): Option<String> =
    maybePerson.flatMap { person ->
        person.address.flatMap { address ->
            address.country.flatMap { country ->
                country.code
            }
        }
    }

//Monad continuation
fun getCountryCodeMC(maybePerson: Option<Person>): Option<String> =
    binding {
        val (person) = maybePerson
        val (address) = person.address
        val (country) = address.country
        val (code) = country.code
        code
    }