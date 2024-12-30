/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.vitorpamplona.amethyst.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Account
import com.vitorpamplona.amethyst.ui.actions.NewPostViewModel
import com.vitorpamplona.amethyst.ui.theme.placeholderText

@Composable
fun NewPollRecipientsField(
    pollViewModel: NewPostViewModel,
    account: Account,
) {
    // if no recipients, add user's pubkey
    if (pollViewModel.zapRecipients.isEmpty()) {
        pollViewModel.zapRecipients.add(account.userProfile().pubkeyHex)
    }

    // TODO allow add multiple recipients and check input validity

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = pollViewModel.zapRecipients[0],
        onValueChange = { /* TODO */ },
        enabled = false,
        label = {
            Text(
                text = stringResource(R.string.poll_zap_recipients),
                color = MaterialTheme.colorScheme.placeholderText,
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.poll_zap_recipients),
                color = MaterialTheme.colorScheme.placeholderText,
            )
        },
    )
}
