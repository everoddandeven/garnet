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
package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.ui.navigation.routeFor
import com.vitorpamplona.amethyst.ui.note.elements.NoteDropDownMenu
import com.vitorpamplona.amethyst.ui.screen.MessageSetCard
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageSetCompose(
    messageSetCard: MessageSetCard,
    routeForLastRead: String,
    showHidden: Boolean = false,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    val baseNote = remember { messageSetCard.note }

    val popupExpanded = remember { mutableStateOf(false) }
    val enablePopup = remember { { popupExpanded.value = true } }

    val scope = rememberCoroutineScope()

    val backgroundColor =
        calculateBackgroundColor(
            createdAt = messageSetCard.createdAt(),
            routeForLastRead = routeForLastRead,
            accountViewModel = accountViewModel,
        )

    val columnModifier =
        remember(backgroundColor.value) {
            Modifier.background(backgroundColor.value)
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 10.dp,
                )
                .combinedClickable(
                    onClick = {
                        scope.launch {
                            routeFor(
                                baseNote,
                                accountViewModel.userProfile(),
                            )
                                ?.let { nav(it) }
                        }
                    },
                    onLongClick = enablePopup,
                )
                .fillMaxWidth()
        }

    Column(columnModifier) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                modifier = remember { Modifier.width(55.dp).padding(top = 5.dp, end = 5.dp) },
            ) {
                MessageIcon(
                    remember { Modifier.size(16.dp).align(Alignment.TopEnd) },
                )
            }

            Column(modifier = remember { Modifier.padding(start = 10.dp) }) {
                NoteCompose(
                    baseNote = baseNote,
                    routeForLastRead = null,
                    isBoostedNote = true,
                    isHiddenFeed = showHidden,
                    quotesLeft = 1,
                    parentBackgroundColor = backgroundColor,
                    accountViewModel = accountViewModel,
                    nav = nav,
                )

                NoteDropDownMenu(baseNote, popupExpanded, null, accountViewModel, nav)
            }
        }
    }
}
