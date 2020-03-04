package com.miamirov.hw4

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*

data class Image(
    var preview: Bitmap? = null,
    var description: String? = "",
    var previewUrl: String? = "",
    var highResUrl: String? = "",
    var likeCount: Int = 0,
    var rePostCount: Int = 0
):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Bitmap::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(preview, flags)
        parcel.writeString(description)
        parcel.writeString(previewUrl)
        parcel.writeString(highResUrl)
        parcel.writeInt(likeCount)
        parcel.writeInt(rePostCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }
}

class ImageListViewHolder(val root: View) : RecyclerView.ViewHolder(root) {
    val imagePreview: ImageView = root.image_preview
    val imageDescription: TextView = root.image_description
}

class ImageListAdapter(
    private val images: List<Image>,
    val onClick: (Image) -> Unit
) : RecyclerView.Adapter<ImageListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageListViewHolder {
        return ImageListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item,
                parent,
                false
            )
        ).apply {
            root.setOnClickListener {
                onClick(images[adapterPosition])
            }
        }
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageListViewHolder, position: Int) {
        holder.imagePreview.setImageBitmap(images[position].preview)
        holder.imageDescription.text = images[position].description

    }
}
