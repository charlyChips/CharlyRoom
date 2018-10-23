package charlychips.com.charlyroom

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import android.widget.Toast
import charlychips.com.charlyroom.Utils.Bluetooth
import charlychips.com.charlyroom.Utils.Custom

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.title_bluetooth.view.*

class MainActivity : AppCompatActivity() {

    var toast: Toast? = null
    var bluetooth: Bluetooth
    var connectionListener: Bluetooth.ConnectionListener

    init {
        bluetooth = Bluetooth.getInstance { s ->
            /*
            toast?.cancel()
            toast = Toast.makeText(this@MainActivity, s, Toast.LENGTH_SHORT)
            toast?.show()
            */

            if (s.contains("L".toRegex())) {
                bt_foco.isChecked = true
            }
            if (s.contains("l".toRegex())) {
                bt_foco.isChecked = false
            }

            if (s.contains("C".toRegex())) {
                bt_luces_colores.isChecked = true
            }
            if (s.contains("c".toRegex())) {
                bt_luces_colores.isChecked = false
            }


        }
        connectionListener = object : Bluetooth.ConnectionListener {
            override fun connectionSuccess(s: String?) {


                bt_block.isEnabled = false
            }

            override fun connectionFailed(s: String?) {


                bt_block.isEnabled = true
            }

            override fun connectionResult(s: String?) {

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.hide()

        tv_title.typeface = Custom.getMarioFont(this)
        tv_titleFront.typeface = Custom.getMarioFont(this)

        val tfBoton = Custom.getRoundFont(this)
        bt_luces_colores.typeface = tfBoton
        bt_foco.typeface = tfBoton
        bt_focoPecera.typeface = tfBoton
        bt_filtro.typeface = tfBoton

        setTitleLikeMario()

        bt_block.setOnClickListener{
            _ ->
            bluetooth.getDialogDevices(this, connectionListener).apply {
                setIcon(ContextCompat.getDrawable(this@MainActivity,R.drawable.ic_bluetooth))

                val v = LayoutInflater.from(this@MainActivity).inflate(R.layout.title_bluetooth,null).apply {
                    tv_bluetooth_title.typeface = Custom.getMarioFont(this@MainActivity)
                    tv_bluetooth_titleFront.typeface = Custom.getMarioFont(this@MainActivity)

                    var sb = StringBuilder()
                    sb.append("<font color=#F03522>B</font>") // red
                    sb.append("<font color=#26ACE6>L</font>") // blue
                    sb.append("<font color=#FDD201>U</font>") // yellow
                    sb.append("<font color=#26ACE6>E</font>") // blue
                    sb.append("<font color=#35B64A>T</font>") // green
                    sb.append("<font color=#FDD201>O</font>") // yellow
                    sb.append("<font color=#26ACE6>O</font>") // blue
                    sb.append("<font color=#35B64A>T</font>") // green
                    sb.append("<font color=#FDD201>H</font>") // yellow

                    tv_bluetooth_title.setText(Html.fromHtml(sb.toString()), TextView.BufferType.SPANNABLE)
                }

                setCustomTitle(v)
                show()
            }

        }
        /*
        fab.setOnClickListener { _ ->
            bluetooth.getDialogDevices(this, connectionListener).show()
        }
        */

        bt_foco.setOnCheckedChangeListener { _, b ->

            if (b) {
                bluetooth.conexionBt?.write("L")
                iv_shadow.visibility = View.GONE
            } else {
                bluetooth.conexionBt?.write("l")
                iv_shadow.visibility = View.VISIBLE
            }
        }

        bt_luces_colores.setOnCheckedChangeListener { _, b ->
            if (b) {
                bluetooth.conexionBt?.write("C")
                showShadowColors()
            } else {
                bluetooth.conexionBt?.write("c")
                hideShadowColors()
            }

        }
        bt_focoPecera.setOnCheckedChangeListener { _, b ->
            if (b) {
                bluetooth.conexionBt?.write("P")
                showShadowColors()
            } else {
                bluetooth.conexionBt?.write("p")
                hideShadowColors()
            }

        }
        bt_filtro.setOnCheckedChangeListener { _, b ->
            if (b) {
                bluetooth.conexionBt?.write("F")
                showShadowColors()
            } else {
                bluetooth.conexionBt?.write("f")
                hideShadowColors()
            }

        }


    }

    fun setTitleLikeMario(){
        var sb = StringBuilder()
        sb.append("<font color=#F03522>C</font>") // red
        sb.append("<font color=#26ACE6>H</font>") // blue
        sb.append("<font color=#FDD201>A</font>") // yellow
        sb.append("<font color=#26ACE6>R</font>") // blue
        sb.append("<font color=#35B64A>L</font>") // green
        sb.append("<font color=#FDD201>Y</font>") // yellow
        sb.append("\n")
        sb.append("<font color=#26ACE6>R</font>") // blue
        sb.append("<font color=#35B64A>O</font>") // green
        sb.append("<font color=#FDD201>O</font>") // yellow
        sb.append("<font color=#F03522>M</font>") //red
        tv_title.setText(Html.fromHtml(sb.toString()), TextView.BufferType.SPANNABLE);
    }

    override fun onResume() {
        super.onResume()
        doParallax()
        animMario()
        bluetooth.conectar(this, connectionListener)
    }

    override fun onPause() {
        super.onPause()
        bluetooth.disconnect()
    }

    fun showShadowColors() {
        iv_shadowColors.visibility = View.VISIBLE
        val duration = 200
        val a = AnimationDrawable().apply {
            isOneShot = false
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.draw_shadow_red), duration)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.draw_shadow_yellow), duration)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.draw_shadow_blue), duration)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.draw_shadow_green), duration)
            iv_shadowColors.setImageDrawable(this)
            start()
        }
    }

    fun hideShadowColors() {
        iv_shadowColors.setImageDrawable(null)
        iv_shadowColors.visibility = View.GONE

    }

    fun doParallax() {
        val dBack = 20000
        val dFront = 2800

        var metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val w = metrics.widthPixels

        val a1 = ObjectAnimator.ofFloat(bg1, "translationX", *floatArrayOf(0f, -w.toFloat())).apply {
            duration = dBack.toLong()
            interpolator = LinearInterpolator()
            repeatCount = -1
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                    bg1.scaleX *= -1f
                    bg2.scaleX *= -1f
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
        }
        val a2 = ObjectAnimator.ofFloat(bg2, "translationX", *floatArrayOf(w.toFloat() - 3f, 0f)).apply {
            duration = dBack.toLong()
            interpolator = LinearInterpolator()
            repeatCount = -1

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(p0: Animator?) {
                }

                override fun onAnimationEnd(p0: Animator?) {
                }

                override fun onAnimationCancel(p0: Animator?) {
                }

                override fun onAnimationStart(p0: Animator?) {
                }

            })
        }

        val a3 = ObjectAnimator.ofFloat(floor1, "translationX", *floatArrayOf(w.toFloat() - 3f, 0f)).apply {
            duration = dFront.toLong()
            interpolator = LinearInterpolator()
            repeatCount = -1
        }
        val a4 = ObjectAnimator.ofFloat(floor2, "translationX", *floatArrayOf(0f, -w.toFloat())).apply {
            duration = dFront.toLong()
            interpolator = LinearInterpolator()
            repeatCount = -1
        }
        AnimatorSet().apply {
            playTogether(a2, a1, a4, a3)
            start()
        }
    }

    fun animMario() {
        val durationPerFrame = 28
        val animation = AnimationDrawable().apply {
            isOneShot = false
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario0), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario1), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario2), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario3), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario4), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario5), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario6), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario7), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario8), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario9), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario10), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario11), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario12), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario13), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario14), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario15), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario16), durationPerFrame)
            addFrame(ContextCompat.getDrawable(this@MainActivity, R.drawable.mario17), durationPerFrame)


        }
        iv_mario.setImageDrawable(animation)
        animation.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
