package com.example.tetris;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    int xWidth, xHeight;//游戏区域宽度高度
    View view;//游戏区域控件
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
        switch (boxType) {
            case 0:
                boxes = new Point[]{new Point(4, 0), new Point(5, 0), new Point(4, 1), new Point(5, 1)};
                break;
            case 1:
                boxes = new Point[]{new Point(4, 1), new Point(5, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 2:
                boxes = new Point[]{new Point(4, 1), new Point(3, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 3:
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 4:
                boxes = new Point[]{new Point(4, 0), new Point(3, 0), new Point(5, 0), new Point(6, 0)};
                break;
            case 5:
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(5, 1), new Point(5, 2)};
                break;
            case 6:
                boxes = new Point[]{new Point(5, 1), new Point(5, 0), new Point(4, 1), new Point(4, 2)};
                break;
        }
    }

    public void initListener() {//初始化监听
        findViewById(R.id.arrow_left).setOnClickListener(v -> move(-1, 0));
        findViewById(R.id.arrow_right).setOnClickListener(v -> move(1, 0));
        findViewById(R.id.arrow_rotate).setOnClickListener(v -> rotate());
        findViewById(R.id.arrow_down).setOnClickListener(v -> move(0, 1));
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
        return (x < 0 || y < 0 || x >= maps.length || y >= maps[0].length);
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

        boxPaint = new Paint();//初始化方块画笔
        boxPaint.setColor(0xff000000);

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
            }
        };
        view.setLayoutParams(new FrameLayout.LayoutParams(xWidth, xHeight));
        layoutGame.addView(view);//添加进父容器
    }
}