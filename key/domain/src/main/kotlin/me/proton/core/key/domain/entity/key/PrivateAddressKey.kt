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

package me.proton.core.key.domain.entity.key

import me.proton.core.crypto.common.pgp.Armored

/**
 * @property signedKeyList Updated signed key list of the address. Only needed for the backend calls,
 * when a change to the address keys is made.
 */

data class PrivateAddressKey(
    val addressId: String,
    val privateKey: PrivateKey,
    val token: Armored?,
    val signature: Armored?,
    val signedKeyList: PublicSignedKeyList? = null
)
