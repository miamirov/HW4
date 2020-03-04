package com.miamirov.hw4


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_image.*

const val HIGH_RES_URL_TOKEN = "url"
const val POST_TEXT_TOKEN = "text"

class ImageActivity : AppCompatActivity() {
    private var broadCastReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        startService(Intent(this, HighResImageLoaderService::class.java).apply {
            putExtra(HIGH_RES_URL_TOKEN, intent.getStringExtra(HIGH_RES_URL_TOKEN))
        })



        broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val byteArray = intent?.getByteArrayExtra(INTENT_SERVICE_ACTION_TOKEN_BYTE_ARRAY)
                if (byteArray != null) {
                    val byteImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    main_image.setImageBitmap(byteImage)
                    main_image.visibility = View.VISIBLE
                }
            }
        }

        registerReceiver(
            broadCastReceiver,
            IntentFilter(INTENT_SERVICE_ACTION_TOKEN_RESPONSE).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
            })

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadCastReceiver)
    }
}