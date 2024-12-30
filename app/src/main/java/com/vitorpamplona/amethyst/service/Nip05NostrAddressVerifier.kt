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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.vitorpamplona.amethyst.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response

class Nip05NostrAddressVerifier() {
    fun assembleUrl(nip05address: String): String? {
        val parts = nip05address.trim().split("@")

        if (parts.size == 2) {
            return "https://${parts[1]}/.well-known/nostr.json?name=${parts[0]}"
        }
        if (parts.size == 1) {
            return "https://${parts[0]}/.well-known/nostr.json?name=_"
        }

        return null
    }

    suspend fun fetchNip05Json(
        nip05: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) = withContext(Dispatchers.IO) {
        checkNotInMainThread()

        val url = assembleUrl(nip05)

        if (url == null) {
            onError("Could not assemble url from Nip05: \"${nip05}\". Check the user's setup")
            return@withContext
        }

        try {
            val request =
                Request.Builder()
                    .header("User-Agent", "Amethyst/${BuildConfig.VERSION_NAME}")
                    .url(url)
                    .build()

            HttpClientManager.getHttpClient()
                .newCall(request)
                .enqueue(
                    object : Callback {
                        override fun onResponse(
                            call: Call,
                            response: Response,
                        ) {
                            checkNotInMainThread()

                            response.use {
                                if (it.isSuccessful) {
                                    onSuccess(it.body.string())
                                } else {
                                    onError(
                                        "Could not resolve $nip05. Error: ${it.code}. Check if the server is up and if the address $nip05 is correct",
                                    )
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call,
                            e: java.io.IOException,
                        ) {
                            onError(
                                "Could not resolve $url. Check if the server is up and if the address $nip05 is correct",
                            )
                            e.printStackTrace()
                        }
                    },
                )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            onError("Could not resolve '$url': ${e.message}")
        }
    }

    suspend fun verifyNip05(
        nip05: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        // check fails on tests
        checkNotInMainThread()

        val mapper = jacksonObjectMapper()

        fetchNip05Json(
            nip05,
            onSuccess = {
                checkNotInMainThread()

                // NIP05 usernames are case insensitive, but JSON properties are not
                // converts the json to lowercase and then tries to access the username via a
                // lowercase version of the username.
                val nip05url =
                    try {
                        mapper.readTree(it.lowercase())
                    } catch (e: Throwable) {
                        if (e is CancellationException) throw e
                        onError("Error Parsing JSON from Lightning Address. Check the user's lightning setup")
                        null
                    }

                val parts = nip05.split("@")
                val user =
                    if (parts.size == 2) {
                        parts[0].lowercase()
                    } else {
                        "_"
                    }

                val hexKey = nip05url?.get("names")?.get(user)?.asText()

                if (hexKey == null) {
                    onError("Username not found in the NIP05 JSON")
                } else {
                    onSuccess(hexKey)
                }
            },
            onError = onError,
        )
    }
}
