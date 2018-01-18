package cloud.kloboukuv.bobiktankbt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Set;

public class BluetoothDevicesList extends AppCompatActivity {

    ListView devices;

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> bluetoothDevices;
    public static String EXTRA_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_devices_list);
        getSupportActionBar().hide();

        devices = findViewById(R.id.listView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }
    }

    protected void onResume(){
        super.onResume();
        devices.setAdapter(null);
        pairedDevicesList();
    }


    private void pairedDevicesList()
    {
        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();

        if (bluetoothDevices.size()>0)
        {
            for(BluetoothDevice device : bluetoothDevices)
            {
                list.add(device.getName() + "\n" + device.getAddress());
            }
        }


        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devices.setAdapter(adapter);
        devices.setOnItemClickListener(myListClickListener);

    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> adapterView, View view, int arg2, long arg3)
        {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent(BluetoothDevicesList.this, BluetoothRobotController.class);
            intent.putExtra(EXTRA_ADDRESS, address);
            startActivity(intent);
        }
    };
}

