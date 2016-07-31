package io.github.dantetam.opstrykontest;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Dante on 7/30/2016.
 */
class GameScreen extends TextView {

    private int margin;
    private int marginColor;

    private Paint mMarginPaint;
    private Paint mTextPaint;

    public GameScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.GameScreen,
                0, 0);

        try {
            margin = a.getInt(R.styleable.GameScreen_margin, 10);
            marginColor = a.getColor(R.styleable.GameScreen_margin_color, Color.BLACK);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        mMarginPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mMarginPaint.setColor(super.getCurrentTextColor());
        mMarginPaint.setColor(marginColor);
        mMarginPaint.setTextSize(super.getTextSize());

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //mMarginPaint.setColor(super.getCurrentTextColor());
        mTextPaint.setColor(super.getCurrentTextColor());
        mTextPaint.setTextSize(super.getTextSize());

        this.setTextAlignment(TEXT_ALIGNMENT_CENTER);
    }

    public void setMargin(int m) {
        margin = m;
        invalidate();
        requestLayout();
    }

    public int getMargin() {
        return margin;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("heart", 0, 200, mTextPaint);
        //canvas.drawText
        //canvas.drawRect(getLeft() + margin, getTop() + margin, getRight() - margin, getBottom() - margin, mMarginPaint);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mMarginPaint);
        //canvas.drawRect(getLeft() + margin, getTop() + margin, getRight() - margin, getBottom() - margin, mMarginPaint);
    }

}