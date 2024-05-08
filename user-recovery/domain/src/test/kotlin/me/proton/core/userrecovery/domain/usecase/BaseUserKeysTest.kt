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

package me.proton.core.userrecovery.domain.usecase

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.PGPCrypto
import me.proton.core.crypto.common.pgp.exception.CryptoException
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.canUnlock
import me.proton.core.key.domain.entity.key.KeyId
import me.proton.core.key.domain.entity.key.PrivateKey
import me.proton.core.key.domain.entity.key.UnlockedPrivateKey
import me.proton.core.key.domain.fingerprint
import me.proton.core.key.domain.lock
import me.proton.core.key.domain.unlockOrNull
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.repository.PassphraseRepository
import org.junit.Before

/**
 * Test data: 1 User -> 2 UserKey -> 1 primary/active and 1 inactive.
 */
abstract class BaseUserKeysTest {

    internal val testSecretValid = "valid"
    internal val testSecretInvalid = "invalid"

    internal val unlockedKey = mockk<UnlockedPrivateKey> {
        every { this@mockk.unlockedKey } returns mockk { every { value } returns "unlocked".toByteArray() }
    }

    internal val testPrivateKeyInactive = mockk<PrivateKey> {
        every { this@mockk.isActive } returns false
        every { this@mockk.isPrimary } returns false
        every { this@mockk.key } returns "inactive.key"
    }
    internal val testPrivateKeyPrimary = mockk<PrivateKey> {
        every { this@mockk.isActive } returns true
        every { this@mockk.isPrimary } returns true
        every { this@mockk.key } returns "active.key"
    }
    internal val testKey1 = mockk<UserKey> {
        every { this@mockk.privateKey } returns testPrivateKeyPrimary
        every { this@mockk.recoverySecret } returns testSecretValid
        every { this@mockk.active } returns true
        every { this@mockk.keyId } returns KeyId("testKey1")
    }
    internal val testKey2 = mockk<UserKey> {
        every { this@mockk.privateKey } returns testPrivateKeyInactive
        every { this@mockk.recoverySecret } returns testSecretInvalid
        every { this@mockk.active } returns false
        every { this@mockk.keyId } returns KeyId("testKey2")
    }
    internal val testUser = mockk<User> {
        every { this@mockk.userId } returns UserId("userId")
        every { this@mockk.keys } returns listOf(testKey1, testKey2)
    }

    internal val testUserManager = mockk<UserManager> {
        coEvery { this@mockk.getUser(any(), any()) } returns testUser
    }

    internal val testDecodedSecret1 = "decodedSecret1".toByteArray()
    internal val testDecodedSecret2 = "decodedSecret2".toByteArray()
    internal val testFingerprint1 = "fingerprint1"
    internal val testFingerprint2 = "fingerprint2"
    internal val testPgpCrypto = mockk<PGPCrypto>(relaxed = true) {
        every { this@mockk.getBase64Decoded(testSecretValid) } returns testDecodedSecret1
        every { this@mockk.getBase64Decoded(testSecretInvalid) } returns testDecodedSecret2
        every { this@mockk.decryptDataWithPassword(any(), testDecodedSecret1) } returns "{\"keys\":[[0]]}".toByteArray()
        every { this@mockk.decryptDataWithPassword(any(), testDecodedSecret2) } throws CryptoException()
    }
    internal val testCryptoContext = mockk<CryptoContext>(relaxed = true) {
        every { this@mockk.pgpCrypto } returns testPgpCrypto
    }
    internal val encryptedPassphrase = EncryptedByteArray("passphrase".toByteArray())
    internal val testPassphraseRepository = mockk<PassphraseRepository> {
        coEvery { this@mockk.getPassphrase(any()) } returns encryptedPassphrase
    }

    @Before
    open fun before() {
        mockkStatic(PrivateKey::unlockOrNull)
        every { testPrivateKeyPrimary.unlockOrNull(any()) } returns unlockedKey
        every { testPrivateKeyInactive.unlockOrNull(any()) } returns unlockedKey

        every { testPrivateKeyPrimary.fingerprint(any()) } returns testFingerprint1
        every { testPrivateKeyInactive.fingerprint(any()) } returns testFingerprint2

        every { testPrivateKeyPrimary.canUnlock(any()) } returns true
        every { testPrivateKeyInactive.canUnlock(any()) } returns true

        mockkStatic(UnlockedPrivateKey::lock)
        mockkConstructor(UnlockedPrivateKey::class)
        every { anyConstructed<UnlockedPrivateKey>().lock(any(), any()) } returns testPrivateKeyInactive
    }
}
