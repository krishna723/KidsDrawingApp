package com.example.kidsdrawingapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var drawingView:DrawingView?=null
    private var mImageButtonCurrentPaint: ImageButton?=null
    val openGalleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if(result.resultCode== RESULT_OK && result.data!=null){
            val imageBackGround: ImageView= findViewById(R.id.iv_background)
            imageBackGround.setImageURI(result.data?.data)
        }
    }

    val requestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
        permissions->
        permissions.entries.forEach{
            val permissionName=it.key
            val isGranted=it.value

            if(isGranted){
//                Toast.makeText(this@MainActivity,"Permission granted now you can read the storage files", Toast.LENGTH_LONG).show()

                val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            }else{

                if(permissionName==android.Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this@MainActivity,"Permission denied", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView=findViewById(R.id.drawing_View)

        val linearLayoutPaintColor=findViewById<LinearLayout>(R.id.linearLayout)

        mImageButtonCurrentPaint=linearLayoutPaintColor[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
        )
        val ib_brush: ImageButton=findViewById(R.id.ib_brush)

        ib_brush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
        val ib_undo: ImageButton=findViewById(R.id.ib_undo)
        val ib_redo: ImageButton=findViewById(R.id.ib_redo)
        val ib_save: ImageButton=findViewById(R.id.ib_save)

        ib_undo.setOnClickListener{
            //Toast.makeText(this,"Hi",Toast.LENGTH_LONG).show()
            undoDrawing()
        }
        ib_redo.setOnClickListener{
           // Toast.makeText(this,"Hi",Toast.LENGTH_LONG).show()
            redoDrawing()
        }
        ib_save.setOnClickListener{
           if(isReadStorageAllow()) {

               lifecycleScope.launch{
                   val flDrawingView: FrameLayout=findViewById(R.id.flDrawingViewContainer)

                   saveBitmapFile(getBitmapFromView(flDrawingView))

               }

           }
        }

        val ibGallery : ImageButton=findViewById(R.id.ib_gallery)

        ibGallery.setOnClickListener{
            requestStoragePermission()
        }

    }

    private fun redoDrawing() {
        drawingView?.onClickRedo()
    }

    private fun undoDrawing() {
        drawingView?.onClickUndo()
    }

    @SuppressLint("WrongViewCast")
    private fun showBrushSizeChooserDialog(){

        val brushDialog= Dialog(this)
       // brushDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
       // brushDialog.setCancelable(false)
        brushDialog.setContentView(R.layout.dialog_brush_size)
       // brushDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        brushDialog.setTitle("Select brush size: ")
        val smallBtn : ImageButton =brushDialog.findViewById(R.id.ib_small_brush)
        val mediumBtn: ImageButton=brushDialog.findViewById(R.id.ib_medium_brush)
        val largeBtn: ImageButton=brushDialog.findViewById(R.id.ib_large_brush)

        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        }
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(15.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }

    fun paintClicked(view: View){
        //Toast.makeText(this, "button clicked",Toast.LENGTH_LONG).show()
        if(view!== mImageButtonCurrentPaint){
            val imageButton= view as ImageButton
            val colorTag=imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint=view
        }
    }

    private fun isReadStorageAllow(): Boolean{
        val result=ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)

        return result==PackageManager.PERMISSION_GRANTED
    }


    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)

        ){
            showRationaleDialog("Kids Drawing App"," Kids Drawing App" + "need to Access Storage")
        }else{
            requestPermission.launch(arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
               // android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }
    private fun showRationaleDialog(title: String, message: String){
        val builder: AlertDialog.Builder= AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){dialog,_ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun getBitmapFromView(view: View) : Bitmap{
        val returnedBitmap=Bitmap.createBitmap(view.width,view.height, Bitmap.Config.ARGB_8888)
        val canvas=Canvas(returnedBitmap)
        val bgDrawable=view.background
        if(bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?): String{

        var result=""
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try{
                    val bytes=ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f= File(externalCacheDir?.absoluteFile.toString()+ File.separator + "KidsDrawingApp_" + System.currentTimeMillis()/1000 +".png")
                    val fo=FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result= f.absolutePath

                    runOnUiThread{
                        if(result.isNotEmpty()){
                            Toast.makeText(this@MainActivity,"File save successfully: $result",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(this@MainActivity,"Something went wrong while saving the file ",Toast.LENGTH_LONG).show()
                        }
                    }
                }
                catch (e: java.lang.Exception){
                    result=""
                    e.printStackTrace()

                }
            }
        }
        return result
    }

    }

