package com.hutchgroup.elog.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.hutchgroup.elog.R;

public class Needle extends View {

    private Paint linePaint;
    private Path linePath;
    private Paint needleScrewPaint;

    private Matrix matrix;
    private int framePerSeconds = 100;
    private long animationDuration = 10000;
    private long startTime;
    private float centerX, centerY;
    private float needleLength, needSmallLength;
    private float needleWide;

    public Needle(Context context) {
        super(context);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public Needle(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public Needle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        matrix = new Matrix();
        this.startTime = System.currentTimeMillis();
        this.postInvalidate();
        init();
    }

    public void setCenter(float x, float y) {
        centerX = x;
        centerY = y;
        init();
    }

    public void setNeedleLength(float longLength, float smallLength) {
        needleLength = longLength;
        needSmallLength = smallLength;
        init();
    }

    public void setNeedleWide(float wide) {
        needleWide = wide;
        init();
    }

    private void init(){

        linePaint = new Paint();
        linePaint.setColor(getResources().getColor(R.color.yellow2)); // Set the color
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE); // set the border and fills the inside of needle
        linePaint.setAntiAlias(true);
        linePaint.setStrokeWidth(5.0f); // width of the border
        //linePaint.setShadowLayer(8.0f, 0.1f, 0.1f, Color.GRAY); // Shadow of the needle

        linePath = new Path();

        //centerX = 130f;
        //centerY = 50f;
        //needleLength = 300f;
        //needleWide = 10f;
        //smallLength = 70
        linePath.moveTo(centerX - needSmallLength, centerY);
        linePath.lineTo(centerX, centerY - needleWide);
        linePath.lineTo(centerX + needleLength, centerY);
        linePath.lineTo(centerX, centerY + needleWide);
        linePath.lineTo(centerX - needSmallLength, centerY);

//        linePath.moveTo(centerX, centerY);
//        linePath.lineTo(centerX + 70f, centerY - needleWide);
//        linePath.lineTo(centerX + needleLength, centerY);
//        linePath.lineTo(centerX + 70f, centerY + needleWide);
//        linePath.lineTo(centerX, centerY);

//        linePath.moveTo(50.0f, 50.0f);
//        linePath.lineTo(130.0f, 40.0f);
//        linePath.lineTo(600.0f, 50.0f);
//        linePath.lineTo(130.0f, 60.0f);
//        linePath.lineTo(50.0f, 50.0f);
        //linePath.addCircle(130.0f, 50.0f, 20.0f, Path.Direction.CW);
        linePath.close();

        needleScrewPaint = new Paint();
        needleScrewPaint.setColor(Color.BLACK);
        needleScrewPaint.setAntiAlias(true);
        needleScrewPaint.setShader(new RadialGradient(centerX, centerY, 10.0f,
                Color.DKGRAY, Color.BLACK, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        long elapsedTime = System.currentTimeMillis() - startTime;

        matrix.postRotate(1.0f, centerX, centerY); // rotate 10 degree every second
        canvas.concat(matrix);

        canvas.drawPath(linePath, linePaint);

        //canvas.drawCircle(centerX, centerY, 16.0f, needleScrewPaint);

        if(elapsedTime < animationDuration){
            this.postInvalidateDelayed(10000 / framePerSeconds);
        }

        //this.postInvalidateOnAnimation();
        invalidate();
    }

}
