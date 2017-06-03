package it.polimi.LM39.server;
import it.polimi.LM39.exception.SocketPlayerException;
import it.polimi.LM39.model.MainBoard;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * every player connected to the socket has this object which manipulates connection between server and client
 * this is the server side, so we send objects and strings and receive strings from client
 */
public class SocketPlayer extends NetworkPlayer implements Runnable{
	    private Socket socket;
	    private ServerInterface serverInterface;
	    private ObjectInputStream objInput;			//player's interface and I/O streams
	    private ObjectOutputStream objOutput;
	    private String message;						//information which will be send to the client
	    private MainBoard mainBoard;
	    private String clientAction;
	    /*
	     * the constructor initialize the streams and start the thread
	     */
	    public SocketPlayer(ServerInterface serverInterface, Socket socket) throws IOException {
	          this.socket = socket;
	          this.serverInterface = serverInterface;
	          this.objOutput = new ObjectOutputStream(this.socket.getOutputStream()); 
	          this.objOutput.flush();	//needed to avoid deadlock
	          this.objInput = new ObjectInputStream(new BufferedInputStream(this.socket.getInputStream()));
	    }
	    /*
	     * used from the controller to set what we want to send
	     */
	    public void setMessage(String message,MainBoard mainBoard){
	    	this.message = message;
	    	this.mainBoard = mainBoard;
	    }
	    public void setMessage(String message){
	    	this.message = message;
	    }
	    private Boolean messageState(){
	    	if(this.message != null)
	    		return true;
	    	else
	    		return false;
	    }
	    /*
	     * infinite loop which listen the client from input and send the available information to it
	     * it checks if we have something to send to the client and waits for the answer
	     */
	    public void run() {
	        this.serverInterface.joinRoom(this);
	        try {
	    		//TODO ask if it is possible to suppress sonarlint bug for infinite loops
	        	while(true){
	        		if(this.messageState()){
	        			objOutput.writeObject(this.mainBoard);
	        			objOutput.writeUTF(this.message);
	        		}
	        		if(objInput.available() > 0){
	        			clientAction = objInput.readUTF();
	        			//TODO handle the clientaction
	        		}
	        	}
	        }catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	    }
	    /*
	     * this method return the client action to the game controller
	     */
	    public String sendMessage(){
	    	while(this.clientAction == null)
	    		continue;
	    	return this.clientAction;
	    }
}