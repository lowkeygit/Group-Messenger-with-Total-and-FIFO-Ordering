package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;
import java.util.Timer;

/**
 * Created by sherlock on 3/8/15.
 */
public class Message implements Comparable
{
    String id;
    double priorityNum;
    boolean isFinalProposed;
    int order;
    String msg;

    public Message(String id, double priorityNum, boolean isFinalProposed, int order, String msg)
    {
        this.id = id;
        this.priorityNum = priorityNum;
        this.isFinalProposed = isFinalProposed;
        this.order = order;
        this.msg = msg;
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


//
//    private void multicast(Normal msgToSend) throws IOException
//    {
//        try {
//            Socket socket;
//            OutputStream out;
//            OutputStreamWriter writer;
//
//            ObjectOutputStream write;
//
//            Iterator<String> it = remotePortList.iterator();
//            while (it.hasNext()) {
//                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                        Integer.parseInt(it.next()));
//                out = socket.getOutputStream();
////                writer = new OutputStreamWriter(out);
//                write = new ObjectOutputStream(out);
//                write.writeObject(msgToSend);
//                write.close();;
//                out.close();
////                writer.write(msgToSend);
////                writer.flush();
////                writer.close();
//                socket.close();
//            }
//        }
//        catch (UnknownHostException e) {
//            Log.e(TAG, "Multicast ClientTask UnknownHostException");
//        } catch (IOException e) {
//            Log.e(TAG, "Multicast ClientTask socket IOException");
//        }
//    }
//
//    private void unicast(String msg, String portNum)
//    {
//        try {
//            Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
//                    Integer.parseInt(portNum));
//            OutputStream out = socket.getOutputStream();
//            OutputStreamWriter writer = new OutputStreamWriter(out);
//            writer.write(msg);
//            writer.flush();
//            writer.close();
//            socket.close();
//        }
//        catch (UnknownHostException e) {
//            Log.e(TAG, "Unicast ClientTask UnknownHostException");
//        } catch (IOException e) {
//            Log.e(TAG, "Unicast ClientTask socket IOException");
//        }
//    }




///////////////


////                    String[] msg = buffer.toString().split(indentifier);
////                    String mm = new String(bb);
////                    buff.close();
////                    String[] msg = mm.split(indentifier);
//                    //If this process receives a normal message
//                    if(!msgReceived.type.equals("order") && !msgReceived.type.equals("finalOrder"))
//                    {
//                        String id = msgReceived.id;
//                        String portNum = id.split("-")[0];
//                        String val = msgReceived.message;
//                        double propNum = ++proposalNum + uniqueIden;
//
//                        if(!holdBackMap.containsKey(id))
//                        {
//                            holdBackMap.put(id, val);
//                            prioritySet.add(new Message(id, propNum, false));
//                        }
//
//                        String proposeMsg = "order" + indentifier + id + indentifier + propNum;
//                        Transfer proposal = new Transfer(id, "", "order");
//                        proposal.priorNum = propNum+"";
//                        unicast(proposeMsg, portNum);
//                    }
//                    else if(msgReceived.type.equals("order"))
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