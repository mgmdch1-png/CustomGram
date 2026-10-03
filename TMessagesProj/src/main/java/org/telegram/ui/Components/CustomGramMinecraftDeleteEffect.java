package org.telegram.ui.Components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroupOverlay;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.CustomGramConfig;

/**
 * Lightweight CustomGram message-removal renderer.
 *
 * The target message is snapshotted once. Animation frames only draw cached bitmap
 * tiles and simple pixel particles, so we never walk/re-layout the Telegram view tree
 * per frame. This keeps the effect independent from message type and bubble shape.
 */
public final class CustomGramMinecraftDeleteEffect extends View {

    private final RecyclerListView host;
    private final Bitmap snapshot;
    private final int originX;
    private final int originY;
    private final int style;
    private final Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint pixelPaint = new Paint();
    private final Rect src = new Rect();
    private final RectF dst = new RectF();
    private float progress;
    private ValueAnimator animator;
    private Runnable done;
    private boolean finished;

    private CustomGramMinecraftDeleteEffect(
            @NonNull RecyclerListView host,
            @NonNull Bitmap snapshot,
            int originX,
            int originY,
            int style,
            Runnable done
    ) {
        super(host.getContext());
        this.host = host;
        this.snapshot = snapshot;
        this.originX = originX;
        this.originY = originY;
        this.style = resolveStyle(style);
        this.done = done;
        setWillNotDraw(false);
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public static boolean animate(@NonNull RecyclerListView host, @NonNull View target, int style, Runnable done) {
        if (host.getWidth() <= 0 || host.getHeight() <= 0 || target.getWidth() <= 0 || target.getHeight() <= 0) {
            if (done != null) done.run();
            return false;
        }

        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(Math.max(1, target.getWidth()), Math.max(1, target.getHeight()), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            target.draw(canvas);
        } catch (Throwable e) {
            if (done != null) done.run();
            return false;
        }

        int[] targetLocation = new int[2];
        int[] hostLocation = new int[2];
        target.getLocationInWindow(targetLocation);
        host.getLocationInWindow(hostLocation);
        int left = targetLocation[0] - hostLocation[0];
        int top = targetLocation[1] - hostLocation[1];

        CustomGramMinecraftDeleteEffect effect = new CustomGramMinecraftDeleteEffect(host, bitmap, left, top, style, done);
        ViewGroupOverlay overlay = host.getOverlay();
        effect.layout(0, 0, host.getWidth(), host.getHeight());
        overlay.add(effect);
        target.setVisibility(INVISIBLE);
        effect.start();
        return true;
    }

    private static int resolveStyle(int style) {
        if (style == CustomGramConfig.DELETE_EFFECT_RANDOM) {
            return CustomGramConfig.DELETE_EFFECT_TNT + (int) (Math.abs(System.nanoTime()) % 4L);
        }
        if (style < CustomGramConfig.DELETE_EFFECT_TNT || style > CustomGramConfig.DELETE_EFFECT_FIRE) {
            return CustomGramConfig.DELETE_EFFECT_TNT;
        }
        return style;
    }

    private void start() {
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(style == CustomGramConfig.DELETE_EFFECT_TNT ? 720L : 620L);
        animator.addUpdateListener(a -> {
            progress = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) { finish(); }
            @Override public void onAnimationCancel(Animator animation) { finish(); }
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (style) {
            case CustomGramConfig.DELETE_EFFECT_PORTAL:
                drawPortal(canvas);
                break;
            case CustomGramConfig.DELETE_EFFECT_ENDERMAN:
                drawEnderman(canvas);
                break;
            case CustomGramConfig.DELETE_EFFECT_FIRE:
                drawFire(canvas);
                break;
            case CustomGramConfig.DELETE_EFFECT_TNT:
            default:
                drawTnt(canvas);
                break;
        }
    }

    private void drawTnt(Canvas canvas) {
        float fuse = Math.min(1f, progress / 0.24f);
        if (progress < 0.24f) {
            float pulse = 1f + 0.035f * (float) Math.sin(fuse * Math.PI * 6f);
            canvas.save();
            canvas.scale(pulse, pulse, originX + snapshot.getWidth() / 2f, originY + snapshot.getHeight() / 2f);
            paint.setAlpha(255);
            canvas.drawBitmap(snapshot, originX, originY, paint);
            canvas.restore();
            if (((int) (fuse * 8)) % 2 == 1) {
                pixelPaint.setColor(Color.argb(90, 255, 255, 255));
                canvas.drawRect(originX, originY, originX + snapshot.getWidth(), originY + snapshot.getHeight(), pixelPaint);
            }
            drawTntBlock(canvas, originX + snapshot.getWidth() / 2f, originY - AndroidUtilities.dp(4), fuse);
            return;
        }
        float p = (progress - 0.24f) / 0.76f;
        drawTiles(canvas, p, 1);
        drawExplosionPixels(canvas, p);
    }

    private void drawPortal(Canvas canvas) {
        drawTiles(canvas, progress, 2);
        int count = 24;
        float cx = originX + snapshot.getWidth() / 2f;
        float cy = originY + snapshot.getHeight() / 2f;
        for (int i = 0; i < count; i++) {
            float seed = hash(i, 77);
            float angle = seed * 6.28318f + progress * 7f;
            float radius = (1f - progress) * Math.max(snapshot.getWidth(), snapshot.getHeight()) * (0.15f + hash(i, 11) * 0.55f);
            float x = cx + (float) Math.cos(angle) * radius;
            float y = cy + (float) Math.sin(angle) * radius;
            int size = AndroidUtilities.dp(3 + (i % 3));
            pixelPaint.setColor((i & 1) == 0 ? Color.rgb(151, 71, 255) : Color.rgb(92, 31, 170));
            pixelPaint.setAlpha((int) (210 * (1f - progress)));
            canvas.drawRect(x, y, x + size, y + size, pixelPaint);
        }
    }

    private void drawEnderman(Canvas canvas) {
        drawTiles(canvas, progress, 3);
        float cx = originX + snapshot.getWidth() / 2f;
        float cy = originY + snapshot.getHeight() / 2f;
        for (int i = 0; i < 22; i++) {
            float x = cx + (hash(i, 21) - .5f) * snapshot.getWidth() * 1.35f;
            float y = cy + (hash(i, 31) - .5f) * snapshot.getHeight() * 1.35f - progress * AndroidUtilities.dp(34);
            int s = AndroidUtilities.dp(2 + (i % 4));
            pixelPaint.setColor((i % 3) == 0 ? Color.rgb(218, 103, 255) : Color.rgb(135, 45, 220));
            pixelPaint.setAlpha((int) (230 * (1f - progress)));
            canvas.drawRect(x, y, x + s, y + s, pixelPaint);
        }
    }

    private void drawFire(Canvas canvas) {
        drawTiles(canvas, progress, 4);
        for (int i = 0; i < 26; i++) {
            float x = originX + hash(i, 41) * snapshot.getWidth();
            float y = originY + snapshot.getHeight() * (0.35f + 0.65f * hash(i, 51)) - progress * AndroidUtilities.dp(48 + i % 20);
            int s = AndroidUtilities.dp(3 + i % 4);
            int color;
            switch (i % 3) {
                case 0: color = Color.rgb(255, 214, 67); break;
                case 1: color = Color.rgb(255, 126, 35); break;
                default: color = Color.rgb(210, 53, 30); break;
            }
            pixelPaint.setColor(color);
            pixelPaint.setAlpha((int) (230 * (1f - progress)));
            canvas.drawRect(x, y, x + s, y + s, pixelPaint);
        }
    }

    /** mode: 1 explosion, 2 portal-collapse, 3 ender-teleport, 4 burn-up. */
    private void drawTiles(Canvas canvas, float p, int mode) {
        int block = Math.max(AndroidUtilities.dp(10), 10);
        float cx = snapshot.getWidth() / 2f;
        float cy = snapshot.getHeight() / 2f;
        int alpha = Math.max(0, (int) (255 * (1f - p * 0.92f)));
        paint.setAlpha(alpha);

        for (int y = 0; y < snapshot.getHeight(); y += block) {
            for (int x = 0; x < snapshot.getWidth(); x += block) {
                int right = Math.min(snapshot.getWidth(), x + block);
                int bottom = Math.min(snapshot.getHeight(), y + block);
                float dx = 0f;
                float dy = 0f;
                float scale = 1f;
                float rx = x + (right - x) / 2f - cx;
                float ry = y + (bottom - y) / 2f - cy;
                float noise = hash(x / block, y / block);

                if (mode == 1) {
                    float len = (float) Math.sqrt(rx * rx + ry * ry) + 1f;
                    float force = AndroidUtilities.dp(74) * p * p * (0.45f + noise * 0.9f);
                    dx = rx / len * force;
                    dy = ry / len * force + AndroidUtilities.dp(18) * p * p;
                    scale = 1f - 0.45f * p;
                } else if (mode == 2) {
                    float angle = p * (2.5f + noise * 3.2f);
                    float cos = (float) Math.cos(angle);
                    float sin = (float) Math.sin(angle);
                    float shrink = 1f - p;
                    float tx = (rx * cos - ry * sin) * shrink;
                    float ty = (rx * sin + ry * cos) * shrink;
                    dx = tx - rx;
                    dy = ty - ry;
                    scale = Math.max(.12f, shrink);
                } else if (mode == 3) {
                    dx = (noise - .5f) * AndroidUtilities.dp(60) * p;
                    dy = -AndroidUtilities.dp(85) * p * (0.25f + noise);
                    if (noise < p * .8f) dx += AndroidUtilities.dp(90) * (noise > .5f ? 1 : -1);
                    scale = 1f - .25f * p;
                } else {
                    dx = (noise - .5f) * AndroidUtilities.dp(18) * p;
                    dy = -AndroidUtilities.dp(55) * p * (0.35f + noise);
                    scale = 1f - .35f * p;
                }

                src.set(x, y, right, bottom);
                float w = (right - x) * scale;
                float h = (bottom - y) * scale;
                float left = originX + x + dx + ((right - x) - w) / 2f;
                float top = originY + y + dy + ((bottom - y) - h) / 2f;
                dst.set(left, top, left + w, top + h);
                canvas.drawBitmap(snapshot, src, dst, paint);
            }
        }
    }

    private void drawExplosionPixels(Canvas canvas, float p) {
        float cx = originX + snapshot.getWidth() / 2f;
        float cy = originY + snapshot.getHeight() / 2f;
        for (int i = 0; i < 34; i++) {
            float angle = hash(i, 91) * 6.28318f;
            float radius = AndroidUtilities.dp(18 + (int) (hash(i, 92) * 85)) * p;
            float x = cx + (float) Math.cos(angle) * radius;
            float y = cy + (float) Math.sin(angle) * radius + AndroidUtilities.dp(20) * p * p;
            int size = AndroidUtilities.dp(3 + i % 5);
            int color;
            switch (i % 4) {
                case 0: color = Color.WHITE; break;
                case 1: color = Color.rgb(255, 218, 79); break;
                case 2: color = Color.rgb(255, 112, 37); break;
                default: color = Color.rgb(201, 45, 31); break;
            }
            pixelPaint.setColor(color);
            pixelPaint.setAlpha((int) (230 * (1f - p)));
            canvas.drawRect(x, y, x + size, y + size, pixelPaint);
        }
    }

    private void drawTntBlock(Canvas canvas, float cx, float bottom, float fuse) {
        int size = AndroidUtilities.dp(22);
        float l = cx - size / 2f;
        float t = bottom - size;
        pixelPaint.setAlpha(255);
        pixelPaint.setColor((int) (fuse * 8) % 2 == 0 ? Color.rgb(195, 45, 36) : Color.rgb(238, 238, 238));
        canvas.drawRect(l, t, l + size, t + size, pixelPaint);
        pixelPaint.setColor(Color.rgb(245, 235, 211));
        canvas.drawRect(l, t + size * .36f, l + size, t + size * .64f, pixelPaint);
        pixelPaint.setColor(Color.rgb(45, 30, 25));
        pixelPaint.setTextSize(AndroidUtilities.dp(8));
        pixelPaint.setFakeBoldText(true);
        pixelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("TNT", cx, t + size * .59f, pixelPaint);
        pixelPaint.setFakeBoldText(false);
        pixelPaint.setTextAlign(Paint.Align.LEFT);
    }

    private static float hash(int x, int y) {
        int n = x * 374761393 + y * 668265263;
        n = (n ^ (n >> 13)) * 1274126177;
        n ^= n >> 16;
        return (n & 0x7fffffff) / (float) 0x7fffffff;
    }

    private void finish() {
        if (finished) return;
        finished = true;
        try {
            host.getOverlay().remove(this);
        } catch (Throwable ignore) {}
        if (!snapshot.isRecycled()) snapshot.recycle();
        Runnable callback = done;
        done = null;
        if (callback != null) callback.run();
    }
}
