package com.pavlov.MyShadowGallery.security

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.TransitionDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.APK
import com.pavlov.MyShadowGallery.util.APKM

class ThreeStepsActivity : AppCompatActivity() {

    //    private lateinit var loadingIndicator: ProgressBar
//    private lateinit var utilStepsBox: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var errorTextWeb: TextView
    private lateinit var errorIcon: ImageView
    private lateinit var inputButton: Button
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private lateinit var oldKeyButton: Button
    private var isPasswordExists: Boolean = false // Add this variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_steps)
        sharedPreferences =
            getSharedPreferences(APK.PREFS_NAME, Context.MODE_PRIVATE)
        errorIcon = findViewById(R.id.error_icon)
        errorTextWeb = findViewById(R.id.error_text_web)
        inputButton = findViewById(R.id.retry_button)
        yesButton = findViewById(R.id.yes_button)
        noButton = findViewById(R.id.no_button)
        oldKeyButton = findViewById(R.id.button_old_key)

        isPasswordExists = intent.getBooleanExtra("isPasswordExists", false) // ФЛАГ С intent

        // Call the appropriate method based on the flag
        if (isPasswordExists) {
            step2()
        } else {
            stepZero()
        }
    }

    // три шага для входа в приложение

    fun stepZero() {
        errorIcon.setImageResource(R.drawable.ic_launcher_foreground)
        errorTextWeb.text = resources.getString(R.string.step00)
        inputButton.setOnClickListener {
            step1()
        }
    }

    private fun step1() { // ПАРОЛЬ
        val emptyIcon = ContextCompat.getDrawable(this, android.R.color.transparent)
        errorIcon.setImageDrawable(emptyIcon)
        val drawable1 = ContextCompat.getDrawable(this, R.drawable.three_steps1)
        val drawable2 = ContextCompat.getDrawable(this, R.drawable.three_steps2)
        val transitionDrawable = TransitionDrawable(arrayOf(drawable2, drawable1))
        errorIcon.background = transitionDrawable
        transitionDrawable.startTransition(4000)
        yesButton.visibility = View.VISIBLE
        noButton.visibility = View.VISIBLE
        yesButton.setOnClickListener {
            val intent = Intent(this, SetPasswordActivity::class.java)
            startActivity(intent)
        }
        noButton.setOnClickListener {
            step2()
        }
        val savedPassword = APKM(context = this).getMastersSecret(APK.KEY_SMALL_SECRET)
        if (savedPassword.isNullOrBlank()) {
            errorTextWeb.text = resources.getString(R.string.step01_01)
            inputButton.visibility = View.GONE
        } else {
            errorTextWeb.text = resources.getString(R.string.step01_02)
            inputButton.visibility = View.VISIBLE
            inputButton.text = resources.getString(R.string.step01_03)
            inputButton.setOnClickListener {
//                val delPassword = true
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("delPassword", true)
                startActivity(intent)
            }
        }


    }

    private fun step2() { // МАСКИРОВКА
        val emptyIcon = ContextCompat.getDrawable(this, android.R.color.transparent)
        errorIcon.setImageDrawable(emptyIcon)
        val drawable1 = ContextCompat.getDrawable(this, R.drawable.three_steps1)
        val drawable2 = ContextCompat.getDrawable(this, R.drawable.three_steps2)
        errorTextWeb.text = resources.getString(R.string.step02_00)
        yesButton.visibility = View.VISIBLE
        noButton.visibility = View.VISIBLE
        inputButton.visibility = View.GONE
        val transitionDrawable = TransitionDrawable(arrayOf(drawable1, drawable2))
        errorIcon.background = transitionDrawable
        transitionDrawable.startTransition(4000)
        val editor = sharedPreferences.edit()
        yesButton.setOnClickListener {
            editor.putBoolean(APK.KEY_EXIST_OF_MIMICRY, true)
            editor.apply()
            if (sharedPreferences.getBoolean(
                    APK.KEY_EXIST_OF_PASSWORD,
                    false
                )) {
                errorTextWeb.text = resources.getString(R.string.step02_01)
            } else {
                errorTextWeb.text = resources.getString(R.string.step02_02)
            }
            inputButton.text = resources.getString(R.string.step02_03)
            inputButton.visibility = View.VISIBLE
            yesButton.visibility = View.GONE
            noButton.visibility = View.GONE
            inputButton.setOnClickListener {
                step3()
            }

        }
        noButton.setOnClickListener {
            editor.putBoolean(APK.KEY_EXIST_OF_MIMICRY, false)
            editor.apply()
            step3()
        }
    }

    private fun step3() { // КЛЮЧ ШИФРОВАНИЯ
        errorTextWeb.text = resources.getString(R.string.step03_01)
        inputButton.text = "?"
        inputButton.visibility = View.GONE
        yesButton.visibility = View.VISIBLE
        noButton.visibility = View.VISIBLE
        errorIcon.visibility = View.GONE

        yesButton.setOnClickListener {
            Toast.makeText(this, R.string.wait, Toast.LENGTH_SHORT).show()
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }
        noButton.setOnClickListener {
//            val editor = sharedPreferences.edit()
//            editor.putBoolean(APK.KEY_EXIST_OF_ENCRYPTION_K, false)
//            editor.putBoolean(APK.KEY_USE_THE_ENCRYPTION_K, false)
//            editor.apply()
            val displayIntent = Intent(this, MainPageActivity::class.java)
            startActivity(displayIntent)
        }

    }
}