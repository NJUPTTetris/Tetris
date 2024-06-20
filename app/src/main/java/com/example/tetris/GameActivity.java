package com.example.tetris;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    int xWidth, xHeight;//游戏区域宽度高度
    View view;//游戏区域控件
    Paint mapPaint;//地图画笔
    Paint linePaint;//初始化辅助线画笔
    Paint boxPaint;//初始化方块画笔
    boolean[][] maps;//地图
    Point[] boxes;//方块
    int boxSize;//方块大小
    final int TUBE = 7;//方块种类
    int boxType;//选择方块类型

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initData();
        newBoxes();
        initView();
        initListener();
    }

    public void newBoxes() {//新的方块
        Random random = new Random();
        boxType = random.nextInt(TUBE);
        boxPaint = new Paint();//初始化方块画笔
        switch (boxType) {
            case 0:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.red));
                boxes = new Point[]{new Point(4, 0), new Point(5, 0), new Point(4, 1), new Point(5, 1)};
                break;
            case 1:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.orange));
                boxes = new Point[]{new Point(4, 1), new Point(5, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 2:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.yellow));
                boxes = new Point[]{new Point(4, 1), new Point(3, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 3:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.green));
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 4:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.cyan));
                boxes = new Point[]{new Point(4, 0), new Point(3, 0), new Point(5, 0), new Point(6, 0)};
                break;
            case 5:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.blue));
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(5, 1), new Point(5, 2)};
                break;
            case 6:
                boxPaint.setColor(ContextCompat.getColor(this, R.color.purple));
                boxes = new Point[]{new Point(5, 1), new Point(5, 0), new Point(4, 1), new Point(4, 2)};
                break;
        }
    }


    public void initListener() {//初始化监听
        findViewById(R.id.arrow_left).setOnClickListener(v -> {
            Animation(v); // 调用封装的动画函数
            move(-1, 0);
        });
        findViewById(R.id.arrow_right).setOnClickListener(v -> {
            Animation(v);
            move(1, 0);
        });
        findViewById(R.id.arrow_rotate).setOnClickListener(v -> {
            Animation(v);
            rotate();
        });
        findViewById(R.id.arrow_down).setOnClickListener(v -> {
            Animation(v);
            while(moveBottom()){
                continue;
            }//优化加速
        });
        findViewById(R.id.btn_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public boolean moveBottom() {//下落
        if (move(0, 1))//移动成功不作处理
            return true;
        for (Point box : boxes)//移动失败堆积处理
            maps[box.x][box.y] = true;
        newBoxes();//生成新的方块
        view.invalidate();//调用重绘
        return false;
    }

    public boolean move(int x, int y) {//移动
        for (Point box : boxes) {
            if (checkBoundary(box.x + x, box.y + y)) return false;
        }
        for (Point box : boxes) {//遍历方块数组
            box.x += x;
            box.y += y;
        }
        view.invalidate();//调用重绘
        return true;
    }

    public boolean checkBoundary(int x, int y) {//边界判断
        return (x < 0 || y < 0 || x >= maps.length || y >= maps[0].length|| maps[x][y]);
    }

    public void rotate() {//顺时针旋转90
        if (boxType == 0) return;
        for (Point box : boxes) {
            int checkX = -box.y + boxes[0].y + boxes[0].x;
            int checkY = box.x - boxes[0].x + boxes[0].y;
            if (checkBoundary(checkX, checkY)) return;
        }

        for (Point box : boxes) {//遍历方块数组
            int checkX = -box.y + boxes[0].y + boxes[0].x;
            int checkY = box.x - boxes[0].x + boxes[0].y;
            box.x = checkX;
            box.y = checkY;
        }
        view.invalidate();//调用重绘
    }

    public void initData() {
        xWidth = Math.round(160f * getResources().getDisplayMetrics().density);//获取游戏区域宽度
        xHeight = xWidth * 3;
        maps = new boolean[10][30];//初始化地图
        boxSize = xWidth / maps.length;
    }

    public void initView() {//初始化视图

        linePaint = new Paint();//初始化线条画笔
        linePaint.setColor(0xff666666);
        linePaint.setStrokeWidth(3);
        linePaint.setAntiAlias(true);

        mapPaint = new Paint();
        mapPaint.setColor(0x50000000);
        mapPaint.setAntiAlias(true);

        FrameLayout layoutGame = findViewById(R.id.layoutGame);//得到父容器
        view = new View(this) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {//重写游戏区域绘制
                super.onDraw(canvas);
                for (int x = 0; x < maps.length; x++)//地图辅助线
                    canvas.drawLine(x * boxSize, 0, x * boxSize, view.getHeight(), linePaint);

                for (int y = 0; y < maps[0].length; y++)
                    canvas.drawLine(0, y * boxSize, view.getWidth(), y * boxSize, linePaint);

                for (Point box : boxes) {//方块绘制
                    canvas.drawRect(box.x * boxSize, box.y * boxSize, box.x * boxSize + boxSize, box.y * boxSize + boxSize, boxPaint);
                }
                for (int x = 0; x < maps.length; x++) {
                    for (int y = 0; y < maps[x].length; y++) {//绘制地图
                        if (maps[x][y])
                            canvas.drawRect(x * boxSize, y * boxSize,
                                    x * boxSize + boxSize, y * boxSize + boxSize, mapPaint);
                    }
                }

            }
        };
        view.setLayoutParams(new FrameLayout.LayoutParams(xWidth, xHeight));
        layoutGame.addView(view);//添加进父容器
    }

    private void Animation(final View v) {
        // 创建一个按钮缩小的属性动画，从1倍到0.8倍大小
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.8f);

        // 设置动画持续时间，单位毫秒
        scaleXAnimator.setDuration(100); // 缩小动画时间 0.25秒
        scaleYAnimator.setDuration(100); // 缩小动画时间 0.25秒

        // 添加一个监听器，在缩小动画完成后启动放大动画
        scaleXAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 创建一个按钮放大的属性动画，从0.8倍到1倍大小
                ObjectAnimator scaleXAnimatorReverse = ObjectAnimator.ofFloat(v, "scaleX", 0.8f, 1f);
                ObjectAnimator scaleYAnimatorReverse = ObjectAnimator.ofFloat(v, "scaleY", 0.8f, 1f);

                // 设置动画持续时间，单位毫秒
                scaleXAnimatorReverse.setDuration(100); // 放大动画时间 0.25秒
                scaleYAnimatorReverse.setDuration(100); // 放大动画时间 0.25秒

                // 同时播放X和Y方向的放大动画
                scaleXAnimatorReverse.start();
                scaleYAnimatorReverse.start();
            }
        });

        // 启动缩小动画
        scaleXAnimator.start();
        scaleYAnimator.start();
    }
}
