package com.example.canvasdrawing

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.example.canvasdrawing.ui.theme.DrawingAppTheme
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DrawingAppTheme {
                DrawingApp()
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun DrawingApp() {
    val context = LocalContext.current
    var currentColor by remember { mutableStateOf(Color.Black) }

    val paths = remember { mutableStateListOf<Pair<Path, Color>>() }
    val undonePaths = remember { mutableStateListOf<Pair<Path, Color>>() }

    val currentPath = remember { Path() }
    val canvasSize = remember { mutableStateOf(IntSize.Zero) }

    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showPicker by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf(Color.Black) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFEEEEEE))
        .pointerInput(Unit) {
            detectDragGestures(
                onDragStart = {
                    currentPath.moveTo(it.x, it.y)
                },
                onDrag = { change, dragAmount ->
                    currentPath.lineTo(change.position.x, change.position.y)
                },
                onDragEnd = {
                    paths.add(currentPath to currentColor)
                    undonePaths.clear()
                    currentPath.reset()
                }
            )
        }
    ) {

        // Canvas
        Canvas(modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize.value = it }
        ) {
            paths.forEach { (path, color) ->
                drawPath(path = path, color = color, style = Stroke(width = 5f))
            }
            drawPath(path = currentPath, color = currentColor, style = Stroke(width = 5f))
        }

        IconButton(onClick = { showPicker = true }) {
            Icon(
                painter = painterResource(id = R.drawable.palette_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                contentDescription = "Color Picker",
                tint = selectedColor
            )
        }

        if (showPicker) {
            ColorPickerDialog(
                initialColor = selectedColor,
                onColorSelected = { selectedColor = it },
                onDismiss = { showPicker = false }
            )
        }

            // Undo
            IconButton(onClick = {
                if (paths.isNotEmpty()) {
                    undonePaths.add(paths.removeLast())
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.undo_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                    contentDescription = "Redo",
                    tint = Color.Black
                )
            }

            // Redo
            IconButton(onClick = {
                if (undonePaths.isNotEmpty()) {
                    paths.add(undonePaths.removeLast())
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.redo_24dp_e3e3e3_fill0_wght400_grad0_opsz24),
                    contentDescription = "Redo",
                    tint = Color.Black
                )
            }
        }

        // Save / Share buttons
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            Button(onClick = {
                val bitmap = createBitmap(canvasSize.value.width, canvasSize.value.height)
                val canvas = android.graphics.Canvas(bitmap)
                val paint = android.graphics.Paint().apply {
                    strokeWidth = 5f
                    style = android.graphics.Paint.Style.STROKE
                    isAntiAlias = true
                }
                paths.forEach { (path, color) ->
                    paint.color = color.toArgb()
                    canvas.drawPath(path.asAndroidPath(), paint)
                }
                currentBitmap = bitmap
                saveImageToGallery(context, bitmap)
            }) {
                Text("Save")
            }

            Button(onClick = {
                currentBitmap?.let {
                    shareImage(context, it)
                }
            }) {
                Text("Share")
            }
        }
    }
@Composable
fun ColorPickerDialog(
    initialColor: Color = Color.Red,
    onColorSelected: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    var hsvColor by remember { mutableStateOf(HsvColor.from(initialColor)) }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick a color") },
        text = {
            ClassicColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                color = hsvColor,
                onColorChanged = { hsvColor = it }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onColorSelected(hsvColor.toColor())
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

