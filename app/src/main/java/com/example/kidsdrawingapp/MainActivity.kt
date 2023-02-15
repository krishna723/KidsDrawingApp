package com.example.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private var drawingView:DrawingView?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView=findViewById(R.id.drawing_View)
        drawingView?.setSizeForBrush(5.toFloat())
    }

    private fun showBrushSizeChooserDialog(){

        val brushDialog= Dialog(this)
        brushDialog.setContentView(R.id.check)

        brushDialog.setTitle("Select brush size: ")


    }
}