package com.example.tetris;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SettingsDialogFragment.OnSettingsChangedListener {
    int speed = 0;//下落速度
    boolean isSwitchOnMusic = true;//是否有音乐

    boolean isSwitchOnSoundEffect = true;//是否有音效

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 找到按钮
        Button startButton = findViewById(R.id.start_button);
        // 设置点击事件监听器
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("speed", speed); //传入配置速度
            intent.putExtra("isSwitchOnMusic", isSwitchOnMusic);//传入是否需要音乐
            intent.putExtra("isSwitchOnSoundEffect", isSwitchOnSoundEffect);//传入是否需要音效
            startActivity(intent);
        });

        // 设置设置按钮点击事件监听器
        findViewById(R.id.settings_button).setOnClickListener(view -> {
            SettingsDialogFragment settingsDialog = SettingsDialogFragment.newInstance(speed, isSwitchOnMusic, isSwitchOnSoundEffect);
            settingsDialog.show(getSupportFragmentManager(), "settings_dialog");
        });
    }

    //从设置界面传入速度
    @Override
    public void onSpeedChanged(int speed) {
        this.speed = speed;
    }

    //从设置界面传入是否需要音乐
    @Override
    public void onMusicSwitchChanged(boolean isSwitchOnMusic) {
        this.isSwitchOnMusic = isSwitchOnMusic;
    }

    //从设置界面传入是否需要音效
    @Override
    public void onSoundEffectSwitchChanged(boolean isSwitchOnSoundEffect) {
        this.isSwitchOnSoundEffect = isSwitchOnSoundEffect;
    }
}