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
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.pavlov.MyShadowGallery.MainPageActivity
import com.pavlov.MyShadowGallery.R
import com.pavlov.MyShadowGallery.util.AppPreferencesKeys

class ThreeStepsActivity : AppCompatActivity() {

    //    private lateinit var loadingIndicator: ProgressBar
//    private lateinit var utilStepsBox: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var errorTextWeb: TextView
    private lateinit var errorIcon: ImageView
    private lateinit var inputButton: Button
    private lateinit var yesButton: Button
    private lateinit var noButton: Button
    private var isPasswordExists: Boolean = false // Add this variable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_three_steps)
        sharedPreferences =
            getSharedPreferences(AppPreferencesKeys.PREFS_NAME, Context.MODE_PRIVATE)
        errorIcon = findViewById(R.id.error_icon)
        errorTextWeb = findViewById(R.id.error_text_web)
        inputButton = findViewById(R.id.retry_button)
        yesButton = findViewById(R.id.yes_button)
        noButton = findViewById(R.id.no_button)

        // Retrieve the flag from the intent
        isPasswordExists = intent.getBooleanExtra("isPasswordExists", false)

        // Call the appropriate method based on the flag
        if (isPasswordExists) {
            step2()
        } else {
            stepZero()
        }
    }

    // три шага для входа в приложение

    fun stepZero() {
//        loadingIndicator.visibility = View.INVISIBLE
        errorIcon.setImageResource(R.drawable.ic_launcher_foreground)
        errorTextWeb.text = resources.getString(R.string.step00)
//        retryButton.text = resources.getString(R.string.goAhead)
//        retryButton.visibility = View.VISIBLE
//        utilStepsBox.visibility = View.VISIBLE
//        mainActivityLayout.alpha = 0.5f
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
        val savedPassword = masterAlias()
        if (savedPassword.isNullOrBlank()) {
            errorTextWeb.text = resources.getString(R.string.step01_01)
            inputButton.visibility = View.GONE
        } else {
            errorTextWeb.text = resources.getString(R.string.step01_02)
            inputButton.visibility = View.VISIBLE
            inputButton.text = resources.getString(R.string.step01_03)
            inputButton.setOnClickListener {
                val intent = Intent(this, SetPasswordActivity::class.java)
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

        yesButton.setOnClickListener {
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_MIMICRY_SWITCH, true).apply()
            if (isPasswordExists) {
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
            val editor = sharedPreferences.edit()
            editor.putBoolean(AppPreferencesKeys.KEY_MIMICRY_SWITCH, false).apply()
            step3()
        }
    }

    private fun step3() { // КЛЮЧ ШИФРОВАНИЯ
        errorTextWeb.text = resources.getString(R.string.step03_00)
//        val pixels = (60 * resources.displayMetrics.density).toInt()
//        val params = LinearLayout.LayoutParams(pixels, ViewGroup.LayoutParams.WRAP_CONTENT)
//        inputButton.layoutParams = params
        inputButton.text = "?"
//        yesButton.layoutParams = params
//        yesButton.text = "✔️"
//        noButton.layoutParams = params
//        noButton.text = "❌"
        inputButton.visibility = View.VISIBLE
        yesButton.visibility = View.VISIBLE
        noButton.visibility = View.VISIBLE

        yesButton.setOnClickListener {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }
        noButton.setOnClickListener {
            val displayIntent = Intent(this, MainPageActivity::class.java)
            startActivity(displayIntent)
        }
        inputButton.setOnClickListener {
            val displayIntent = Intent(this, KeyInputActivity::class.java)
            startActivity(displayIntent)
        }
    }

    fun masterAlias(): String? {
        val masterAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences: SharedPreferences =
            EncryptedSharedPreferences.create(
                AppPreferencesKeys.MY_SECRETS_PREFS_NAME,
                masterAlias,
                applicationContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        return sharedPreferences.getString(AppPreferencesKeys.KEY_SMALL_SECRET, "")
    }

}