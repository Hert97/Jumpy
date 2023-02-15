package com.example.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.JumpyActivity
import com.example.jumpy.R

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_menu)

        val buttonPlay : Button = findViewById(R.id.button_play)
        val buttonExit : Button = findViewById(R.id.button_exit)
        val buttonCredit : Button = findViewById(R.id.button_credit)

        buttonPlay.setOnClickListener(this)
        buttonExit.setOnClickListener(this)
        buttonCredit.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.button_play -> {
                val intent = Intent(this, JumpyActivity::class.java)
                startActivity(intent)
            }
            R.id.button_exit -> {
                finish()
            }
            R.id.button_credit -> {
                val intent = Intent(this, CreditActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
