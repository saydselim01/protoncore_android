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

package me.proton.core.humanverification.presentation.viewmodel.verification

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.country.presentation.entity.CountryUIModel
import me.proton.core.humanverification.domain.entity.TokenType
import me.proton.core.humanverification.domain.entity.VerificationResult
import me.proton.core.humanverification.domain.usecase.VerifyCode
import me.proton.core.humanverification.presentation.exception.TokenCodeVerificationException
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.network.domain.session.SessionId
import me.proton.core.presentation.viewmodel.ProtonViewModel
import me.proton.core.presentation.viewmodel.ViewModelResult

/**
 * View model class that handles and supports [TokenType.CAPTCHA] verification method (type) fragment.
 */
internal class HumanVerificationCaptchaViewModel @ViewModelInject constructor(
    private val verifyCode: VerifyCode,
    private val networkManager: NetworkManager
) : ProtonViewModel(), HumanVerificationCode {

    private val _codeVerificationResult = MutableStateFlow<ViewModelResult<Boolean>>(ViewModelResult.None)
    private val _networkConnectionState = MutableStateFlow<ViewModelResult<Boolean>>(ViewModelResult.None)

    /**
     * Code is sometimes referred as a token, so token on BE and code on UI, it is same thing.
     */
    val codeVerificationResult = _codeVerificationResult.asStateFlow()
    val networkConnectionState = _networkConnectionState.asStateFlow()

    init {
        networkWatcher()
    }

    /**
     * Contacts the API and sends the human verification token code.
     */
    fun verifyTokenCode(sessionId: SessionId, token: String?) {
        requireNotNull(token)
        require(token.isNotEmpty()) { "Verification token is empty." }
        viewModelScope.launch {
            val result = verifyCode(sessionId, TokenType.CAPTCHA.name, token)
            if (result is VerificationResult.Success) {
                _codeVerificationResult.tryEmit(ViewModelResult.Success(true))
            } else {
                _codeVerificationResult.tryEmit(ViewModelResult.Error(TokenCodeVerificationException()))
            }
        }
    }

    /**
     * Watches for any network changes and informs the UI for any state change so that it can act
     * accordingly for any network dependent tasks.
     */
    private fun networkWatcher() {
        networkManager.observe().onEach { status ->
            _networkConnectionState.tryEmit(
                ViewModelResult.Success(
                    when (status) {
                        NetworkStatus.Metered, NetworkStatus.Unmetered -> true
                        else -> false
                    }
                )
            )
        }.launchIn(viewModelScope)
    }
}
