package com.ekzamen
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.gson.Gson
import java.io.*
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private lateinit var editTextBrand: EditText
    private lateinit var editTextYear: EditText
    private lateinit var editTextEngineVolume: EditText
    private lateinit var editTextDealerEmail: EditText
    private lateinit var searchButton: Button
    private lateinit var imageViewCar: ImageView
    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
    private lateinit var car: Car
    private lateinit var webView: WebView
    private lateinit var const: ConstraintLayout
    private lateinit var languageButton: Button

    private lateinit var currentLocale: Locale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextBrand = findViewById(R.id.editTextBrand)
        editTextYear = findViewById(R.id.editTextYear)
        editTextEngineVolume = findViewById(R.id.editTextEngineVolume)
        editTextDealerEmail = findViewById(R.id.editTextDealerEmail)
        searchButton = findViewById(R.id.searchButton)
        imageViewCar = findViewById(R.id.imageViewCar)
        const = findViewById(R.id.constant)

        webView = findViewById(R.id.webView)

        // Настройте WebView
        webView.webViewClient = WebViewClient()

        // Восстанавливаем данные из файла при создании активности
        car = loadCarData()

        // Заполняем поля данными
        populateFields()

        languageButton = findViewById(R.id.languageButton)

        currentLocale = LocaleHelper.getLocale(this)

        languageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        searchButton.setOnClickListener {
            // Получаем данные из полей ввода
            val brand = editTextBrand.text.toString()
            val year = editTextYear.text.toString()
            val engineVolume = editTextEngineVolume.text.toString()

            // Формируем строку запроса для поиска в Google
            val searchQuery = "$brand $year $engineVolume"
            val googleSearchUrl = "https://www.google.com/search?q=${Uri.encode(searchQuery)}"

            // Загружаем URL-адрес Google Search в WebView
            webView.loadUrl(googleSearchUrl)

            // Показываем WebView
            webView.visibility = WebView.VISIBLE
            const.visibility = WebView.VISIBLE
            // Сохраняем данные при нажатии searchButton
            saveCarData()
            showToast("Данные сохранены")
        }

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        const.visibility = View.GONE
        }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("Русский", "Польский", "Украинский")
        val currentLanguageIndex = when (currentLocale.language) {
            "ru" -> 0
            "pl" -> 1
            "uk" -> 2
            else -> 0
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Выберите язык")
        builder.setSingleChoiceItems(languages, currentLanguageIndex) { dialog, which ->
            val selectedLanguage = when (which) {
                0 -> "ru"
                1 -> "pl"
                2 -> "uk"
                else -> "ru"
            }
            LocaleHelper.setLocale(this, selectedLanguage)
            dialog.dismiss()
            recreate() // Пересоздание активности для применения изменений
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.setLocale(locale)

        val resources: Resources = resources
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


    private fun populateFields() {
        editTextBrand.setText(car.brand)
        editTextYear.setText(car.year.toString())
        editTextEngineVolume.setText(car.engineVolume.toString())
        editTextDealerEmail.setText(car.dealerEmail)

        // Загрузка изображения из галереи
        imageViewCar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }
    }

    // Обработка результатов выбора изображения из галереи
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImageUri = data?.data
            // Далее, вы можете использовать выбранное изображение
            if (selectedImageUri != null) {
                // Загрузка изображения в ImageView
                imageViewCar.setImageURI(selectedImageUri)
                // Сохраните URI изображения в объекте car, если нужно
                car.photoUrl = selectedImageUri.toString()
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onPause() {
        super.onPause()
        // Сохраняем данные при уходе в фоновый режим
        saveCarData()
    }

    private fun saveCarData() {
        car.brand = editTextBrand.text.toString()
        car.year = editTextYear.text.toString().toIntOrNull() ?: 0
        car.engineVolume = editTextEngineVolume.text.toString().toFloatOrNull() ?: 0.0f
        car.dealerEmail = editTextDealerEmail.text.toString()

        // Сериализуем объект Car в формат JSON и сохраняем в файл
        val gson = Gson()
        val json = gson.toJson(car)

        try {
            openFileOutput("car_data.txt", Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadCarData(): Car {
        try {
            //создаем файл car_data.txt через Gson в формате сериализации CROB
            val inputStream: FileInputStream = openFileInput("car_data.txt")
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)

            val stringBuilder = StringBuilder()
            var text: String? = null
            while ({ text = bufferedReader.readLine(); text }() != null) {
                stringBuilder.append(text)
            }

            val gson = Gson()
            return gson.fromJson(stringBuilder.toString(), Car::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Возвращаем новый объект Car, если не удалось загрузить данные
        return Car()
    }
}