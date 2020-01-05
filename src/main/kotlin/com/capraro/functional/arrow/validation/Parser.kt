package com.capraro.functional.arrow.validation

import arrow.core.*
import arrow.core.extensions.fx
import arrow.core.extensions.nonemptylist.semigroup.semigroup
import arrow.core.extensions.validated.applicative.applicative
import com.google.common.net.HostAndPort
import com.typesafe.config.Config
import com.typesafe.config.ConfigException
import com.typesafe.config.ConfigFactory

fun <A> get(path: String, extractor: (String) -> A): Either<ConfigError, A> = try {
    Either.right(extractor(path))
} catch (e: ConfigException.Missing) {
    Either.left(ConfigError.ParameterIsMissing(path))
} catch (e: ConfigException.WrongType) {
    Either.left(ConfigError.CouldNotParse)
}

fun validateBusinessConfig(config: Config): ValidatedNel<ConfigError, BusinessConfig> = run {

    val unvalidatedTAE = get("app.thresholdA") { p -> config.getInt(p) }
    val tAe = unvalidatedTAE.flatMap { unvalidatedTa ->
        if (unvalidatedTa < 0)
            Either.left(ConfigError.ThresholdATooLow(unvalidatedTa, 0))
        else
            Either.right(unvalidatedTa)
    }

    val unvalidatedTCE = get("app.thresholdC") { p -> config.getInt(p) }
    val tCe = unvalidatedTCE.flatMap { unvalidatedTc ->
        if (unvalidatedTc > 10000)
            Either.left(ConfigError.ThresholdCTooHigh(unvalidatedTc, 10000))
        else
            Either.right(unvalidatedTc)
    }

    val unvalidatedTBE = get("app.thresholdB") { p -> config.getInt(p) }
    val tBe = Either.fx<ConfigError, Int> {
        val (ta) = unvalidatedTAE
        val (tb) = unvalidatedTBE
        val (tc) = unvalidatedTCE
        if (tb in (ta + 1) until tc)
            Either.right(tb).bind()
        else
            Either.left(ConfigError.ThresholdBNotInBetween(tb, ta, tc)).bind()
    }


    val tAV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tAe).toValidatedNel()
    val tBV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tBe).toValidatedNel()
    val tCV: ValidatedNel<ConfigError, Int> = Validated.fromEither(tCe).toValidatedNel()

    ValidatedNel.applicative(Nel.semigroup<ConfigError>()).map(tAV, tBV, tCV) {
        val a = it.a
        val b = it.b
        val c = it.c
        BusinessConfig(a, b, c)
    }.fix()
}

fun validateKafkaConfig(config: Config): ValidatedNel<ConfigError, KafkaConfig> = run {

    val applicationIdV = Validated.fromEither(get("kafka.applicationId") { config.getString(it) }).toValidatedNel()

    val serversE = get("kafka.bootstrapServers") { config.getStringList(it) }

    val serversV: ValidatedNel<ConfigError, ListK<HostAndPort>> = serversE.map { rawList ->
        if (rawList.isEmpty()) {
            ConfigError.NoBootstrapServers.invalidNel<ConfigError>()
        } else {
            rawList.withIndex()
                .map { validateHost(it.value, it.index) }.k()
                .traverse(Validated.applicative<Nel<ConfigError>>(Nel.semigroup()), { it })
                .fix()
        }
    }.getOrHandle { it.invalidNel() }

    // Combine
    ValidatedNel.applicative<Nel<ConfigError>>(Nel.semigroup()).map(applicationIdV, serversV) { KafkaConfig(it.a, it.b.toList()) }.fix()
}

fun validateHost(rawString: String, index: Int): ValidatedNel<ConfigError, HostAndPort> = try {
    HostAndPort.fromString(rawString).withDefaultPort(9092).validNel<HostAndPort>()
} catch (e: IllegalArgumentException) {
    ConfigError.InvalidHost(rawString, index).invalidNel<ConfigError>()
}

fun main() {

    val config = ConfigFactory.load()

    val businessValidation = validateBusinessConfig(config)
    val kafkaValidation = validateKafkaConfig(config)

    val finalValidation: ValidatedNel<ConfigError, ApplicationConfig> = Validated
        .applicative<Nel<ConfigError>>(Nel.semigroup())
        .map(kafkaValidation, businessValidation) { ApplicationConfig(it.a, it.b) }
        .fix()

    finalValidation.fold({ errors ->
        "Invalid : $errors"
    }, { conf ->
        "Valid: $conf"
    }).let { println(it) }

}