package charlychips.com.charlyroom.Utils

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.ArrayList
import java.util.UUID


/**
 *
 * <uses-permission android:name="android.permission.BLUETOOTH"></uses-permission>
 * <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"></uses-permission>
 */
class Bluetooth private constructor() {
    internal val handlerRxState = 0                         //used to identify handler message
    private var btSocket: BluetoothSocket? = null
    private val recDataString = StringBuilder()
    var conexionBt: ConnectedThread? = null
    private var btAdapter: BluetoothAdapter? = null

    internal var btHandler: Handler
    var isConnected = false
        private set

    var namesList = ArrayList<String>()
    var dirList = ArrayList<String>()

    var rxListener: RxListener? = null
    var connectionListener:ConnectionListener? = null


    init {

        btHandler = object : Handler() {
            override fun handleMessage(msg: android.os.Message) {
                if (msg.what == handlerRxState) {                                        //if message is what we want
                    val readMessage = msg.obj as String
                    //echo(readMessage);
                    // msg.arg1 = bytes from connect thread

                    rxListener?.onReceivedData(readMessage)
                    Log.d("Bluetooth", "handleMessage: ===============$readMessage")


                }
            }
        }
    }
    /*
    public void setBtHandler(Handler handler){
        btHandler = handler;
    }
    */

    private fun checkBluetooth(activity: Activity) {
        // Check device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter() // CHECK THIS OUT THAT IT WORKS!!!
        if (btAdapter == null) {
            Toast.makeText(activity, "No Bluetooth Device", Toast.LENGTH_SHORT).show()
        } else {
            if (btAdapter!!.isEnabled) {
                Log.d("Bluetooth", "...Bluetooth Activado...")
            } else {
                //Pide permiso al usuario de encender el Bluetooth
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activity.startActivityForResult(enableBtIntent, 1)
            }
        }
    }


    fun conectar(activity: Activity) {

        address = loadAddress(activity)
        AsynkConecta(activity, connectionListener).execute()
    }

    fun conectar(activity: Activity,address:String) {

        AsynkConecta(activity, connectionListener).execute()
    }


    //TODO address
    //TODO btAdapter
    private fun connect(): String {
        if (address == null ) return ""
        if(btAdapter == null){
            btAdapter = BluetoothAdapter.getDefaultAdapter()
        }
        if(btAdapter == null) return ""
        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = createBluetoothSocket(device)
        } catch (e: IOException) {
            return "La creación del Socket fallo"
        }

        // Establecemos Conexion Bluetooth.
        try {
            btSocket!!.connect()
            isConnected = true
        } catch (e: IOException) {
            try {
                btSocket!!.close()
            } catch (e2: IOException) {
            }

            return "Error al conectar"
            //return false;
        }

        conexionBt = ConnectedThread(btSocket!!)
        conexionBt!!.start()
        return "Conectado"
    }


    fun getDialogDevices(activity: Activity): AlertDialog {

        val dialogBuilder = AlertDialog.Builder(activity)
        dialogBuilder.setTitle("Dispositivos Bluetooth")
        checkBluetooth(activity)
        readPairedDevices()

        val itemsDialog = arrayOfNulls<CharSequence>(namesList.size)
        for (i in namesList.indices) {
            itemsDialog[i] = "" + namesList[i]
        }
        dialogBuilder.setItems(itemsDialog, DialogInterface.OnClickListener { dialog, which ->
            //Creamos socket Bluetooth
            address = dirList[which]
            if (address == null) {
                dialog.dismiss()
                Toast.makeText(activity, "Agregue Dispositivo en Configuraciones", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            saveAddress(activity, address!!)
            conectar(activity)
            dialog.dismiss()
        })
        return dialogBuilder.create()
    }

    //==============  Privados

    private fun readPairedDevices() {
        // Get the local Bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        // Get a set of currently paired devices and append to 'pairedDevices'
        val pairedDevices = btAdapter!!.bondedDevices

        namesList = ArrayList()
        dirList = ArrayList()
        if (pairedDevices.size > 0) {

            for (device in pairedDevices) {
                dirList.add(device.address)
                namesList.add(device.name)
            }
        } else {
            namesList.add("Sin dispositivos...")
        }
    }

    //------------------------ConnectedActivity
    fun disconnect() {
        isConnected = false
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket?.close()
            btSocket = null
        } catch (e2: IOException) {
            //insert code to deal with this
        }

    }

    @Throws(IOException::class)
    private fun createBluetoothSocket(device: BluetoothDevice): BluetoothSocket {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID)
        //creates secure outgoing connecetion with BT device using UUID
    }
    //------------------------------------------------------------THREAD

    //create new class for connect thread
    inner class ConnectedThread//creation of the connect thread
    (socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                //Create I/O streams for connection
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }


        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)            //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes)
                    // Send the obtained bytes to the UI Activity via handler
                    btHandler.obtainMessage(handlerRxState, bytes, -1, readMessage).sendToTarget()
                } catch (e: IOException) {
                    break
                }

            }
        }

        //write method
        fun write(input: String): Boolean {
            val msgBuffer = input.toByteArray()           //converts entered String into bytes
            try {
                mmOutStream!!.write(msgBuffer)                //write bytes over BT connection via outstream

            } catch (e: IOException) {
                //if you cannot write, close the application
                return false
            }

            return true
        }

        fun write(msgBuffer: ByteArray): Boolean {
            try {
                mmOutStream!!.write(msgBuffer)                //write bytes over BT connection via outstream
            } catch (e: IOException) {
                //if you cannot write, close the application
                return false

            }

            return true
        }
    }

    private inner class AsynkConecta(internal var activity: Activity, internal var cl: ConnectionListener?) : AsyncTask<Int, Int, String>() {

        override fun doInBackground(vararg integers: Int?): String {
            return connect()
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)

            cl?.connectionResult(s)
            if (isConnected)
                cl?.connectionSuccess(s)
            else
                cl?.connectionFailed(s)

        }
    }

    interface ConnectionListener {
        fun connectionSuccess(s: String)
        fun connectionFailed(s: String)
        fun connectionResult(s: String)
    }

    interface RxListener {
        fun onReceivedData(rxData: String)
    }



    private fun getPrefs(context: Context):SharedPreferences{
        return context.getSharedPreferences("BTcharlyRoom",Context.MODE_PRIVATE)
    }
    private fun saveAddress(context: Context,address:String){
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        editor.putString("address",address)
        editor.apply()
    }
    private fun loadAddress(context: Context):String?{
        return getPrefs(context).getString("address",null)
    }

    companion object {
        // SPP UUID service - this should work for most devices
        private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        // String for MAC address
        private var address: String? = null

        var EXTRA_DEVICE_ADDRESS = "device_address"
        //=== Singleton

        private var mBluetooth: Bluetooth? = null
        val instance: Bluetooth
        get(){
            if (mBluetooth == null) {
                mBluetooth = Bluetooth()
            }
            return mBluetooth!!
        }
    }
}
