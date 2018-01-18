package cloud.kloboukuv.bobiktankbt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;
import java.util.UUID;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;
import android.support.v4.content.ContextCompat;


public class BluetoothRobotController extends AppCompatActivity {

    Button btnLeft, btnRight, btnForward, btnBackward, btnStop, btnLeds, btnHorn, btnAutopilot;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter bluetoothAdapter = null;
    BluetoothSocket bluetoothSocket = null;
    private boolean isBluetoothConnected = false;
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    boolean isMovement = false;
    boolean autopilot = false;
    private int mSelectedColor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        address = intent.getStringExtra(BluetoothDevicesList.EXTRA_ADDRESS);

        setContentView(R.layout.bluetooth_robot_controller);

        btnLeft = findViewById(R.id.btn_left);
        btnRight = findViewById(R.id.btn_right);
        btnForward = findViewById(R.id.btn_forward);
        btnBackward = findViewById(R.id.btn_backward);
        btnStop = findViewById(R.id.btn_stop);
        btnLeds = findViewById(R.id.btn_leds);
        btnHorn = findViewById(R.id.btn_horn);
        btnAutopilot = findViewById(R.id.btn_autopilot);

        int[] mColors = getResources().getIntArray(R.array.default_rainbow);
        mSelectedColor = ContextCompat.getColor(this, R.color.flamingo);

        new BluetoothConnector().execute();

        final ColorPickerDialog dlgColors = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                mSelectedColor,
                5,
                ColorPickerDialog.SIZE_SMALL,
                true
        );

        dlgColors.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener()
        {
            @Override
            public void onColorSelected(int color) {
                mSelectedColor = color;
                Log.v("CONTROLLER", Integer.toString(color));
            }

        });


        btnLeds.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlgColors.show(getFragmentManager(), "color_dialog_test");
            }
        });

        btnHorn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendCommand("H");
            }
        });

        btnAutopilot.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(autopilot)
                {
                    autopilot = false;
                    btnAutopilot.setText(getString(R.string.btn_autopilot));
                    sendCommand("A0");
                }
                else{
                    autopilot = true;
                    btnAutopilot.setText(getString(R.string.btn_manual));
                    sendCommand("A1");
                }
            }
        });


        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isMovement = false;
                sendCommand("0");
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!isMovement) {
                        sendCommand("L");
                        isMovement = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    isMovement = false;
                    return true;
                }
                return false;
            }
        });

        btnRight. setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!isMovement) {
                        sendCommand("R");
                        isMovement = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    isMovement = false;
                    return true;
                }
                return false;
            }
        });


        btnForward.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!isMovement) {
                        sendCommand("F");
                        isMovement = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    isMovement = false;
                    return true;
                }
                return false;
            }
        });

        btnBackward.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!isMovement) {
                        sendCommand("B");
                        isMovement = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    isMovement = false;
                    return true;
                }
                return false;
            }
        });
    }

    private String getTime(String pattern){
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }

    boolean sendCommand(String command)
    {
        boolean retval;
        if (bluetoothSocket !=null)
        {
            try
            {
                bluetoothSocket.getOutputStream().write((command + "\n").getBytes());
                retval = true;
            }
            catch (IOException e)
            {
                toastMessage(getString(R.string.bt_com_error));
                retval = false;
            }
        }
        else
        {
            retval = false;
        }
        return retval;
    }

    private void disconnect()
    {
        if (bluetoothSocket !=null)
        {
            try
            {
                bluetoothSocket.close();
            }
            catch (IOException e)
            { toastMessage(getString(R.string.bt_com_error));}
        }
    }

    private void toastMessage(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class BluetoothConnector extends AsyncTask<Void, Void, Void>
    {
        private boolean success = true;

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(BluetoothRobotController.this, getString(R.string.bt_connecting_title), getString(R.string.bt_connecting_msg));
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (bluetoothSocket == null || !isBluetoothConnected)
                {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    bluetoothSocket.connect();
                }
            }
            catch (IOException e)
            {
                success = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!success)
            {
                toastMessage(getString(R.string.bt_conecting_error));
                finish();
            }
            else
            {
                toastMessage(getString(R.string.bt_conecting_ok));
                isBluetoothConnected = true;
            }
            progress.dismiss();
        }
    }

    protected void onDestroy()
    {
        disconnect();
        super.onDestroy();
    }
}

