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
package com.vitorpamplona.amethyst.service

import com.vitorpamplona.amethyst.service.relays.Client
import com.vitorpamplona.amethyst.service.relays.FeedType
import com.vitorpamplona.amethyst.service.relays.JsonFilter
import com.vitorpamplona.amethyst.service.relays.Relay
import com.vitorpamplona.amethyst.service.relays.TypedFilter
import com.vitorpamplona.quartz.events.LnZapPaymentResponseEvent
import com.vitorpamplona.quartz.events.RelayAuthEvent
import com.vitorpamplona.quartz.signers.NostrSigner

class NostrLnZapPaymentResponseDataSource(
    private val fromServiceHex: String,
    private val toUserHex: String,
    private val replyingToHex: String,
    private val authSigner: NostrSigner,
) : NostrDataSource("LnZapPaymentResponseFeed") {
    val feedTypes = setOf(FeedType.WALLET_CONNECT)

    private fun createWalletConnectServiceWatcher(): TypedFilter {
        // downloads all the reactions to a given event.
        return TypedFilter(
            types = feedTypes,
            filter =
                JsonFilter(
                    kinds = listOf(LnZapPaymentResponseEvent.KIND),
                    authors = listOf(fromServiceHex),
                    tags =
                        mapOf(
                            "e" to listOf(replyingToHex),
                            "p" to listOf(toUserHex),
                        ),
                    limit = 1,
                ),
        )
    }

    val channel = requestNewChannel()

    override fun updateChannelFilters() {
        val wc = createWalletConnectServiceWatcher()

        channel.typedFilters = listOfNotNull(wc).ifEmpty { null }
    }

    override fun auth(
        relay: Relay,
        challenge: String,
    ) {
        super.auth(relay, challenge)

        RelayAuthEvent.create(relay.url, challenge, authSigner) {
            Client.send(
                it,
                relay.url,
            )
        }
    }
}
