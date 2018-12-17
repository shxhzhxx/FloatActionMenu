package com.shxhzhxx.floatactionmenu

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton


private const val TAG = "FloatActionMenu"
private const val DURATION = 300L
private const val PRIMARY_DURATION = 200L

class FloatActionMenu : LinearLayout {
    private val initVisible: Boolean
    private val primaryButton: CardView
    private val mFastOutSlowInInterpolator = FastOutSlowInInterpolator()
    private val mFastOutLinearInInterpolator = FastOutLinearInInterpolator()
    var visibleListener: ((Boolean) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = VERTICAL
        val colorAccent = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, colorAccent, true)
        primaryButton = LayoutInflater.from(context).inflate(R.layout.float_primary_btn, this, false) as CardView
        val lp = primaryButton.layoutParams as LinearLayout.LayoutParams
        val dp6 = resources.displayMetrics.density * 6
        val background: Int
        val elevation: Float
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.FloatActionMenu, 0, 0)
        try {
            initVisible = a.getBoolean(R.styleable.FloatActionMenu_initVisible, false)
            background = a.getColor(R.styleable.FloatActionMenu_primaryBtnBackground, colorAccent.data)
            elevation = a.getDimension(R.styleable.FloatActionMenu_primaryBtnElevation, dp6)
            lp.bottomMargin = a.getDimensionPixelSize(R.styleable.FloatActionMenu_primaryBtnMarginBottom, dp6.toInt())
            lp.topMargin = a.getDimensionPixelSize(R.styleable.FloatActionMenu_primaryBtnMarginTop, dp6.toInt())
            lp.leftMargin = a.getDimensionPixelSize(R.styleable.FloatActionMenu_primaryBtnMarginLeft, dp6.toInt())
            lp.rightMargin = a.getDimensionPixelSize(R.styleable.FloatActionMenu_primaryBtnMarginRight, dp6.toInt())
        } finally {
            a.recycle()
        }
        primaryButton.layoutParams = lp
        primaryButton.setCardBackgroundColor(background)
        primaryButton.cardElevation = elevation
        primaryButton.apply {
            if (!initVisible) {
                alpha = 0f
                scaleX = 0f
                scaleY = 0f
                visibility = View.INVISIBLE
            }
        }
        addView(primaryButton)
        primaryButton.setOnClickListener {
            if (isMenuVisible) {
                hideMenu()
            } else {
                showMenu()
            }
        }


        val handler = Handler(Looper.myLooper())
        var cnt = 30
        handler.post(object : Runnable {
            override fun run() {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && primaryButton.isLaidOut)
                    || (primaryButton.width != 0 || primaryButton.height != 0) || --cnt < 0
                ) {
                    val centerY = (primaryButton.top + primaryButton.height / 2).toFloat()
                    getViews().forEach { hideView(it, centerY) }
                } else {
                    handler.post(this)
                }
            }
        })
    }

    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        super.addView(child, childCount - 1, params)
        if (child != null && child != primaryButton && !isMenuVisible) {
            hideView(child, (primaryButton.top + primaryButton.height / 2).toFloat())
        }
    }

    private fun hideView(view: View, centerY: Float) {
        view.visibility = View.INVISIBLE
        view.alpha = 0f
        view.translationY = centerY - (view.top + view.height / 2).toFloat()
    }

    fun showMenu() {
        showPrimaryButton { showViews() }
    }

    private fun showViews() {
        val rotateDuration = (DURATION * (1f - primaryButton.rotation / 135f)).toLong()
        primaryButton.animate().apply { cancel() }.withLayer().rotation(135f).setDuration(rotateDuration)
            .setInterpolator(mFastOutSlowInInterpolator).withEndAction { visibleListener?.invoke(true) }.start()

        getViews().forEach {
            it.apply { visibility = View.VISIBLE }.animate().apply { cancel() }.withLayer().translationY(0f).alpha(1f)
                .setDuration(rotateDuration).setInterpolator(mFastOutSlowInInterpolator).start()
        }
    }

    fun showPrimaryButton(endAction: () -> Unit) {
        primaryButton.apply { visibility = View.VISIBLE }.animate().apply { cancel() }.alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration((PRIMARY_DURATION * (1f - primaryButton.alpha)).toLong())
            .setInterpolator(mFastOutLinearInInterpolator).withEndAction(endAction).withLayer().start()
    }

    fun hideMenu(hideFab: Boolean = !initVisible, listener: (() -> Unit)? = null) {
        val rotateDuration = (DURATION * (primaryButton.rotation / 135f)).toLong()
        val centerY = (primaryButton.top + primaryButton.height / 2).toFloat()
        getViews().forEach {
            it.animate().apply { cancel() }.withLayer().translationY(centerY - (it.top + it.height / 2).toFloat())
                .alpha(0f).setDuration(rotateDuration).setInterpolator(mFastOutSlowInInterpolator).start()
        }

        primaryButton.animate().apply { cancel() }.withLayer().rotation(0f).setDuration(rotateDuration)
            .setInterpolator(mFastOutSlowInInterpolator)
            .withEndAction {
                getViews().forEach { it.visibility = View.INVISIBLE }
                if (hideFab) {
                    primaryButton.animate().apply { cancel() }.withLayer().alpha(0f).scaleX(0f).scaleY(0f)
                        .setDuration((PRIMARY_DURATION * (primaryButton.alpha)).toLong())
                        .setInterpolator(mFastOutLinearInInterpolator)
                        .withEndAction {
                            listener?.invoke()
                            visibleListener?.invoke(false)
                        }
                        .start()
                } else {
                    listener?.invoke()
                    visibleListener?.invoke(false)
                }
            }
            .start()
    }

    val isMenuVisible get() = primaryButton.rotation != 0f

    private fun getViews() = ArrayList<View>().apply {
        for (i in 0 until childCount - 1) {
            add(getChildAt(i))
        }
    }


    /**
     * hide fab after it is laid out.
     * Otherwise, the next show will not have animation
     * */
    private fun hideFabWithoutAnimation(fab: FloatingActionButton) {
        /*
            trick:
            we don't want the hide animation here. While there is not api to hide without animation.
            hide animation use alpha and scale animation underlay, we can skip the alpha animation to make user unconscious.
            * */
        fab.alpha = 0f

        val handler = Handler(Looper.myLooper())
        var cnt = 30
        handler.post(object : Runnable {
            override fun run() {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && fab.isLaidOut) || (fab.width != 0 || fab.height != 0) || --cnt < 0) {
                    fab.hide()
                } else {
                    handler.post(this)
                }
            }
        })
    }
}