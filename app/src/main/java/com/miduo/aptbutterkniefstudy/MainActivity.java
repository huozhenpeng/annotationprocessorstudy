package com.miduo.aptbutterkniefstudy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.miduo.butterknife_annotation.BindView;
import com.miduo.butterknife_annotation.OnClick;
import com.miduo.butterknife_core.ButterKnife;
import com.miduo.butterknife_core.Unbinder;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.textview)
    TextView textView;

    Unbinder unbinder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder=ButterKnife.bind(this);
        textView.setText("ButterKnife Test");
    }

    @OnClick(R.id.textview)
    void onClick(View view)
    {
        Toast.makeText(this,"点击",Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
