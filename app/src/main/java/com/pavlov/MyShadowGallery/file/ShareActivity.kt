package com.pavlov.MyShadowGallery.file

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pavlov.MyShadowGallery.R

class ShareActivity : AppCompatActivity() {

    private lateinit var shareHandler: ShareHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        shareHandler = ShareHandler(this)

        if (Intent.ACTION_SEND == intent.action && intent.type?.startsWith("image/") == true) {
            // Получаем URI изображения
            val imageUri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            // Проверяем, что URI не является null
            if (imageUri != null) {
                shareHandler.handleSharedImage(imageUri)
            }
        }
        this.finish()
    }
}