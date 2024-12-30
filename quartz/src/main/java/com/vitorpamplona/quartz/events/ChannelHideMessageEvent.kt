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
package com.vitorpamplona.quartz.events

import androidx.compose.runtime.Immutable
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.signers.NostrSigner
import com.vitorpamplona.quartz.utils.TimeUtils

@Immutable
class ChannelHideMessageEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: Array<Array<String>>,
    content: String,
    sig: HexKey,
) : Event(id, pubKey, createdAt, KIND, tags, content, sig), IsInPublicChatChannel {
    override fun channel() =
        tags.firstOrNull { it.size > 3 && it[0] == "e" && it[3] == "root" }?.get(1)
            ?: tags.firstOrNull { it.size > 1 && it[0] == "e" }?.get(1)

    fun eventsToHide() = tags.filter { it.firstOrNull() == "e" }.mapNotNull { it.getOrNull(1) }

    companion object {
        const val KIND = 43
        const val ALT = "Hide message instruction for public chats"

        fun create(
            reason: String,
            messagesToHide: List<String>?,
            signer: NostrSigner,
            createdAt: Long = TimeUtils.now(),
            onReady: (ChannelHideMessageEvent) -> Unit,
        ) {
            val tags =
                (
                    messagesToHide?.map { arrayOf("e", it) }?.toTypedArray()
                        ?: emptyArray()
                ) + arrayOf(arrayOf("alt", ALT))

            signer.sign(createdAt, KIND, tags, reason, onReady)
        }
    }
}
