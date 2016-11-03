package com.hutchgroup.elog;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hutchgroup.elog.beans.AppSettings;
import com.hutchgroup.elog.beans.SettingsBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.DailyLogDB;
import com.hutchgroup.elog.db.EventDB;

import java.io.ByteArrayOutputStream;

public class CertifyLogActivity extends AppCompatActivity {
    private LinearLayout signature, sig;
    private signature mSignature;
    private ImageView imgSign;
    private Bitmap mBitmap;
    View mView;
    String logIds, sign;
    Button btnCertify, btnNotReady, btnReset;
    int driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utility.isLargeScreen(getApplicationContext())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.PORTRAIT.ordinal()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.LANSCAPE.ordinal()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (Utility._appSetting.getOrientation() == AppSettings.AppOrientation.AUTO.ordinal()) {
                Settings.System.putInt( getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }
        }

        setContentView(R.layout.activity_certify_log);
        initialize();
    }

    private void initialize() {
        driverId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        imgSign = (ImageView) findViewById(R.id.imgSig);
        signature = (LinearLayout) findViewById(R.id.etSignature);
        sig = (LinearLayout) findViewById(R.id.sigSignature);
        btnReset = (Button) findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignature.clear();
            }
        });
        btnCertify = (Button) findViewById(R.id.btnCertify);
        btnCertify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  mSignature.save(mView);
                /*if (sign == null || sign.equals("")) {
                    Utility.showAlertMsg("Please Add Signature");
                    return;
                }
*/
                if (DailyLogDB.DailyLogCertify(sign, driverId, logIds)) {
                    String[] logs = logIds.split(",");
                    // need to discuss about this should we enter multiple event related to multiple certification
                    for (int i = 0; i < logs.length; i++) {
                        int logId = Integer.parseInt(logs[i]);
                        int n = DailyLogDB.getCertifyCount(logId) + 1;
                        DailyLogDB.CertifyCountUpdate(logId, n);
                        if (n > 9)
                            n = 9;
                        // to be discuss about event
                        //123 LogFile.write(CertifyLogActivity.class.getName() + "::initialize " + "DailyLogCertify driverId:" + driverId, LogFile.USER_INTERACTION, LogFile.DRIVEREVENT_LOG);
                        EventDB.EventCreate(Utility.getCurrentDateTime(), 4, n, "Driver's " + n + "'th certification of a daily record", 1, 1, logId, driverId, "");
                    }
                }
                setResult(RESULT_OK);
                finish();
            }
        });
        btnNotReady = (Button) findViewById(R.id.btnNotReady);
        btnNotReady.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
      /*  mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        signature.addView(mSignature, ActionBar.LayoutParams.FILL_PARENT,
                ActionBar.LayoutParams.FILL_PARENT);
        mView = signature;*/
        Bundle b = getIntent().getExtras();
        logIds = b.getString("LogIds");
    }

    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {
            try {

                if (mBitmap == null) {
                    int width = signature.getWidth();
                    int height = (signature.getHeight() == 0 ? 250 : signature
                            .getHeight());
                    mBitmap = Bitmap.createBitmap(width, height,
                            Bitmap.Config.RGB_565);
                }
                Canvas canvas = new Canvas(mBitmap);
                v.draw(canvas);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                mBitmap.compress(Bitmap.CompressFormat.PNG, 100,
                        byteArrayOutputStream);
                sig.setVisibility(View.VISIBLE);
                imgSign.setImageBitmap(mBitmap);
                signature.setVisibility(View.GONE);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                sign = Base64.encodeToString(byteArray, Base64.DEFAULT);
            } catch (Exception e) {
                sign = "";
            }

        }

        public void clear() {
            sign = "";
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            // mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;

                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);

                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

}
