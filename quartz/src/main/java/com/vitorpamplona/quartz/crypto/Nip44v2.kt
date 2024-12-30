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
package com.vitorpamplona.quartz.crypto

import android.util.Log
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.vitorpamplona.quartz.encoders.Hex
import com.vitorpamplona.quartz.encoders.toHexKey
import fr.acinq.secp256k1.Secp256k1
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import java.util.Base64
import kotlin.math.floor
import kotlin.math.log2

class Nip44v2(val secp256k1: Secp256k1, val random: SecureRandom) {
    private val sharedKeyCache = SharedKeyCache()

    private val libSodium = SodiumAndroid()
    private val lazySodium = LazySodiumAndroid(libSodium)
    private val hkdf = Hkdf()

    private val h02 = Hex.decode("02")
    private val saltPrefix = "nip44-v2".toByteArray(Charsets.UTF_8)
    private val hashLength = 32

    private val minPlaintextSize: Int = 0x0001 // 1b msg => padded to 32b
    private val maxPlaintextSize: Int = 0xffff // 65535 (64kb-1) => padded to 64kb

    fun clearCache() {
        sharedKeyCache.clearCache()
    }

    fun encrypt(
        msg: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): EncryptedInfo {
        return encrypt(msg, getConversationKey(privateKey, pubKey))
    }

    fun encrypt(
        plaintext: String,
        conversationKey: ByteArray,
    ): EncryptedInfo {
        val nonce = ByteArray(hashLength)
        random.nextBytes(nonce)
        return encryptWithNonce(plaintext, conversationKey, nonce)
    }

    fun encryptWithNonce(
        plaintext: String,
        conversationKey: ByteArray,
        nonce: ByteArray,
    ): EncryptedInfo {
        val messageKeys = getMessageKeys(conversationKey, nonce)
        val padded = pad(plaintext)

        val ciphertext = ByteArray(padded.size)

        lazySodium.cryptoStreamChaCha20IetfXor(
            ciphertext,
            padded,
            padded.size.toLong(),
            messageKeys.chachaNonce,
            messageKeys.chachaKey,
        )

        val mac = hmacAad(messageKeys.hmacKey, ciphertext, nonce)

        return EncryptedInfo(
            nonce = nonce,
            ciphertext = ciphertext,
            mac = mac,
        )
    }

