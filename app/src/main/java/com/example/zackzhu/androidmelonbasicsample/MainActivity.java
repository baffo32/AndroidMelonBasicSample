package com.example.zackzhu.androidmelonbasicsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.bluetooth.*;
import android.widget.TextView;
import android.widget.Toast;

import com.axio.melonplatformkit.AnalysisResult;
import com.axio.melonplatformkit.DeviceHandle;
import com.axio.melonplatformkit.DeviceManager;
import com.axio.melonplatformkit.IDeviceManagerListener;
import com.axio.melonplatformkit.ISignalAnalyzerListener;
import com.axio.melonplatformkit.SignalAnalyzer;

import java.util.ArrayList;
import java.util.Arrays;


/* Usage instructions:
        * 1. Enable bluetooth on your device
        * 2. turn on the headband and put it on your head
        * 3. Run this project
        * 4. Press 'Start Analyze' button
        * 5. if can not see 'Start Analyze' button, kill the app and start again
        * 6. You should see Raw EEG data
        * on the screen.
*/
public class MainActivity extends ActionBarActivity implements IDeviceManagerListener,ISignalAnalyzerListener {

    DeviceManager mDeviceManager;
    DeviceHandle mConnectedHandle;

    int currentHandleIndex;
    ArrayList<DeviceHandle> mDeviceHandleArray;
    boolean isConnectedBeofore;

    Button tryConnectBtn;
    Button startAnalyzeBtn;

    TextView statusTextView;

    public static final int REQUEST_ENABLE_BT = 1111;

    public static SignalAnalyzer mSignalAnalyzer = new SignalAnalyzer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check bluetooth first
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        else
        {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        //setup device and scan
        setupMelonDevice();
        mDeviceManager.startScan();

       //setup UI, tryConnectBtn is for test, try to connect another device
        statusTextView=(TextView)findViewById(R.id.text1);
        tryConnectBtn= (Button)findViewById(R.id.tryButton);
        tryConnectBtn.setVisibility(View.GONE);
        tryConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Basic Sample", "clicking try another one");
                currentHandleIndex++;
                if (currentHandleIndex >= 0 && currentHandleIndex < mDeviceHandleArray.size()) {
                    DeviceHandle handle = mDeviceHandleArray.get(currentHandleIndex);
                    handle.disconnect();
                    handle.connect();
                    Log.d("Basic Sample", "device connecting = " + handle.getName());
                }

            }
        });


        //when this button is shown,you can click for get a graph data.
        startAnalyzeBtn= (Button) findViewById(R.id.Analyze);
        startAnalyzeBtn.setVisibility(View.GONE);
        startAnalyzeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Basic Sample", "start analyze");
                mConnectedHandle.addAnalyzer(mSignalAnalyzer);
                mConnectedHandle.startStreaming();
            }
        });


    }

    /**
     *
     * check if connect to a device and show a analyze button.
     *
     */
    public  void checkConnectedDeviceAndUpdateUI()
    {
        if (currentHandleIndex >= 0 && currentHandleIndex < mDeviceHandleArray.size()) {
            final DeviceHandle dhandle = mDeviceHandleArray.get(currentHandleIndex);
            Log.d("Basic Sample", "device name = " + dhandle.getName() + "device status = " + dhandle.getState());
            if (dhandle.getState() == DeviceHandle.DeviceState.CONNECTED) {
                mConnectedHandle = dhandle;
                startAnalyzeBtn.setVisibility(View.VISIBLE);

            } else {
                startAnalyzeBtn.setVisibility(View.GONE);
            }

        }

    }

    /**
     *
     * call back for DeviceManager.
     *
     */
    public void onDeviceFound(final DeviceHandle deviceHandle) {

        Log.d("Basic Sample", "onDeviceFound name = " + deviceHandle.getName() + "status = " + deviceHandle.getState());
        mDeviceHandleArray.add(deviceHandle);
        DeviceHandle handle = mDeviceHandleArray.get(currentHandleIndex);
        //only connect the first device when be found
        if (isConnectedBeofore) {
            return;
        }
        else {
            Log.d("Basic Sample", "onDeviceFound Try to Connect = " + deviceHandle.getName() + "status = " + deviceHandle.getState());
            isConnectedBeofore = true;
            handle.connect();
        }

    }

    private void setupMelonDevice() {
        mDeviceManager = DeviceManager.getManager();
        mDeviceManager.addListener(this);
        mDeviceHandleArray = new ArrayList<DeviceHandle>();
        mSignalAnalyzer.addListener(this);

        RawVoltageView rawVoltageView = (RawVoltageView)findViewById(R.id.rvGraph);
        mSignalAnalyzer.addListener(rawVoltageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeviceScanStopped() {

    }

    @Override
    public void onDeviceScanStarted() {

    }

    @Override
    public void onDeviceReady(DeviceHandle deviceHandle) {

    }

    @Override
    public void onDeviceDisconnected(DeviceHandle deviceHandle) {

    }

    @Override
    public void onDeviceConnected(DeviceHandle deviceHandle) {
        Log.d("Basic Sample", "onDeviceConnected name = " + deviceHandle.getName() + "status = " + deviceHandle.getState());
        statusTextView.setText("onDeviceConnected name = " + deviceHandle.getName() + "status = " + deviceHandle.getState());

        checkConnectedDeviceAndUpdateUI();
    }

    @Override
    public void onDeviceConnecting(DeviceHandle deviceHandle) {

    }

    @Override
    public void onDeviceUnknowStatus(DeviceHandle deviceHandle) {

    }

    @Override
    /**
     *
     * get the data from SignalAnalyzer and show it.
     *
     */
    public void onAnalyzedSamples(SignalAnalyzer signalAnalyzer, AnalysisResult leftAnalysisResult, AnalysisResult rightAnalysisResult) {

        float[] leftChannelData = leftAnalysisResult.getFilteredSignal();
        float[] rightChannelData = rightAnalysisResult.getFilteredSignal();
        //Log.d("Basic Sample", "onAnalyzedSamples = " + Arrays.toString(leftChannelData));
        Log.d("Basic Sample", "fft = " + Arrays.toString(leftAnalysisResult.fftBuffer.getAmplitude()));


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT
                && resultCode == Activity.RESULT_OK) {
            //setup device and scan
            setupMelonDevice();
            mDeviceManager.startScan();
            }
        }



}
