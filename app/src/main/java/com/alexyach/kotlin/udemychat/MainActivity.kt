package com.alexyach.kotlin.udemychat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alexyach.kotlin.udemychat.databinding.ActivityMainBinding
import com.alexyach.kotlin.udemychat.ui.signin.SignInFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, SignInFragment.newInstance())
                .commit()
        }

    }
}