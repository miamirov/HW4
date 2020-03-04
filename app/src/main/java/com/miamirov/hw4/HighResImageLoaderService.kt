package com.miamirov.hw4

import android.app.IntentService
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.net.URL

const val INTENT_SERVICE_ACTION_TOKEN_RESPONSE = "RESPONSE"
const val INTENT_SERVICE_ACTION_TOKEN_BYTE_ARRAY = "imageArray"

class HighResImageLoaderService : IntentService("ImageLoader") {
    init {
        setIntentRedelivery(true)
    }

    override fun onHandleIntent(intent: Intent?) {
        val url = intent?.getStringExtra(HIGH_RES_URL_TOKEN) ?: return
        val image = BitmapFactory.decodeStream(URL(url).openConnection().getInputStream())

        sendBroadcast(Intent().apply {
            action = INTENT_SERVICE_ACTION_TOKEN_RESPONSE
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(
                INTENT_SERVICE_ACTION_TOKEN_BYTE_ARRAY,
                ByteArrayOutputStream().apply {
                    image.compress(Bitmap.CompressFormat.JPEG, 100, this)
                }.toByteArray()
            )
        })
    }
}