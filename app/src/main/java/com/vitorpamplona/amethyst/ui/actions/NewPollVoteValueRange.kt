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
package com.vitorpamplona.amethyst.ui.actions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.ui.theme.DoubleHorzSpacer
import com.vitorpamplona.amethyst.ui.theme.placeholderText

@Composable
fun NewPollVoteValueRange(pollViewModel: NewPostViewModel) {
    val colorInValid =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.error,
            unfocusedBorderColor = Color.Red,
        )
    val colorValid =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.placeholderText,
        )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedTextField(
            value = pollViewModel.valueMinimum?.toString() ?: "",
            onValueChange = { pollViewModel.updateMinZapAmountForPoll(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = if (pollViewModel.isValidvalueMinimum.value) colorValid else colorInValid,
            label = {
                Text(
                    text = stringResource(R.string.poll_zap_value_min),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.sats),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
        )

        Spacer(modifier = DoubleHorzSpacer)

        OutlinedTextField(
            value = pollViewModel.valueMaximum?.toString() ?: "",
            onValueChange = { pollViewModel.updateMaxZapAmountForPoll(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            colors = if (pollViewModel.isValidvalueMaximum.value) colorValid else colorInValid,
            label = {
                Text(
                    text = stringResource(R.string.poll_zap_value_max),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
            placeholder = {
                Text(
                    text = stringResource(R.string.sats),
                    color = MaterialTheme.colorScheme.placeholderText,
                )
            },
        )
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.poll_zap_value_min_max_explainer),
            color = MaterialTheme.colorScheme.placeholderText,
            modifier = Modifier.padding(vertical = 10.dp),
        )
    }
}

@Preview
@Composable
fun NewPollVoteValueRangePreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        NewPollVoteValueRange(NewPostViewModel())
    }
}