    fun decrypt(
        payload: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String? {
        return decrypt(payload, getConversationKey(privateKey, pubKey))
    }

    fun decrypt(
        decoded: EncryptedInfo,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String? {
        return decrypt(decoded, getConversationKey(privateKey, pubKey))
    }

    fun decrypt(
        payload: String,
        conversationKey: ByteArray,
    ): String? {
        val decoded = EncryptedInfo.decodePayload(payload) ?: return null
        return decrypt(decoded, conversationKey)
    }

    fun decrypt(
        decoded: EncryptedInfo,
        conversationKey: ByteArray,
    ): String? {
        val messageKey = getMessageKeys(conversationKey, decoded.nonce)
        val calculatedMac = hmacAad(messageKey.hmacKey, decoded.ciphertext, decoded.nonce)

        check(calculatedMac.contentEquals(decoded.mac)) {
            "Invalid Mac: Calculated ${calculatedMac.toHexKey()}, decoded: ${decoded.mac.toHexKey()}"
        }

        val mLen = decoded.ciphertext.size.toLong()
        val padded = ByteArray(decoded.ciphertext.size)

        lazySodium.cryptoStreamChaCha20IetfXor(
            padded,
            decoded.ciphertext,
            mLen,
            messageKey.chachaNonce,
            messageKey.chachaKey,
        )

        return unpad(padded)
    }

    fun getConversationKey(
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): ByteArray {
        val preComputed = sharedKeyCache.get(privateKey, pubKey)
        if (preComputed != null) return preComputed

        val computed = computeConversationKey(privateKey, pubKey)
        sharedKeyCache.add(privateKey, pubKey, computed)
        return computed
    }

    fun calcPaddedLen(len: Int): Int {
        check(len > 0) { "expected positive integer" }
        if (len <= 32) return 32
        val nextPower = 1 shl (floor(log2(len - 1f)) + 1).toInt()
        val chunk = if (nextPower <= 256) 32 else nextPower / 8
        return chunk * (floor((len - 1f) / chunk).toInt() + 1)
    }

    fun pad(plaintext: String): ByteArray {
        val unpadded = plaintext.toByteArray(Charsets.UTF_8)
        val unpaddedLen = unpadded.size

        check(unpaddedLen > 0) { "Message is empty ($unpaddedLen): $plaintext" }

        check(unpaddedLen <= maxPlaintextSize) { "Message is too long ($unpaddedLen): $plaintext" }

        val prefix =
            ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN).putShort(unpaddedLen.toShort()).array()
        val suffix = ByteArray(calcPaddedLen(unpaddedLen) - unpaddedLen)
        return ByteBuffer.wrap(prefix + unpadded + suffix).array()
    }

    private fun bytesToInt(
        byte1: Byte,
        byte2: Byte,
        bigEndian: Boolean,
    ): Int {
        return if (bigEndian) {
            (byte1.toInt() and 0xFF shl 8 or (byte2.toInt() and 0xFF))
        } else {
            (byte2.toInt() and 0xFF shl 8 or (byte1.toInt() and 0xFF))
        }
    }

    fun unpad(padded: ByteArray): String {
        val unpaddedLen: Int = bytesToInt(padded[0], padded[1], true)
        val unpadded = padded.sliceArray(2 until 2 + unpaddedLen)

        check(
            unpaddedLen in minPlaintextSize..maxPlaintextSize &&
                unpadded.size == unpaddedLen &&
                padded.size == 2 + calcPaddedLen(unpaddedLen),
        ) {
            "invalid padding ${unpadded.size} != $unpaddedLen"
        }

        return unpadded.decodeToString()
    }

    fun hmacAad(
        key: ByteArray,
        message: ByteArray,
        aad: ByteArray,
    ): ByteArray {
        check(aad.size == hashLength) {
            "AAD associated data must be 32 bytes, but it was ${aad.size} bytes"
        }

        return hkdf.extract(aad + message, key)
    }

    fun getMessageKeys(
        conversationKey: ByteArray,
        nonce: ByteArray,
    ): MessageKey {
        val keys = hkdf.expand(conversationKey, nonce, 76)
        return MessageKey(
            chachaKey = keys.copyOfRange(0, 32),
            chachaNonce = keys.copyOfRange(32, 44),
            hmacKey = keys.copyOfRange(44, 76),
        )
    }

    class MessageKey(
        val chachaKey: ByteArray,
        val chachaNonce: ByteArray,
        val hmacKey: ByteArray,
    )

    /** @return 32B shared secret */
    fun computeConversationKey(
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): ByteArray {
        val sharedX = secp256k1.pubKeyTweakMul(h02 + pubKey, privateKey).copyOfRange(1, 33)
        return hkdf.extract(sharedX, saltPrefix)
    }

    class EncryptedInfo(
        val nonce: ByteArray,
        val ciphertext: ByteArray,
        val mac: ByteArray,
    ) {
        companion object {
            const val V: Int = 2

            fun decodePayload(payload: String): EncryptedInfo? {
                check(payload.length >= 132 || payload.length <= 87472) {
                    "Invalid payload length ${payload.length} for $payload"
                }
                check(payload[0] != '#') { "Unknown encryption version ${payload.get(0)}" }

                return try {
                    val byteArray = Base64.getDecoder().decode(payload)
                    check(byteArray[0].toInt() == V)
                    return EncryptedInfo(
                        nonce = byteArray.copyOfRange(1, 33),
                        ciphertext = byteArray.copyOfRange(33, byteArray.size - 32),
                        mac = byteArray.copyOfRange(byteArray.size - 32, byteArray.size),
                    )
                } catch (e: Exception) {
                    Log.w("NIP44v2", "Unable to Parse encrypted payload: $payload")
                    null
                }
            }
        }

        fun encodePayload(): String {
            return Base64.getEncoder()
                .encodeToString(
                    byteArrayOf(V.toByte()) + nonce + ciphertext + mac,
                )
        }
    }
}
