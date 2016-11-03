package com.hutchgroup.elog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import com.hutchgroup.elog.common.ConstantFlag;

public class FirstActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ConstantFlag.Flag_Development) {

            SharedPreferences prefs = this.getSharedPreferences("HutchGroup", MODE_PRIVATE);
            prefs.edit().putBoolean("firstrun", false).commit();

            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            startActivity(intent);
            finish();

        } else {
            SharedPreferences prefs = this.getSharedPreferences("HutchGroup", MODE_PRIVATE);
            if (prefs.getBoolean("firstrun", true)) {
                startActivity(new Intent(FirstActivity.this, SetupActivity.class));
                finish();
            } else {
                startActivity(new Intent(FirstActivity.this, SplashActivity.class));
                finish();
            }
        }
    }

}
