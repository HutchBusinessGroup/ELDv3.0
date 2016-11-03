package com.hutchgroup.elog.util;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

import com.hutchgroup.elog.R;

@SuppressWarnings("deprecation")

public class GaugeView extends LinearLayout{

    private int gaugeDiameter;      //Gauge radius
    private int gaugeBackground;    //Gauge background resource
    private int needleBackground;   //Needle background resource
    private int needleWidth;        //Needle width
    private int needleHeight;       //Needle height
    private int needleX;            //Needle X position
    private int needleY;            //Needle Y position
    private int needleDeltaX;       //Needle's X position from the centre of gauge
    private int needleDeltaY;       //Needle's Y position from the centre of gauge
    private int deflectTime;        //Animation time when needle deflects to a higher angle
    private int releaseTime;        //Animation time when needle deflects to a lower angle
    private int pivotX;             //Needles X Axis of rotation
    private int pivotY;             //Needles Y Axis of rotation
    private int deltaXAxis;         //Needles new X Axis of rotation
    private int deltaYAxis;         //Needles new Y Axis of rotation
    private float currentValue;     //Current needle value
    private float minValue;         //Minimum needle value
    private float maxValue;         //Maximum needle value
    private float currentAngle;     //Current angular position of needle(Used in rotate animation)
    private float previousAngle;    //To store last known angular position of needle(Used in rotate animation)
    private float minAngle;         //Minimum angle of needle
    private float maxAngle;         //Maximum angle of needle
    private float currentDegrees;   //Current angular position of needle

    private boolean animateDeflect; //Enable/Disable rotate animation

    NeedleDeflectListener NDL;
    public interface NeedleDeflectListener {
        /**Called when needle value or angle is changed*/
        public void onDeflect(float angle,float value);

    }

    /**Register a callback to be invoked when the needle value/angle is changed.*/
    public void setOnNeedleDeflectListener(NeedleDeflectListener eventListener) {
        NDL=eventListener;
    }

    private AbsoluteLayout guageBack;
    private LinearLayout gaugeNeedle;

