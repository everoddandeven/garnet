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
package com.vitorpamplona.amethyst.service.notifications

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.LruCache
import androidx.core.content.ContextCompat
import com.vitorpamplona.amethyst.Amethyst
import com.vitorpamplona.amethyst.LocalPreferences
import com.vitorpamplona.amethyst.service.notifications.NotificationUtils.getOrCreateDMChannel
import com.vitorpamplona.amethyst.service.notifications.NotificationUtils.getOrCreateTipChannel
import com.vitorpamplona.amethyst.service.notifications.NotificationUtils.getOrCreateZapChannel
import com.vitorpamplona.quartz.events.Event
import com.vitorpamplona.quartz.events.GiftWrapEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.unifiedpush.android.connector.MessagingReceiver

class PushMessageReceiver : MessagingReceiver() {
    companion object {
        private val TAG = "Amethyst-OSSPushReceiver"
    }

    private val appContext = Amethyst.instance.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val eventCache = LruCache<String, String>(100)
    private val pushHandler = PushDistributorHandler

    override fun onMessage(
        context: Context,
        message: ByteArray,
        instance: String,
    ) {
        val messageStr = String(message)
        Log.d(TAG, "New message ${message.decodeToString()} for Instance: $instance")
        scope.launch {
            try {
                parseMessage(messageStr)?.let { receiveIfNew(it) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.d(TAG, "Message could not be parsed: ${e.message}")
            }
        }
    }

    private suspend fun parseMessage(message: String): GiftWrapEvent? {
        (Event.fromJson(message) as? GiftWrapEvent)?.let {
            return it
        }
        return null
    }

    private suspend fun receiveIfNew(event: GiftWrapEvent) {
        if (eventCache.get(event.id) == null) {
            eventCache.put(event.id, event.id)
            EventNotificationConsumer(appContext).consume(event)
        }
    }

    override fun onNewEndpoint(
        context: Context,
        endpoint: String,
        instance: String,
    ) {
        Log.d(TAG, "New endpoint provided:- $endpoint for Instance: $instance")
        val sanitizedEndpoint = endpoint.dropLast(5)
        pushHandler.setEndpoint(sanitizedEndpoint)
        scope.launch(Dispatchers.IO) {
            RegisterAccounts(LocalPreferences.allSavedAccounts()).go(sanitizedEndpoint)
            notificationManager().getOrCreateZapChannel(appContext)
            notificationManager().getOrCreateTipChannel(appContext)
            notificationManager().getOrCreateDMChannel(appContext)
        }
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val intentData = intent.dataString
        val intentAction = intent.action.toString()
        Log.d(TAG, "Intent Data:- $intentData Intent Action: $intentAction")
        super.onReceive(context, intent)
    }

    override fun onRegistrationFailed(
        context: Context,
        instance: String,
    ) {
        Log.d(TAG, "Registration failed for Instance: $instance")
        scope.cancel()
        pushHandler.forceRemoveDistributor(context)
    }

    override fun onUnregistered(
        context: Context,
        instance: String,
    ) {
        val removedEndpoint = pushHandler.endpoint
        Log.d(TAG, "Endpoint: $removedEndpoint removed for Instance: $instance")
        Log.d(TAG, "App is unregistered. ")
        pushHandler.forceRemoveDistributor(context)
        pushHandler.removeEndpoint()
    }

    fun notificationManager(): NotificationManager {
        return ContextCompat.getSystemService(appContext, NotificationManager::class.java)
            as NotificationManager
    }
}
