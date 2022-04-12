package com.example.test;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.beans.value.ObservableValue;
import javafx.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;


public class ChatClientController {

	@FXML  private TextField messageTextField;
	@FXML  private TextArea messageTextArea;
	@FXML  private Button startButton;
	@FXML  private Button exitButton;


	@FXML
	void exitButtonClicked(ActionEvent event) {
		try {
			System.exit(0);
		}
		catch (Exception e)
		{
			System.out.println("Problem starting client");
		}
	}
	public static final int PORT = 5001;

	ObjectInputStream myInputStream;
	ObjectOutputStream myOutputStream;
	String myName, serverName;


	@FXML
	void startButtonClicked(ActionEvent event) {

		try {
			myName = JOptionPane.showInputDialog("Enter your user name: ");
			serverName = JOptionPane.showInputDialog("Enter the server name: ");
			InetAddress addr = InetAddress.getByName(serverName);
			Socket socket = new Socket(addr, PORT);

			myOutputStream = new ObjectOutputStream(socket.getOutputStream());
			myOutputStream.flush();
			// Send client's name to server
			myOutputStream.writeObject(myName);
			myOutputStream.flush();
			myInputStream = new ObjectInputStream(socket.getInputStream());
			messageTextArea.appendText("Welcome to the Chat Group, " + myName + "\n");

			// Use a thread to receive messages form server
			ClientConnection c = new ClientConnection(myInputStream);
			c.start();
		}
		catch (Exception e)
		{
			System.out.println("Problem starting client");
		}

	}



	@FXML
	void enterMessage(ActionEvent event) {
		String currMsg = messageTextField.getText();
		messageTextField.setText("");
		try {
			myOutputStream.writeObject(myName + ": " + currMsg);
			myOutputStream.flush();
		}
		catch (IOException ioException)  {
			System.out.println("Problem sending message");
		}
	}

	private class ClientConnection extends Thread  {

		ObjectInputStream inStr;

		public ClientConnection (ObjectInputStream oistr) {
			inStr = oistr;
		}


		// Receive messages from server and display them at client end
		public void run()
		{
			while (true)
			{
				try {
					String currMsg = (String) myInputStream.readObject();
					messageTextArea.appendText(currMsg+"\n");
				}
				catch (ClassNotFoundException e)  {
					System.out.println("Error receiving message....shutting down");
				}
				catch (IOException e) {
					System.out.println("Problem reading");
				}
			}
		}
	}
}

