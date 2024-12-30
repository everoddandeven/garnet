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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextDirection
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.note.ErrorMessageDialog
import com.vitorpamplona.amethyst.ui.note.payViaIntent
import com.vitorpamplona.quartz.encoders.LnWithdrawalUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MayBeWithdrawal(lnurlWord: String) {
    var lnWithdrawal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(key1 = lnurlWord) {
        launch(Dispatchers.IO) { lnWithdrawal = LnWithdrawalUtil.findWithdrawal(lnurlWord) }
    }

    Crossfade(targetState = lnWithdrawal) {
        if (it != null) {
            ClickableWithdrawal(withdrawalString = it)
        } else {
            Text(
                text = lnurlWord,
                style = LocalTextStyle.current.copy(textDirection = TextDirection.Content),
            )
        }
    }
}

@Composable
fun ClickableWithdrawal(withdrawalString: String) {
    val context = LocalContext.current

    val withdraw = remember(withdrawalString) { AnnotatedString("$withdrawalString ") }

    var showErrorMessageDialog by remember { mutableStateOf<String?>(null) }

    if (showErrorMessageDialog != null) {
        ErrorMessageDialog(
            title = context.getString(R.string.error_dialog_pay_withdraw_error),
            textContent = showErrorMessageDialog ?: "",
            onDismiss = { showErrorMessageDialog = null },
        )
    }

    ClickableText(
        text = withdraw,
        onClick = { payViaIntent(withdrawalString, context) { showErrorMessageDialog = it } },
        style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.primary),
    )
}
