/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.usersettings.domain.usecase

import me.proton.core.usersettings.domain.repository.DeviceSettingsRepository
import javax.inject.Inject

class UpdateDeviceSettings @Inject constructor(
    private val repository: DeviceSettingsRepository,
) {
    suspend fun updateIsTelemetryEnabled(isEnabled: Boolean) =
        repository.updateIsTelemetryEnabled(isEnabled)

    suspend fun updateIsCrashReportEnabled(isEnabled: Boolean) =
        repository.updateIsCrashReportEnabled(isEnabled)
}
