/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.auth.test.robot.signup

import me.proton.core.auth.presentation.R
import me.proton.core.humanverification.test.robot.HvCodeRobot
import me.proton.core.test.android.instrumented.matchers.inputFieldMatcher
import me.proton.test.fusion.Fusion.view

public object ForExternalRobot {

    private val emailInput = view.withCustomMatcher(inputFieldMatcher(R.id.emailInput))
    private val nextButton = view.withId(R.id.nextButton)
    private val switchButton = view.withId(R.id.switchButton)

    public fun fillEmail(email: String): ForExternalRobot = apply {
        emailInput.typeText(email)
    }

    public fun clickNext(): HvCodeRobot {
        nextButton.click()
        return HvCodeRobot
    }

    public fun clickSwitch(): ForInternalRobot {
        switchButton.click()
        return ForInternalRobot
    }

    public fun uiElementsDisplayed() {
        emailInput.checkIsDisplayed()
        nextButton.checkIsDisplayed()
    }
}
