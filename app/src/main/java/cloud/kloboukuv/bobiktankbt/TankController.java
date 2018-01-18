package cloud.kloboukuv.bobiktankbt;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.IOException;
import java.util.UUID;


public class TankController extends AppCompatActivity {

    Button btnLeft, btnRight, btnForward, btnBackward, btnStop;
    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    boolean movementStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_main2);

        //call the widgtes
        btnLeft = (Button)findViewById(R.id.button2);
        btnRight = (Button)findViewById(R.id.button3);
        btnForward = (Button)findViewById(R.id.button5);
        btnBackward = (Button)findViewById(R.id.button6);
        btnStop = (Button)findViewById(R.id.button7);

        new ConnectBT().execute(); //Call the class to connect

        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                movementStarted = false;
                sendCommand("0");
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!movementStarted) {
                        sendCommand("L");
                        movementStarted = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    movementStarted = false;
                    return true;
                }
                return false;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!movementStarted) {
                        sendCommand("R");
                        movementStarted = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    movementStarted = false;
                    return true;
                }
                return false;
            }
        });


        btnForward.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!movementStarted) {
                        sendCommand("F");
                        movementStarted = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    movementStarted = false;
                    return true;
                }
                return false;
            }
        });

        btnBackward.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    if(!movementStarted) {
                        sendCommand("B");
                        movementStarted = true;
                    }
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP){
                    sendCommand("0");
                    movementStarted = false;
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
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write((command + "\n").getBytes());
                retval = true;
            }
            catch (IOException e)
            {
                msg("Error");
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
        if (btSocket!=null)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e)
            { msg("Error");}
        }
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(TankController.this, "Připojuji se k tanku", "Tož trpělivost!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice device = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Tož nejde to připojit. Zkus to znova.");
                finish();
            }
            else
            {
                msg("Tank připojen");
                isBtConnected = true;
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

