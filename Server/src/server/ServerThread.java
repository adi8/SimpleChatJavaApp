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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Aditya Venkateshwaran
 */
public class ServerThread extends Thread 
{
    private Socket s;
    
    public ServerThread(Socket clientSocket)
    {
        s = clientSocket;   
    }
    
    public void run()
    {
        try 
        {
            InputStream is = null;   
            InputStreamReader isr= null;
            BufferedReader br = null;
            
            OutputStream os = null;
            OutputStreamWriter osw = null;
            BufferedWriter bw = null;
            
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
                
                long sec=0;
                boolean blocked = Server.users_blocked.contains(in[1]);
                if(blocked)
                {
                    sec = System.currentTimeMillis() - Server.users_blocked.get(in[1]);
                    if(sec>Server.BLOCK_TIME)
                    {
                        Server.users_blocked.remove(in[1]);
                        blocked = false;
                    }
                }
                
                if(!blocked && Server.authenticate(in))
                {
                    flag = true;
                    Server.users_ol.add(in[1]);
                    bw.write("Welcome!\n");
                    bw.write("2\n");
                }
                else if(blocked)
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
            }while(flag == false && i!=0);
            
            if(flag==true)
            {
                String command;
                String output;
                command = null;
                do
                {
                    command = br.readLine();

                    switch(command.split(" ")[0])
                    {
                        case "whoelse":
                            output = Server.whoelse(in[1]);
                            bw.write(output+"\n");
                            bw.flush();
                            break;
                        case "wholast":
                            output = Server.wholast(in);
                            bw.write(output+"\n");
                            bw.flush();
                            break;
                        case "logout":
                            Server.logout(in[1]);
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
        }
    }
}
