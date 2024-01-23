package com.pavlov.MyShadowGallery.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.pavlov.MyShadowGallery.ItemLoaderActivity
import com.pavlov.MyShadowGallery.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


class Encryption(private val context: Context) {
    val itemLoaderActivity = context as ItemLoaderActivity
    private val photoList = ArrayList<String>()

    companion object {

    }

    fun getPhotoList(): List<String> {
        return photoList
    }

    fun deleteFile(fileName: String) {
        // Удаляем файл из списка
        photoList.remove(fileName)

        // Здесь добавьте логику удаления файла с файловой системы
        // Например:
        val fileToDelete = File(context.filesDir, fileName)
        fileToDelete.delete()
    }

    fun addPhotoToList(int: Int, photoUri: Uri) {
        val fileName = photoUri.lastPathSegment ?: ""
        photoList.add(int, fileName)
    }

    fun canSaveFilesFromGallery(): Boolean {
        return true
        Log.d("=== Encryption", "=== canSaveFilesFromGallery: ${Boolean}")
    }

    fun encryptImage(imageUri: Uri, fileName: String) {
        val encryptionKey = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET)
        Log.d("=== Encryption", "=== готовится к шифрованию, принимаем на вход fileName: ${fileName}")
        // Получаем путь к файлу, который нужно зашифровать
        val inputStream = context.contentResolver.openInputStream(imageUri) ?: return
        var encryptedFile = File(context.applicationContext.filesDir, "${fileName}k")
        if (File(context.applicationContext.filesDir, "${fileName}k").exists()) {
            Log.d("=== Encryption", "=== файл fileName существует, будет перезапись: ${fileName}k")
            val existingFile = File(context.applicationContext.filesDir, "${fileName}k")
            existingFile.delete()
        }
        Log.d("=== Encryption", "=== готовится к шифрованию: ${encryptedFile.name}")
        Log.d("=== Encryption", "=== путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
        val outputStream = FileOutputStream(encryptedFile)
        val messageDigest = MessageDigest.getInstance("SHA-256")
        Log.d("=== Encryption", "=== файл messageDigest: ${messageDigest}")
        val hashedKey = messageDigest.digest(encryptionKey.toByteArray())
        Log.d("=== Encryption", "=== ключ: ${encryptionKey}")
        Log.d("=== Encryption", "=== файл hashedKey: ${hashedKey}")
        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
        Log.d("=== Encryption", "=== cipher")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            val encryptedBytes = cipher.update(buffer, 0, read)
            outputStream.write(encryptedBytes)
        }
        val encryptedBytes = cipher.doFinal()
        outputStream.write(encryptedBytes)

        if (File(context.applicationContext.filesDir, fileName).exists()) {
            Log.d("=== Encryption", "=== сейчас в директории существует файл fileName: ${fileName}")
        }
        if (File(context.applicationContext.filesDir, "${fileName}k").exists()) {
            Log.d("=== Encryption", "=== сейчас в директории существует файл {fileName}k: ${fileName}k")
        }

        val isEncryptedFileSaved = encryptedFile.exists()
        if (!isEncryptedFileSaved) {// Проверка на сохранение файла
            Log.e("=== Encryption", "=== Ошибка сохранения зашифрованного файла")
            return
        }
        toast(context.getString(R.string.encrypted_save))
        toast(encryptedFile.name)
        Log.d("=== Encryption", "=== Зашифрованный файл сохранен: ${encryptedFile.name}")
        Log.d("=== Encryption", "=== Путь к зашифрованному файлу: ${encryptedFile.absolutePath}")
        //если задать имя fileName = "my_secret_photo.jpg", то файл будет сохранен в следующем виде:
        //storage/emulated/0/Android/data/[app_package_name]/files/my_secret_photo.jpg

