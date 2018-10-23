package cdchips.com.animsprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import java.io.IOException
import java.util.ArrayList

/**
 * Created by NubekDev on 15/08/18.
 */
class SpriteAnimator {

    var sheet: ArrayList<Bitmap>? = null
    var context: Context

    constructor(context: Context, resId: Int, columns: Int, rows: Int, howMany: Int) {
        this.context = context
        sheet = cutSprites(context, resId, columns, rows, howMany)
    }

    fun getAnimation(durationPerFrame: Int): AnimationDrawable? {
        val animation = AnimationDrawable()
        animation.isOneShot = false // repeat animation
        if (sheet != null) {
            for (f in sheet!!) {
                animation.addFrame(BitmapDrawable(context.resources, f),
                        durationPerFrame)
            }
            return animation
        }
        return null

    }

    fun getAnimation(durationPerFrame: Int, startFrame: Int, endFrame: Int): AnimationDrawable? {
        val animation = AnimationDrawable()
        animation.isOneShot = false // repeat animation
        if (sheet != null) {
            for (i in startFrame..endFrame) {
                animation.addFrame(BitmapDrawable(context.resources, sheet!![i]),
                        durationPerFrame)
            }
            return animation
        }
        return null
    }

    fun cutSprites(context: Context, resId: Int, columns: Int, rows: Int, howManyTotal: Int): ArrayList<Bitmap> {
        val bmp = getBitmapFromAssets(context, resId)
        val sprites = ArrayList<Bitmap>()
        if (bmp != null) {
            // cut bitmaps from bird bmp to array of bitmaps
            Log.w("Bitmap", "h -> ${bmp!!.height} , w -> ${bmp!!.width}")
            val FRAME_W = bmp!!.width / columns
            val FRAME_H = bmp!!.height / rows
            var currentFrame = 0

            for (i in 0..rows - 1) {
                for (j in 0..columns - 1) {

                    Log.w("Bitmap", "y -> ${FRAME_H * i} , h -> ${FRAME_H}")
                    sprites!!.add(Bitmap.createBitmap(bmp, FRAME_W
                            * j, FRAME_H * i, FRAME_W, FRAME_H))

                    /*
                    sprites!![currentFrame] = Bitmap.createScaledBitmap(
                            sprites!![currentFrame], FRAME_W / SCALE_FACTOR, FRAME_H / SCALE_FACTOR, true);

*/
                    if (++currentFrame >= howManyTotal) break

                }
            }
        } else {
            Log.w("Main", "Bitmap null")
        }
        return sprites
    }

    fun getBitmapFromAssets(context: Context,
                            resId: Int): Bitmap? {
        var bitmap: Bitmap? = null

        try {
            bitmap = BitmapFactory.decodeResource(context.resources, resId);
        } catch (ioe: IOException) {
            // manage exception
        } finally {
        }

        return bitmap
    }


/*
        fun getAnimationWithSprites(context:Context, resId:Int,columns:Int,rows:Int,howMany:Int,durationPerFrame:Int):AnimationDrawable{

            val frames = cutSprites(context,resId,columns,rows,howMany)
            val animation = AnimationDrawable()
            animation.isOneShot = false // repeat animation
            for(f in frames) {
                animation.addFrame(BitmapDrawable(context.resources, f),
                        durationPerFrame)
            }
            return animation
        }

        fun getAnimationWithSpritesSpecific(context:Context, resId:Int,columns:Int,rows:Int,columnStart:Int,rowStart:Int,howMany:Int,durationPerFrame:Int):AnimationDrawable{

            val frames = cutSpritesSpecific(context,resId,columns,rows,columnStart,rowStart,howMany)
            val animation = AnimationDrawable()
            animation.isOneShot = false // repeat animation
            for(f in frames) {
                animation.addFrame(BitmapDrawable(context.resources, f),
                        durationPerFrame)
            }
            return animation
        }


        fun cutSpritesSpecific(context:Context, resId:Int,columns:Int,rows:Int,columnStart:Int,rowStart:Int,howManyTotal:Int):ArrayList<Bitmap>{
            val bmp = getBitmapFromAssets(context,resId)
            val sprites = ArrayList<Bitmap>()
            if (bmp != null) {
                val FRAME_W = bmp!!.width / columns
                val FRAME_H = bmp!!.height / rows
                var currentFrame = 0

                for (j in columnStart..howManyTotal) {
                    sprites!!.add(Bitmap.createBitmap(bmp, FRAME_W
                            * j, FRAME_H * rowStart, FRAME_W, FRAME_H))
                }

            }else{
                Log.w("Main","Bitmap null")
            }
            return sprites
        }
        */

}