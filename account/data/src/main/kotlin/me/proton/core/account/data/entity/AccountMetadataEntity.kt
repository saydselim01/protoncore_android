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

package me.proton.core.account.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.account.domain.entity.AccountMetadataDetails
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId

@Entity(
    primaryKeys = ["userId", "product"],
    indices = [
        Index("userId"),
        Index("product"),
        Index("primaryAtUtc")
    ],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AccountMetadataEntity(
    val userId: UserId,
    val product: Product,
    val primaryAtUtc: Long,
    val migrations: List<String>? = null
) {
    fun toAccountMetadataDetails() = AccountMetadataDetails(
        primaryAtUtc = primaryAtUtc,
        migrations = migrations.orEmpty()
    )
}
