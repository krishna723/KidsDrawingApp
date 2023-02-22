package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.alpha
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView (context: Context, attrs: AttributeSet): View(context,attrs) {
    private var mDrawPath: CustomPath?=null
    private var mCanvasBitmap: Bitmap?=null
    private var mDrawPaint: Paint?=null
    private var mCanvasPaint: Paint?=null
    private var mBrushSize:Float=0.toFloat()
    private var color= Color.RED
    private var canvas: Canvas?=null
    private var mPaths=ArrayList<CustomPath>()
    private val mUndoPath=ArrayList<CustomPath>()
    private var lastX = 0f
    private var lastY = 0f



//initialize the code
    init{
        setUpDrawing()
    }

    fun onClickUndo(){
        val size=mPaths.size
        if(size > 0){
            mUndoPath.add(mPaths[size-1])
            mPaths.removeAt(size-1)
            invalidate()
        }
    }
    fun onClickRedo(){
        val size=mUndoPath.size
        if(size > 0){
            mPaths.add(mUndoPath[size-1])
            mUndoPath.removeAt(size-1)
            invalidate()
        }
    }
    private fun setUpDrawing() {
        mDrawPaint= Paint()
       // mDrawPaint!!.isAntiAlias()
        mDrawPath=CustomPath(color,mBrushSize)
        mDrawPaint!!.color=color
        mDrawPaint!!.style= Paint.Style.STROKE
        mDrawPaint!!.strokeJoin= Paint.Join.ROUND
        mDrawPaint!!.strokeCap= Paint.Cap.ROUND
      //  mDrawPaint.run { alpha(0xff) }
        mCanvasPaint= Paint(Paint.ANTI_ALIAS_FLAG)
        //mBrushSize=20.toFloat()



    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap= Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888)
        canvas= Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for (path in mPaths){
            mDrawPaint!!.strokeWidth=path.brushThickness
            mDrawPaint!!.color=path!!.color
            canvas.drawPath(path!!,mDrawPaint!!)
        }
        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth=mDrawPath!!.brushThickness
            mDrawPaint!!.color=mDrawPath!!.color
            canvas.drawPath(mDrawPath!!,mDrawPaint!!)

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX=event?.x
        val touchY= event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN->{
                mDrawPath!!.color=color
                mDrawPath!!.brushThickness=mBrushSize
                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!,touchY!!)
//                lastX = touchX
//                lastY = touchY

            }
            MotionEvent.ACTION_MOVE->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
//                val dx = Math.abs(touchX!!-lastX)
//                val dy = Math.abs(touchY!! - lastY)
//                if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
//                    mDrawPath?.quadTo(lastX, lastY, (touchX + lastX) / 2, (touchX + lastY) / 2)
//                    lastX = touchX
//                    lastY = touchX
//                }
            }
            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!)
                mDrawPath=CustomPath(color,mBrushSize)
            }
            else-> return false
        }
        invalidate()
        return true
    }

    fun setSizeForBrush(newSize:Float){
        mBrushSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth=mBrushSize
    }


    fun setColor(newColor: String){
        color=Color.parseColor(newColor)
        mDrawPaint!!.color=color
    }

//    companion object{
//        const val TOUCH_TOLERANCE = 4f
//    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float): android.graphics.Path() {

    }

}