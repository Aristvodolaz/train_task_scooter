package com.example.train_task_scooter.activity;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (layoutResId() != 0) {
            setContentView(layoutResId());
            ButterKnife.bind(this);
        }
        if (isFinishing()) return;
        initViews(savedInstanceState);
    }

    abstract protected void initViews(@Nullable Bundle saveInstanceState);
    @LayoutRes
    abstract protected int layoutResId();
    @StringRes
    abstract protected int titleResId();

}
