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

/**
 *
 * @author Aditya Venkateshwaran
 */
public class Server {

    protected static ArrayList user;
    protected static ArrayList pass;
    private static int port;
    //protected static ArrayList users_ol = new ArrayList();
    protected static Hashtable<String, InetAddress> users_ol = new Hashtable<String, InetAddress>();
    protected static final long BLOCK_TIME = 60*1000;
    protected static Hashtable<String, Long> users_blocked = new Hashtable<String, Long>();
    protected static Hashtable<String, Long> users_offline = new Hashtable<String, Long>();
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException 
    {
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
    
    public static boolean authenticate(String a[])
    {
        int temp;
        if(user.contains(a[1]))
        {
            temp = user.indexOf(a[1]);

            if(pass.get(temp).equals(a[2]))
                return true;
            
            return false;
        }
        
        return false;
    }
    
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
                usrol += usr_ol[i] +"\n";
        }
        
        if(usrol.equals(""))
            return "No one is online!";
        else
            return usrol;
    }
    
    public static void logout(String usr)
    {
        users_ol.remove(usr);
        users_offline.put(usr, System.currentTimeMillis());
    }
    
    public static String wholast(String inp[])
    {
        String tmp = Arrays.toString(users_ol.keySet().toArray());
        String ul[] = tmp.substring(1, tmp.length()-1).split(", ");
        String user_last = "";
        int i;
        
        for(i = 0; i < ul.length; i++)
            user_last += ul[i] + "\n";
        
        long limit = Integer.parseInt(inp[2])*60*60*1000;
        long time = System.currentTimeMillis() - limit;
        
        if(!users_offline.isEmpty())
        {
            Set<String> keys = users_offline.keySet();
            for(String key : keys)
            {
                long utime = users_offline.get(key);
                if(utime > time)
                    user_last += key +"\n";
            }
        }
        
        return user_last;
    }
    
    public static long isBlocked(String usr)
    {
        long tmp = 0;
        if(users_blocked.contains(usr))
        {
            tmp = System.currentTimeMillis() - Server.users_blocked.get(usr);
            if(tmp>Server.BLOCK_TIME)
            {
                Server.users_blocked.remove(usr);
            }
            else
                tmp = -1;
        }
        return tmp;
    }
}
