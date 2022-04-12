package com.example.test;// ChatServer.java
// Simple implementation of a server for a chat group


import java.util.*;
import java.io.*;
import java.net.*;

public class ChatServer {

    public static final int PORT = 5001;

    // Each client will get a thread in the server so we need
    // an array of sockets and threads
    private int MaxUsers;    // Max number of clients at a time
    private Socket [] users;
    private UserThread [] threads;
    private int numUsers;    // Number of clients currently logged on
    String word;
    String guess;

    public ChatServer(int m)
    {
        MaxUsers = m;
        users = new Socket[MaxUsers];
        threads = new UserThread[MaxUsers];   // Set things up and start
        numUsers = 0;                         // Server running

        try {
            runServer();
        }
        catch (Exception e) {
            System.out.println("Problem with server");
        }
    }

    // Method to send a message to all clients.  This is synchronized
    // to ensure that a message is not interrupted by another message
    public synchronized void SendMsg(String word)
    {
        for (int i = 0; i < numUsers; i++)
        {
            ObjectOutputStream out = threads[i].getOutputStream();
            try {
                out.writeObject(word);
                out.flush();
                System.out.println(word + " received!");
            }
            catch (IOException e) {
                System.out.println("Problem sending message");
            }
        }
    }

    // Client logs off and is removed from server.  Again, this is
    // synchronized so that the arrays do not become inconsistent
    public synchronized void removeClient(int id, String word)
    {
        try
        {
            users[id].close();
        }
        catch (IOException e)
        {
            System.out.println("Already closed");
        }
        users[id] = null;
        threads[id] = null;
        // fill up "gap" in arrays
        for (int i = id; i < numUsers-1; i++)
        {
            users[i] = users[i+1];
            threads[i] = threads[i+1];
            threads[i].setId(i);
        }
        numUsers--;
        SendMsg(word + " has logged off");
    }

    private void runServer() throws IOException
    {
        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Started: " + s);

        try {
            while(true)
            {
                if (numUsers < MaxUsers)
                {
                    try {
                        // wait for client
                        Socket newSocket = s.accept();
                        // set up streams and thread for client
                        synchronized (this)
                        {
                            users[numUsers] = newSocket;
                            ObjectInputStream in = new ObjectInputStream(newSocket.getInputStream());
                            String word = (String)in.readObject();
                            SendMsg(word + " has just been added");

                            threads[numUsers] = new UserThread(newSocket, numUsers, word, in);
                            threads[numUsers].start();
                            System.out.println("Connection " + numUsers + users[numUsers]);
                            numUsers++;
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Problem with connection...terminating");
                    }
                }  // end if
            }  // end while
        }   // end try
        finally {
            System.out.println("Server shutting down");
        }
    }

    // Server allocates one thread for client and delas with it as follows
    private class UserThread extends Thread
    {
        private Socket mySocket;
        private ObjectInputStream myInputStream;
        private ObjectOutputStream myOutputStream;
        private int myId;
        private String myName;

        private UserThread(Socket newSocket, int id, String word,
                           ObjectInputStream in) throws IOException
        {
            mySocket = newSocket;
            myId = id;
            myName = word;
            myInputStream = in;
            myOutputStream = new ObjectOutputStream(newSocket.getOutputStream());
        }

        public ObjectInputStream getInputStream()
        {
            return myInputStream;
        }

        public ObjectOutputStream getOutputStream()
        {
            return myOutputStream;
        }

        public synchronized void setId(int newId)
        {
            myId = newId;
        }

        // Each UserThread will gets the next message from its corresponding
        // client.  Each message is then sent to the other clients by the Server.
        // When there are no more messages myReader.readLine() throws
        // an IOException.  Then the thread will die.

        public void run()
        {
            boolean alive = true;
            while (alive)
            {
                String newMsg = null;
                try {
                    newMsg = (String) myInputStream.readObject();
                    synchronized(this)
                    {
                        ChatServer.this.SendMsg(newMsg);
                        System.out.println("Word received by the server: " + newMsg);
                    }
                }
                catch (ClassNotFoundException e)  {
                    System.out.println("Error receiving message....shutting down");
                    alive =false;
                }
                catch (IOException e) {
                    System.out.println("Client closing!!");
                    alive = false;
                }
            }
            removeClient(myId, myName);
        }
    }

    public static void main(String [] args)
    {
        ChatServer Server = new ChatServer(2);
    }
}


