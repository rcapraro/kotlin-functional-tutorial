package com.capraro.functional.safeprogramming

class CreditCard

private class Donut {
    companion object {
        const val price = 2
    }
}

private class Payment(val creditCard: CreditCard, val amount: Int) {
    fun combine(payment: Payment): Payment =
        if (creditCard == payment.creditCard)
            Payment(creditCard, amount + payment.amount)
        else
            throw IllegalArgumentException("Cards don't match.")

    companion object {
        fun groupByCard(payments: List<Payment>): List<Payment> =
            payments.groupBy { it.creditCard }
                .values
                .map { it.reduce(Payment::combine) }
    }
}

private class Purchase(val donut: List<Donut>, val payment: Payment)

private fun buyDonuts(quantity: Int = 1, creditCard: CreditCard): Purchase =
    Purchase(
        List(quantity) {
            Donut()
        },
        Payment(
            creditCard,
            Donut.price * quantity
        )
    )

fun main() {
    val creditCard = CreditCard()
    val purchase = buyDonuts(5, creditCard)
    assert(purchase.payment.amount == Donut.price * 5)
    assert(purchase.payment.creditCard == creditCard)
}