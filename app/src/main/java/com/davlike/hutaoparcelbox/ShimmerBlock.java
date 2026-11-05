package com.davlike.hutaoparcelbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.View;

/** Presentation-only loading placeholder; never draws a real or mock barcode. */
final class ShimmerBlock extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private final Matrix translation = new Matrix();
    private final Palette palette;
    private final float radius;
    private LinearGradient highlight;
    private float phase, band;

    ShimmerBlock(Context context, float radius) {
        super(context);
        palette = Palette.of(context);
        this.radius = radius;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setPhase(float value) { phase = value; invalidate(); }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        bounds.set(0, 0, width, height);
        band = Math.max(width * 0.45f, 32 * getResources().getDisplayMetrics().density);
        highlight = new LinearGradient(-band, 0, band, Math.max(1, height),
            new int[]{0x00ffffff, palette.dark ? 0x22ffffff : 0xccffffff, 0x00ffffff},
            new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setShader(null);
        paint.setColor(palette.surfaceAlt);
        canvas.drawRoundRect(bounds, radius, radius, paint);
        if (highlight != null) {
            translation.setTranslate((getWidth() + 2 * band) * phase - band, 0);
            highlight.setLocalMatrix(translation);
            paint.setShader(highlight);
            canvas.drawRoundRect(bounds, radius, radius, paint);
        }
    }
}
