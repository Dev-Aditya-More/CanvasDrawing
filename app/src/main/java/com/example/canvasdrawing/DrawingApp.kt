package com.example.canvasdrawing

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "Drawing_${System.currentTimeMillis()}.png"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DrawingApp")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        val stream = resolver.openOutputStream(it)
        stream?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        contentValues.clear()
        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, contentValues, null, null)

        Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
    }
}


fun shareImage(context: Context, bitmap: Bitmap) {
    val file = File(context.cacheDir, "shared_drawing.png")
    val out = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    out.flush()
    out.close()

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}
