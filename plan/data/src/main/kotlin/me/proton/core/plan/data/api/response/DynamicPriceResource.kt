/*
 * Copyright (c) 2023 Proton AG
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

package me.proton.core.plan.data.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.plan.domain.entity.DynamicPlanPrice

@Serializable
internal data class DynamicPriceResource(
    @SerialName("ID")
    val id: String,

    @SerialName("Currency")
    val currency: String,

    @SerialName("Current")
    val current: Int, // cents

    @SerialName("Default")
    val default: Int? = null // cents
)

internal fun DynamicPriceResource.toDynamicPlanPrice(): DynamicPlanPrice = DynamicPlanPrice(
    id = id,
    currency = currency,
    current = current,
    default = default
)
