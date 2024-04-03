/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.presentation.compose

import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.SecureFlagPolicy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import me.proton.core.domain.entity.UserId
import me.proton.core.userrecovery.presentation.compose.view.DeviceRecoveryDialog

object DeviceRecoveryDeeplink {
    object Arg {
        const val UserId = "{userId}"
    }

    object Recovery {
        const val Deeplink: String = "user/${Arg.UserId}/device/recovery"
        fun get(userId: UserId): String = "user/${userId.id}/device/recovery"
    }
}

internal fun NavGraphBuilder.addDeviceRecoveryDialog(
    userId: UserId,
    onDismiss: () -> Unit
) {
    dialog(
        route = DeviceRecoveryDeeplink.Recovery.Deeplink,
        arguments = listOf(
            navArgument(DeviceRecoveryDeeplink.Arg.UserId) {
                type = NavType.StringType
                defaultValue = userId.id
            },
        ),
        dialogProperties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            securePolicy = SecureFlagPolicy.SecureOn
        )
    ) {
        DeviceRecoveryDialog(
            onDismiss = { onDismiss() }
        )
    }
}
