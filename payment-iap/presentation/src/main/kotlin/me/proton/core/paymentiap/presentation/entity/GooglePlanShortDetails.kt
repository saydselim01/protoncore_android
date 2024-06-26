/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
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

package me.proton.core.paymentiap.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import me.proton.core.domain.entity.AppStore
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.presentation.entity.PlanShortDetails
import me.proton.core.payment.presentation.entity.PaymentVendorDetails

/**
 * @param vendors Map of plan names for app vendors (the plan names are for the given [subscriptionCycle]).
 */
@Parcelize
public data class GooglePlanShortDetails(
    val name: String,
    val displayName: String,
    val subscriptionCycle: SubscriptionCycle,
    val amount: Long? = null,
    val currency: String? = null,
    val formattedPriceAndCurrency: String? = null,
    val services: Int,
    val type: Int,
    val vendors: Map<AppStore, PaymentVendorDetails>
) : Parcelable {

    public companion object {
        public fun fromPlanShortDetails(plan: PlanShortDetails): GooglePlanShortDetails =
            GooglePlanShortDetails(
                name = plan.name,
                displayName = plan.displayName,
                subscriptionCycle = plan.subscriptionCycle,
                amount = null,
                currency = null,
                formattedPriceAndCurrency = null,
                services = plan.services,
                type = plan.type,
                vendors = plan.vendors
            )
    }
}
