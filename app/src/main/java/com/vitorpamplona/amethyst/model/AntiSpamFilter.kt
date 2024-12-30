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
package com.vitorpamplona.amethyst.model

import android.util.Log
import android.util.LruCache
import androidx.compose.runtime.Stable
import androidx.lifecycle.LiveData
import com.vitorpamplona.amethyst.service.checkNotInMainThread
import com.vitorpamplona.amethyst.service.relays.Relay
import com.vitorpamplona.amethyst.ui.components.BundledUpdate
import com.vitorpamplona.quartz.encoders.HexKey
import com.vitorpamplona.quartz.encoders.Nip19Bech32
import com.vitorpamplona.quartz.events.Event
import kotlinx.coroutines.Dispatchers

data class Spammer(val pubkeyHex: HexKey, var duplicatedMessages: Set<HexKey>)

class AntiSpamFilter {
    val recentMessages = LruCache<Int, String>(1000)
    val spamMessages = LruCache<Int, Spammer>(1000)

    var active: Boolean = true

    fun isSpam(
        event: Event,
        relay: Relay?,
    ): Boolean {
        checkNotInMainThread()

        if (!active) return false

        val idHex = event.id

        // if short message, ok
        // The idea here is to avoid considering repeated "GM" messages spam.
        if (event.content.length < 50) return false

        // if the message is actually short but because it cites a user/event, the nostr: string is
        // really long, make it ok.
        // The idea here is to avoid considering repeated "@Bot, command" messages spam, while still
        // blocking repeated "lnbc..." invoices or fishing urls
        if (event.content.length < 180 && Nip19Bech32.nip19regex.matcher(event.content).find()) return false

        // double list strategy:
        // if duplicated, it goes into spam. 1000 spam messages are saved into the spam list.

        // Considers tags so that same replies to different people don't count.
        val hash = (event.content + event.tags.flatten().joinToString(",")).hashCode()

        if (
            (recentMessages[hash] != null && recentMessages[hash] != idHex) || spamMessages[hash] != null
        ) {
            Log.w(
                "Potential SPAM Message for sharing",
                "${Nip19Bech32.createNEvent(event.id, event.pubKey, event.kind, null)}",
            )
            Log.w(
                "Potential SPAM Message",
                "${event.id} ${recentMessages[hash]} ${spamMessages[hash] != null} ${relay?.url} ${event.content.replace("\n", " | ")}",
            )

            // Log down offenders
            logOffender(hash, event)

            liveSpam.invalidateData()

            return true
        }

        recentMessages.put(hash, idHex)

        return false
    }

    @Synchronized
    private fun logOffender(
        hashCode: Int,
        event: Event,
    ) {
        if (spamMessages.get(hashCode) == null) {
            spamMessages.put(hashCode, Spammer(event.pubKey, setOf(recentMessages[hashCode], event.id)))
        } else {
            val spammer = spamMessages.get(hashCode)
            spammer.duplicatedMessages = spammer.duplicatedMessages + event.id
        }
    }

    val liveSpam: AntiSpamLiveData = AntiSpamLiveData(this)
}

@Stable
class AntiSpamLiveData(val cache: AntiSpamFilter) : LiveData<AntiSpamState>(AntiSpamState(cache)) {
    // Refreshes observers in batches.
    private val bundler = BundledUpdate(300, Dispatchers.IO)

    fun invalidateData() {
        checkNotInMainThread()

        bundler.invalidate {
            checkNotInMainThread()

            if (hasActiveObservers()) {
                postValue(AntiSpamState(cache))
            }
        }
    }
}

class AntiSpamState(val cache: AntiSpamFilter)
