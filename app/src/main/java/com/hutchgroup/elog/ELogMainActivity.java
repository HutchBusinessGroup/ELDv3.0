package com.hutchgroup.elog;

import android.support.v7.app.AppCompatActivity;

import com.hutchgroup.elog.common.CanMessages;


public abstract class ELogMainActivity extends AppCompatActivity {

    public abstract void machineOn();

    public abstract void machineOff();

    public abstract void autoChangeStatus();

    public abstract void resetFlag();

    public abstract void promptToChangeStatus();

    public abstract void freezeLayout();

    public abstract void hideFreezeLayout();

    public abstract void saveIntermediateLog();
    //public abstract void updateOdometer();

    @Override
    protected void onStart() {
        super.onStart();

        ELogApplication app = (ELogApplication) getApplication();
        app.setActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ELogApplication app = (ELogApplication) getApplication();
        app.setActivity(this);
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        ELogApplication app = (ELogApplication) getApplication();
        app.setActivity(null);
        super.onDestroy();
    }

}
