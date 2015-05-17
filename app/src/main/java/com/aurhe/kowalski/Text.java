package com.aurhe.kowalski;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;

public class Text {
    private float x, y, buttonX, buttonY, buttonWidth, buttonHeight;
    private int screenX, screenY;
    private String text;
    private TextPaint textPaint;
    private Paint buttonPaint;
    private boolean isButton;
    private Rect buttonRect;


    public Text(String text, float x, float y, int size, float density, Paint.Align align, int color) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.isButton = false;

        int screenSize = (int) (size * density);

        textPaint = new TextPaint();
        textPaint.setTextAlign(align);
        textPaint.setTextSize(screenSize);
        textPaint.setColor(color);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public Text(String text, float x, float y, int size, float density, Paint.Align align, int color,
                float buttonX, float buttonY, float buttonWidth, float buttonHeight, int buttonColor) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.isButton = true;
        this.buttonX = buttonX;
        this.buttonY = buttonY;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;

        buttonPaint = new Paint();
        buttonPaint.setColor(buttonColor);

        int screenSize = (int) (size * density);

        textPaint = new TextPaint();
        textPaint.setTextAlign(align);
        textPaint.setTextSize(screenSize);
        textPaint.setColor(color);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    public void setText(String text) {
        this.text = text;
    }

    public void draw(Canvas canvas) {

        if (isButton) {
            canvas.drawRect(buttonRect, buttonPaint);
        }

        canvas.drawText(text, screenX, screenY, textPaint);
    }

    public void updatePosition(int width, int height) {
        screenX = (int) (width * x);
        screenY = (int) (height * y);

        if (isButton) {
            buttonRect = new Rect((int) (width * buttonX), (int) (height * buttonY), (int) (width * buttonWidth), (int) (height * buttonHeight));
        }

    }

    public boolean contains(float x, float y) {
        return buttonRect.contains((int) x, (int) y);
    }
}
