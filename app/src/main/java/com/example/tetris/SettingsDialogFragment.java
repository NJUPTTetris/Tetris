package com.example.tetris;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

public class SettingsDialogFragment extends DialogFragment {

    public interface OnSettingsChangedListener {
        void onSpeedChanged(int speed);

        void onMusicSwitchChanged(boolean isChecked);

        void onSoundEffectSwitchChanged(boolean isChecked);
    }

    private OnSettingsChangedListener listener;

    // 用于传递设置的静态方法
    public static SettingsDialogFragment newInstance(int speed, boolean isSwitchOnMusic, boolean isSwitchOnSoundEffect) {
        SettingsDialogFragment fragment = new SettingsDialogFragment();
        Bundle args = new Bundle();
        args.putInt("speed", speed);
        args.putBoolean("isSwitchOnMusic", isSwitchOnMusic);
        args.putBoolean("isSwitchOnSoundEffect", isSwitchOnSoundEffect);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // 确保宿主Activity实现了监听器接口
        if (context instanceof OnSettingsChangedListener) {
            listener = (OnSettingsChangedListener) context;
        } else {
            throw new ClassCastException(context + " must implement SettingsDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        // 加载自定义布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_settings, null);

        SeekBar seekBar_speed = view.findViewById(R.id.seekBar_speed);
        SwitchCompat switch_music = view.findViewById(R.id.switch_music);
        SwitchCompat switch_sound_effect = view.findViewById(R.id.switch_sound_effect);

        Bundle args = getArguments();
        if (args != null) {
            int speed = args.getInt("speed", 0); // 假设默认速度为0
            boolean isSwitchOnMusic = args.getBoolean("isSwitchOnMusic", true);
            boolean isSwitchOnSoundEffect = args.getBoolean("isSwitchOnSoundEffect", true);

            // 设置SeekBar的值
            seekBar_speed.setProgress(speed);
            // 设置SwitchCompat的值
            switch_music.setChecked(isSwitchOnMusic);
            switch_sound_effect.setChecked(isSwitchOnSoundEffect);
        }
        seekBar_speed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 如果需要，可以在这里更新 UI 或执行其他操作
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 当开始拖动 SeekBar 时调用
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 当停止拖动 SeekBar 时调用
                int finalValue = seekBar.getProgress();
                if (listener != null) {
                    listener.onSpeedChanged(finalValue);
                }
            }
        });
        // 为音乐开关设置监听器
        switch_music.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onMusicSwitchChanged(isChecked);
            }
        });
        // 为音效开关设置监听器
        switch_sound_effect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onSoundEffectSwitchChanged(isChecked);
            }
        });
        builder.setView(view);
        // 创建并返回对话框
        return builder.create();
    }
}