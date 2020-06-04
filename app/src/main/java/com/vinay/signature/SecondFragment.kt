package com.vinay.signature

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_second.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.Math.log10
import java.text.DecimalFormat
import kotlin.math.pow


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val byteArray = requireArguments().getByteArray("bitmap")
        val bmp = BitmapFactory.decodeByteArray(byteArray!!, 0, byteArray.size)
        actualImageView.setImageBitmap(bmp)

//    val dd = Utils.compressImage(bmp)


        //
//    Glide
//      .with(this)
//      .load(bmp)
//      .toBytes(CompressFormat.JPEG, 80)
//      .atMost()
//      .fitCenter()
//      .override(500, 500)
//      .signature(LongSignature()) // uses System.currentTimeMillis()
//      .into(object : SimpleTarget<ByteArray?>() {
//        fun onResourceReady(
//          resource: ByteArray?,
//          glideAnimation: GlideAnimation<in ByteArray?>?
//        ) {
//          saveImage(resource)
//        }
//      })


//    val compressFormat = ImageCompress.compressImageWithSize(byteArray, 150000)
        val compressFormatWithWidth = ImageCompress.compressImageDataWithWidth(byteArray, 480)

//    val bmps = BitmapFactory.decodeByteArray(compressFormat!!, 0, compressFormat.size)
        val bmpWidthCompress = BitmapFactory.decodeByteArray(
            compressFormatWithWidth!!,
            0,
            compressFormatWithWidth.size
        )


        //
        val out = ByteArrayOutputStream()
        bmpWidthCompress.compress(Bitmap.CompressFormat.PNG, 70, out)
        val decoded = BitmapFactory.decodeStream(ByteArrayInputStream(out.toByteArray()))




        compressedImageView.setImageBitmap(decoded)


        val actualLength = byteArray.size
//    val comLength = compressFormat.size

        val actualBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
//    val compBase64 = Base64.encodeToString(compressFormat!!, Base64.NO_WRAP);


        val actualLengths = calcBase64SizeInKBytes(actualBase64)
//    val comLengths = calcBase64SizeInKBytes(compBase64)

//    Utils.storePhotoOnDisk1(bmps)
//    Utils.storePhotoOnDisk1(decoded)
//    Utils.storePhotoOnDisk1(bmp)

        actualSizeTextView.text =
            String.format("Size : %s", getReadableFileSize((byteArray.size).toLong()))
        compressedSizeTextView.text = String.format(
            "Size : %s",
            getReadableFileSize(decoded.toByteArray(Bitmap.CompressFormat.PNG)!!.size.toLong())
        )

        view.findViewById<Button>(R.id.button_second)
            .setOnClickListener {
                findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
            }

        view.findViewById<ImageView>(R.id.actualImageView)
            .setOnClickListener {
                Utils.storePhotoOnDisk(bmp)
            }
        view.findViewById<ImageView>(R.id.compressedImageView)
            .setOnClickListener {
//        Utils.storePhotoOnDisk(bmps)


            }
    }

    private fun getReadableFileSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun calcBase64SizeInKBytes(base64String: String): Double? {
        var result = -1.0
        var padding = 0
        if (base64String.endsWith("==")) {
            padding = 2
        } else {
            if (base64String.endsWith("=")) padding = 1
        }
        result = Math.ceil(base64String.length / 4.toDouble()) * 3 - padding
        return result / 1000
    }


}