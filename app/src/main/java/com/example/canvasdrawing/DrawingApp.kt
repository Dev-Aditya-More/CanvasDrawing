package com.example.canvasdrawing

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun DrawingApp() {
    var path by remember { mutableStateOf(Path()) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { path.moveTo(it.x, it.y) },
                        onDrag = { change, _ ->
                            path.lineTo(change.position.x, change.position.y)
                        }
                    )
                }
        ) {


            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(width = 5f) // ✅ Set stroke width properly
            )

        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    path = Path() // Clear drawing
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    currentBitmap = captureCanvasBitmap(path)
                    saveImageToGallery(currentBitmap!!, context)
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save")
            }

            Button(
                onClick = {
                    currentBitmap?.let { shareImage(context, it) }
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Share")
            }
        }
    }
}
