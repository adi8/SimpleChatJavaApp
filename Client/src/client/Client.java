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

/**
 *
 * @author Aditya Venkateshwaran
 */
public class Client extends Thread{
    
    private static Socket s;
    private static InputStream is;
    private static InputStreamReader isr;
    private static BufferedReader br;
        
    private static OutputStream os;
    private static OutputStreamWriter osw;
    private static BufferedWriter bw;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException 
    {
                
        //String temp[] = Arrays.toString(args).split(" ");
        
        int port;
        String ip;
        String command="";
        String msg = "";
        
        //port = Integer.parseInt(temp[1]);
        //ip = temp[0];
        port = 4119;
        ip = "10.6.31.102";
        s = new Socket("localhost", port);
        //InetAddress localAddr = InetAddress.getByName("locahost");
        //Socket s  = new Socket(ip, port, locaAddr, port);
        int tmp;
        
        while(true)
        {
            isr = new InputStreamReader(System.in);
            br = new BufferedReader(isr);
            
            System.out.println("Enter Username:");
            String username = br.readLine();

            System.out.println("Enter the password:");
            String password = br.readLine();

            os = s.getOutputStream();
            osw = new OutputStreamWriter(os);
            bw = new BufferedWriter(osw);

            bw.write(ip+" "+username+" "+password+"\n");
            bw.flush();

            is = s.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            msg = br.readLine();
            System.out.println(msg);
            
            tmp = Integer.parseInt(br.readLine());
            
            if(tmp == 0 || tmp == 2)
                break;
        }
        
        ClientReceiveThread c = new ClientReceiveThread(s);
        c.start();
        
        if(!(tmp == 0))
        {    
            while(true)
            {
               //System.out.println("Command: ");
               br = new BufferedReader(new InputStreamReader(System.in));
               command = br.readLine();
               
               bw.write(command+"\n");
               bw.flush();
               Client.sleep(500);
            }
        }
    }
}
