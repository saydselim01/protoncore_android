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

package me.proton.core.network.domain.scopes

import me.proton.core.domain.entity.UserId

sealed class MissingScopeState {

    /**
     * One or more missing scope needed.
     *
     * Note: Usually followed by either [ScopeObtainSuccess] or [ScopeObtainFailed].
     *
     * @see [ScopeObtainSuccess]
     * @see [ScopeObtainFailed].
     */
    data class ScopeMissing(val userId: UserId, val missingScopes: List<Scope>) : MissingScopeState()

    /**
     * The missing scope has been obtained successfully.
     */
    object ScopeObtainSuccess : MissingScopeState()

    /**
     * The user has not verified and the scope has not been granted.
     */
    object ScopeObtainFailed : MissingScopeState()
}
