package com.hutchgroup.elog;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.fragments.DockingFragment;
import com.hutchgroup.elog.fragments.ELogFragment;

public class daily_log_dashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_log_dashboard);
    }

    private void redirectToDailyLog()
    {
       /* if (undockingMode) {
            replaceFragment(new DockingFragment());
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }
        isOnDailyLog = true;
        bInspectDailylog = false;
        replaceFragment(ELogFragment.newInstance());
        title = getApplicationContext().getResources().getString(R.string.menu_daily_log);
        previousScreen = currentScreen;
        currentScreen = DailyLog_Screen;*/
    }
}
