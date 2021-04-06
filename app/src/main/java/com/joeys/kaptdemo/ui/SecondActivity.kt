package com.joeys.kaptdemo.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import com.joeys.binding_annotation.BindsView
import com.joeys.binding_core.InnerBinding
import com.joeys.kaptdemo.R

class SecondActivity : AppCompatActivity() {

    @BindsView(R.id.iv)
    lateinit var iv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        InnerBinding.bind(this)
        iv.setImageResource(R.mipmap.ic_launcher)
    }
}