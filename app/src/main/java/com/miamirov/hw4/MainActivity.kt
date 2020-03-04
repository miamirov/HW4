package com.miamirov.hw4

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference
import java.net.URL

const val API_PREF = "https://api.vk.com/method/photos.search?access_token="
const val QUERY_SIZE = 20
const val API_VERSION = "5.102"

class MainActivity : AppCompatActivity() {

    private var asyncTask: ImageLoader? = null
    private var queryList: ArrayList<Image>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edit_query.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                executeQuery(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return true
            }
        })

        asyncTask?.attachActivity(this)
    }

    override fun onRetainCustomNonConfigurationInstance(): ImageLoader? {
        return asyncTask
    }

    override fun onDestroy() {
        asyncTask?.activityReference = WeakReference(null)
        super.onDestroy()
    }

    private fun getQueryUrl(query: String): String {
        return "$API_PREF${BuildConfig.apiKey}&q=$query&count=$QUERY_SIZE&v=$API_VERSION"
    }

    private fun executeQuery(query: String) {
        asyncTask?.cancel(true)
        asyncTask?.activityReference = WeakReference(null)
        asyncTask = ImageLoader(this).apply {
            execute(getQueryUrl(query))
        }
    }

    private fun setImageList(ImageList: List<Image>?) {
        queryList = ImageList as ArrayList<Image>?
        if (ImageList != null) {
            image_recycler_view.visibility = View.VISIBLE
            image_recycler_view.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = ImageListAdapter(ImageList) {
                    startActivity(Intent(this@MainActivity, ImageActivity::class.java).apply {
                        putExtra(HIGH_RES_URL_TOKEN, it.highResUrl)
                        putExtra(POST_TEXT_TOKEN, it.description)
                    })
                }
            }
        }
    }

    internal fun onLoadCompleted(result: List<Image>?) {
        if (result != null) {
            setImageList(result)
        }
        asyncTask = null
    }

    class ImageLoader(activity: MainActivity?) :
        AsyncTask<String, Int, List<Image>>() {
        var activityReference = WeakReference(activity)
        private var cachedResult: List<Image>? = null

        override fun onPreExecute() {
            super.onPreExecute()
            activityReference.get()?.apply {
                image_recycler_view.visibility = View.GONE
            }
        }

        override fun doInBackground(vararg params: String?): List<Image> {
            return getListFromResponse(URL(params[0]).openConnection().getInputStream().reader().readText())
        }

        override fun onPostExecute(result: List<Image>?) {
            activityReference.get()?.image_recycler_view!!.visibility = View.VISIBLE
            activityReference.get()?.onLoadCompleted(result) ?: run {
                cachedResult = result
            }
        }


        fun attachActivity(activity: MainActivity) {
            activityReference = WeakReference(activity)
            cachedResult?.run {
                activityReference.get()?.onLoadCompleted(this)
                cachedResult = null
            }
        }

        private fun getListFromResponse(response: String?): List<Image> {
            if (response != null) {
                val json = Gson().fromJson(response, JsonObject::class.java)
                if (!json.has("response")) {
                    return emptyList()
                }

                val jsonResponse = json.get("response").asJsonObject
                if (!jsonResponse.has("items")) {
                    return emptyList()
                }
                val items = jsonResponse.get("items").asJsonArray
                val responseResult: ArrayList<Image> = ArrayList()
                items.forEach {
                    val item = it.asJsonObject
                    responseResult.add(Image().apply {
                        val imageSizes = item.get("sizes").asJsonArray
                        if (item.has("text")) {
                            description = item.get("text").asString
                        }
                        previewUrl = imageSizes[0].asJsonObject.get("url").asString
                        highResUrl =
                            imageSizes[imageSizes.size() - 1].asJsonObject.get("url").asString
                        preview = downloadPreview(previewUrl)
                    })
                }
                return responseResult
            } else {
                return emptyList()
            }
        }

        private fun downloadPreview(previewUrl: String): Bitmap {
            return BitmapFactory.decodeStream(URL(previewUrl).openConnection().getInputStream())
        }
    }
}

