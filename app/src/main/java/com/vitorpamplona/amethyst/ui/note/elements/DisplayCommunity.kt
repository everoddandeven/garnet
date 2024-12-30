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
package com.vitorpamplona.amethyst.ui.note.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.AnnotatedString
import com.vitorpamplona.amethyst.model.Note
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.HalfStartPadding
import com.vitorpamplona.quartz.encoders.ATag
import com.vitorpamplona.quartz.events.CommunityDefinitionEvent

@Composable
fun DisplayFollowingCommunityInPost(
    baseNote: Note,
    accountViewModel: AccountViewModel,
    nav: (String) -> Unit,
) {
    Column(HalfStartPadding) {
        Row(verticalAlignment = Alignment.CenterVertically) { DisplayCommunity(baseNote, nav) }
    }
}

@Composable
private fun DisplayCommunity(
    note: Note,
    nav: (String) -> Unit,
) {
    val communityTag =
        remember(note) { note.event?.getTagOfAddressableKind(CommunityDefinitionEvent.KIND) } ?: return

    val displayTag = remember(note) { AnnotatedString(getCommunityShortName(communityTag)) }
    val route = remember(note) { "Community/${communityTag.toTag()}" }

    ClickableText(
        text = displayTag,
        onClick = { nav(route) },
        style =
            LocalTextStyle.current.copy(
                color =
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.52f,
                    ),
            ),
        maxLines = 1,
    )
}

private fun getCommunityShortName(communityTag: ATag): String {
    val name =
        if (communityTag.dTag.length > 10) {
            communityTag.dTag.take(10) + "..."
        } else {
            communityTag.dTag.take(10)
        }

    return "/n/$name"
}
