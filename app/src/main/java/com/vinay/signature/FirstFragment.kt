package com.vinay.signature

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val signatureView = view.findViewById(R.id.signature) as SignatureView


        view.findViewById<Button>(R.id.button_first)
            .setOnClickListener {
                val bitmap = signatureView.getBitmaps()

//          val stream = ByteArrayOutputStream()
//          resizedImage.compress(Bitmap.CompressFormat.PNG, 100, stream)
//          val byteArray = stream.toByteArray()
//          val actualLength = byteArray.size

                val bundle = Bundle()
                bundle.putByteArray("bitmap", bitmap.toByteArray(Bitmap.CompressFormat.PNG))
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
            }
    }

    fun Bitmap.toByteArray(format: Bitmap.CompressFormat): ByteArray? {
        var data: ByteArray? = null
        try {
            val outputStream = ByteArrayOutputStream()
            this.compress(format, 100, outputStream)
            data = outputStream.toByteArray()
            outputStream.close()
            return data
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return data
    }
}