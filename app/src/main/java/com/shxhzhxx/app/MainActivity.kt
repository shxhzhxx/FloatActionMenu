package com.shxhzhxx.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showMenu.setOnClickListener { floatActionMenu.showMenu() }
        hideMenu.setOnClickListener { floatActionMenu.hideMenu() }
        showPrimaryBtn.setOnClickListener { floatActionMenu.showPrimaryButton() }
        hidePrimaryBtn.setOnClickListener { floatActionMenu.hidePrimaryButton() }
        addBtn.setOnClickListener {
            floatActionMenu.addView(FloatingActionButton(this).apply {
                size = FloatingActionButton.SIZE_MINI
            })
        }

        floatActionMenu.showMenu()
    }
}
