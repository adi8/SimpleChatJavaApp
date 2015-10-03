/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class ServerThread extends Thread 
{
    private Socket s;
    protected String username;
//    protected BlockingQueue messages = new LinkedBlockingQueue();
    
    private InputStream is = null;   
    private InputStreamReader isr= null;
    private BufferedReader br = null;
    
    private OutputStream os = null;
    private OutputStreamWriter osw = null;
    private BufferedWriter bw = null;
    
    public ServerThread(Socket clientSocket)
    {
        s = clientSocket;   
    }
    
    public void run()
    {
        try 
        {
            boolean flag = false;
            int i = 3;
            String in[];
            do
            {
                is = s.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
            
                String input="";
                input = br.readLine();
            
                in = input.split(" ");
            
                os = s.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
                
                long sec = Server.isBlocked(in[1]);
                
                if(sec!=-1 && Server.authenticate(in))
                {
                    flag = true;
                    Server.users_ol.put(in[1], s.getInetAddress());
                    if(Server.users_offline.contains(in[1]))
                        Server.users_offline.remove(in[1]);
                    bw.write("Welcome!\n");
                    bw.write("2\n");
                }
                else if(sec == -1)
                {
                    bw.write("User is blocked. Try after "+(60-(sec/1000))+" seconds.\n");
                    bw.write("0\n");
                }
                else
                {
                    i--;
                    if(i!=0)
                    {
                        bw.write("Either user not registered or password didn't match. Try Again! "
                                + "("+i+" attempts remaining.)\n");
                        bw.write("1\n");
                    }
                    else
                    {
                        if(Server.user.contains(in[1]))
                        {
                            Server.users_blocked.put(in[1], System.currentTimeMillis());
                        }
                        bw.write("Blocked\n");
                        bw.write("0\n");
                    }
                }
                bw.flush();

                username = in[1];
                this.setName(username);
                Server.sthreads.add(this);
                
            }while(flag == false && i!=0);
            
            if(flag==true)
            {
                String command;
                String output;
                command = null;
                do
                {
                    command = br.readLine();
                    String tmp[] = command.split(" ");
                    
                    switch(tmp[0])
                    {
                        case "whoelse":
                            output = Server.whoelse(username);
                            bw.write(output+"\n");
                            bw.flush();
                            break;
                        case "wholast":
                            output = Server.wholast(tmp[1]+" "+username);
                            bw.write(output+"\n");
                            bw.flush();
                            break;
                        case "broadcast":
                            Server.broadcast(tmp[1], username);
                            break;
                        case "logout":
                            Server.logout(username);
                            bw.write("Logged Out.\n");
                            bw.flush();
                            break;
                        default:
                            bw.write("Oops wrong command! Try Again\n");
                            bw.flush();
                            break;
                    }
                }while(!command.equalsIgnoreCase("logout"));
            }
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMsg(String msg) throws IOException
    {
        os = s.getOutputStream();
        osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);
        
        bw.write(msg+"\n");
        bw.flush();
    }
    
}
