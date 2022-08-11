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

package me.proton.core.paymentcommon.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.paymentcommon.domain.MAX_PLAN_QUANTITY
import me.proton.core.paymentcommon.domain.entity.Currency
import me.proton.core.paymentcommon.domain.entity.PaymentBody
import me.proton.core.paymentcommon.domain.entity.Subscription
import me.proton.core.paymentcommon.domain.entity.SubscriptionCycle
import me.proton.core.paymentcommon.domain.repository.PaymentsRepository
import javax.inject.Inject

/**
 * Creates new subscription.
 * Authorized. This means that it could only be used for upgrades. Sign ups should be handled through Human Verification
 * Headers with token and token type "payment".
 */
public class PerformSubscribe @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val humanVerificationManager: HumanVerificationManager,
    private val clientIdProvider: ClientIdProvider,
) {
    /**
     * @param codes optional an array of [String] coupon or gift codes used for discounts.
     * @param paymentToken optional??? payment token.
     */
    public suspend operator fun invoke(
        userId: UserId,
        amount: Long,
        currency: Currency,
        cycle: SubscriptionCycle,
        planNames: List<String>,
        codes: List<String>? = null,
        paymentToken: String? = null
    ): Subscription {
        require(amount >= 0)
        require(planNames.isNotEmpty())
        require(paymentToken != null || amount <= 0) {
            "Payment Token must be supplied when the amount is bigger than zero. Otherwise it should be null."
        }
        return paymentsRepository.createOrUpdateSubscription(
            sessionUserId = userId,
            amount = amount,
            currency = currency,
            payment = if (amount == 0L) null else PaymentBody.TokenPaymentBody(paymentToken!!),
            codes = codes,
            plans = planNames.map { it to MAX_PLAN_QUANTITY }.toMap(),
            cycle = cycle
        ).also {
            if (paymentToken != null) {
                // Clear any previous payment token (unauthenticated session cookie HV details).
                // HV payment token is previously added by BillingCommonViewModel.
                val clientId = requireNotNull(clientIdProvider.getClientId(sessionId = null))
                humanVerificationManager.clearDetails(clientId)
            }
        }
    }
}