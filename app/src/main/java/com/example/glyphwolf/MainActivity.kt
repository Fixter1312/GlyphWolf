package com.example.glyphwolf

import android.content.*
import android.graphics.*
import android.os.BatteryManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var glyphView: GlyphView
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateFromBattery()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glyphView = GlyphView(this)
        setContentView(glyphView)
        updateFromBattery()
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(batteryReceiver) } catch (_: Exception) {}
    }

    private fun updateFromBattery() {
        val i = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = i?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = i?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) (level * 100f / scale) else 100f

        val brightness = (pct / 100f * 255f).toInt().coerceIn(0, 255)

        glyphView.setBrightness(brightness)
        GlyphMatrixClient.trySendWolfFrame(this, brightness)
    }
}

class GlyphView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private var brightness = 255

    fun setBrightness(b: Int) {
        brightness = b
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = WOLF[0].size
        val h = WOLF.size
        val cell = minOf(width / w.toFloat(), height / h.toFloat())
        val startX = (width - w * cell) / 2f
        val startY = (height - h * cell) / 2f
        val alpha = brightness
        paint.color = Color.argb(alpha, 255, 255, 255)
        canvas.drawColor(Color.BLACK)

        for (y in 0 until h) {
            for (x in 0 until w) {
                if (WOLF[y][x] == 1) {
                    val left = startX + x * cell
                    val top = startY + y * cell
                    canvas.drawRect(left, top, left + cell, top + cell, paint)
                }
            }
        }
    }
}

object GlyphMatrixClient {
    private const val CLASS_NAME = "com.nothing.glyph.matrix.GlyphMatrix"
    private const val METHOD_INIT = "init"
    private const val METHOD_SHOW = "showFrame"

    fun trySendWolfFrame(context: Context, brightness: Int) {
        runCatching {
            val clazz = Class.forName(CLASS_NAME)
            runCatching {
                val m = clazz.methods.firstOrNull { it.name == METHOD_INIT && it.parameterTypes.size == 1 }
                m?.invoke(null, context)
            }
            val width = 25
            val height = 25
            val frame = buildFrameBytes(brightness)
            val instance = clazz.getDeclaredConstructor().newInstance()
            val method = clazz.methods.first { it.name == METHOD_SHOW }
            method.invoke(instance, width, height, frame, brightness)
        }.onFailure { it.printStackTrace() }
    }

    private fun buildFrameBytes(brightness: Int): ByteArray {
        val w = WOLF[0].size
        val h = WOLF.size
        val out = ByteArray(w * h)
        var idx = 0
        for (y in 0 until h) for (x in 0 until w)
            out[idx++] = if (WOLF[y][x] == 1) brightness.toByte() else 0
        return out
    }
}

private val WOLF = arrayOf(
intArrayOf(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,1,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,0,1,0,1,1,1,0,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,1,1,1,1,0,0,1,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,1,0,1,1,1,1,1,0,1,1,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,1,1,0,1,1,0,0,1,1,1,1,1,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,1,1,0,1,1,0,0,0,1,1,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,1,1,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,1,1,1,1,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,1,1,1,0,1,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,1,1,1,0,0,0,1,1,1,0,0,0,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,1,1,0,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,1,1,0,0,0,1,0,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,1,0,0,0,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,1,1,0,1,1,0,0,1,0,0,1,1,0,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,1,0,0,0,0,1,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,1,0,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,1,1,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,1,1,0,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0),
intArrayOf(0,0,0,0,0,0,0,0,0,1,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0)
)
