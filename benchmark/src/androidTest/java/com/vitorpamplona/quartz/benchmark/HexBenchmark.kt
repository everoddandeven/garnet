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
package com.vitorpamplona.quartz.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitorpamplona.quartz.encoders.HexValidator
import junit.framework.TestCase.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Benchmark, which will execute on an Android device.
 *
 * The body of [BenchmarkRule.measureRepeated] is measured in a loop, and Studio will output the
 * result. Modify your code to see how it affects performance.
 */
@RunWith(AndroidJUnit4::class)
class HexBenchmark {
    @get:Rule val benchmarkRule = BenchmarkRule()

    val testHex = "48a72b485d38338627ec9d427583551f9af4f016c739b8ec0d6313540a8b12cf"

    @Test
    fun hexDecodeOurs() {
        benchmarkRule.measureRepeated { com.vitorpamplona.quartz.encoders.Hex.decode(testHex) }
    }

    @Test
    fun hexEncodeOurs() {
        val bytes = com.vitorpamplona.quartz.encoders.Hex.decode(testHex)

        benchmarkRule.measureRepeated {
            assertEquals(testHex, com.vitorpamplona.quartz.encoders.Hex.encode(bytes))
        }
    }

    @Test
    fun hexDecodeBaseSecp() {
        benchmarkRule.measureRepeated { fr.acinq.secp256k1.Hex.decode(testHex) }
    }

    @Test
    fun hexEncodeBaseSecp() {
        val bytes = fr.acinq.secp256k1.Hex.decode(testHex)

        benchmarkRule.measureRepeated { assertEquals(testHex, fr.acinq.secp256k1.Hex.encode(bytes)) }
    }

    @Test
    fun isHex() {
        benchmarkRule.measureRepeated { HexValidator.isHex(testHex) }
    }
}
