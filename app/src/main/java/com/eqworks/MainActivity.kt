package com.eqworks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MainActivity : AppCompatActivity() {
    lateinit var locationLibrary: Library

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Driver Code
        // You can remove the text view line if you don't want to display the result
        // the element will be displayed in the info log with tag EQWORKS-LIBRARY
        findViewById<Button>(R.id.main_activity_button).setOnClickListener{
            locationLibrary = Library(this).apply {
                textView = findViewById(R.id.main_activity_text_view)
                setup()
                getLastLocation()
            }
        }
    }


    /**
     * Need to pass the result s to the library in order to process the results
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationLibrary.onRequestPermissionsResult(requestCode, grantResults)
    }
}