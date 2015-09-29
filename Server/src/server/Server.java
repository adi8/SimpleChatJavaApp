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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class Server {

    protected static ArrayList user;
    protected static ArrayList pass;
    private static int port;
    protected static ArrayList users_ol = new ArrayList();
    protected static final long BLOCK_TIME = 60*1000;
    protected static Hashtable<String, Long> users_blocked = new Hashtable<String, Long>();
    
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
        String usr_ol[]= new String[users_ol.size()];
        int i;
        
        users_ol.toArray(usr_ol);
        
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
    }
    
    public static String wholast(String inp[])
    {
        return null;
    }
}
