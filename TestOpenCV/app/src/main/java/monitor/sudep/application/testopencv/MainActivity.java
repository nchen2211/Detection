package monitor.sudep.application.testopencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements CameraBridgeViewBase.CvCameraViewListener2, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    private static final String phoneNo = "1-626-898-0528";
    private static final String message = "SEIZURE ALERT";

    // loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    // These var are used to fix camera orientation from 270' to 0'
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private BackgroundSubtractorMOG2 sub;
    private Mat mGray;
    private Mat mRgb;
    private Mat mFGMask;
    private List<MatOfPoint> contours;
    private double lRate = 0.5;
    private int motionCount = 0;

    private SeekBar sb;

    // call OpenCV manager to helo our app communicate with android phone to make
    // OpenCV work
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial1_surface_view);

        mOpenCvCameraView = (JavaCameraView)findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        //sets up seek bar to adjust learning rate
        sb = (SeekBar)findViewById(R.id.seekBar1);
        sb.setProgress(5);
        sb.setMax(10);
        sb.setOnSeekBarChangeListener(this);
    }

    protected void setDisplayOrientation(Camera camera, int angle){
        Method downPolymorphic;
        try
        {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[] { int.class });
            if (downPolymorphic != null)
                downPolymorphic.invoke(camera, new Object[] { angle });
        }
        catch (Exception e1)
        {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    // receive image data when the camera preview starts on your screen
    public void onCameraViewStarted(int width, int height) {
        sub = new BackgroundSubtractorMOG2();

        //mRgba = new Mat(height,width,CvType.CV_8UC4);
        //mRgbaF = new Mat(height,width,CvType.CV_8UC4);
        //mRgbaT = new Mat(width,width,CvType.CV_8UC4);

        mRgb = new Mat();
        mFGMask = new Mat();
        mGray = new Mat();

        //arraylist to hold individual contours
        contours = new ArrayList<MatOfPoint>();
    }

    // destroy image data when you stop camera preview on your phone screen
    public void onCameraViewStopped() {
        mRgb.release();
    }

    // OpenCV orients the camera to the left by 90'. So if the apps is in
    // portrait more, the camera will be in -90 or 270 degrees orientation
    public Mat onCameraFrame(CvCameraViewFrame inputFrame){
        //mRgba = inputFrame.rgba();

        mRgb = inputFrame.rgba();
        contours.clear();

        //rotate mRgba 90'
        Mat mRgbaT = mRgb.t();
        Core.flip(mRgb.t(), mRgbaT, 1);
        Imgproc.resize(mRgbaT, mRgbaT, mRgb.size());

        //gray frame because it requires less resource to process
        mGray = inputFrame.gray();
        Mat mGrayT = mGray.t();
        Core.flip(mGray.t(), mGrayT, 1);
        Imgproc.resize(mGrayT, mGrayT, mGray.size());

        //this function converts the gray frame into the correct RGB format for the BackgroundSubtractorMOG apply function
        Imgproc.cvtColor(mGrayT, mRgbaT, Imgproc.COLOR_GRAY2RGB);

        //apply detects objects moving and produces a foreground mask
        //the lRate updates dynamically dependent upon seekbar changes
        sub.apply(mRgbaT, mFGMask, lRate);

        //erode and dilate are used to remove noise from the foreground mask
        Imgproc.erode(mFGMask, mFGMask, new Mat());
        Imgproc.dilate(mFGMask, mFGMask, new Mat());

        //drawing contours around the objects by first called findContours and then calling drawContours
        //RETR_EXTERNAL retrieves only external contours
        //CHAIN_APPROX_NONE detects all pixels for each contour
        Imgproc.findContours(mFGMask, contours, new Mat(), Imgproc.RETR_EXTERNAL , Imgproc.CHAIN_APPROX_NONE);

        //draws all the contours in red with thickness of 2
        Imgproc.drawContours(mRgbaT, contours, -1, new Scalar(255, 0, 0), 2);
        ++motionCount;
        Log.d(TAG, "###################### COUNT: " + Integer.toString(motionCount));

        if (motionCount == 20)
            sendText();

        return mRgbaT;
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
        /*Toast.makeText(getApplicationContext(), "SMS sent.",
                Toast.LENGTH_LONG).show();*/
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
    {
        lRate = (double) arg1 / 10.0;
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0)
    {
        // TODO Auto-generated method stub

    }
}