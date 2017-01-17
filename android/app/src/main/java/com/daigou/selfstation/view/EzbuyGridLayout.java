package com.daigou.selfstation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridLayout;


/**
 * 暂时不支持不规则布局的分割线
 */
public class EzbuyGridLayout extends GridLayout {
    public EzbuyGridLayout(Context context) {
        super(context);
    }

    public EzbuyGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EzbuyGridLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (getChildCount() > 0) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(0xFFEFEFEF);

            for (int i = 0; i < getChildCount(); i++) {
                drawLine(getChildAt(i), canvas, paint);
            }
        }
    }

    private void drawLine(View view, Canvas canvas, Paint paint) {
        canvas.drawLine(view.getLeft(), view.getTop(), view.getLeft(), view.getBottom(), paint);
        canvas.drawLine(view.getLeft(), view.getTop(), view.getRight(), view.getTop(), paint);
        canvas.drawLine(view.getRight(), view.getTop(), view.getRight(), view.getBottom(), paint);
        canvas.drawLine(view.getLeft(), view.getBottom(), view.getRight(), view.getBottom(), paint);
    }

    public void setRowAndColumn(int column, int total) {
        int row = (total % column == 0) ? total / column : total / column + 1;
        setRowCount(row);
        setColumnCount(column);
    }
}