package charlychips.com.charlyroom.Utils

import android.content.Context
import android.graphics.Typeface

/**
 * Created by NubekDev on 03/10/18.
 */
object Custom{
    fun getMarioFont(context: Context):Typeface{
        return Typeface.createFromAsset(context.assets,"fonts/mario.ttf")
    }
    fun getBorderFont(context: Context):Typeface{
        return Typeface.createFromAsset(context.assets,"fonts/border.ttf")
    }
    fun getRoundFont(context: Context):Typeface{
        return Typeface.createFromAsset(context.assets,"fonts/round.ttf")
    }
}