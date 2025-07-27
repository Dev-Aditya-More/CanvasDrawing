package com.example.canvasdrawing

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DrawingApp() {
    val paths = remember { mutableStateListOf<Path>() }
    val undonePaths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFFF0F0F0))
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                },
                                onDrag = { change, _ ->
                                    currentPath?.lineTo(change.position.x, change.position.y)
                                },
                                onDragEnd = {
                                    currentPath?.let {
                                        paths.add(it)
                                        undonePaths.clear()
                                    }
                                    currentPath = null
                                }
                            )
                        }
                        .onSizeChanged { canvasSize = it }
                ) {
                    paths.forEach { path ->
                        drawPath(path = path, color = Color.Black, style = Stroke(width = 5f))
                    }

                    currentPath?.let {
                        drawPath(path = it, color = Color.Black, style = Stroke(width = 5f))
                    }
                }

                // Top-Right Icons Row
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = {
                        if (paths.isNotEmpty()) {
                            val last = paths.removeLast()
                            undonePaths.add(last)
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.undo_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }

                    IconButton(onClick = {
                        if (undonePaths.isNotEmpty()) {
                            val path = undonePaths.removeLast()
                            paths.add(path)
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.redo_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }

                    IconButton(onClick = {
                        paths.clear()
                        undonePaths.clear()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(onClick = {
                    val bitmap = createBitmap(canvasSize.width, canvasSize.height)
                    val canvas = android.graphics.Canvas(bitmap)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLACK
                        strokeWidth = 5f
                        style = android.graphics.Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    paths.forEach { canvas.drawPath(it.asAndroidPath(), paint) }
                    currentBitmap = bitmap
                    saveImageToGallery(context, bitmap)
                }) {
                    Text("Save")
                }

                Button(onClick = {
                    currentBitmap?.let { shareImage(context, it) }
                }) {
                    Text("Share")
                }
            }
        }
    }
}
