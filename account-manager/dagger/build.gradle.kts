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

import studio.forface.easygradle.dsl.*
import studio.forface.easygradle.dsl.android.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

libVersion = Version(1, 0, 6)

android(minSdk = 23)

dependencies {

    implementation(

        project(Module.kotlinUtil),
        project(Module.network),
        project(Module.crypto),
        project(Module.domain),
        project(Module.data),

        // Features
        project(Module.account),
        project(Module.accountManagerData),
        project(Module.accountManagerDomain),
        project(Module.accountManagerPresentation),
        project(Module.authDomain),
        project(Module.user),
        project(Module.key),

        // Android
        `android-ktx`,
        `hilt-android`,
        `room-runtime`
    )

    kapt(
        `assistedInject-processor-dagger`,
        `hilt-android-compiler`,
        `hilt-androidx-compiler`
    )
    compileOnly(`assistedInject-annotations-dagger`)
}
