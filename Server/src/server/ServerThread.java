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
    private final Socket s;
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
    
    /**
     * This method listens for the commands sent by the client and processes them accordingly.
     */
    @Override
    public void run()
    {
        try 
        {
            if(login())
            {
                String command;
                String output;
                do
                {
                    command = br.readLine();
                    
                    String tmp[] = command.split(" "); // to check what the command is. Using tmp[0].
                    
                    switch(tmp[0])
                    {
                        case "whoelse":
                            output = Server.whoelse(username);
                            bw.write(output+"\n");
                            bw.flush();
                            break;
                        case "wholast":
                            output = "Something went wrong! Please check the time value sent. ";
                            try
                            {
                                int i = Integer.parseInt(tmp[1]);
                                output = Server.wholast(tmp[1], username);
                            }catch(Exception e)
                            {
                                bw.write(output+ "\n");
                                bw.flush();
                            }
                            if(!output.contains("wrong"))
                            {
                                bw.write(output+"\n");
                                bw.flush();
                            }
                            break;
                        case "broadcast":
                            if(!tmp[1].equalsIgnoreCase("user")) //  Checks if it is a broadcast user <user> message <message> command.
                            {
                                output = Server.broadcast(tmp, username, 1);
                                bw.write(output);
                                bw.flush();
                            }
                            else if(tmp[1].equalsIgnoreCase("message")) // Checks if it is a broadcast message <message> command.
                            {
                                output = Server.broadcast(tmp, username, 0);
                                bw.write(output);
                                bw.flush();
                            }
                            else
                            {
                                bw.write("Oops command sent is wrong! Please check the command\n");
                                bw.flush();
                            }
                            break;
                        case "message":
                            output = Server.privateMsg(command,username);
                            bw.write(output);
                            bw.flush();
                            break;
                        case "logout":
                            Server.logout(username);
                            bw.write("Logged Out.\n");
                            bw.flush();
                            Server.sthreads.remove(this);
                            s.close(); // closes the connection.
                            break;
                        default:
                            bw.write("Oops wrong command! Try Again\n");
                            bw.flush();
                            break;
                    }
                }while(!command.equalsIgnoreCase("logout"));
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * The method sends message received from other clients. 
     * @throws IOException
     * @throws InterruptedException 
     */
    public void sendMsg() throws IOException, InterruptedException
    {
        String msg;
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
    
    /**
     * The method authenticates the client.
     * @return true: client logged in; false: Client login failed.
     * @throws IOException 
     */
    public boolean login() throws IOException
    {
        boolean blocked;
        boolean flag = false;
        int i = 3;
        String in[];
        
        do
        {
            is = s.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            
            String input;
            input = br.readLine();
            
            in = input.split(" "); // Receives the username and password from the client.
            
            os = s.getOutputStream();
            osw = new OutputStreamWriter(os);
            bw = new BufferedWriter(osw);
            
            blocked = Server.isBlocked(in[0]);
            
            if(!blocked && Server.authenticate(in)) // Checks if the user is not blocked and if so checks if the username password combination exists in database.
            {
                flag = true;
                Server.users_ol.put(in[0], s.getInetAddress());
                if(Server.users_offline.contains(in[0])) // Checks if user was in offline list.
                    Server.users_offline.remove(in[0]); // Removes the user from the offline list.
                username = in[0];
                this.setName(username); // Sets the name of the serverthreadr to the user's name, who requests are serviced by the thread.
                Server.sthreads.add(this); // Adds this thread to the list of active serverthreads.
                bw.write("Welcome!\n");
                bw.write("2\n");    //indicates that the client has been autheticated and logged in.
            }
            else if(blocked)
            {
                i = 0;
                long sec = System.currentTimeMillis() - Server.users_blocked.get(in[0]); // Retrieves the time in seconds for which the user will remain blocked.
                bw.write("User is blocked. Try after "+(60 -(sec/1000))+" seconds.\n");
                bw.write("0\n");    //indicates that the client has to stop and exit.
            }
            else
            {
                i--; // Decreases the counter for the number of attempts allowed to a user to login.
                if(i!=0)
                {
                    bw.write("Either user not registered or password didn't match. Try Again! "
                                + "("+i+" attempts remaining.)\n");
                    bw.write("1\n");    //indicates that the user can try entering the username & password again(max 3 times).
                }
                else
                {
                    if(Server.user.contains(in[0]))
                    {
                        Server.users_blocked.put(in[0], System.currentTimeMillis());
                    }
                    bw.write("Blocked\n");
                    bw.write("0\n");    //indicates that the client has to stop and exit.
                }
            }
            bw.flush();
        }while(flag == false && i!=0);
        
        return flag;
    }
}
