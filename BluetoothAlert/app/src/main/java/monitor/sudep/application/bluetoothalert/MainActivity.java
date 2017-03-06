package monitor.sudep.application.bluetoothalert;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static String TAG = MainActivity.class.getSimpleName();
    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private String mSpecificDevice = "Nexus 7";
    private boolean isFound;


    // Member fields
   // private BluetoothAdapter mBtAdapter;
    private BluetoothAdapter adapter;
    BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        adapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(mReceiver, filter);
        adapter.startDiscovery();

        // Set result CANCELED in case the user backs out
        /* setResult(Activity.RESULT_CANCELED);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtAdapter.startDiscovery();*/
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.d(TAG, "in broadcast receiver");
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "start..");
                //discovery starts, we can show progress dialog or perform other tasks
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "finish..");

                if (device.getName() == null) {
                    //if (! device.getName().equals(mSpecificDevice)) {
                    Log.d(TAG, "discovering again.. ");
                    adapter.startDiscovery();
                    Log.d(TAG, "device wrong name: " + device.getName());
                }
//
                //discovery finishes, dismis progress dialog
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "found..");
                //bluetooth device found
                device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d (TAG, "device name: " + device.getName());

                if (device.getName() == null) {
                    //if (! device.getName().equals(mSpecificDevice)) {
                    Log.d(TAG, "discovering again.. ");
                    adapter.startDiscovery();
                    Log.d(TAG, "device wrong name: " + device.getName());
                }
//
                Log.d (TAG, "device name: " + device.getName());
                if (mSpecificDevice.equals(device.getName())) {
                    Log.d (TAG, "cancel discovery");
                    adapter.cancelDiscovery();
                }

                Toast.makeText(getApplicationContext(), device.getName() + " is on", Toast.LENGTH_LONG).show();
            }
        }
    };
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            Log.d(TAG, "in BroadcastReceiver");
//
//            // When discovery finds a device
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Get the BluetoothDevice object from the Intent
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                while (! isFound) {
//                    Log.d(TAG, "search again....");
//                    mBtAdapter.startDiscovery();
//
//                    // If we're already discovering, stop it
//                    if (mBtAdapter.isDiscovering()) {
//                        Log.d(TAG, "FOUND....");
//                        mBtAdapter.cancelDiscovery();
//                        //isFound = true;
//                    }
//                }
//
//                Log.d(TAG, "device name: " + device.getName());
//
//                if (mSpecificDevice.equals(device.getName())) {
//                    Toast.makeText(getApplicationContext(), device.getName() + " is on", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    };

//    @Override
//    protected void onStart() {
//        super.onStart();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//
//        // Make sure we're not doing discovery anymore
//        if (mBtAdapter != null) {
//            mBtAdapter.cancelDiscovery();
//        }
//
//        // Unregister broadcast listeners
//        this.unregisterReceiver(mReceiver);
//    }
//
//    /**
//     * Start device discover with the BluetoothAdapter
//     */
//    private void doDiscovery() {
//
//        Log.d(TAG, "##################### in doDiscovery");
//        // If we're already discovering, stop it
//        if (mBtAdapter.isDiscovering()) {
//            mBtAdapter.cancelDiscovery();
//        }
//
//        // Request discover from BluetoothAdapter
//        mBtAdapter.startDiscovery();
//    }
}
