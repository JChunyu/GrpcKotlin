package com.chunyu.grpckotlin

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.grpc.ManagedChannelBuilder
import io.grpc.android.AndroidChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        findViewById<Button>(R.id.hwBtn).setOnClickListener {
            click()
        }
    }

    private fun click() {
        val port = 50051
        val androidChannel = AndroidChannelBuilder
            .forAddress("localhost", port)
            .context(this@MainActivity)
            .usePlaintext()
            .build()
        val client = HelloWorldClient(androidChannel)
        CoroutineScope(Dispatchers.IO).launch {
            client.sayHello("from android")
        }
    }
}