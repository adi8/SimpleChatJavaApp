/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class ClientReceiveThread extends Thread {
    
    private  InputStream is;
    private  InputStreamReader isr;
    private  BufferedReader br;
    private  Socket s;
    
    public ClientReceiveThread(Socket socket)
    {
        s = socket;
    }
            
    @Override
    public void run() 
    {
        String msg="";
        do
        {
            try 
            {
                is = s.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
                
                msg = br.readLine();
                if(!msg.equals("Logged Out."))
                    System.out.print(System.lineSeparator()+msg+System.lineSeparator()+"Command:");
                else
                {
                    System.out.println(msg);
                    s.shutdownInput();
                }
                
            } catch (IOException ex) {
                Logger.getLogger(ClientReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }while(!msg.equals("Logged Out."));
        
        System.exit(0);
    } 
    
}
