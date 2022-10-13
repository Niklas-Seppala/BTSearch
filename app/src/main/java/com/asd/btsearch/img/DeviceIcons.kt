package com.asd.btsearch.img

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.asd.btsearch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val TAG = "IconProcessor"

/**
 * Handles device icon caching. Both in-memory and disk.
 * No stale entry eviction strategy atm.
 */
private object DeviceIconCache {
    private val cache = HashMap<String, BitmapDrawable>()

    /**
     * Tries to fetch icon first from memory cache.
     * If that fails, checks the disk cache. If icon
     * is found there, creates [BitmapDrawable] from [Bitmap]
     * and stores it in memory cache.
     *
     * @param ctx [Context] Android context.
     * @param key [String] Key to the icon.
     *
     * @return [BitmapDrawable?] Nullable bitmap
     */
    suspend fun tryGet(ctx: Context, key: String): BitmapDrawable? {
        var bitmap = cache[key]
        bitmap ?: run {
            Log.i(TAG, "Memory cache miss for icon $key")
            withContext(Dispatchers.IO) {
                this.runCatching {
                    val f = File(ctx.cacheDir, "img/$key.png")
                    if (f.exists()) {
                        Log.i(TAG, "Found icon $key on disk cache")
                        val istream = f.inputStream()
                        bitmap = BitmapDrawable(ctx.resources, BitmapFactory.decodeStream(istream))
                        istream.close()
                        bitmap?.also {
                            cache[key] = bitmap!!
                            Log.d(TAG, "Read icon $key from disk cache and saved to memory")
                        }
                    }
                }
            }
        }
        return bitmap
    }

    /**
     * Places specified [BitmapDrawable] object both in-memory and disk
     * cache.
     *
     * @param ctx [Context] Android context.
     * @param key [String] Icon key
     * @param bitmap [BitmapDrawable] Bitmap to store.
     */
    suspend fun cache(ctx: Context, key: String, bitmap: BitmapDrawable) {
        if (!cache.contains(key)) {
            this.cache[key] = bitmap
            withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    File(ctx.cacheDir, "img/").mkdir()
                    val f = File(ctx.cacheDir, "img/$key.png")
                    val ostream = f.outputStream()
                    bitmap.bitmap.compress(Bitmap.CompressFormat.PNG, 90, ostream)
                    ostream.close()
                    Log.i(TAG, "Cached $key -> ${bitmap.hashCode()} to disk")
                }
            }
        }
    }
}

/**
 * Utility object that encapsulates functionality about
 * working with dynamically generated device icons.
 */
object DeviceIcons {
    private data class Dimensions<T : Number>(val width: T, val height: T)
    private const val srcIconId = R.drawable.bluetooth_marker
    private const val height = 300
    private lateinit var bmIcon: Bitmap
    private var init = true

    private val textPaint = Paint().also {
        it.style = Paint.Style.FILL
        it.color = Color.BLACK
        it.textSize = 30f
    }

    private val rectPaint = Paint().also {
        it.color = Color.WHITE
    }

    /**
     * Draws new dynamic icon for device.
     *
     * @param ctx [Context] Android context
     * @param mac [String] Device mac-address.
     *
     * @return [BitmapDrawable] Resulting bitmap.
     */
    suspend fun drawIcon(ctx: Context, mac: String): BitmapDrawable {
        // First check the cache
        var resultDrawable = DeviceIconCache.tryGet(ctx, mac)
        if (resultDrawable != null) {
            return resultDrawable
        }

        // Only load source bitmap once.
        if (init) {
            bmIcon = BitmapFactory.decodeResource(ctx.resources, srcIconId)
            init = false
        }

        // Store needed dimensions.
        val srcIconDims = Dimensions(bmIcon.width, bmIcon.height)
        val textDims = Dimensions(textPaint.measureText(mac), 34f)
        val resultDims = Dimensions(height, textDims.width.toInt())

        // Create new bitmap with enough room for text & src icon.
        // Also create canvas object.
        val workingCopy = Bitmap.createBitmap(
            resultDims.width, resultDims.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(workingCopy)

        // Draw text text inside container rect.
        val textStartX = 300 / 2 - textDims.width / 2
        val ovalRect = RectF(
            textStartX,
            srcIconDims.height.toFloat() + 8,
            textStartX + textDims.width,
            srcIconDims.height + 38f
        )
        canvas.drawRect(ovalRect, rectPaint)
        canvas.drawText(mac, textStartX, srcIconDims.height + textDims.height, textPaint)

        // Draw icon
        val iconStartX = 300 / 2 - srcIconDims.width / 2
        val rect = Rect(iconStartX, 0, iconStartX + srcIconDims.width, srcIconDims.height)
        canvas.drawBitmap(bmIcon, null, rect, null)

        // Create drawable
        resultDrawable = BitmapDrawable(ctx.resources, workingCopy)

        // Cache bitmap for further use.
        DeviceIconCache.cache(ctx, mac, resultDrawable)

        return resultDrawable
    }
}
