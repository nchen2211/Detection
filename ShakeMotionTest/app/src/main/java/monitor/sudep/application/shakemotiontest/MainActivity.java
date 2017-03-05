package monitor.sudep.application.shakemotiontest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private final String TAG = MainActivity.class.getSimpleName();

    private final float NOISE =(float)1.0;
    private final int COUNT_DEFAULT = 80;
    private final int CALL_DEFAULT = 1;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final String phoneNo = "1-626-898-0528";
    private static final String message = "SEIZURE ALERT";

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private int countX, countY, countZ, callcount;

    TextView tvX, tvY, tvZ, count_x, count_y, count_z;
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvX = (TextView)findViewById(R.id.x_axis);
        tvY = (TextView)findViewById(R.id.y_axis);
        tvZ = (TextView)findViewById(R.id.z_axis);
        count_x = (TextView) findViewById(R.id.countx);
        count_y = (TextView) findViewById(R.id.county);
        count_z = (TextView) findViewById(R.id.countz);
        count_x.setText("0");
        count_y.setText("0");
        count_z.setText("0");
        iv = (ImageView)findViewById(R.id.image);

        countX = countY = countZ = callcount = 0;

        mInitialized = false;
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int x_stop, y_stop, z_stop = 0;
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;

            tvX.setText("0.0");
            tvY.setText("0.0");
            tvZ.setText("0.0");

            mInitialized = true;
        } else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);

            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;

            mLastX = x;
            mLastY = y;
            mLastZ = z;

            tvX.setText(Float.toString(deltaX));
            if (deltaX != 0.0) {
                ++countX;
                count_x.setText(Integer.toString(countX));
            }

            tvY.setText(Float.toString(deltaY));
            if (deltaY != 0.0) {
                ++countY;
                count_y.setText(Integer.toString(countY));
            }

            tvZ.setText(Float.toString(deltaZ));
            if (deltaZ != 0.0) {
                ++countZ;
                count_z.setText(Integer.toString(countZ));
            }

            if (callcount == 0) {
                if (countX >= COUNT_DEFAULT && countY >= COUNT_DEFAULT && countZ >= COUNT_DEFAULT) {
                    x_stop = countX;
                    y_stop = countY;
                    z_stop = countZ;

                    count_x.setText(Integer.toString(x_stop));
                    count_y.setText(Integer.toString(y_stop));
                    count_z.setText(Integer.toString(z_stop));

                    ++callcount;
                    //pingParents();
                    sendText();
                }
            }


            //iv.setVisibility(View.VISIBLE);
            /*if (deltaX > deltaY) {
                iv.setImageResource(R.drawable.shaker_fig_1);
            } else if (deltaY > deltaX) {
                iv.setImageResource(R.drawable.shaker_fig_2);
            } else {
                iv.setVisibility(View.INVISIBLE);
            } */
        }
    }

    public void pingParents() {
        Log.d(TAG, "PING PARENT'S PHONE");
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.setData(Uri.parse("tel: 1-626-898-0528"));

        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    public void sendText() {
        Log.d(TAG, "TEXT PARENT'S PHONE");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNo, null, message, null, null);
        Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show();

       /* Intent textIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms: "+ phoneNo));
        textIntent.putExtra("sms_body", message);
        textIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(textIntent);*/
    }

   /* @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    Toast.makeText(getApplicationContext(), "SMS sent.",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                            "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }*/
}
