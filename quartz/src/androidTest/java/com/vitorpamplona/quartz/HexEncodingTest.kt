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
package com.vitorpamplona.quartz

import com.vitorpamplona.quartz.crypto.CryptoUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class HexEncodingTest {
    val testHex = "48a72b485d38338627ec9d427583551f9af4f016c739b8ec0d6313540a8b12cf"

    @Test
    fun testHexEncodeDecodeOurs() {
        assertEquals(
            testHex,
            com.vitorpamplona.quartz.encoders.Hex.encode(
                com.vitorpamplona.quartz.encoders.Hex.decode(testHex),
            ),
        )
    }

    @Test
    fun testHexEncodeDecodeSecp256k1() {
        assertEquals(
            testHex,
            fr.acinq.secp256k1.Hex.encode(
                fr.acinq.secp256k1.Hex.decode(testHex),
            ),
        )
    }

    @Test
    fun testRandoms() {
        for (i in 0..1000) {
            val bytes = CryptoUtils.privkeyCreate()
            val hex = fr.acinq.secp256k1.Hex.encode(bytes)
            assertEquals(
                fr.acinq.secp256k1.Hex.encode(bytes),
                com.vitorpamplona.quartz.encoders.Hex.encode(bytes),
            )
            assertEquals(
                bytes.toList(),
                com.vitorpamplona.quartz.encoders.Hex.decode(hex).toList(),
            )
        }
    }
}
