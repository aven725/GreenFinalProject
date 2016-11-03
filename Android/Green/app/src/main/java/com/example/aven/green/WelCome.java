package com.example.aven.green;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WelCome extends AppCompatActivity {

    /** 頁面停留時間 */
    private static final int NUM_START = 2000;// 3500

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 隱藏標題
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隱藏狀態列
        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wel_come);


        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(NUM_START);
                    gotoHome();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }.start();

    }

    // 進入主頁
    private void gotoHome() {
        Intent intent = new Intent(WelCome.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
