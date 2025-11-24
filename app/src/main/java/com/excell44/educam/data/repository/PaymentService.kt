package com.excell44.educam.data.repository

import kotlinx.coroutines.delay

/**
 * Simple payment simulation service.
 * In production this should call a real payment SDK or backend.
 */
class PaymentService {
    /**
     * Simulate a payment attempt. Returns true for success, false for failure.
     * We simulate network latency and a 70% success chance.
     */
    suspend fun attemptPayment(amountCents: Int = 1000): Boolean {
        delay(800) // simulate network
        // simple random success for simulation
        return (0..99).random() < 70
    }
}
