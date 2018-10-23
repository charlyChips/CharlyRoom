package charlychips.com.charlyroom.Utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;



/**

 <uses-permission android:name="android.permission.BLUETOOTH" />
 <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
 */
public class Bluetooth {
    final int handlerRxState = 0;        				 //used to identify handler message
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    public ConnectedThread conexionBt;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address = null;
    private BluetoothAdapter btAdapter = null;
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    Handler btHandler;
    private boolean connected=false;

    ArrayList<String> namesList;
    ArrayList<String> dirList;


    private Bluetooth(final ReceiverListener listener){

        btHandler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerRxState) {										//if message is what we want
                    String readMessage = (String) msg.obj;
                    //echo(readMessage);
                    // msg.arg1 = bytes from connect thread

                    listener.onReceivedData(readMessage);
                    Log.d("Bluetooth", "handleMessage: ==============="+readMessage);


                }
            }
        };
    }
    //=== Singleton
    public static Bluetooth mBluetooth = null;
    public static Bluetooth getInstance(ReceiverListener rxListener){
        if(mBluetooth == null){
            mBluetooth = new Bluetooth(rxListener);
        }
        return mBluetooth;
    }
    
    private void setConnected(boolean con){
        connected=con;
    }
    public boolean isConnected(){
        return connected;
    }
    /*
    public void setBtHandler(Handler handler){
        btHandler = handler;
    }
    */

    private void checkBluetooth(Activity activity){
        // Check device has Bluetooth and that it is turned on
        btAdapter= BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(btAdapter==null) {
            Toast.makeText(activity, "No Bluetooth Device", Toast.LENGTH_SHORT).show();
        } else {
            if (btAdapter.isEnabled()) {
                Log.d("Bluetooth", "...Bluetooth Activado...");
            } else {
                //Pide permiso al usuario de encender el Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, 1);
            }
        }
    }


    public void conectar(Activity activity,ConnectionListener cl){
        new AsynkConecta(activity,cl).execute();
    }
    private String connect(){
        if(address==null) return "";
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            return "La creación del Socket fallo";
        }
        // Establecemos Conexion Bluetooth.
        try
        {
            btSocket.connect();
            setConnected(true);
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
            }
            return "Error al conectar";
            //return false;
        }
        conexionBt = new ConnectedThread(btSocket);
        conexionBt.start();
        return "Conectado";
    }

    public AlertDialog getDialogDevices(final Activity activity, final ConnectionListener cl){

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
        dialogBuilder.setTitle("Dispositivos Bluetooth");
        checkBluetooth(activity);
        readPairedDevices();

        CharSequence[] itemsDialog = new CharSequence[namesList.size()];
        for(int i=0;i<namesList.size();i++){
            itemsDialog[i] = ""+namesList.get(i);
        }
        dialogBuilder.setItems(itemsDialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Creamos socket Bluetooth
                address = dirList.get(which);
                if(address==null){
                    dialog.dismiss();
                    Toast.makeText(activity, "Agregue Dispositivo en Configuraciones", Toast.LENGTH_SHORT).show();
                    return;
                }
                conectar(activity,cl);
                dialog.dismiss();

            }
        });
        return dialogBuilder.create();
    }

    //==============  Privados

    private void readPairedDevices(){
        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        // Get a set of currently paired devices and append to 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        namesList = new ArrayList<>();
        dirList = new ArrayList<>();
        if (pairedDevices.size() > 0) {

            for (BluetoothDevice device : pairedDevices) {
                dirList.add(device.getAddress());
                namesList.add(device.getName());
            }
        }else{
            namesList.add("Sin dispositivos...");
        }
    }

//------------------------ConnectedActivity
    public void disconnect(){
        setConnected(false);
        if(btSocket==null) return;
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }
    //------------------------------------------------------------THREAD

    //create new class for connect thread
    public class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);            //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    btHandler.obtainMessage(handlerRxState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //write method
        public boolean write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream

            } catch (IOException e) {
                //if you cannot write, close the application
                return false;
            }
            return true;
        }

        public boolean write(byte[] msgBuffer) {
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                return false;

            }
            return true;
        }
    }

    private class AsynkConecta extends AsyncTask<Integer,Integer,String>{

        Activity activity;
        ConnectionListener cl=null;
        public AsynkConecta(Activity activity,ConnectionListener cl){
            this.activity = activity;
            this.cl = cl;
        }
        @Override
        protected String doInBackground(Integer... integers) {
            return connect();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            cl.connectionResult(s);
            if(isConnected()) cl.connectionSuccess(s);
            else cl.connectionFailed(s);

        }
    }

    public interface ConnectionListener{
        void connectionSuccess(String s);
        void connectionFailed(String s);
        void connectionResult(String s);
    }
    public interface ReceiverListener{
        void onReceivedData(String rxData);
    }
}
