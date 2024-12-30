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

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vitorpamplona.quartz.crypto.KeyPair
import com.vitorpamplona.quartz.encoders.Hex
import com.vitorpamplona.quartz.encoders.Nip19Bech32
import com.vitorpamplona.quartz.encoders.decodePrivateKeyAsHexOrNull
import com.vitorpamplona.quartz.encoders.hexToByteArray
import com.vitorpamplona.quartz.events.Event
import com.vitorpamplona.quartz.events.FhirResourceEvent
import com.vitorpamplona.quartz.events.TextNoteEvent
import com.vitorpamplona.quartz.signers.NostrSignerInternal
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class NIP19EmbedTests {
    @Test
    fun testEmbedKind1Event() {
        val signer =
            NostrSignerInternal(
                KeyPair(Hex.decode("e8e7197ccc53c9ed4cf9b1c8dce085475fa1ffdd71f2c14e44fe23d0bdf77598")),
            )

        var textNote: Event? = null

        val countDownLatch = CountDownLatch(1)

        TextNoteEvent.create("I like this. It could solve the ninvite problem in #1062, and it seems like it could be applied very broadly to limit the spread of events that shouldn't stand on their own or need to be private. The one question I have is how long are these embeds? If it's 50 lines of text, that breaks the human readable (or at least parseable) requirement of kind 1s. Also, encoding json in a tlv is silly, we should at least use the tlv to reduce the payload size.", signer = signer) {
            textNote = it
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        assertNotNull(textNote)

        val bech32 = Nip19Bech32.createNEmbed(textNote!!)

        println(bech32)

        val decodedNote = (Nip19Bech32.uriToRoute(bech32)?.entity as Nip19Bech32.NEmbed).event

        assertTrue(decodedNote.hasValidSignature())

        assertEquals(textNote!!.toJson(), decodedNote.toJson())
    }

    @Test
    fun testVisionPrescriptionEmbedEvent() {
        val signer =
            NostrSignerInternal(
                KeyPair(Hex.decode("e8e7197ccc53c9ed4cf9b1c8dce085475fa1ffdd71f2c14e44fe23d0bdf77598")),
            )

        var eyeglassesPrescriptionEvent: Event? = null

        val countDownLatch = CountDownLatch(1)

        FhirResourceEvent.create(fhirPayload = visionPrescriptionFhir, signer = signer) {
            eyeglassesPrescriptionEvent = it
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        assertNotNull(eyeglassesPrescriptionEvent)

        val bech32 = Nip19Bech32.createNEmbed(eyeglassesPrescriptionEvent!!)

        println(eyeglassesPrescriptionEvent!!.toJson())
        println(bech32)

        val decodedNote = (Nip19Bech32.uriToRoute(bech32)?.entity as Nip19Bech32.NEmbed).event

        assertTrue(decodedNote.hasValidSignature())

        assertEquals(eyeglassesPrescriptionEvent!!.toJson(), decodedNote.toJson())
    }

    @Test
    fun testVisionPrescriptionBundleEmbedEvent() {
        val signer =
            NostrSignerInternal(
                KeyPair(Hex.decode("e8e7197ccc53c9ed4cf9b1c8dce085475fa1ffdd71f2c14e44fe23d0bdf77598")),
            )

        var eyeglassesPrescriptionEvent: Event? = null

        val countDownLatch = CountDownLatch(1)

        FhirResourceEvent.create(fhirPayload = visionPrescriptionBundle, signer = signer) {
            eyeglassesPrescriptionEvent = it
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        assertNotNull(eyeglassesPrescriptionEvent)

        val bech32 = Nip19Bech32.createNEmbed(eyeglassesPrescriptionEvent!!)

        println(eyeglassesPrescriptionEvent!!.toJson())
        println(bech32)

        val decodedNote = (Nip19Bech32.uriToRoute(bech32)?.entity as Nip19Bech32.NEmbed).event

        assertTrue(decodedNote.hasValidSignature())

        assertEquals(eyeglassesPrescriptionEvent!!.toJson(), decodedNote.toJson())
    }

    @Test
    fun testVisionPrescriptionBundle2EmbedEvent() {
        val signer =
            NostrSignerInternal(
                KeyPair(decodePrivateKeyAsHexOrNull("nsec1arn3jlxv20y76n8ek8ydecy9ga06rl7aw8evznjylc3ap00hwkvqx4vvy6")!!.hexToByteArray()),
            )

        var eyeglassesPrescriptionEvent: Event? = null

        val countDownLatch = CountDownLatch(1)

        FhirResourceEvent.create(fhirPayload = visionPrescriptionBundle2, signer = signer) {
            eyeglassesPrescriptionEvent = it
            countDownLatch.countDown()
        }

        Assert.assertTrue(countDownLatch.await(1, TimeUnit.SECONDS))

        assertNotNull(eyeglassesPrescriptionEvent)

        val bech32 = Nip19Bech32.createNEmbed(eyeglassesPrescriptionEvent!!)

        println(eyeglassesPrescriptionEvent!!.toJson())
        println(bech32)

        val decodedNote = (Nip19Bech32.uriToRoute(bech32)?.entity as Nip19Bech32.NEmbed).event

        assertTrue(decodedNote.hasValidSignature())

        assertEquals(eyeglassesPrescriptionEvent!!.toJson(), decodedNote.toJson())
    }

    val visionPrescriptionFhir = "{\"resourceType\":\"VisionPrescription\",\"status\":\"active\",\"created\":\"2014-06-15\",\"patient\":{\"reference\":\"Patient/Donald Duck\"},\"dateWritten\":\"2014-06-15\",\"prescriber\":{\"reference\":\"Practitioner/Adam Careful\"},\"lensSpecification\":[{\"eye\":\"right\",\"sphere\":-2,\"prism\":[{\"amount\":0.5,\"base\":\"down\"}],\"add\":2},{\"eye\":\"left\",\"sphere\":-1,\"cylinder\":-0.5,\"axis\":180,\"prism\":[{\"amount\":0.5,\"base\":\"up\"}],\"add\":2}]}"
    val visionPrescriptionBundle = "{\"resourceType\":\"Bundle\",\"id\":\"bundle-vision-test\",\"type\":\"document\",\"entry\":[{\"resourceType\":\"Practitioner\",\"id\":\"2\",\"active\":true,\"name\":[{\"use\":\"official\",\"family\":\"Careful\",\"given\":[\"Adam\"]}],\"gender\":\"male\"},{\"resourceType\":\"Patient\",\"id\":\"1\",\"active\":true,\"name\":[{\"use\":\"official\",\"family\":\"Duck\",\"given\":[\"Donald\"]}],\"gender\":\"male\"},{\"resourceType\":\"VisionPrescription\",\"status\":\"active\",\"created\":\"2014-06-15\",\"patient\":{\"reference\":\"#1\"},\"dateWritten\":\"2014-06-15\",\"prescriber\":{\"reference\":\"#2\"},\"lensSpecification\":[{\"eye\":\"right\",\"sphere\":-2,\"prism\":[{\"amount\":0.5,\"base\":\"down\"}],\"add\":2},{\"eye\":\"left\",\"sphere\":-1,\"cylinder\":-0.5,\"axis\":180,\"prism\":[{\"amount\":0.5,\"base\":\"up\"}],\"add\":2}]}]}"

    val visionPrescriptionBundle2 = "{\"resourceType\":\"Bundle\",\"id\":\"bundle-vision-test\",\"type\":\"document\",\"entry\":[{\"resourceType\":\"Practitioner\",\"id\":\"2\",\"active\":true,\"name\":[{\"use\":\"official\",\"family\":\"Smith\",\"given\":[\"Dr. Joe\"]}],\"gender\":\"male\"},{\"resourceType\":\"Patient\",\"id\":\"1\",\"active\":true,\"name\":[{\"use\":\"official\",\"family\":\"Doe\",\"given\":[\"Jane\"]}],\"gender\":\"male\"},{\"resourceType\":\"VisionPrescription\",\"status\":\"active\",\"created\":\"2014-06-15\",\"patient\":{\"reference\":\"#1\"},\"dateWritten\":\"2014-06-15\",\"lensSpecification\":[{\"eye\":\"right\",\"sphere\":-2,\"prism\":[{\"amount\":0.5,\"base\":\"down\"}],\"add\":2},{\"eye\":\"left\",\"sphere\":-1,\"cylinder\":-0.5,\"axis\":180,\"prism\":[{\"amount\":0.5,\"base\":\"up\"}],\"add\":2}]}]}"
}
