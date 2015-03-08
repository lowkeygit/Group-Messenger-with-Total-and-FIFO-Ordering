package edu.buffalo.cse.cse486586.groupmessenger2;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static private int deliveryCount = 0;
    static private int proposalNum = 0;
    static TreeSet<Message> prioritySet = new TreeSet<Message>();
    static ConcurrentHashMap<String, String> holdBackMap = new ConcurrentHashMap<String, String>();
    HashMap<String, String> remotePortMap;
    static HashMap<String, Integer> proposedMap = new HashMap<String, Integer>();
    static HashMap<String, Double> maxPriorityMap = new HashMap<String, Double>();
    static HashMap<String, String> deliveryMap = new HashMap<>();

    private Uri myUri = null;
    private static final String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger1.provider";
    private static final String SCHEME = "content";

    private int counter = 0;
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    ArrayList<String> remotePortList = null;
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";

    static final int SERVER_PORT = 10000;
    static String myPort;
    TextView tv = null;

    private final String indentifier = "mimanshu";
    private static double uniqueIden;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        remotePortList = new ArrayList<String>();
        remotePortList.add(REMOTE_PORT0);
        remotePortList.add(REMOTE_PORT1);
        remotePortList.add(REMOTE_PORT2);
        remotePortList.add(REMOTE_PORT3);
        remotePortList.add(REMOTE_PORT4);

        remotePortMap = new HashMap<String, String>();
        remotePortMap.put("5554", "11108");
        remotePortMap.put("5556", "11112");
        remotePortMap.put("5558", "11116");
        remotePortMap.put("5560", "11120");
        remotePortMap.put("5562", "11124");

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        uniqueIden= (Integer.parseInt(myPort)%10)*0.1;

        try
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(AUTHORITY);
            uriBuilder.scheme(SCHEME);
            myUri = uriBuilder.build();
        }
        catch (Exception ex)
        {
            Log.v(TAG, "URI generation error");
        }

        try
        {
            ServerSocket socket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, socket);
        }
        catch (IOException ex)
        {
            Log.v(TAG, "Server Socket creation Error");
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        //My Code -----------------------------
        final EditText et = (EditText)findViewById(R.id.editText1);

        findViewById(R.id.button4).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tv.append(message);
                final String message = et.getText().toString();
                et.setText("");

                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, message);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    /***
     * ServerTask is an AsyncTask that should handle incoming messages. It is created by
     * ServerTask.executeOnExecutor() call in SimpleMessengerActivity.
     *
     * Please make sure you understand how AsyncTask works by reading
     * http://developer.android.com/reference/android/os/AsyncTask.html
     *
     * @author stevko
     *
     */
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        //Code reused from PA-1
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            //--------My Code Starts
            try
            {
                while(true)
                {
                    Socket socket = serverSocket.accept();
                    InputStream read = socket.getInputStream();
//                    InputStreamReader reader = new InputStreamReader(read);
                    StringBuffer buffer = new StringBuffer();
                    ObjectInputStream io = new ObjectInputStream(read);
                    Transfer obj = null;
                    try {
                        obj = (Transfer) io.readObject();
                    }
                    catch(ClassNotFoundException ex)
                    {
                        Log.v(TAG,"Class Not found when read the object ::"+ex.getMessage());
                    }
                    if(obj != null)
                        Log.v(TAG, "Not Null Object");
//                    BufferedInputStream buff = new BufferedInputStream(read);
//                    byte[] bb = new byte[128];
//                    buff.read(bb,0,128);
//                    int data = reader.read();
//                    while(data != -1)
//                    {
//                        buffer.append((char)data);
//                        data = reader.read();
//                    }
//                    reader.close();

//                    publishProgress(buffer.toString());
//                    String[] msg = buffer.toString().split(indentifier);
//                    String mm = new String(bb);
//                    buff.close();
//                    String[] msg = mm.split(indentifier);
//                    //If this process receives a normal message
//                    if(!msg[0].equals("order") && !msg[0].equals("finalOrder"))
//                    {
//                        String id = msg[0];
//                        String portNum = id.split("-")[0];
//                        String val = msg[1];
//                        double propNum = ++proposalNum + uniqueIden;
//
//                        if(!holdBackMap.containsKey(id))
//                        {
//                            holdBackMap.put(id, val);
//                            prioritySet.add(new Message(id, propNum, false));
//                        }
//
//                        String proposeMsg = "order" + indentifier + id + indentifier + propNum;
//                        unicast(proposeMsg, portNum);
//                    }
//                    else if(msg[0].equals("order"))
//                    {
//                        double priorityNum = Double.parseDouble(msg[2]);
//
//                        if(!proposedMap.containsKey(msg[1]))
//                        {
//                            proposedMap.put(msg[1], 1);
//                            maxPriorityMap.put(msg[1], priorityNum);
//                        }
//                        else
//                        {
//                            proposedMap.put(msg[1], proposedMap.get(msg[1]) + 1);
//                            if(maxPriorityMap.get(msg[1]) < priorityNum)
//                                maxPriorityMap.put(msg[1], priorityNum);
//                        }
//
//                        if(proposedMap.get(msg[1]) == 5)
//                        {
//                            String proposeMsg = "finalOrder" + indentifier + msg[1] + indentifier + maxPriorityMap.get(msg[1]);
//                            multicast(proposeMsg);
//                        }
//                    }
//
//                    else if(msg[0].equals("finalOrder"))
//                    {
//                        Message m = new Message(msg[1], Double.parseDouble(msg[2]), true);
//                        Iterator<Message> it = prioritySet.iterator();
//                        while(it.hasNext())
//                        {
//                            Message temp = it.next();
//                            if(temp.id.equals(m.id))
//                            {
//                                if(prioritySet.remove(temp))
//                                    prioritySet.add(m);
//
//                                break;
//                            }
//                        }
//
//                        it = prioritySet.iterator();
//                        while(it.hasNext())
//                        {
//                            Message temp = it.next();
//                            if(deliveryCount + 1 == (int)temp.priorityNum)
//                                if(prioritySet.remove(temp))
//                                {
//                                    deliveryCount++;
//                                    publishProgress(new String[]{holdBackMap.get(msg[1]), deliveryCount+""});
//                                }
//                        }
//                    }


                    socket.close();
                }
            }
            catch (IOException ex){
                Log.v(TAG, "Error in ServerTask::"+ex.getMessage());
            }

            //--------My Code Ends
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            String strReceived = strings[0].trim();
            if(tv != null)
                tv.append(strReceived+"\t\n");

            ContentValues values = new ContentValues();
            values.put("key", strings[1]);
            values.put("value", strReceived);
            getContentResolver().insert(myUri, values);
            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */

            String filename = "SimpleMessengerOutput";
            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }

    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {
                String id = myPort+"-"+counter++;
//                Socket socket;
                String msgToSend = msgs[0];
//                msgToSend = id + indentifier + msgToSend;
//                OutputStream out;
//                OutputStreamWriter writer;
//                Iterator<String> it = remotePortList.iterator();
//                while(it.hasNext())
//                {
//                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                            Integer.parseInt(it.next()));
//                    out = socket.getOutputStream();
//                    writer = new OutputStreamWriter(out);
//                    writer.write(msgToSend);
//                    writer.flush();
//                    writer.close();
//                    socket.close();
//                }
//                deliveryMap.put(id, msgToSend);
                multicast(new Transfer(id, msgToSend, "Normal"));
//                //--------My Code Ends
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

    public class Message implements Comparable
    {
        String id;
        double priorityNum;
        boolean isFinalProposed;

        public Message(String id, double priorityNum, boolean isFinalProposed)
        {
            this.id = id;
            this.priorityNum = priorityNum;
            this.isFinalProposed = isFinalProposed;
        }

        @Override
        public int compareTo(Object o1)
        {
            Message temp = (Message)o1;
            if(this.priorityNum < temp.priorityNum)
                return -1;
            else if(this.priorityNum == temp.priorityNum)
                return 0;
            else
                return 1;
        }
    }

    public class PQueueComaparator implements Comparator<Message>
    {
        @Override
        public int compare(Message lhs, Message rhs) {
            if(lhs.priorityNum < rhs.priorityNum)
                return -1;
            else if(lhs.priorityNum == rhs.priorityNum)
                return 0;
            else
                return 1;
        }
    }

    private void multicast(Transfer msgToSend) throws IOException, UnknownHostException
    {
        try {
            Socket socket;
            OutputStream out;
            OutputStreamWriter writer;

            ObjectOutputStream write;

            Iterator<String> it = remotePortList.iterator();
            while (it.hasNext()) {
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(it.next()));
                out = socket.getOutputStream();
//                writer = new OutputStreamWriter(out);
                write = new ObjectOutputStream(out);
                write.writeObject(msgToSend);
                write.close();;
                out.close();
//                writer.write(msgToSend);
//                writer.flush();
//                writer.close();
                socket.close();
            }
        }
        catch (UnknownHostException e) {
            Log.e(TAG, "Multicast ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "Multicast ClientTask socket IOException");
        }
    }

    private void unicast(String msg, String portNum)
    {
        try {
            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(portNum));
            OutputStream out = socket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(msg);
            writer.flush();
            writer.close();
            socket.close();
        }
        catch (UnknownHostException e) {
            Log.e(TAG, "Unicast ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "Unicast ClientTask socket IOException");
        }
    }
}

class Transfer implements Serializable
{
    private static final long serialVersionUID = 1L;
    String id;
    String message;
    String type;
    public Transfer(String id, String message, String type)
    {
        this.id = id;
        this.message = message;
        this.type = type;
    }
}