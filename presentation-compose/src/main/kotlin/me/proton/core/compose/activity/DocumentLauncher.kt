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

package me.proton.core.compose.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Register a request to prompt the user to open a document.
 */
@Composable
fun rememberOpenDocumentLauncher(
    mimeTypes: Array<String> = arrayOf("*/*"),
    onFilePicked: (Uri?) -> Unit,
) = rememberLauncherWithInput(
    input = mimeTypes,
    contracts = ActivityResultContracts.OpenDocument(),
    onResult = onFilePicked
)

/**
 * Register a request to prompt the user to open (possibly multiple) documents.
 */
@Composable
fun rememberOpenMultipleDocumentsLauncher(
    mimeTypes: Array<String> = arrayOf("*/*"),
    modifyIntent: ((Intent) -> Unit)? = null,
    onFilesPicked: (List<Uri>) -> Unit,
) = rememberLauncherWithInput(
    input = mimeTypes,
    contracts = object : ActivityResultContracts.OpenMultipleDocuments() {
        override fun createIntent(context: Context, input: Array<String>) = super.createIntent(context, input).apply {
            modifyIntent?.invoke(this)
        }
    },
    onResult = onFilesPicked
)

/**
 * Register a request to prompt the user to select a directory.
 */
@Composable
fun rememberOpenDocumentTreeLauncher(
    initialLocation: Uri = Uri.EMPTY,
    onDirectoryPicked: (Uri?) -> Unit,
) = rememberLauncherWithInput(
    input = initialLocation,
    contracts = ActivityResultContracts.OpenDocumentTree(),
    onResult = onDirectoryPicked
)

/**
 * Register a request to prompt the user to create a document.
 */
@Composable
fun rememberCreateDocumentLauncher(
    mimeType: String,
    onDocumentCreated: (Uri?) -> Unit,
    modifyIntent: ((Intent) -> Unit)? = null,
) = rememberLauncherWithInput(
    input = mimeType,
    contracts = object : ActivityResultContracts.CreateDocument(mimeType) {
        override fun createIntent(context: Context, input: String) = super.createIntent(context, input).apply {
            modifyIntent?.invoke(this)
        }
    },
    onResult = onDocumentCreated
)
