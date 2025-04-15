package com.example.canvasdrawing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.tooling.preview.Preview
import com.example.canvasdrawing.ui.theme.DrawingAppTheme
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import android.graphics.Canvas as AndroidCanvas


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrawingAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DrawingApp()
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewDrawingApp() {
    DrawingAppTheme {
        DrawingApp()
    }
}



// Capture Canvas as Bitmap
fun captureCanvasBitmap(path: Path): Bitmap {
    val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val paint = android.graphics.Paint().apply { color = android.graphics.Color.BLACK; strokeWidth = 5f }
    canvas.drawPath(path.asAndroidPath(), paint)
    return bitmap
}

// Save Image to Gallery
fun saveImageToGallery(bitmap: Bitmap, context: Context) {
    val filename = "Drawing_${System.currentTimeMillis()}.png"
    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
    val outputStream: OutputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()

    MediaStore.Images.Media.insertImage(context.contentResolver, file.absolutePath, filename, "Drawing")
    Toast.makeText(context, "Saved to Gallery!", Toast.LENGTH_SHORT).show()
}

// Share Image
fun shareImage(context: Context, bitmap: Bitmap) {
    val uri = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        bitmap,
        "Shared Drawing",
        "Drawn using Jetpack Compose"
    )
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, Uri.parse(uri))
        type = "image/png"
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share via"))
}