    /**Custom view used for creating analog gauges like speedometer*/
    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if(!isInEditMode()){
            LayoutInflater layoutInflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layoutInflater.inflate(R.layout.gauge_layout,this);
            initView();
        }

    }


    private void initView() // Initializes the view
    {
        this.gaugeBackground=R.drawable.speed_guage_1;
        this.needleBackground=R.drawable.needle;
        this.gaugeDiameter=0;
        this.needleWidth=0;
        this.needleHeight=0;
        this.needleX=0;
        this.needleY=0;
        this.needleDeltaX=0;
        this.needleDeltaY=0;
        this.currentValue=0;
        this.minValue=0;
        this.maxValue=100;
        this.currentAngle=0;
        this.minAngle=0;
        this.maxAngle=360;
        this.deflectTime=0;
        this.releaseTime=0;
        this.pivotX=0;
        this.pivotY=0;
        this.previousAngle=0;
        this.deltaXAxis=0;
        this.deltaYAxis=0;
        this.currentDegrees=0;
        this.animateDeflect=true;
        this.gaugeNeedle=(LinearLayout)findViewById(R.id.gaugeNeedleLay);
        this.guageBack=(AbsoluteLayout) findViewById(R.id.gaugeFrame);
        this.guageBack.setBackgroundResource(gaugeBackground);
        this.gaugeNeedle.setBackgroundResource(needleBackground);
        this.gaugeNeedle.bringToFront();
    }


    /**Sets a background resource for the gauge*/
    public void setGaugeBackgroundResource(int resID)
    {
        gaugeBackground=resID;
        guageBack.setBackgroundResource(0);
        guageBack.setBackgroundResource(gaugeBackground);
        guageBack.refreshDrawableState();
    }


    /**Sets the Diameter of the gauge*/
    public void setDiameter(int diameter)
    {
        gaugeDiameter=diameter;
        guageBack.setLayoutParams(new android.widget.LinearLayout.LayoutParams(gaugeDiameter,gaugeDiameter));
    }


    /**Sets a background resource for the needle*/
    public void setNeedleBackgroundResource(int resID)
    {
        needleBackground=resID;
        gaugeNeedle.setBackgroundResource(needleBackground);
    }


    /**Creates a needle at the centre of the gauge.
     <br> <b>deltaX</b>: Adjusts needle's X position from the centre of gauge
     <br> <b>deltaY</b>: Adjusts needle's Y position from the centre of gauge*/
    public void createNeedle(int width,int height,int deltaX,int deltaY)
    {
        this.needleWidth=width;
        this.needleHeight=height;
        this.needleDeltaX=deltaX;
        this.needleDeltaY=deltaY;

        this.needleX=guageBack.getLeft()+(gaugeDiameter/2)+needleDeltaX-needleWidth/2;
        this.needleY=guageBack.getTop()+(gaugeDiameter/2)+needleDeltaY;

        this.pivotX=needleWidth/2;
        this.pivotY=Math.abs(needleDeltaY);

        AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(this.needleWidth,this.needleHeight,this.needleX,this.needleY);
        gaugeNeedle.setLayoutParams(params);
    }

    /**Sets a reference background for the gauge*/
    public void setReferenceBackground()
    {
        //guageBack.setBackgroundResource(R.drawable.degrees);
    }

    /**Removes the reference background of the gauge*/
    public void removeReferenceBackground()
    {
        guageBack.setBackgroundResource(this.gaugeBackground);
    }

    /**Sets the current needle value*/
    public void setCurrentValue(float value)
    {

        if(value>maxValue)
            this.currentValue=maxValue;
        else if(value<minValue)
            this.currentValue=minValue;
        else
            this.currentValue=value;

        this.currentAngle=(((this.currentValue-this.minValue)*(this.maxAngle-this.minAngle))
                /(this.maxValue-this.minValue))+this.minAngle;
        setCurrentAngle(this.currentAngle);

    }

    /**Sets the needle value range*/
    public void setValueRange(float min_Value,float max_Value)
    {
        this.minValue=min_Value;
        this.maxValue=max_Value;

    }

    /**Sets the needle angle range (0-360)*/
    public void setAngleRange(float min_Angle,float max_Angle)
    {
        if(min_Angle<0)
            min_Angle=0;
        if(max_Angle>360)
            max_Angle=360;
        this.minAngle=min_Angle;
        this.maxAngle=max_Angle;
    }

    /**Sets the current needle angle*/
    public void setCurrentAngle(float angle)
    {
        if(angle>maxAngle)
            this.currentAngle=maxAngle;
        else if(angle<minAngle)
            this.currentAngle=minAngle;
        else
            this.currentAngle=angle;


        RotateAnimation needleDeflection=new RotateAnimation(this.previousAngle, this.currentAngle,this.pivotX,this.pivotY){
            protected void applyTransformation(float interpolatedTime,Transformation t) {
                currentDegrees=previousAngle+(currentAngle-previousAngle)*interpolatedTime;
                currentValue=(((currentDegrees-minAngle)*(maxValue-minValue))/(maxAngle-minAngle))+minValue;
                if(NDL!=null)
                    NDL.onDeflect(currentDegrees,currentValue);
                super.applyTransformation(interpolatedTime, t);
            }

        };

        needleDeflection.setAnimationListener(new AnimationListener() {@Override
        public void onAnimationStart(Animation arg0) {}@Override
        public void onAnimationRepeat(Animation arg0) {}@Override
        public void onAnimationEnd(Animation arg0) {previousAngle=currentAngle;}});


        if(currentAngle>this.previousAngle)
            needleDeflection.setDuration(this.deflectTime);
        else
            needleDeflection.setDuration(this.releaseTime);

        if(!animateDeflect)
            needleDeflection.setDuration(0);

        needleDeflection.setFillAfter(true);
        this.gaugeNeedle.startAnimation(needleDeflection);
        this.gaugeNeedle.refreshDrawableState();
    }

    /**Sets the needle's animation time
     <br> <b>deflectTime</b>: Time taken by the needle to deflect to a higher value/angle
     <br> <b>releaseTime</b>: Time taken by the needle to deflect to a lower value/angle*/
    public void setAnimationTime(int deflectTime,int releaseTime)
    {
        this.releaseTime=releaseTime;
        this.deflectTime=deflectTime;
    }

    /**Sets the axis of needle rotation with respect to the centre of gauge*/
    public void setDeltaAxis(int deltaX,int deltaY)
    {
        this.deltaXAxis=deltaX;
        this.deltaYAxis=deltaY;
        this.pivotX=(needleWidth/2)+deltaXAxis;
        this.pivotY=deltaYAxis;
    }

    /**Returns the current needle angle*/
    public float getCurrentAngle()
    {
        return this.currentDegrees;
    }

    /**Returns the Background resource ID of the gauge*/
    public int getGaugeBackgroundResource()
    {
        return this.gaugeBackground;
    }

    /**Returns the Diameter of the gauge*/
    public int getDiameter()
    {
        return this.gaugeDiameter;
    }

    /**Returns the Background resource ID of the needle*/
    public int getNeedleBackgroundResource()
    {
        return this.needleBackground;
    }

    /**Returns the current needle value*/
    public float getCurrentValue()
    {
        return this.currentValue;
    }

    /**Returns the needle width*/
    public int getNeedleWidth()
    {
        return this.needleWidth;
    }

    /**Returns the needle height*/
    public int getNeedleHeight()
    {
        return this.needleHeight;
    }

    /**Returns the X position of needle*/
    public int getNeedlePositionX()
    {
        return this.needleX;
    }

    /**Returns the Y position of needle*/
    public int getNeedlePositionY()
    {
        return this.needleY;
    }

    /**Returns the X axis of rotation of needle*/
    public int getNeedleAxisX()
    {
        return this.pivotX;
    }

    /**Returns the X axis of rotation of needle*/
    public int getNeedleAxisY()
    {
        return this.pivotY;
    }

    /**Returns the minimum needle value*/
    public float getMinValue()
    {
        return this.minValue;
    }

    /**Returns the maximum needle value*/
    public float getMaxValue()
    {
        return this.maxValue;
    }

    /**Returns the minimum needle angle*/
    public float getMinAngle()
    {
        return this.minAngle;
    }

    /**Returns the maximum needle angle*/
    public float getMaxAngle()
    {
        return this.maxAngle;
    }

    /**Returns the needle deflect time*/
    public int getDeflectTime()
    {
        return this.deflectTime;
    }

    /**Returns the needle release time*/
    public int getReleaseTime()
    {
        return this.releaseTime;
    }

    /**Enable/disable needle animation*/
    public void setNeedleAnimation(boolean EnableAnimation)
    {
        this.animateDeflect=EnableAnimation;

    }

    /**Returns needle animation state*/
    public boolean getNeedletAnimation()
    {
        return this.animateDeflect;

    }

}