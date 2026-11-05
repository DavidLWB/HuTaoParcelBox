package com.davlike.hutaoparcelbox;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;

/** Flat first-frame cover shown inside the platform card; never draws a nested window. */
final class PddLoadingView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Palette palette;
    private boolean animating;
    private long animationStarted;

    PddLoadingView(Context context) {
        super(context);
        palette = Palette.of(context);
        setBackgroundColor(palette.surface);
        setClickable(true);
        setContentDescription("正在加载拼多多取件信息");
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    void setAnimating(boolean value) {
        if (animating == value) return;
        animating = value;
        animationStarted = SystemClock.uptimeMillis();
        invalidate();
    }

    private void block(Canvas canvas, float x, float y, float width, float height) {
        float radius = Math.min(12f, height / 2f);
        paint.setShader(null);
        paint.setColor(palette.surfaceAlt);
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), radius, radius, paint);
        float progress = animating
            ? ((SystemClock.uptimeMillis() - animationStarted) % 1400L) / 1400f : 0f;
        float center = x - width * .4f + progress * width * 1.8f;
        paint.setShader(new LinearGradient(center - width * .35f, 0, center + width * .35f, 0,
            new int[]{0x00ffffff, palette.dark ? 0x22ffffff : 0xccffffff, 0x00ffffff},
            null, Shader.TileMode.CLAMP));
        canvas.drawRoundRect(new RectF(x, y, x + width, y + height), radius, radius, paint);
        paint.setShader(null);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float density = getResources().getDisplayMetrics().density;
        float width = getWidth() / density;
        float pad = 18f;
        float inner = Math.max(1f, width - pad * 2);
        canvas.save();
        canvas.scale(density, density);
        block(canvas, pad, 20, Math.min(140f, inner * .45f), 14);
        block(canvas, pad, 48, inner, 96);
        block(canvas, pad, 158, inner * .55f, 12);
        block(canvas, pad, 182, inner * .42f, 12);
        block(canvas, pad, 212, inner, 64);
        float noteY = 212 + 64 + 28;
        if (noteY <= getHeight() / density - 12) {
            paint.setShader(null);
            paint.setColor(palette.muted);
            paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
            paint.setTextSize(12);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("正在准备取件信息…", width / 2, noteY, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
        canvas.restore();
        if (animating && isShown() && getAlpha() > 0) postInvalidateOnAnimation();
    }
}
