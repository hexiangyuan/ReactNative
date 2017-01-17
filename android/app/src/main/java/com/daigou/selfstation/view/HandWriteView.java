package com.daigou.selfstation.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class HandWriteView extends View {

    private static final float TOUCH_TOLERANCE = 4;

    private Paint m_Paint;
    private float mX, mY;

    private Path mPath;
    private Canvas canvas;
    private Bitmap bitmap;
    private int height;
    private int width;

    public HandWriteView(Context context, AttributeSet attr) {
        super(context, attr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);
        onCanvasInitialization();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft()
                - getPaddingRight();
        height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingLeft()
                - getPaddingRight();
        bitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(this.bitmap);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onCanvasInitialization() {
        m_Paint = new Paint();
        m_Paint.setAntiAlias(true);
        m_Paint.setDither(true);
        m_Paint.setColor(Color.parseColor("#000000"));
        m_Paint.setStyle(Paint.Style.STROKE);
        m_Paint.setStrokeJoin(Paint.Join.ROUND);
        m_Paint.setStrokeCap(Paint.Cap.ROUND);
        m_Paint.setStrokeWidth(4);
        m_Paint.setColor(Color.BLACK);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.DITHER_FLAG));
    }

    public void reset() {
        this.mX = 0;
        this.mY = 0;
    }

    public void touchStart(float x, float y) {
        this.reset();
        mPath = new Path();
        mPath.moveTo(x, y);
        this.mX = x;
        this.mY = y;
        invalidate();
    }

    public void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            if(mPath == null) {
                return;
            }
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
        canvas.drawPath(this.mPath, m_Paint);
        invalidate();
    }

    public void touchUp() {
        if(mPath == null) {
            return;
        }
        mPath.lineTo(mX, mY);
        canvas.drawPath(this.mPath, m_Paint);
        invalidate();
    }

    public void clear() {
        if(mPath != null) {
            mPath = null;
        }
        if (bitmap != null) {
            bitmap = null;
            bitmap = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);
            invalidate();
        }
    }

}
