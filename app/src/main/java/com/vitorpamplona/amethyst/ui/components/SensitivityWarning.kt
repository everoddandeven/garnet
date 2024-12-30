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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vitorpamplona.amethyst.R
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.ButtonBorder
import com.vitorpamplona.amethyst.ui.theme.ButtonPadding
import com.vitorpamplona.quartz.events.EventInterface

@Composable
fun SensitivityWarning(
    note: Note,
    accountViewModel: AccountViewModel,
    content: @Composable () -> Unit,
) {
    note.event?.let { SensitivityWarning(it, accountViewModel, content) }
}

@Composable
fun SensitivityWarning(
    event: EventInterface,
    accountViewModel: AccountViewModel,
    content: @Composable () -> Unit,
) {
    val hasSensitiveContent = remember(event) { event.isSensitive() }

    SensitivityWarning(hasSensitiveContent, accountViewModel, content)
}

@Composable
fun SensitivityWarning(
    hasSensitiveContent: Boolean,
    accountViewModel: AccountViewModel,
    content: @Composable () -> Unit,
) {
    if (hasSensitiveContent) {
        SensitivityWarning(accountViewModel, content)
    } else {
        content()
    }
}

@Composable
fun SensitivityWarning(
    accountViewModel: AccountViewModel,
    content: @Composable () -> Unit,
) {
    val accountState by accountViewModel.accountLiveData.observeAsState()

    var showContentWarningNote by
        remember(accountState) { mutableStateOf(accountState?.account?.showSensitiveContent != true) }

    Crossfade(targetState = showContentWarningNote) {
        if (it) {
            ContentWarningNote { showContentWarningNote = false }
        } else {
            content()
        }
    }
}

@Composable
fun ContentWarningNote(onDismiss: () -> Unit) {
    Column {
        Row(modifier = Modifier.padding(horizontal = 12.dp)) {
            Column(modifier = Modifier.padding(start = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Box(
                        Modifier.height(80.dp).width(90.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = stringResource(R.string.content_warning),
                            modifier = Modifier.size(70.dp).align(Alignment.BottomStart),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                        Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = stringResource(R.string.content_warning),
                            modifier = Modifier.size(30.dp).align(Alignment.TopEnd),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = stringResource(R.string.content_warning),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }

                Row {
                    Text(
                        text = stringResource(R.string.content_warning_explanation),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 10.dp),
                        textAlign = TextAlign.Center,
                    )
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(
                        modifier = Modifier.padding(top = 10.dp),
                        onClick = onDismiss,
                        shape = ButtonBorder,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        contentPadding = ButtonPadding,
                    ) {
                        Text(
                            text = stringResource(R.string.show_anyway),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}
