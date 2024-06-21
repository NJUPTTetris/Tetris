package com.example.tetris;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
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
    private MediaPlayer bgMediaPlayer; // 背景音乐的 MediaPlayer
    private MediaPlayer soundEffectPlayer; // 短暂音效的 MediaPlayer
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initData();
        newBoxes();
        initView();
        initListener();
        playBackgroundMusic();

    }

    public void newBoxes() {//新的方块
        Random random = new Random();
        boxType = random.nextInt(TUBE);
        boxPaint = new Paint();//初始化方块画笔
        switch (boxType) {
            case 0://粉碎男孩 Smashboy
                boxPaint.setColor(ContextCompat.getColor(this, R.color.red));
                boxes = new Point[]{new Point(4, 0), new Point(5, 0), new Point(4, 1), new Point(5, 1)};
                break;
            case 1://橘色瑞克 Orange Ricky
                boxPaint.setColor(ContextCompat.getColor(this, R.color.orange));
                boxes = new Point[]{new Point(4, 1), new Point(5, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 2://蓝色瑞克 Blue Ricky
                boxPaint.setColor(ContextCompat.getColor(this, R.color.blue));
                boxes = new Point[]{new Point(4, 1), new Point(3, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 3://小T Teewee
                boxPaint.setColor(ContextCompat.getColor(this, R.color.green));
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(3, 1), new Point(5, 1)};
                break;
            case 4://英雄 Hero
                boxPaint.setColor(ContextCompat.getColor(this, R.color.cyan));
                boxes = new Point[]{new Point(4, 0), new Point(3, 0), new Point(5, 0), new Point(6, 0)};
                break;
            case 5://罗德岛Z Rhode Island Z
                boxPaint.setColor(ContextCompat.getColor(this, R.color.yellow));
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(5, 0), new Point(3, 1)};
                break;
            case 6://克里夫蘭Z Cleveland Z
                boxPaint.setColor(ContextCompat.getColor(this, R.color.purple));
                boxes = new Point[]{new Point(4, 1), new Point(4, 0), new Point(3, 0), new Point(5, 1)};
                break;
        }
    }

    public void initListener() {//初始化监听
        findViewById(R.id.arrow_left).setOnClickListener(v -> {
            Animation(v); // 调用封装的动画函数
            playSound(R.raw.sound_change);// 调用播放音效的函数
            move(-1, 0);
        });
        findViewById(R.id.arrow_right).setOnClickListener(v -> {
            Animation(v);
            playSound(R.raw.sound_change);
            move(1, 0);
        });
        findViewById(R.id.arrow_rotate).setOnClickListener(v -> {
            Animation(v);
            playSound(R.raw.sound_change);
            rotate();
        });
        findViewById(R.id.arrow_down).setOnClickListener(v -> {
            Animation(v);
            playSound(R.raw.sound_change);
            while (moveBottom()) {
                continue;
            }//优化加速
        });
        findViewById(R.id.btn_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recreate();
            }
        });
        final Button btnStop = findViewById(R.id.btn_stop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
                    stopBackgroundMusic();
                    btnStop.setText("Continue");
                } else {
                    playBackgroundMusic();
                    btnStop.setText("Stop");
                }
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
        return (x < 0 || y < 0 || x >= maps.length || y >= maps[0].length || maps[x][y]);
    }

    public void rotate() {//顺时针旋转90
        if (boxType == 0) return;
        for (Point box : boxes) {//笛卡尔旋转公式
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
        linePaint.setColor(0xffb4b779);
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

    private void playSound(int soundResId) {
        // 播放音效的方法
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release(); // 释放之前的 MediaPlayer 资源
        }
        soundEffectPlayer = MediaPlayer.create(this, soundResId); // 指定要播放的音效文件
        soundEffectPlayer.start(); // 开始播放音效
    }

    // 播放背景音乐，循环播放
    private void playBackgroundMusic() {
        if (bgMediaPlayer == null) {
            bgMediaPlayer = MediaPlayer.create(this, R.raw.backgroud_music);
            bgMediaPlayer.setLooping(true); // 设置背景音乐循环播放
            bgMediaPlayer.start();
        }
    }

    // 停止背景音乐播放
    private void stopBackgroundMusic() {
        if (bgMediaPlayer != null) {
            bgMediaPlayer.stop();
            bgMediaPlayer.release();
            bgMediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 停止背景音乐播放
        stopBackgroundMusic();

        // 停止并释放短暂音效的 MediaPlayer
        if (soundEffectPlayer != null) {
            soundEffectPlayer.release();
            soundEffectPlayer = null;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_A:
                View leftButton = findViewById(R.id.arrow_left);
                if (leftButton != null) {
                    leftButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_D:
                View rightButton = findViewById(R.id.arrow_right);
                if (rightButton != null) {
                    rightButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_W:
                View rotateButton = findViewById(R.id.arrow_rotate);
                if (rotateButton != null) {
                    rotateButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_S:
                View downButton = findViewById(R.id.arrow_down);
                if (downButton != null) {
                    downButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
        }
        return super.onKeyDown(keyCode, event);
    }
}