        val originalFile = File(imageUri.path) // Удаление оригинального изображения
        val isOriginalFileDeleted = originalFile.delete()
        Log.e("=== Encryption", "=== чекаем оригинал файла до удаления ${originalFile.name}")
        Log.e("=== Encryption", "=== чекаем оригинал файла до удаления${imageUri.path}")
        if (!isOriginalFileDeleted) { // Проверка на удаление оригинала
            inputStream.close()        // Закрытие потоков
            outputStream.flush()
            outputStream.close()
            Log.e("=== Encryption", "=== чекаем оригинал файла после удаления ${originalFile.name}")
            Log.e("=== Encryption", "=== чекаем оригинал файла после удаления${imageUri.path}")
        }
    }

    fun decryptImage(file: File): Bitmap {
        val decryptionKey = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET)
        Log.e("=== Encryption", "=== Начало декодирования. файл file: ${file.name}")

        val encryptedBytes = file.readBytes()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hashedKey = messageDigest.digest(decryptionKey.toByteArray())

        val keySpec = SecretKeySpec(hashedKey, "AES")
        val cipher = Cipher.getInstance("AES")
        Log.e("=== Encryption", "=== cipher")

        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        val decryptedBitmap = BitmapFactory.decodeByteArray(decryptedBytes, 0, decryptedBytes.size)

        Log.e("=== Encryption", "=== Успешный конец декодирования. файл decryptedBitmap: ${decryptedBitmap}")
        toast(context.getString(R.string.decrypting))

        return decryptedBitmap
    }

    fun isDecryptable(file: File): Boolean {
        return try {
            val decryptionKey = APKM(context).getMastersSecret(APK.KEY_BIG_SECRET)
            Log.e("=== Encryption", "=== Начало декодирования. файл file: ${file.name}")
            val encryptedBytes = file.readBytes()
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashedKey = messageDigest.digest(decryptionKey.toByteArray())
            val keySpec = SecretKeySpec(hashedKey, "AES")
            val cipher = Cipher.getInstance("AES")
            Log.e("=== Encryption", "=== cipher")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            cipher.doFinal(encryptedBytes)
            Log.e("=== Encryption", "=== Успешный конец декодирования.")
            true
        } catch (e: Exception) {
            Log.e("=== Encryption", "=== Ошибка при декодировании файла: ${e.message}")
            false
        }
    }

    fun createThumbnail(context: Context, input: Any) {
        var imageUri: Uri? = null
        var file: File? = null

        // Проверяем тип входных данных и устанавливаем imageUri или file соответственно
        when (input) {
            is Uri -> imageUri = input
            is File -> file = input
        }

        if (imageUri == null && file == null) {
            // Неверный ввод, обработайте по необходимости
            return
        }

        // Остальной код остается прежним с небольшими изменениями
        var scaledNumber = APKM(context)
            .getIntFromSharedPreferences(APK.KEY_PREVIEW_SIZE_SEEK_BAR)
            ?: APK.DEFAULT_PREVIEW_SIZE
        if (scaledNumber <= 0) {
            scaledNumber = 1
        }
        if (scaledNumber > 100) {
            scaledNumber = 100
        }

        val requestOptions = RequestOptions()
            .override(scaledNumber, scaledNumber)
            .diskCacheStrategy(DiskCacheStrategy.NONE)

        val loadRequest = if (imageUri != null) {
            Glide.with(context)
                .asBitmap()
                .load(imageUri)
        } else {
            Glide.with(context)
                .asBitmap()
                .load(file)
        }

        loadRequest
            .apply(requestOptions)
            .signature(ObjectKey(System.currentTimeMillis()))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    val thumbnailName =
                        saveThumbnailWithRandomFileName(context, resource, imageUri, file)
                    if (thumbnailName.isNotEmpty()) {
                        photoList.add(0, thumbnailName)
                        itemLoaderActivity.notifyDSC()
                        Log.e("=== Encryption", "=== Превью сохранено")
                        if (imageUri != null) {
                            deleteOriginalImage(imageUri)
                        } else {
                            // Обработайте удаление файла при необходимости
                        }
                    } else {
                        toast(context.getString(R.string.save_error))
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // То же, что и раньше
                }
            })
    }

    private fun saveThumbnailWithRandomFileName(
        context: Context,
        thumbnail: Bitmap,
        imageUri: Uri? = null,
        file: File? = null
    ): String {
        val fileName = if (imageUri != null) {
            File(imageUri.path).name
        } else {
            file?.name ?: ""
        }

        val fileExtension = fileName.substringAfterLast(".")
        val previewFileName = if (fileExtension.isNotEmpty()) {
            val fileNameWithoutExtension = fileName.substringBeforeLast(".")
            "${fileNameWithoutExtension}.p"
        } else {
            context.getString(R.string.timber_img)
        }
        val fileToSave = File(context.applicationContext.filesDir, previewFileName)

        try {
            val outputStream = FileOutputStream(fileToSave)
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return previewFileName
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getDefaultThumbnail(context: Context): Bitmap {
        // Здесь вы можете использовать свою иконку документа или другое дефолтное изображение
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_search)
        return drawable?.toBitmap() ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    fun createMiniFile(encryptedFile: File, expansion: String, inSampleSize: Int): File? {
        Log.d("=== PhotoListAdapter", "=== Option 0 selected")
        toast(context.getString(R.string.download))

        // Определяем размеры изображения без загрузки в память
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeFile(encryptedFile.absolutePath, this)
        }

        // Вычисляем inSampleSize
        options.inSampleSize = inSampleSize

        // Загружаем изображение в память с уменьшением размера
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeFile(encryptedFile.absolutePath, options)

        // Создаем выходной файл
        val folder = context.applicationContext.filesDir
        val fileName = removeFileExtension(encryptedFile.name)
        val outputFile = File(folder, "$fileName$expansion")

        // Сжимаем изображение и сохраняем его в файл
        FileOutputStream(outputFile).use { output ->
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 10, output)
        }

        // Освобождаем ресурсы
        bitmap?.recycle()

        return if (outputFile.exists()) {
            Log.d("=== PhotoListAdapter", "=== Output file exists")
            val updatedUri = outputFile.toUri()
            Log.d("=== PhotoListAdapter", "=== Updated URI: $updatedUri")
            addPhotoToList(0, updatedUri)
            toast(context.getString(R.string.done))
            outputFile
        } else {
            Log.e("=== PhotoListAdapter", "=== Output file does not exist")
            toast(context.getString(R.string.save_error))
            null
        }
    }

    fun removeFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf(".")
        return if (lastDotIndex == -1) {
            fileName
        } else {
            fileName.substring(0, lastDotIndex)
        }
    }

    private fun deleteOriginalImage(imageUri: Uri) {
        val file = File(imageUri.path)
        if (file.exists()) {
            file.delete()
        }
    }

    fun getPreviouslySavedFiles(): List<String> {
        val savedFiles = mutableListOf<String>()
        val directory = context.applicationContext.filesDir
        if (directory != null && directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                val sortedFiles = files
                    .filter { it.extension != "kk" && it.extension != "dat" && it.name != "profileInstalled" }
                    .sortedBy { it.lastModified() } // Сортировка по времени создания в возрастающем порядке
                savedFiles.addAll(sortedFiles.map { it.name })
            }
        }
        return savedFiles
    }

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }
}
