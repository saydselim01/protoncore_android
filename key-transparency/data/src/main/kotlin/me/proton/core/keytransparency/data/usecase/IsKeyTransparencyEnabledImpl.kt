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

package me.proton.core.keytransparency.data.usecase

import me.proton.core.keytransparency.data.KeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.GetHostType
import me.proton.core.keytransparency.domain.usecase.HostType
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import java.util.Optional
import javax.inject.Inject

public class IsKeyTransparencyEnabledImpl @Inject constructor(
    @KeyTransparencyEnabled
    private val keyTransparencyEnabled: Optional<Boolean>,
    private val getHostType: GetHostType
) : IsKeyTransparencyEnabled {

    override suspend fun invoke(): Boolean {
        // If not present, KT is disabled by default
        val localFeatureFlag = keyTransparencyEnabled.isPresent && keyTransparencyEnabled.get()
        return localFeatureFlag && getHostType() != HostType.Other
    }
}
