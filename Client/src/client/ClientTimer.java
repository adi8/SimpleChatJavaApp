/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 * 
 */
public class ClientTimer extends Thread {
    
    protected volatile boolean exit;
    private final Socket socket;
    private final long end;
    
    private static OutputStream os;
    private static OutputStreamWriter osw;
    private static BufferedWriter bw;
    
    public ClientTimer(Socket s, long e)
    {
        socket = s;
        exit = false;
        end = e;
    }
    
    @Override
    public void run()
    {
        
        while(!exit && System.currentTimeMillis()<end)
        {}        
        
        if(exit)
        {
            interrupt();
        }
        else
        {
            try 
            {
                os = socket.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
                
                System.out.println("System is logging out.");
                bw.write("logout\n");
                bw.flush();
            
                
            } catch (IOException ex) {
                Logger.getLogger(ClientTimer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
    
