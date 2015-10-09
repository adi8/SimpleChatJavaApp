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
    
    /**
     *  Start a timer and sends appropriate messages when the limit is reached.
     */
    @Override
    public void run()
    {
        
        while(!exit && System.currentTimeMillis()<end) // Waits for 'end' amount of time(seconds) and checks if the timer is to be stopped.
        {}        
        
        if(exit) // Checks if the thread is to be interrupted.
        {
            interrupt(); //  Interrupts the timer.
        }
        else
        {
            try 
            {
                os = socket.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
                
                System.out.println("System is logging out.");
                bw.write("logout\n");  // Logs out the user if the timer limit is exceeded.
                bw.flush();
                
            } catch (IOException ex) {
                Logger.getLogger(ClientTimer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
    
