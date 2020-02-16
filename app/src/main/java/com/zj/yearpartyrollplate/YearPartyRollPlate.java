package com.zj.yearpartyrollplate;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class YearPartyRollPlate extends View {
    private String btnTittle;
    private float innerRadius;
    private float outerRadius;
    private List<Paint> paintList;
    private List<String> prizeList;
    private Paint innerPaint;
    private Paint outPaint;
    private Paint textPaint;
    private int itemCount = 4;
    private int endDegree;
    private float animValue;
    private PrizeListener prizeListener;
    private RectF mRectF;

    public YearPartyRollPlate(Context context) {
        this(context, null);
    }

    public YearPartyRollPlate(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public YearPartyRollPlate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.YearPartyRollPlate);
        btnTittle = typedArray.getString(R.styleable.YearPartyRollPlate_btnTittle) != null ? typedArray.getString(R.styleable.YearPartyRollPlate_btnTittle) : "抽奖";
        innerRadius = typedArray.getDimension(R.styleable.YearPartyRollPlate_innerRadius, -1);
        itemCount = typedArray.getInt(R.styleable.YearPartyRollPlate_itemCount, 4);
        int outerCircleColor = typedArray.getColor(R.styleable.YearPartyRollPlate_outerCircleColor, ContextCompat.getColor(context, R.color.chocolate));
        int innerCircleColor = typedArray.getColor(R.styleable.YearPartyRollPlate_innerCircleColor, ContextCompat.getColor(context, R.color.sandybrown));
        float textSize = typedArray.getDimension(R.styleable.YearPartyRollPlate_textSize, 40);
        typedArray.recycle();

        paintList = new ArrayList<>();
        prizeList = new ArrayList<>();

        innerPaint = new Paint();
        innerPaint.setColor(innerCircleColor);

        outPaint = new Paint();
        outPaint.setColor(outerCircleColor);

        textPaint = new Paint();
        textPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        textPaint.setStrokeWidth(5);
        textPaint.setTextAlign(Paint.Align.CENTER);

        float textWidth = textPaint.measureText(this.btnTittle);

        if (innerRadius < DensityUtil.px2dp(textWidth) / 2) {
            innerRadius = textWidth;
        }

        initPaint(itemCount);
        setClickable(true);
    }

    private void initPaint(int count) {
        if (paintList.size() > 0) {
            paintList.clear();
        }
        for (int i = 0; i < count; i++) {
            Paint paint = new Paint();
            paint.setColor(getRandomColor());
            paintList.add(paint);
            prizeList.add("抽奖" + i);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultSize = DensityUtil.dp2px(200);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int validSize = (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) ? Math.min(widthSize, heightSize) : defaultSize;
        outerRadius = validSize / 2;
        if (outerRadius < innerRadius) {
            throw new IllegalArgumentException("外圆半径不能小于内圆半径，内圆半径默认100dp，建议修改文字大小或内圆半径");
        }
        setMeasuredDimension(validSize, validSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private Path triPath;
    float startAngle;
    float sweepAngle;
    float currentStartAngle;
    float lineAngle;
    float lineEndX;
    float lineEndY;
    int middleWidth = -1;
    int middleHeight = -1;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (middleWidth == -1) {
            middleWidth = getMeasuredWidth() / 2;
        }
        if (middleHeight == -1) {
            middleHeight = getMeasuredHeight() / 2;
        }
        if (mRectF == null) {
            mRectF = new RectF(middleWidth - outerRadius, middleHeight - outerRadius, middleWidth + outerRadius, middleHeight + outerRadius);
        }
        if (triPath == null) {
            triPath = initTriAngle(middleWidth, middleHeight);
        }

        canvas.drawCircle(middleWidth, middleHeight, outerRadius, outPaint);
        startAngle = 0;
        sweepAngle = (float) (360.0 / itemCount);

        for (int i = 0; i < itemCount; i++) {
            startAngle %= 360;
            currentStartAngle = startAngle + animValue * endDegree;
            canvas.drawArc(mRectF, currentStartAngle, sweepAngle, true, paintList.get(i));

            lineAngle = currentStartAngle + sweepAngle / 2;
            lineEndX = (float) (middleWidth + (outerRadius / 1.5) * Math.cos(lineAngle / 180 * Math.PI));
            lineEndY = (float) (middleHeight + (outerRadius / 1.5) * Math.sin(lineAngle / 180 * Math.PI));
            canvas.drawText(prizeList.get(i), lineEndX, lineEndY, textPaint);
            //动画结束
            if (animValue == 1) {
                if (currentStartAngle % 360 < 270 && currentStartAngle % 360 + sweepAngle > 270) {
                    if (prizeListener != null) {
                        prizeListener.getPrize(prizeList.get(i));
                    }
                }
            }
            startAngle += sweepAngle;

        }

        canvas.drawPath(triPath, innerPaint);
        canvas.drawCircle(middleWidth, middleHeight, innerRadius, innerPaint);
        canvas.drawText(btnTittle, middleWidth, middleHeight + innerRadius / 4, textPaint);
    }

    private Path initTriAngle(int middleWidth, int middleHeight) {
        Point leftPoint = new Point();
        Point rightPoint = new Point();
        Point topPoint = new Point();
        leftPoint.x = (int) (middleWidth - innerRadius / 1.35);
        leftPoint.y = middleHeight;
        rightPoint.x = (int) (middleWidth + innerRadius / 1.35);
        rightPoint.y = middleHeight;
        topPoint.x = middleWidth;
        topPoint.y = (int) ((middleHeight - innerRadius) / 2);
        Path triPath = new Path();
        triPath.moveTo(leftPoint.x, leftPoint.y);
        triPath.lineTo(topPoint.x, topPoint.y);
        triPath.lineTo(rightPoint.x, rightPoint.y);
        triPath.close();
        return triPath;
    }

    private ValueAnimator animator;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_UP:
                int eachCount = 360 / itemCount;
                //避免指针正好在两个扇形区之间
                do {
                    endDegree = (int) (360 * 20f + Math.random() * 2 * 360);
                } while (endDegree % eachCount == 0);

                int duration = (int) (10 * 1000 + Math.ceil(Math.random() * 5) * 1000);
                animator = ValueAnimator.ofFloat(0f, endDegree);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.setDuration(duration);
                animator.setInterpolator(new Interpolator() {
                    @Override
                    public float getInterpolation(float x) {
                        double factor = 1.4;
                        animValue = (float) (1.0 - Math.pow((1.0 - x), 2 * factor));
                        postInvalidate();
                        return animValue;
                    }
                });
                animator.start();
                break;
            default:
        }
        return super.onTouchEvent(event);
    }

    public void setPrizeList(List<String> prizeList) {
        this.prizeList = prizeList;
        itemCount = prizeList.size();
        initPaint(itemCount);
    }

    public int getRandomColor() {
        Random random = new Random();
        int temp;
        int r = 0;
        int g = 0;
        int b = 0;
        for (int i = 0; i < 2; i++) {
            temp = random.nextInt(16);
            r = r * 16 + temp;
            temp = random.nextInt(16);
            g = g * 16 + temp;
            temp = random.nextInt(16);
            b = b * 16 + temp;
        }
        return Color.rgb(r, g, b);
    }

    public interface PrizeListener {
        void getPrize(String str);
    }

    public void setPrizeListener(PrizeListener prizeListener) {
        this.prizeListener = prizeListener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
