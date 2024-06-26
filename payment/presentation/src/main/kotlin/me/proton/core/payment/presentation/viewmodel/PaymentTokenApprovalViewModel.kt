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

package me.proton.core.payment.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkStatus
import me.proton.core.payment.domain.entity.PaymentTokenStatus
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.usecase.GetPaymentTokenStatus
import me.proton.core.payment.presentation.entity.SecureEndpoint
import me.proton.core.presentation.viewmodel.ProtonViewModel
import javax.inject.Inject

@HiltViewModel
public class PaymentTokenApprovalViewModel @Inject constructor(
    private val getPaymentTokenStatus: GetPaymentTokenStatus,
    private val secureEndpoint: SecureEndpoint,
    private val networkManager: NetworkManager
) : ProtonViewModel() {

    private val _approvalState = MutableStateFlow<State>(State.Idle)
    private val _networkConnectionState = MutableStateFlow<Boolean?>(null)

    public val approvalState: StateFlow<State> = _approvalState.asStateFlow()
    public val networkConnectionState: StateFlow<Boolean?> = _networkConnectionState.asStateFlow()

    public sealed class State {
        public object Idle : State()
        public object Processing : State()
        public data class Success(val paymentTokenStatus: PaymentTokenStatus) : State()
        public data class Error(val error: Throwable) : State()
    }

    /**
     * Handles the Webview redirect result. It also checks if the payment token has been approved.
     */
    public fun handleRedirection(
        userId: UserId?,
        paymentToken: ProtonPaymentToken,
        uri: Uri,
        paymentReturnHost: String
    ): Boolean = if (uri.host == secureEndpoint.host || uri.host == paymentReturnHost) {
        if (uri.getQueryParameter("cancel") == CANCEL_QUERY_PARAM_VALUE) {
            true
        } else {
            checkPaymentTokenApproved(userId, paymentToken)
            false
        }
    } else {
        false
    }

    /**
     * Watches for any network changes and informs the UI for any state change so that it can act
     * accordingly for any network dependent tasks.
     */
    public fun watchNetwork() {
        viewModelScope.launch {
            networkManager.observe().collect { status ->
                _networkConnectionState.tryEmit(
                    when (status) {
                        NetworkStatus.Metered, NetworkStatus.Unmetered -> true
                        else -> false
                    }
                )
            }
        }
    }

    private fun checkPaymentTokenApproved(userId: UserId?, paymentToken: ProtonPaymentToken) = flow {
        emit(State.Processing)
        emit(State.Success(getPaymentTokenStatus(userId, paymentToken).status))
    }.catch {
        _approvalState.tryEmit(State.Error(it))
    }.onEach {
        _approvalState.tryEmit(it)
    }.launchIn(viewModelScope)

    private companion object {
        private const val CANCEL_QUERY_PARAM_VALUE: String = "1"
    }
}
