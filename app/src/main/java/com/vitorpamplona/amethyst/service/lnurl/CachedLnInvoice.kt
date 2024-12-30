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
package com.vitorpamplona.amethyst.service.lnurl

import android.util.LruCache
import androidx.compose.runtime.Stable
import com.vitorpamplona.quartz.encoders.LnInvoiceUtil
import kotlinx.coroutines.CancellationException
import java.text.NumberFormat

@Stable
data class InvoiceAmount(val invoice: String, val amount: String?)

object CachedLnInvoiceParser {
    val lnInvoicesCache = LruCache<String, InvoiceAmount>(20)

    fun cached(lnurl: String): InvoiceAmount? {
        return lnInvoicesCache[lnurl]
    }

    fun parse(lnbcWord: String): InvoiceAmount? {
        val myInvoice = LnInvoiceUtil.findInvoice(lnbcWord)
        if (myInvoice != null) {
            val myInvoiceAmount =
                try {
                    NumberFormat.getInstance().format(LnInvoiceUtil.getAmountInSats(myInvoice))
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    e.printStackTrace()
                    null
                }

            val lnInvoice = InvoiceAmount(myInvoice, myInvoiceAmount)

            lnInvoicesCache.put(lnbcWord, lnInvoice)

            return lnInvoice
        }

        return null
    }
}
