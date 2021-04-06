package com.joeys.kaptdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.joeys.binding_annotation.BindsView
import com.joeys.binding_core.InnerBinding
import com.joeys.kaptdemo.ui.SecondActivity

class MainActivity : AppCompatActivity() {
    @BindsView(R.id.tv)
    lateinit var tv: TextView

    @BindsView(R.id.btn)
    lateinit var btn: Button

    @BindsView(R.id.fragmentRoot)
    lateinit var fragmentRoot: FrameLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        InnerBinding.bind(this)
        btn.text = "这事button"
        btn.setOnClickListener {
            startActivity(Intent(this@MainActivity, SecondActivity::class.java))
        }
        supportFragmentManager.beginTransaction()
            .add(R.id.fragmentRoot, BlankFragment())
            .commit()

    }
}