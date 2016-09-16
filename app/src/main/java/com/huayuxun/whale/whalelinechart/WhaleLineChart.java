package com.huayuxun.whale.whalelinechart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jinhui on 2016/8/31.
 */
public class WhaleLineChart extends View {
    private int totalPosition = 0;             //总的分数
    private int targetPosition = 0;            //指定的分数，用来确定小人运动到哪一点，矩形运动到哪一个位置停止
    private int drawRectCount = 0;             //根据指定分数计算出需要绘制的矩形个数
    private boolean isStartAnimation = false;

    private float rectFAnimatorValue = 0;       //矩形动画过程中变化的值
    private ValueAnimator rectFValueAnimator;   //矩形动画

    private Path linePath;                      //曲线的路径
    private float lastLineAnimatorValue = 0;    //上一次折线动画过程中变化的值
    private float lineAnimatorValue = 0;        //折线动画过程中变化的值
    private ValueAnimator lineValueAnimator;    //折线动画

    private RectF rectF;                        //矩形
    private float rectWidth;                    //矩形宽度
    private List<Float> rectHeightList;         //每一列矩形高度
    private float widthDistance;                //设置的横向间隔
    private float heightDistance;               //设置的纵向间隔
    private int currentRect = 0;                //当前矩形位置
    private Timer timer;
    private TimerTask task = new TimerTask(){
        public void run(){
            // 在此处添加执行的代码
            Log.e("TimerTask","start"+ currentRect++);
        }
    };
=

    //距离文本的间隔
    private float textDistance = 20;
    private float textSize;                     //文本的字体大小
    private List<String> textList;              //每一列文本的内容
    private List<Integer> textValueList;         //每一列文本数值的内容


    private float pivotX;                       //屏幕的中心X
    private float pivotY;                       //屏幕的中心Y
    private Paint rectPaint;                    //用于绘制矩形的画笔
    private Paint veilRectPaint;                //用于绘制遮幕矩形的画笔
    private Paint linePaint;                    //用于绘制折线的画笔
    private Paint textPaint;                    //用于绘制文字的画笔

    private Rect imgRect;                       //图片的大小
    private Rect imgPositionRectF;              //图片在屏幕的位置


    private Canvas lineChartCanvas;             //绑定bitmap的画布
    private Bitmap canvasBitmap;                //用一个bitmap保存画布
    private DisplayMetrics dm = getResources().getDisplayMetrics();


    private Bitmap imgBitmap;                   //卖座小人图片
    private PathMeasure imgPathMeasure;
    private float[] mCurrentPosition = new float[2];
    private float[] mTanPositon = new float[2];

    {
        textSize = 35f;
        rectWidth = 60f;
        widthDistance = 100f;
        heightDistance = 100f;
        rectHeightList = new ArrayList<Float>() {
            {
                add(160f);
                add(240f);
                add(320f);
                add(400f);
                add(480f);
            }
        };
        textList = new ArrayList<String>() {
            {
                add("V1");
                add("V2");
                add("V3");
                add("V4");
                add("V5");
            }
        };

        textValueList = new ArrayList<Integer>() {
            {
                add(100);
                add(500);
                add(1000);
                add(2000);
                add(5000);
            }
        };
    }

    public ValueAnimator getRectFValueAnimator() {
        return rectFValueAnimator;
    }

    public ValueAnimator getLineValueAnimator() {
        return lineValueAnimator;
    }

    public WhaleLineChart(Context context) {
        this(context, null);
    }

    public WhaleLineChart(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhaleLineChart(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        timer = new Timer();
        timer.schedule(task, 10);//开启定时器，delay 1s后执行task
        initPaint();
        initPath();
    }

    //初始化贝塞尔曲线路径
    private void initPath() {
        //起点
        float firstPointX = widthDistance;
        float firstPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(0));
        //终点
        float lastPointX = (rectHeightList.size() * (widthDistance + rectWidth) - widthDistance) + widthDistance;
        float lastPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(rectHeightList.size() - 1));
        //控制点
        float controlPointX = 3 * widthDistance + rectWidth * 3;
        float controlPointY = -(heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(1));

