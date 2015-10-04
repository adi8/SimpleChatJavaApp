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
    protected BlockingQueue messages = new LinkedBlockingQueue();
    
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
            boolean blocked = false;
            int i = 3;
            
            do
            {
                is = s.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
            
                String input="";
                input = br.readLine();
            
                String in[] = input.split(" ");
            
                os = s.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
                
                blocked = Server.isBlocked(in[0]);
                
                if(!blocked && Server.authenticate(in))
                {
                    flag = true;
                    Server.users_ol.put(in[0], s.getInetAddress());
                    if(Server.users_offline.contains(in[0]))
                        Server.users_offline.remove(in[0]);
                    username = in[0];
                    this.setName(username);
                    Server.sthreads.add(this);
                    bw.write("Welcome!\n");
                    bw.write("2\n");
                }
                else if(blocked)
                {
                    i = 0;
                    long sec = System.currentTimeMillis() - Server.users_blocked.get(in[0]);
                    bw.write("User is blocked. Try after "+(60 -(sec/1000))+" seconds.\n");
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
                        if(Server.user.contains(in[0]))
                        {
                            Server.users_blocked.put(in[0], System.currentTimeMillis());
                        }
                        bw.write("Blocked\n");
                        bw.write("0\n");
                    }
                }
                bw.flush();
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
                            if(!tmp[1].equalsIgnoreCase("user"))
                            {
                                String t[] = command.split(" ", 2);
                                Server.broadcast(t[1], username);
                            }
                            else
                            {
                                Server.broadcast(tmp, username);
                            }
                            break;
                        case "message":
                            Server.privateMsg(command,username);
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
    
    public void sendMsg() throws IOException, InterruptedException
    {
        String msg="";
        os = s.getOutputStream();
        osw = new OutputStreamWriter(os);
        bw = new BufferedWriter(osw);
        
        while(!messages.isEmpty())
        {
            msg = (String) messages.take();
            bw.write(msg+"\n");
            bw.flush();
        }
    }
}
