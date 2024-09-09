package com.uditpatidar.zomatoapp.ui.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import com.uditpatidar.zomatoapp.R
import com.uditpatidar.zomatoapp.ui.Fragment.HomeFragment

class SplaceScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splace_screen)
        window.statusBarColor = ContextCompat.getColor(this, R.color.sky)

        Handler().postDelayed({
            val intent= Intent(this@SplaceScreenActivity,Dashboard::class.java)
            startActivity(intent)
            finish()
        },3000)

    }
}