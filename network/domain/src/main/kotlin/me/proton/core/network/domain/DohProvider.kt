/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.core.network.domain

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import me.proton.core.network.domain.serverconnection.DohAlternativesListener
import me.proton.core.network.domain.session.SessionId
import java.util.concurrent.TimeUnit

/**
 * Gets the list of alternative baseUrls for Proton API.
 */
interface DohService {
    suspend fun getAlternativeBaseUrls(sessionId: SessionId?, primaryBaseUrl: String): List<String>?
}

/**
 * Refreshes alternative urls for [baseUrl] using given list of DoH services ([dohServices]). Makes
 * sure that only one refresh operation takes place at one time for given baseUrl. Single instance
 * should exist per baseUrl.
 */
class DohProvider(
    private val baseUrl: String,
    private val apiClient: ApiClient,
    private val dohServices: List<DohService>,
    private val protonDohService: DohService,
    private val networkMainScope: CoroutineScope,
    private val prefs: NetworkPrefs,
    private val monoClockMs: () -> Long,
    private val sessionId: SessionId?,
    private val dohAlternativesListener: DohAlternativesListener?
) {

    suspend fun refreshAlternatives() = withContext(networkMainScope.coroutineContext) {
        if (monoClockMs() >= lastAlternativesRefresh + MIN_REFRESH_INTERVAL_MS) {
            val allServicesFailed = tryDohServices()
            lastAlternativesRefresh = monoClockMs()

            if (allServicesFailed && dohAlternativesListener != null) {
                dohAlternativesListener.onAlternativesUnblock {
                    tryProtonDohService()
                }
            }
        }
    }

    private suspend fun tryProtonDohService(): Boolean {
        val success = withTimeoutOrNull(apiClient.dohServiceTimeoutMs) {
            val result = protonDohService.getAlternativeBaseUrls(sessionId, baseUrl)
            if (result != null)
                prefs.alternativeBaseUrls = result
            result != null
        }
        return success ?: false
    }

    private suspend fun tryDohServices(): Boolean = coroutineScope {
        var allServicesFailed = true
        val jobs = mutableListOf<Job>()
        dohServices.mapTo(jobs) { service ->
            launch {
                val success = withTimeoutOrNull(apiClient.dohServiceTimeoutMs) {
                    val result = service.getAlternativeBaseUrls(sessionId, baseUrl)
                    if (result != null)
                        prefs.alternativeBaseUrls = result
                    result != null
                } ?: false
                if (success) {
                    allServicesFailed = false
                    jobs.forEach { it.cancel() }
                }
            }
        }
        jobs.joinAll()
        allServicesFailed
    }

    companion object {
        val MIN_REFRESH_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10)

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        @Volatile var lastAlternativesRefresh = Long.MIN_VALUE
    }
}
