package com.example.tetris;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GameoverDialogFragment extends DialogFragment {

    public static GameoverDialogFragment newInstance(int current, int highest) {
        GameoverDialogFragment fragment = new GameoverDialogFragment();
        Bundle args = new Bundle();
        args.putInt("current", current);
        args.putInt("highest", highest);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.DialogFragmentTransparentStyle);
        // 加载自定义布局
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_gameover, null);

        TextView current_score = view.findViewById(R.id.current_score);
        ProgressBar progressBar_score = view.findViewById(R.id.progressBar_score);
        TextView text_highest = view.findViewById(R.id.text_highest);
        ImageButton btn_play_again = view.findViewById(R.id.btn_play_again);
        ImageButton btn_home = view.findViewById(R.id.btn_home);
        Drawable progressDrawable = ContextCompat.getDrawable(requireActivity(), R.drawable.blue_progress);
        progressBar_score.setProgressDrawable(progressDrawable);
        Bundle args = getArguments();
        if (args != null) {
            int current = args.getInt("current", 0); // 假设默认成绩为0
            int highest = args.getInt("highest", 0); // 假设最高成绩为0

            // 将int转换为String来设置TextView的文本
            current_score.setText(String.valueOf(current));
            text_highest.setText("Highest " + String.valueOf(highest));

            progressBar_score.setProgress(current);
            progressBar_score.setMax(highest);
        }

        btn_play_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 关闭DialogFragment
                dismiss();
                if (getActivity() != null) {
                    getActivity().recreate();
                }
            }
        });
        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 关闭DialogFragment
                dismiss();

                // 确保DialogFragment已经关闭后再启动MainActivity
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
        AlertDialog dialog = builder.setView(view).create();
        dialog.setCanceledOnTouchOutside(false);// 禁止通过点击对话框外部来关闭对话框
        return dialog;
    }
}