        linePath = new Path();
        linePath.moveTo(firstPointX, firstPointY);
        linePath.quadTo(controlPointX, controlPointY, lastPointX, lastPointY);
        imgPathMeasure = new PathMeasure(linePath, false);
        //计算最后一个点的坐标以及角度
        float[] lastPathX = new float[2];
        float[] lastPathTan = new float[2];
        imgPathMeasure.getPosTan(imgPathMeasure.getLength(), lastPathX, lastPathTan);
        //计算方位角
        float degrees = (float) (Math.atan2(lastPathTan[1], lastPathTan[0]) * 180.0 / Math.PI);
    }

    //初始化画笔
    private void initPaint() {
        //初始化矩形画笔
        rectPaint = new Paint();
        rectPaint.setColor(Color.YELLOW);
        rectPaint.setStrokeCap(Paint.Cap.ROUND);
        rectPaint.setStrokeWidth(3);
        rectPaint.setAntiAlias(true);
        rectPaint.setAlpha(100);
        rectPaint.setStyle(Paint.Style.FILL);

        //初始化遮幕层矩形画笔
        veilRectPaint = new Paint();
        veilRectPaint.setColor(Color.YELLOW);
        veilRectPaint.setStrokeCap(Paint.Cap.ROUND);
        veilRectPaint.setStrokeWidth(3);
        veilRectPaint.setAntiAlias(true);
        veilRectPaint.setAlpha(50);
        veilRectPaint.setStyle(Paint.Style.FILL);

        //初始化折线画笔
        linePaint = new Paint();
        linePaint.setColor(Color.CYAN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(5);
        linePaint.setAntiAlias(true);

        //初始化文本内容画笔
        textPaint = new Paint();
        textPaint.setStrokeWidth(3);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.argb(100, 182, 181, 182));
        textPaint.setTextSize(textSize);
        textPaint.setAntiAlias(true);
        mCurrentPosition = new float[2];
        imgBitmap = ((BitmapDrawable) getResources().getDrawable(R.mipmap.img, null)).getBitmap();
    }

    public WhaleLineChart(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getLineChartWidth(widthMeasureSpec), getLineChartHeight(heightMeasureSpec));
    }

    //根据给出的数据，动态设置当wrap_content时的控件宽度
    public int getLineChartWidth(int widthMeasureSpec) {
        int width = 0;
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize;
        } else {
            width = (int) (rectHeightList.size() * rectWidth + (rectHeightList.size() + 1) * widthDistance);
            if (specMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, specSize);
            }
        }
        return width;
    }

    //根据给出的数据，动态设置当wrap_content时的控件长度
    public int getLineChartHeight(int heightMeasureSpec) {
        int height = 0;
        int specSize = MeasureSpec.getSize(heightMeasureSpec);
        int specMode = MeasureSpec.getMode(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            height = specSize;
        } else {
            height = (int) (small(imgBitmap).getHeight() + heightDistance + 2 * textSize + 2 * textDistance + rectHeightList.get(rectHeightList.size() - 1));
            if (specMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, specSize);
            }
        }
        return height;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (canvasBitmap != null) {
            canvasBitmap .recycle();
        }
        pivotX = getWidth() / 2;
        pivotY = getHeight() / 2;
        lineChartCanvas = new Canvas();
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        lineChartCanvas.setBitmap(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //将画布的中心移到屏幕的中心
        canvas.save();
        canvas.translate(0, getHeight());
        //背景绘制一次就可以了
        drawBackGround(canvas);
        drawAnimation(canvas);
        canvas.restore();
    }

    //绘制背景，矩形，文字，以及曲线
    private void drawBackGround(Canvas canvas) {
        drawChartRect(canvas);
        drawChartText(canvas);
        drawChartLine(canvas);
    }

    //开始矩形以及动画
    private void drawAnimation(Canvas canvas) {
        //保存上一次小人图的状态
        Log.e("Tag", "onDraw: " + mCurrentPosition[0] + " : " + mCurrentPosition[1]);
        if (isStartAnimation) {
//            drawChartRect(canvas, rectHeightList.get(i));
            canvas.drawBitmap(small(imgBitmap), mCurrentPosition[0] - rectWidth, mCurrentPosition[1] - small(imgBitmap).getHeight(), linePaint);
        }
    }


    //图表矩形绘制
    private void drawChartRect(Canvas canvas) {

        //判空
        if (rectHeightList == null || rectHeightList.size() == 0) {
            return;
        }
        for (int i = 0; i < rectHeightList.size(); i++) {
            rectF = new RectF(i * rectWidth + (i + 1) * widthDistance, -(rectHeightList.get(i)) - textSize - textDistance, i * rectWidth + (i + 1) * widthDistance + rectWidth, -textSize - textDistance);
            //这里这么写是因为圆角矩形一开始是一条直线
            canvas.drawRoundRect(rectF, 50, 50, rectPaint);

        }
    }

    //图标文字绘制
    private void drawChartText(Canvas canvas) {
        //判空
        if (textList == null || textList.size() == 0) {
            return;
        }
        for (int i = 0; i < rectHeightList.size(); i++) {

            canvas.drawText(textList.get(i), i * rectWidth + (i + 1) * heightDistance + rectWidth / 2, 0, textPaint);
            canvas.drawText(textValueList.get(i) + "分", i * rectWidth + (i + 1) * heightDistance + rectWidth / 2, -(textSize + 2 * textDistance + rectHeightList.get(i)), textPaint);

        }
    }


    //利用贝塞尔曲线画曲线以及画小人
    private void drawChartLine(Canvas canvas) {
        canvas.drawPath(linePath, linePaint);
    }

    //动态绘制小人的属性动画
    public void startAnimation(int duration) {
        final ValueAnimator imgAnimator = ValueAnimator.ofFloat(0, imgPathMeasure.getLength());
        imgAnimator.setDuration(duration);
        imgAnimator.setInterpolator(new LinearInterpolator());
        imgAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                //获取当前点坐标封装到mCurrentPosition中
                float rate = (float) totalPosition / textValueList.get(textValueList.size() - 1);
                Log.e("rate:", "" + rate);
                if (value < rate * imgPathMeasure.getLength()) {
                    imgPathMeasure.getPosTan(value, mCurrentPosition, mTanPositon);
                    Log.e("mTanPositon0:", "" + mTanPositon[0]);
                    Log.e("mTanPositon1:", "" + mTanPositon[1]);
                    if (value == 0) {
                        postInvalidateDelayed(10000);
                    } else {
                        invalidate();
                    }
                } else {
                    imgAnimator.cancel();
                }
            }
        });
        rectFValueAnimator = ValueAnimator.ofFloat(0, totalPosition);
        rectFValueAnimator.setInterpolator(new LinearInterpolator());
        rectFValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rectFAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        imgAnimator.start();
    }


    private void startRectFAnimation(int i) {
        rectFValueAnimator = rectFValueAnimator.ofFloat(0, rectHeightList.get(i)).setDuration(500);
        rectFValueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rectFValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rectFAnimatorValue = (int) animation.getAnimatedValue();
//                    if (rectFAnimatorValue<){
//
//                    }else if(){
//
//                    }else if(){
//
//                    }
                invalidate();
            }
        });

        rectFValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                rectFAnimatorValue = 0;
                startRectFAnimation(++currentRect);
            }
        });
        //开始动画
        rectFValueAnimator.start();
    }

    //操作bitmap缩放的方法
    private static Bitmap small(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.35f, 0.35f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    //对外接口：设置分数
    public void setScore(int score) {
        isStartAnimation = true;
        totalPosition = score;
        for (int i = 0; i < textValueList.size(); i++) {
            targetPosition = score - textValueList.get(i);
            if (score < textValueList.get(i)) {
                drawRectCount = i;
                Log.e("targetPosition:", "" + targetPosition);
                break;
            }
        }
        startAnimation(6000);
    }
}