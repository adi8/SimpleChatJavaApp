/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class Server 
{

    protected static ArrayList user;
    protected static ArrayList pass;
    private static int port;
    protected static Hashtable<String, InetAddress> users_ol = new Hashtable<String, InetAddress>();
    protected static final long BLOCK_TIME = 60*1000;
    protected static Hashtable<String, Long> users_blocked = new Hashtable<String, Long>();
    protected static Hashtable<String, Long> users_offline = new Hashtable<String, Long>();
    protected static ArrayList<ServerThread> sthreads = new ArrayList<ServerThread>();
    
    /**
     * This method creates a new server thread for each client to process their requests.
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException 
    {
        attachShutDownHook();
        port = 4119;
        //port = Integer.parseInt(Arrays.toString(args));
        
        init();
        ServerSocket ss = new ServerSocket(port);
        
        System.out.println("Server Started");
        
        while(true)
        {
            Socket s = ss.accept();
            new ServerThread(s).start();
        }
    }
    
    /**
     * Initializes the server. Reads the already registered user password text file.
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void init() throws FileNotFoundException, IOException
    {
        user = new ArrayList();
        pass = new ArrayList();    
        
        FileInputStream fis;
        int port;
        File f;
        byte in[];
        int l, i;
        
        f = new File("user_pass.txt");
        fis = new FileInputStream(f);
        l = (int)f.length();
        in = new byte[l];
        fis.read(in);
        fis.close();
        
        String u_p_string = new String(in);
        
        String temp[]= u_p_string.split(" ");
        
        String temp_usr="";
        String temp_pass="";
        
        for(i = 0; i < temp.length; i++)
        {
            if(i % 2 == 0)
                temp_usr += temp[i].trim()+" ";
            else
                temp_pass += temp[i].trim()+ " ";
        }
        
        String p[] = temp_pass.split(" ");
        String u[] = temp_usr.split(" ");
        
        for(i = 0; i < u.length; i++)
        {
            user.add(u[i]);
            pass.add(p[i]);
        }
    }
    
    /**
     * This method authenticates the user. 
     * @param a
     * @return returns true if the users password matches else false
     */
    public static boolean authenticate(String a[])
    {
        int temp;
        if(user.contains(a[0]))
        {
            temp = user.indexOf(a[0]);

            if(pass.get(temp).equals(a[1]))
                return true;
            
            return false;
        }
        
        return false;
    }
    
    /**
     * This method shows the users that are currently online.
     * @param usr
     * @return String which contains the online users
     */
    public static String whoelse(String usr)
    {
        int i;
        String tmp = Arrays.toString(users_ol.keySet().toArray());
        int l = tmp.length();
        String usr_ol[] = tmp.substring(1, l-1).split(", ");
        
        String usrol = "";
        for(i = 0; i < usr_ol.length; i++)
        {
            if(!usr.equals(usr_ol[i]))
                usrol += usr_ol[i] +" ";
        }
        
        if(usrol.equals(""))
            return "No one is online!";
        else
            return usrol;
    }
    
    /**
     * This method logs out the user by removing it from the users_ol list and adding to the users_offline list.
     * @param usr 
     */
    public static void logout(String usr)
    {
        users_ol.remove(usr);
        users_offline.put(usr, System.currentTimeMillis());
    }
    
    /**
     *  This method returns the users that were online during the past t minutes.
     * @param t
     * @param name
     * @return String containing list of users
     */
    public static String wholast(String t, String name)
    {
        try
        {
            String user_last = "";

            if(!users_ol.isEmpty()) 
            {
                Set<String> users = users_ol.keySet();
                for(String s: users) // Adds users currently online to the wholast list. 
                {
                    if(!name.equalsIgnoreCase(s))
                        user_last += s +" ";
                }
            }
            long limit = Integer.parseInt(t)*60*1000;
            long time = System.currentTimeMillis() - limit;

            if(!users_offline.isEmpty())
            {
                Set<String> keys = users_offline.keySet();
                for(String key : keys) // Adds users who are currently offline but meet the time limit condition.
                {
                    long utime = users_offline.get(key);
                    if(utime > time && !name.equalsIgnoreCase(key)) // To check if the offline user 'key' falls within the time limit. 
                            user_last += key +" ";
                }
            }
            if(user_last.equalsIgnoreCase("")) // Checks if the list of users logged in previously within the time limit is null.
                return "No one was Online";
            else
                return user_last;
        }catch(Exception e)
        {
            return "Error. Something went wrong. Check the command sent.";
        }
    }
    
    /**
     *  This method checks whether a user is blocked.
     * @param usr
     * @return boolean value depending on whether the usr is blocked(true) or not(false)
     */
    public static boolean isBlocked(String usr)
    {
        long tmp = 0;
        boolean flag = false;
        
        if(users_blocked.containsKey(usr)) // Checks if user is in blocked list.
        {
            tmp = System.currentTimeMillis() - Server.users_blocked.get(usr);
            if(tmp>Server.BLOCK_TIME) // Checks if BLOCK_TIME has been exceeded. 
            {
                Server.users_blocked.remove(usr); // Removes user 'usr' from blocked list.
            }
            else
            {
                flag = true;
            }
        }
        return flag;
    }
    
    /**
     *  This method broadcasts a message to either all online users or select users.
     * @param inp
     * @param name
     * @param choice
     * @return String: Conformation of the message sent OR Users to whom message wasn't sent.
     * @throws InterruptedException
     * @throws IOException 
     */
    public static String broadcast(String inp[], String name, int choice) throws InterruptedException, IOException
    {
        String msg = "";
        String output="Broadcast message sent!!";
        int i;
        int l;
        
        if(choice == 1)
        {
            ArrayList<String> u = new ArrayList<String>();  // Stores the users to whom message is to be sent.
            l = inp.length;
            for(i = 2; i < l; i++) // Extracts the users from the command.
            {
                if(inp[i].equalsIgnoreCase("message"))
                    break;
                else
                    u.add(inp[i]);
            }

            for(i = i+1; i < l; i++) // Extracts the message from the command.
            {
                msg += inp[i]+" ";
            }

            for(ServerThread s : sthreads) 
            {
                if(u.contains(s.getName())) // Checks the users who are online (from the list) and sends the message to them.
                {
                    u.remove(s.getName());
                    s.messages.put(name+":"+msg);
                    s.sendMsg();
                }
            }
            
            if(!u.isEmpty()) // Checks if there were users offiline when the message was sent.
            {
                l = u.size();
                output = "Following Users not online. Message not sent to them! : ";
                
                for(i = 0; i < l; i++) 
                {
                    output += u.get(i)+ " ";
                }
            }
            
            return output;
        }
        else
        {
            l = inp.length;
            
            for(i = 2; i < l; i++) // Extracts the message from the command.
            {
                msg += inp[i]+" ";
            }
            
            for(ServerThread s : sthreads)
            {
                if(!s.getName().equalsIgnoreCase(name)) // Prevents sending a message to one self.
                {   
                    s.messages.put(name+":"+msg);
                    s.sendMsg();
                }
            }
            
            return output;
        }
    }
    
    /**
     *  This message sends a private message to a specific user.
     * @param command
     * @param name
     * @return String: Confirmation of the message sent OR not sent. 
     * @throws InterruptedException
     * @throws IOException 
     */
    public static String privateMsg(String command, String name) throws InterruptedException, IOException
    {
        String tmp[] = command.split(" ");
        String output = "Message sent!!";
        String u = tmp[1];  // Stores the user to whom private message is to be sent.
        String msg = "";
        int i;
        int l = tmp.length;
        
        for(i = 2; i < l; i++)  // Extracts the message from the command.
            msg+=tmp[i]+" ";
        
        if(users_ol.containsKey(u))
        {
            for(ServerThread s: sthreads)  // Sends message to the user 'u'.
            {
                if(s.getName().equalsIgnoreCase(u))
                {
                    s.messages.put(name+":"+msg);
                    s.sendMsg();
                    break;
                }
            }
            
            return output;
        }
        else
        {
            return "User: "+u+" not online! Message discarded!";
        }
    }
    
    /**
     * This method captures the ctrl+c and performs necessary operations.
     */
    public static void attachShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                
                for(ServerThread s: sthreads)
                {
                    try {
                        s.messages.put("Server terminated! Sorry for the inconvenience. Try logging again later.");
                        s.sendMsg();
                        sthreads.remove(s);
                        s.interrupt();
                        
                    } catch (InterruptedException | IOException ex) {
                        Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                System.out.println(System.lineSeparator()+"System has been terminated!");
            }
        });
    }
    
}
