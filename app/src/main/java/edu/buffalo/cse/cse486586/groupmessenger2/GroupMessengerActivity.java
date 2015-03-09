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

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.concurrent.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.*;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static private int deliveryCount = 0;
    static private int proposalNum = 0;
    private int counter = 0;

    static TreeSet<Message> prioritySet = new TreeSet<Message>();
    static ConcurrentHashMap<String, Message> holdBackMap = new ConcurrentHashMap<String, Message>();
    static HashMap<String, Integer> fifoVector = new HashMap<>();
    static HashMap<String, Integer> proposedMap = new HashMap<String, Integer>();
    static HashMap<String, Double> maxPriorityMap = new HashMap<String, Double>();
    static HashMap<String, PriorityQueue<Message>> fifoOrderQueue = new HashMap<String, PriorityQueue<Message>>();
    static HashMap<String, Timer> timerMap = new HashMap<String, Timer>();

    private Uri myUri = null;
    private static final String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger2.provider";
    private static final String SCHEME = "content";
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

        PriorityQueue<Message> queue = new PriorityQueue<Message>(10, new FifoComaparator());
        fifoOrderQueue.put(REMOTE_PORT0, queue);
        fifoOrderQueue.put(REMOTE_PORT1, queue);
        fifoOrderQueue.put(REMOTE_PORT2, queue);
        fifoOrderQueue.put(REMOTE_PORT3, queue);
        fifoOrderQueue.put(REMOTE_PORT4, queue);

        fifoVector.put(REMOTE_PORT0, 0);
        fifoVector.put(REMOTE_PORT1, 0);
        fifoVector.put(REMOTE_PORT2, 0);
        fifoVector.put(REMOTE_PORT3, 0);
        fifoVector.put(REMOTE_PORT4, 0);

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
                    ObjectInputStream io = new ObjectInputStream(read);
                    Normal objMessage = null;
                    Agreed objAgreement = null;
                    Proposal objProposal = null;
                    try {
                        Object obj = io.readObject();
                        if(obj instanceof Normal)
                            objMessage = (Normal)obj;
                        if(obj instanceof Proposal)
                            objProposal = (Proposal)obj;
                        if(obj instanceof Agreed)
                            objAgreement = (Agreed)obj;
                    }
                    catch(ClassNotFoundException ex)
                    {
                        Log.v(TAG,"Class Not found when read the object ::"+ex.getMessage());
                    }

                    if(objMessage != null)
                    {
                        String id = objMessage.id;
                        String portNum = id.split("-")[0];
                        String val = objMessage.message;
                        double propNum = ++proposalNum + uniqueIden;

                        if(!holdBackMap.containsKey(id))
                        {
                            Message temp = new Message(id, propNum, false, objMessage.sendOrder, val);
                            holdBackMap.put(id, temp);
                            prioritySet.add(temp);
                            Timer time = new Timer();
                            time.schedule(new failureHandler(id), 1000);
                            timerMap.put(id,time);
                        }

                        Proposal objProposed = new Proposal(id,propNum,portNum);
                        new ProposalClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, objProposed);
                    }

                    else if(objProposal != null)
                    {
                        double priorityNum = objProposal.proposedNum;

                        if(!proposedMap.containsKey(objProposal.id))
                        {
                            proposedMap.put(objProposal.id, 1);
                            maxPriorityMap.put(objProposal.id, priorityNum);
                        }

                        else
                        {
                            proposedMap.put(objProposal.id, proposedMap.get(objProposal.id) + 1);
                            if(maxPriorityMap.get(objProposal.id) < priorityNum)
                                maxPriorityMap.put(objProposal.id, priorityNum);
                        }

                        if(proposedMap.get(objProposal.id) == 5)
                        {
                            Agreed objAgreed = new Agreed(objProposal.id, maxPriorityMap.get(objProposal.id));
                            new AgreementClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, objAgreed);
                        }
                    }

                    else  if(objAgreement != null)
                    {
                        Message m = new Message(objAgreement.id, objAgreement.agreedNumber, true,0,"");
                        Timer time = timerMap.get(objAgreement.id);
                        time.cancel();
                        Iterator<Message> it = prioritySet.iterator();
                        while(it.hasNext())
                        {
                            Message temp = it.next();
                            if(temp.id.equals(m.id))
                            {
                                it.remove();
                                m.order = temp.order;
                                m.msg = temp.msg;
                                prioritySet.add(m);
                                break;
                            }
                        }
                        it = prioritySet.iterator();
                        while(it.hasNext())
                        {
                            Message temp = it.next();
                            if(!temp.isFinalProposed) break;
                            it.remove();
                            String process = temp.id.split("-")[0];
                            if(fifoOrderQueue.containsKey(process))
                            {
                                PriorityQueue<Message> queue = fifoOrderQueue.get(process);
                                queue.add(temp);

                                Message last = queue.peek();
                                if(fifoVector.containsKey(process))
                                {
                                    int lastCount  = fifoVector.get(process);
                                    if(lastCount + 1 == last.order)
                                    {
                                        last = queue.remove();
                                        fifoOrderQueue.put(process, queue);
                                        fifoVector.put(process, lastCount + 1);
                                        publishProgress(new String[]{holdBackMap.get(last.id).msg, deliveryCount+""});
                                        deliveryCount++;
                                    }
                                }
                            }
                        }
                    }
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
//
//            String filename = "SimpleMessengerOutput";
//            String string = strReceived + "\n";
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(string.getBytes());
//                outputStream.close();
//            } catch (Exception e) {
//                Log.e(TAG, "File write failed");
//            }

            return;
        }

    }

    private class failureHandler extends TimerTask
    {
        String msgId;
        public failureHandler(String id)
        {
            msgId = id;
        }

        @Override
        public void run() {
            Message temp = holdBackMap.get(msgId);
            prioritySet.remove(temp);
            tv.append("Process Failed "+msgId);
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

            String id = myPort+"-"+ ++counter;
            String msgToSend = msgs[0];
            Normal objTransfer = new Normal(id,msgToSend,counter);
            Socket socket;
            OutputStream out;
            ObjectOutputStream writer;
            Iterator<String> it = remotePortList.iterator();
            while (it.hasNext()) {
                try {
                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(it.next()));
                    out = socket.getOutputStream();
                    writer = new ObjectOutputStream(out);
                    writer.writeObject(objTransfer);
                    writer.close();
                    out.close();
                    socket.close();
                }
                catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                }
            }
            return null;
        }
    }
    private class AgreementClientTask extends AsyncTask<Agreed, Void, Void> {

        @Override
        protected Void doInBackground(Agreed... msgs) {
                Agreed objTransfer = msgs[0];
                Socket socket;
                OutputStream out;
                ObjectOutputStream writer;
                Iterator<String> it = remotePortList.iterator();
                while (it.hasNext()) {
                    try {
                        socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(it.next()));
                        out = socket.getOutputStream();
                        writer = new ObjectOutputStream(out);
                        writer.writeObject(objTransfer);
                        writer.close();
                        out.close();
                        socket.close();
                    } catch (UnknownHostException e) {
                        Log.e(TAG, "ClientTask UnknownHostException");
                    } catch (IOException e) {
                        Log.e(TAG, "ClientTask socket IOException");
                    }
                }
            return null;
        }
    }
    private class ProposalClientTask extends AsyncTask<Proposal, Void, Void> {

        @Override
        protected Void doInBackground(Proposal... msgs) {
            try {
                Proposal objProposed = msgs[0];

                Socket socket;
                OutputStream out;
                ObjectOutputStream writer;
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(objProposed.portNum));
                out = socket.getOutputStream();
                writer = new ObjectOutputStream(out);
                writer.writeObject(objProposed);
                writer.close();
                out.close();
                socket.close();
            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }

    static class PQueueComaparator implements Comparator<Message>
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

    static class FifoComaparator implements Comparator<Message>
    {
        @Override
        public int compare(Message lhs, Message rhs) {
            if(lhs.order < rhs.order)
                return -1;
            else if(lhs.order == rhs.order)
                return 0;
            else
                return 1;
        }
    }
}




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