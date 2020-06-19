package util;

import java.util.LinkedList;
import java.util.List;

import javafx.util.Pair;

public class BlockChain {
	 private static BlockChain block_chain = null; 
	   
	 private List<Pair<String,String>> MessagesHash;
	 
	 private BlockChain() 
	 { 
		 MessagesHash = new LinkedList<Pair<String,String>>();
	 } 
	 // static method to create instance of Singleton class 
	 public static BlockChain getInstance() 
	 {
		 if (block_chain == null) 
			 block_chain = new BlockChain();
		 return block_chain; 
	 }
	 
	 public boolean isMessageHashPresent(String messageId){
		 for(Pair<String,String> message:MessagesHash){
			 if(message.getKey().equals(messageId))
				 return true;
		 }
		 return false;
	 }
	 public void CheckAndAdd(String messageId,String hashValue){
		 Pair<String,String> p = new Pair<String,String>(messageId,hashValue);
		 int ch = 0;
		 for(Pair<String,String> message:MessagesHash){
			 if(message.getKey().equals(messageId)){
				 p = message;
				 ch=1;
				 return;
			 }
		 }
		 if(ch == 1){
			 MessagesHash.remove(p);
		 }
		 MessagesHash.add(new Pair<String,String>(messageId,hashValue));
	 }
	 public void addMessageHash(String messageId,String hashValue){
		 MessagesHash.add(new Pair<String,String>(messageId,hashValue));
	 }
	 
	 public boolean isMessageHashValid(String messageId,String hashValue){
		 for(Pair<String,String> message:MessagesHash){
			 if(message.getKey().equals(messageId)){
				if(message.getValue().equals(hashValue))
					return true;
				return false;
			 }
		 }
		 return false;
	 }
}
