package com.uditpatidar.zomatoapp.ui.Activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.uditpatidar.zomatoapp.R

class SimpleActivity : AppCompatActivity() {

    private lateinit var editText1: EditText
    private lateinit var editText2: EditText
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        // Initialize views
        editText1 = findViewById(R.id.edit_text_1) // Replace with actual ID
        editText2 = findViewById(R.id.edit_text_2) // Replace with actual ID
        imageView = findViewById(R.id.image_to_Swap)

        imageView.setOnClickListener {
            swapTextWithAnimation()
        }
    }

    private fun swapTextWithAnimation() {
        // Get current Y positions
        val y1 = editText1.y
        val y2 = editText2.y

        // Create animations for both EditTexts
        val anim1 = ObjectAnimator.ofFloat(editText1, "y", y2)
        val anim2 = ObjectAnimator.ofFloat(editText2, "y", y1)

        // Set duration for the animations
        anim1.duration = 300
        anim2.duration = 300

        // Start both animations
        anim1.start()
        anim2.start()

        // Add a listener to handle the end of the animation
        anim1.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                // Swap text values when the animation ends
                val tempText = editText1.text.toString()
                editText1.setText(editText2.text.toString())
                editText2.setText(tempText)

                // Reset the Y positions after swapping
                editText1.y = y1
                editText2.y = y2
            }
        })
    }
}
