/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class Client
{
    
    private static Socket s;
    private static InputStream is;
    private static InputStreamReader isr;
    private static BufferedReader br;
        
    private static OutputStream os;
    private static OutputStreamWriter osw;
    private static BufferedWriter bw;
    
    private static boolean flag;
    private static final long TIME_OUT = 1*60*1000;

    /**This method creates a client and sets up a session with the server by using socket 
     * Allows the user to enter commands using a while loop 
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException 
    {
        attachShutDownHook();
        flag = true;
        int port;
        String ip;
        String command;
        String msg;
        
        //port = Integer.parseInt(args[1]);
        //ip = args[0];
        //InetAddress localAddr = InetAddress.getByName("locahost");
        //try 
        //{
        //    Socket s  = new Socket(ip, port, locaAddr, port);
        //}
        //catch(Exception e)
        //{   
        //    flag = false;
        //    System.out.println("Server is not running!! Try again later."); 
        //}
        
        port = 4119;
        ip = "10.6.31.102";
        try
        {
            s = new Socket("localhost", port);
        }
        catch(Exception e) // To check if the server is up and running.
        {
            flag = false;
            System.out.println("Server is not running!! Try again later.");
        }
        
        
        
        int tmp;
        
        if(flag == true)
        {
            while(true) // To send user credentials to the server to login.
            {
                isr = new InputStreamReader(System.in);
                br = new BufferedReader(isr);

                System.out.println("Enter Username:");
                String username = br.readLine().trim();

                System.out.println("Enter the password:");
                String password = br.readLine();

                os = s.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);

                bw.write(username+" "+password+"\n");
                bw.flush();

                is = s.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                msg = br.readLine();
                System.out.println(msg);

                tmp = Integer.parseInt(br.readLine());
                
                /*
                    tmp == 0: indicates either the user is blocked or has entered the wrong username or password 3 times.
                    tmp == 2: indicates that the user has successfully logged in.
                    tmp == 1: (not checked for) indicates user has still some attempts remaining to try logging in.
                */
                if(tmp == 0 || tmp == 2)
                    break;
            }

            if(!(tmp == 0)) // Checks if the user is blocked or 
            {   
                ClientTimer ct;
                ClientReceiveThread c = new ClientReceiveThread(s); // Creates the client thread to read the users inputstream for messages from the server.
                c.start(); 

                System.out.println("Enter commands to start chatting!");
                System.out.print("Command:");

                while(true) // Loop to allow the user to input command.
                {
                    ct = new ClientTimer(s,System.currentTimeMillis()+TIME_OUT); // Creates a new timer to check for user inactivity.
                    ct.start();

                    br = new BufferedReader(new InputStreamReader(System.in));
                    command = br.readLine(); 

                    ct.exit = true; // Resets the timer as user entered a command and is active.

                    bw.write(command+"\n");
                    bw.flush();
                }
            }
        }
    }
    
    public static void attachShutDownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                try 
                {
                    if(!s.isInputShutdown() && flag)
                    {
                        os = s.getOutputStream();
                        osw = new OutputStreamWriter(os);
                        bw = new BufferedWriter(osw);
                    
                        bw.write("logout\n");
                        bw.flush();
                        System.out.println(System.lineSeparator()+"Logged out!");
                        s.close();
                    }
                    else
                        s.close();
                    
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
}
