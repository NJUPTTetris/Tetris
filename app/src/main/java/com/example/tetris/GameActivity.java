package com.example.tetris;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GameActivity extends AppCompatActivity {
    int speed;//速度
    boolean isSwitchOnMusic;//是否有音乐
    boolean isSwitchOnSoundEffect;//是否有音效
    int xWidth, xHeight;//游戏区域宽度高度
    View view;//游戏区域控件
    Paint mapPaint;//地图画笔
    Paint linePaint;//初始化辅助线画笔
    Paint boxPaint;//初始化方块画笔
    int[][] maps;//地图
    Point[] boxes;//方块
    Point[] nextboxes; //方块
    int strokeWidth = 2;
    int boxSize;//方块大小
    final int TUBE = 8;//方块种类
    int boxType;//选择方块类型
    int nextboxType;//选择方块类型
    // 新增一个变量来存储下一个方块的类型
    int boxColor;
    //当前分数
    public int score;
    //最高分
    public int scoremax;
    //游戏结束状态
    public boolean isOver;
    private TextView txtCurrentScore;
    private TextView txtHighScore;
    private boolean isPaused = false; // 控制游戏是否暂停
    private Handler autoMoveHandler = new Handler(); // 用于自动下落的Handler
    private MediaPlayer bgMediaPlayer; // 背景音乐的 MediaPlayer
    private MediaPlayer soundEffectPlayer; // 短暂音效的 MediaPlayer
    int[] boxColors = {0, Color.parseColor("#C62828"), // 红色
            Color.parseColor("#D84315"), // 橙色
            Color.parseColor("#F9A825"), // 黄色
            Color.parseColor("#558B2F"), // 绿色
            Color.parseColor("#00695C"), // 青色
            Color.parseColor("#6990D5"), // 蓝色
            Color.parseColor("#6A1B9A")  // 紫色
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        speed = getIntent().getIntExtra("speed", 0);
        isSwitchOnMusic = getIntent().getBooleanExtra("isSwitchOnMusic", true);//传入是否需要音乐
        isSwitchOnSoundEffect = getIntent().getBooleanExtra("isSwitchOnSoundEffect", true);//传入是否需要音效

        // 获取当前分数的 TextView 并设置初始分数
        txtCurrentScore = findViewById(R.id.current_score);
        txtCurrentScore.setText(String.valueOf(score)); // 设置初始分数
        txtHighScore = findViewById(R.id.high_score); // 初始化最高分的 TextView

        // 初始化分数和最高分
        score = 0;
        scoremax = getHighScoreFromPrefs(); // 从 SharedPreferences 加载最高分
        updateScoreDisplay();
        updateHighScoreDisplay();

        initData();//初始数据
        initView();//基本视图实现
        newBoxes();//新方块生成
        initListener();
        // 确保游戏开始时自动下落
        onResume();
        playBackgroundMusic();//背景音乐
    }

    //自动下降
    private Runnable autoMoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isPaused && moveBottom()) {
                autoMoveHandler.postDelayed(this, 500 - speed * 50L); // 500毫秒后再次执行，可以根据需要调整时间间隔
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!isPaused) {
            autoMoveHandler.post(autoMoveRunnable); // 开始自动下落
        }
    }

    //暂停游戏
    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true; // 暂停游戏
        autoMoveHandler.removeCallbacks(autoMoveRunnable); // 移除自动下落的Runnable
    }

    public void newBoxes() { //生成一个新的随机方块
        if (nextboxes == null) {
            nextBoxes();
        }
        boxes = nextboxes;
        boxType = nextboxType;
        nextBoxes();
        // 获取ImageView并设置新的Bitmap
        ImageView nextBlockView = findViewById(R.id.next_image);
        Bitmap nextBlockBitmap = drawNextBlock(nextboxes);
        nextBlockView.setImageBitmap(nextBlockBitmap);
    }

    public void nextBoxes() {
        Random random = new Random();
        nextboxType = random.nextInt(TUBE - 1) + 1; // 生成 1 到 7 之间的随机数

        switch (nextboxType) {
            case 1://粉碎男孩 Smashboy
                nextboxes = new Point[]{new Point(4, 1), new Point(5, 1), new Point(4, 2), new Point(5, 2)};
                break;
            case 2://橘色瑞克 Orange Ricky
                nextboxes = new Point[]{new Point(5, 2), new Point(6, 1), new Point(4, 2), new Point(6, 2)};
                break;
            case 3://罗德岛Z Rhode Island Z
                nextboxes = new Point[]{new Point(5, 2), new Point(5, 1), new Point(6, 1), new Point(4, 2)};
                break;
            case 4://小T Teewee
                nextboxes = new Point[]{new Point(5, 2), new Point(5, 1), new Point(4, 2), new Point(6, 2)};
                break;
            case 5://英雄 Hero
                nextboxes = new Point[]{new Point(4, 1), new Point(3, 1), new Point(5, 1), new Point(6, 1)};
                break;
            case 6://蓝色瑞克 Blue Ricky
                nextboxes = new Point[]{new Point(5, 2), new Point(4, 1), new Point(4, 2), new Point(6, 2)};
                break;
            case 7://克里夫蘭Z Cleveland Z
                nextboxes = new Point[]{new Point(5, 2), new Point(5, 1), new Point(4, 1), new Point(6, 2)};
                break;
        }
        boxPaint.setColor(boxColors[boxType]);
        boxColor = boxColors[nextboxType];
    }

    private Bitmap drawNextBlock(Point[] nextboxes) {
        // 创建一个与ImageView相同大小的Bitmap
        Bitmap bitmap = Bitmap.createBitmap((int) (80 * getResources().getDisplayMetrics().density), (int) (80 * getResources().getDisplayMetrics().density), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        // 设置方块颜色
        Paint nextboxPaint = new Paint();
        // 使用存储的颜色
        nextboxPaint.setColor(boxColor);
        // 计算方块大小
        int boxSize = (int) (20 * getResources().getDisplayMetrics().density);
        // 在Canvas上绘制方块
        for (Point box : nextboxes) {
            canvas.drawRect((box.x - 3) * boxSize + (float) strokeWidth / 2,
                    (box.y - 1) * boxSize + (float) strokeWidth / 2,
                    (box.x - 2) * boxSize - (float) strokeWidth / 2,
                    box.y * boxSize - (float) strokeWidth / 2, nextboxPaint);
        }
        return bitmap;
    }

    public void initListener() {//初始化监听
        findViewById(R.id.arrow_left).setOnClickListener(v -> {
            if (isPaused)
                return;//如果暂停，禁用
            Animation(v); // 调用封装的动画函数
            playSound(R.raw.sound_change);// 调用播放音效的函数
            move(-1, 0);
        });
        findViewById(R.id.arrow_right).setOnClickListener(v -> {
            if (isPaused)
                return;
            Animation(v);
            playSound(R.raw.sound_change);
            move(1, 0);
        });
        findViewById(R.id.arrow_rotate).setOnClickListener(v -> {
            if (isPaused)
                return;
            Animation(v);
            playSound(R.raw.sound_change);
            rotate();
        });
        findViewById(R.id.arrow_little_down).setOnClickListener(v -> {
            if (isPaused)
                return;
            Animation(v);
            playSound(R.raw.sound_change);
            move(0,2);
        });//快速下降两格
        findViewById(R.id.arrow_quick_down).setOnClickListener(v -> {
            if (isPaused)
                return;
            Animation(v);
            playSound(R.raw.sound_down);
            while (moveBottom()) {
                continue;
            }//快速下降到底
        });

        findViewById(R.id.btn_restart).setOnClickListener(v -> recreate());
        final Button btnStop = findViewById(R.id.btn_stop);
        findViewById(R.id.btn_stop).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                if (bgMediaPlayer != null && bgMediaPlayer.isPlaying()) {
                    stopBackgroundMusic();
                    btnStop.setText("Continue");
                } else {
                    playBackgroundMusic();
                    btnStop.setText("Stop");
                }
                isPaused = !isPaused; // 切换暂停状态
                if (isPaused) {
                    autoMoveHandler.removeCallbacks(autoMoveRunnable); // 移除自动下落的Runnable
                } else {
                    autoMoveHandler.post(autoMoveRunnable); // 重新开始自动下落
                }
            }
        });
    }

    //下落
    public boolean moveBottom() {
        //1.移动成功 不作处理
        if (move(0, 1)) return true;
        //2.移动失败 堆积处理
        for (Point box : boxes)
            maps[box.x][box.y] = boxType;
        //3.消行处理
        int lines = clearLine();
        if (lines >= 2)//消多行，音效增加欢呼声
            playSound(R.raw.sound_eliminate_lot);
        if (lines == 1)//消单行音效
            playSound(R.raw.sound_eliminate);
        //4.加分
        addScore(lines);
        //5.生成新的方块
        newBoxes();
        //6.游戏结束判断，调用重绘
        view.invalidate();
        isOver = checkOver();
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
        return (x < 0 || y < 0 || x >= maps.length || y >= maps[0].length || maps[x][y] != 0);
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
        maps = new int[10][30];//初始化地图
        boxSize = xWidth / maps.length;
    }

    public void initView() {//初始化视图
        linePaint = new Paint();//初始化线条画笔
        linePaint.setColor(0x80808000);
        linePaint.setStrokeWidth(strokeWidth);
        linePaint.setAntiAlias(true);

        mapPaint = new Paint();
        mapPaint.setColor(0x50000000);
        mapPaint.setAntiAlias(true);

        boxPaint = new Paint();//初始化方块画笔
        boxPaint.setColor(0xff000000);

        FrameLayout layoutGame = findViewById(R.id.layoutGame);//得到父容器
        view = new View(this) {
            @Override
            protected void onDraw(@NonNull Canvas canvas) {//重写游戏区域绘制
                super.onDraw(canvas);
                //地图辅助线
                for (int x = 0; x <= maps.length; x++)
                    canvas.drawLine(x * boxSize, 0, x * boxSize, view.getHeight(), linePaint);
                for (int y = 0; y <= maps[0].length; y++)
                    canvas.drawLine(0, y * boxSize, view.getWidth(), y * boxSize, linePaint);
                //方块绘制
                for (Point box : boxes) {
                    canvas.drawRect(box.x * boxSize + (float) strokeWidth / 2, // 稍微偏移以避免与边框重叠
                            box.y * boxSize + (float) strokeWidth / 2,
                            box.x * boxSize + boxSize - (float) strokeWidth / 2,
                            box.y * boxSize + boxSize - (float) strokeWidth / 2,
                            boxPaint);
                }
                for (int x = 0; x < maps.length; x++) {
                    for (int y = 0; y < maps[x].length; y++) {
                        int blockType = maps[x][y]; // 获取当前坐标处的方块类型编号
                        if (blockType > 0 && blockType < 8) {
                            // 设置填充颜色
                            mapPaint.setColor(boxColors[blockType]);
                            // 绘制填充
                            canvas.drawRect(x * boxSize + (float) strokeWidth / 2, // 稍微偏移以避免与边框重叠
                                    y * boxSize + (float) strokeWidth / 2,
                                    x * boxSize + boxSize - (float) strokeWidth / 2,
                                    y * boxSize + boxSize - (float) strokeWidth / 2,
                                    mapPaint);
                        }
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
        if (isSwitchOnSoundEffect) {
            // 播放音效的方法
            if (soundEffectPlayer != null) {
                soundEffectPlayer.release(); // 释放之前的 MediaPlayer 资源
            }
            soundEffectPlayer = MediaPlayer.create(this, soundResId); // 指定要播放的音效文件
            soundEffectPlayer.start(); // 开始播放音效
        }
    }

    // 播放背景音乐，循环播放
    private void playBackgroundMusic() {
        if (bgMediaPlayer == null && isSwitchOnMusic) {
            bgMediaPlayer = MediaPlayer.create(this, R.raw.backgroud_music);
            bgMediaPlayer.setLooping(true); // 设置背景音乐循环播放
            bgMediaPlayer.start();
        }
    }

    //消行处理
    public int clearLine() {
        int lines = 0;
        //从下到上循环遍历maps数组，从最后一行开始遍历
        for (int y = maps[0].length - 1; y > 0; y--) {
            // 消行判断
            if (checkLine(y)) {
                deleteLine(y);
                //从消掉的那一行重新遍历
                y++;
                //增加消行计数
                lines++;
            }
        }
        return lines;
    }

    public boolean checkLine(int y) {
        //有一个不为1-7,则该行不能消除
        //遍历maps数组，每个数组代表一行的方块状态
        for (int[] map : maps) {
            //map[y]为空
            if (map[y] == 0)
                return false;
        }
        return true;
    }

    //执行消行
    public void deleteLine(int dy) {
        //从消去的行开始，向上遍历每一行
        for (int y = dy; y > 0; y--) {
            try {
                Thread.sleep(10); // 延时10毫秒
                //调用Thread.sleep()期间线程被中断时抛出的异常
            } catch (InterruptedException e) {
                //打印异常的堆栈跟踪信息到标准错误流
                e.printStackTrace();
            }
            //对于每一行，从左到右遍历每一列
            for (int x = 0; x < maps.length; x++) {
                //将当前行的每个方块状态复制到它上面的行，实现行的下移
                maps[x][y] = maps[x][y - 1];
            }
        }
    }

    //加分
    public void addScore(int lines) {
        if (lines == 0) return;
        int add = lines + (lines - 1);
        score += add;
        updateScoreDisplay();
        // 检查并更新最高分
        updateScoreMax();
    }

    //    游戏结束判断
    public boolean checkOver() {
        for (Point box : boxes)
            // 检查方块是否与地图上的其他方块重叠
            if (maps[box.x][box.y] > 0 && maps[box.x][box.y] < 8) {
                View StopButton = findViewById(R.id.btn_stop);
                if (StopButton != null) {
                    StopButton.performClick(); // 模拟停止点击事件
                }
                GameoverDialogFragment gameoverDialog = GameoverDialogFragment.newInstance(score, scoremax);
                gameoverDialog.show(getSupportFragmentManager(), "GameoverDialog");
                return true; // 游戏结束，方块与地图上的方块重叠
            }
        return false;
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
                View downButton = findViewById(R.id.arrow_little_down);
                if (downButton != null) {
                    downButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
            case KeyEvent.KEYCODE_SPACE:
                View stopButton = findViewById(R.id.btn_stop);
                if (stopButton != null) {
                    stopButton.performClick(); // 模拟点击事件
                    return true; // 表示事件已被处理
                }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void updateScoreDisplay() {
        if (txtCurrentScore != null) {
            txtCurrentScore.setText(String.valueOf(score)); // 更新 TextView 显示的分数
        }
    }

    public void updateHighScoreDisplay() {
        if (txtHighScore != null) {
            txtHighScore.setText(String.valueOf(scoremax));
        }
    }

    public void updateScoreMax() {
        SharedPreferences prefs = getSharedPreferences("TetrisPrefs", MODE_PRIVATE);
        // 从SharedPreferences获取最高分
        int savedHighScore = prefs.getInt("highScore", 0);
        // 如果当前分数大于保存的最高分，则更新最高分为当前分数
        if (score > savedHighScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", score);
            editor.apply();
            // 更新成员变量 highScore
            scoremax = score;
        }
        // 只有在最高分被更新后，才更新 UI
        if (scoremax == score) {
            updateHighScoreDisplay();
        }
    }

    public int getHighScoreFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("TetrisPrefs", MODE_PRIVATE);
        return prefs.getInt("highScore", 0); // 如果没有保存的最高分，则返回0
    }
}