/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random; 
import java.util.Collections;
import java.util.Comparator;

import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.Coord;
import javafx.util.Pair;

/**
 * Implementation of Spray and wait router as depicted in
 * <I>Spray and Wait: An Efficient Routing Scheme for Intermittently
 * Connected Mobile Networks</I> by Thrasyvoulos Spyropoulus et al.
 *
 */
public class SprayAndWaitRouter extends ActiveRouter {
	/** identifier for the initial number of copies setting ({@value})*/
	public static final String NROF_COPIES = "nrofCopies";
	/** identifier for the binary-mode setting ({@value})*/
	public static final String BINARY_MODE = "binaryMode";
	/** SprayAndWait router's settings name space ({@value})*/
	public static final String SPRAYANDWAIT_NS = "SprayAndWaitRouter";
	/** Message property key */
	public static final String MSG_COUNT_PROPERTY = SPRAYANDWAIT_NS + "." +
		"copies";

	protected int populationSize;
	protected int chromosomeSize;// = NoOfBitsForGroupNumber * NoOfGroupsInChromosome
	protected int NoOfBitsForGroupNumber;//Initially it's 3
	protected int NoOfGroupsInChromosome;//Initially it's 3
	protected ArrayList<ArrayList<Integer>> chromosomes;
	protected Map<String,List<Coord>> coordsById;
	protected Map<String,List<String>> neighbours; 

	public SprayAndWaitRouter(Settings s) {
		super(s);
//		Settings snwSettings = new Settings(SPRAYANDWAIT_NS);
//
//		initialNrofCopies = snwSettings.getInt(NROF_COPIES);
//		isBinary = snwSettings.getBoolean( BINARY_MODE);
		this.chromosomes = new ArrayList<ArrayList<Integer>>();
		this.populationSize = 2;
		this.NoOfGroupsInChromosome = 3;
		this.NoOfBitsForGroupNumber = 3;
		this.chromosomeSize = NoOfGroupsInChromosome*NoOfBitsForGroupNumber;
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected SprayAndWaitRouter(SprayAndWaitRouter r) {
		super(r);
//		this.initialNrofCopies = r.initialNrofCopies;
//		this.isBinary = r.isBinary;
		this.chromosomes = r.chromosomes;
		this.populationSize = r.populationSize;
		this.NoOfGroupsInChromosome = r.NoOfGroupsInChromosome;
		this.NoOfBitsForGroupNumber = r.NoOfBitsForGroupNumber;
		this.chromosomeSize = NoOfGroupsInChromosome*NoOfBitsForGroupNumber;
	}

	@Override
	public int receiveMessage(Message m, DTNHost from) {
		return super.receiveMessage(m, from);
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message msg = super.messageTransferred(id, from);
//		Integer nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);
//
//		assert nrofCopies != null : "Not a SnW message: " + msg;
//
//		if (isBinary) {
//			/* in binary S'n'W the receiving node gets floor(n/2) copies */
//			nrofCopies = (int)Math.floor(nrofCopies/2.0);
//		}
//		else {
//			/* in standard S'n'W the receiving node gets only single copy */
//			nrofCopies = 1;
//		}
//
//		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
		return msg;
	}

	@Override
	public boolean createNewMessage(Message msg) {
		makeRoomForNewMessage(msg.getSize());

		msg.setTtl(this.msgTtl);
//		msg.addProperty(MSG_COUNT_PROPERTY, new Integer(initialNrofCopies));
		addToMessages(msg, true);
		return true;
	}

	@Override
	public void update() {
		super.update();
		if (!canStartTransfer() || isTransferring()) {
			return; // nothing to transfer or is currently transferring
		}

		/* try messages that could be delivered to final recipient */
		if (exchangeDeliverableMessages() != null) {
			return;
		}

		/* create a list of SAWMessages that have copies left to distribute */
//		@SuppressWarnings(value = "unchecked")
//		List<Message> copiesLeft = sortByQueueMode(getMessagesWithCopiesLeft());

	//	if (copiesLeft.size() > 0) {
			/* try to send those messages */
			//this.tryMessagesToConnections(copiesLeft, getConnections());
			workflow();
	//	}
	}

	/**
	 * Creates and returns a list of messages this router is currently
	 * carrying and still has copies left to distribute (nrof copies > 1).
	 * @return A list of messages that have copies left
	 */
	protected List<Message> getMessagesWithCopiesLeft() {
		List<Message> list = new ArrayList<Message>();

		for (Message m : getMessageCollection()) {
			Integer nrofCopies = (Integer)m.getProperty(MSG_COUNT_PROPERTY);
			assert nrofCopies != null : "SnW message " + m + " didn't have " +
				"nrof copies property!";
			if (nrofCopies > 1) {
				list.add(m);
			}
		}

		return list;
	}

	/**
	 * Called just before a transfer is finalized (by
	 * {@link ActiveRouter#update()}).
	 * Reduces the number of copies we have left for a message.
	 * In binary Spray and Wait, sending host is left with floor(n/2) copies,
	 * but in standard mode, nrof copies left is reduced by one.
	 */
//	@Override
//	protected void transferDone(Connection con) {
//		Integer nrofCopies;
//		String msgId = con.getMessage().getId();
//		/* get this router's copy of the message */
//		Message msg = getMessage(msgId);
//
//		if (msg == null) { // message has been dropped from the buffer after..
//			return; // ..start of transfer -> no need to reduce amount of copies
//		}
//
//		/* reduce the amount of copies left */
//		nrofCopies = (Integer)msg.getProperty(MSG_COUNT_PROPERTY);
//		if (isBinary) {
//			/* in binary S'n'W the sending node keeps ceil(n/2) copies */
//			nrofCopies = (int)Math.ceil(nrofCopies/2.0);
//		}
//		else {
//			nrofCopies--;
//		}
//		msg.updateProperty(MSG_COUNT_PROPERTY, nrofCopies);
//	}

	@Override
	public SprayAndWaitRouter replicate() {
		return new SprayAndWaitRouter(this);
	}
	public void workflow(){
		coordsById = this.getHost().getCoordsById();
		neighbours = this.getHost().getNeigbours();
		Collection<Message> messages = this.getMessageCollection();
		Collection<Message> messagesCp = new ArrayList<Message>();
		for(Message m : messages){
			messagesCp.add(m);
		}
		List<Connection> connections = getConnections();
		DTNHost stationaryNode = getHost();
		for(Connection con:connections){
			DTNHost to = con.getOtherNode(getHost());
			if(to.getIsItAStationary() == 1){
				stationaryNode = to;
				break;
			}
		}
		for(Message m : messagesCp){
			if(stationaryNode.getIsItAStationary() == 1){
				if(m.getFrom()==this.getHost()){
					stationaryNode.checkAndAdd(m);
				}
				if(!stationaryNode.isMessageUnchanged(m)){
					this.removeMessage(m);
				}
				else if(stationaryNode.isItAnAttackMessage(m)){
					this.removeMessage(m);
				}
				else{
					this.verifyMessage(m);
				}
			}
		}
		messages = this.getMessageCollection();
		for(Message m : messages){
			//if(!m.isItVerified&&!getHost().isItAFabricatingAttacker&&!getHost().isItAFloodingAttacker)
			//	continue;
			List<Message> sendMessage = new ArrayList<Message>();
			List<Connection> tryConnection = new ArrayList<Connection>();
			sendMessage.add(m);

			for(Connection con: connections){
				DTNHost to = con.getOtherNode(getHost());
				if(m.getTo() == to){
					tryConnection.clear();
					tryConnection.add(con);
					this.tryMessagesToConnections(sendMessage, tryConnection);
					break;
					//TO BE IMPLEMENTED SEND MESSAGE TO DESTINATION AND BREAK
				}
				else{
					if(!to.isWorkingDayMovement){
						tryConnection.add(con);
						continue;
					}
					chromosomeInit();
					int maxGenerations = 3;
					for(int k = 0;k<maxGenerations;k++){
						sorting(m,to.getGroupId());
						double threshold = getHost().getLocation().distance(to.getLocation());
						threshold = 16*threshold*threshold;
						if(fitnessFunction(chromosomes.get(0),m,to.getGroupId())<threshold){
							tryConnection.add(con);
							break;
						}
						int initsize =chromosomes.size();
						for(int i = 0;i+1<initsize;i+=2){
							crossover(chromosomes.get(i),chromosomes.get(i+1));
						}
					}
				}
			}
			this.tryMessagesToConnections(sendMessage,tryConnection);
		}
	}
	public ArrayList<String> decode(ArrayList<Integer> chromosome,String currentHostGroup){
		ArrayList<Integer> decoded = new ArrayList<Integer>();
		for(int i = 0; i < NoOfGroupsInChromosome;i++){
			int vl = 0;
			for(int j = 0;j < NoOfBitsForGroupNumber;j++){
				int ps = NoOfBitsForGroupNumber*i+j;
				vl = vl<<1;
				if(chromosome.get(ps)==1)
					vl+=1;
			}
			decoded.add(vl);
		}
		ArrayList<String> groups = new ArrayList<String>();
		for(int i = 0;i < NoOfGroupsInChromosome;i++){
			currentHostGroup = neighbours.get(currentHostGroup).get(decoded.get(i));
			groups.add(currentHostGroup);
		}
		return groups;
	}
	
	public double fitnessFunction(ArrayList<Integer> chromosome,Message message,String currentHostGroup){
		
		double fitness = 0,alpha = 10,beta = 5,gamma = 1;
		DTNHost destination = message.getTo();
		ArrayList<String> decoded = decode(chromosome,currentHostGroup);
		Coord coordA = coordsById.get(decoded.get(0)).get(3);
		Coord coordB = coordsById.get(decoded.get(1)).get(3);
		Coord coordC = coordsById.get(decoded.get(2)).get(3);
		double mean = 	alpha*destination.getLocation().distance(coordA)*destination.getLocation().distance(coordA) +
				beta*destination.getLocation().distance(coordB)*destination.getLocation().distance(coordB) +
				gamma*destination.getLocation().distance(coordB)*destination.getLocation().distance(coordC);
		//NEED TO ADD PLACE FACTOR
		return mean;
	}
	public void chromosomeInit(){
		Random rand = new Random();
		chromosomes.clear();
		for(int i = 0;i<populationSize;i++){
			ArrayList<Integer> chromosome = new ArrayList<Integer>();
			for(int j = 0;j < chromosomeSize;j++){
				chromosome.add(rand.nextInt(2));
			}
			chromosomes.add(chromosome);
		}
	}
	
	public void crossover(ArrayList<Integer> A,ArrayList<Integer> B){
		ArrayList<Integer> C = new ArrayList<Integer>();
		ArrayList<Integer> D = new ArrayList<Integer>();
		Random rand = new Random();
		int crossoverPoint = rand.nextInt(7)+1;
		for(int i = 0;i < A.size();i++){
			if(i<=crossoverPoint){
				C.add(A.get(i));
				D.add(B.get(i));
			}
			else{
				C.add(B.get(i));
				D.add(A.get(i));
			}
		}
		chromosomes.add(C);
		chromosomes.add(D);
	}
	
	public void sorting(Message message,String hostGroup){
		ArrayList<Pair<Double, Integer>> sortedList = new ArrayList<Pair<Double, Integer>>();
		for(int i = 0;i<chromosomes.size();i++){
			double fitness = fitnessFunction(chromosomes.get(i),message,hostGroup);
			sortedList.add(new Pair<Double, Integer>(fitness,i));
		}
		Collections.sort(sortedList, new Comparator<Pair<Double, Integer>>() {
		    @Override
		    public int compare(final Pair<Double, Integer> o1, final Pair<Double, Integer> o2) {
		    	if(o1.getKey()<o2.getKey())
		        	return -1;
		        if(o1.getKey()==o2.getKey())
		        	return 0;
		        return 1;
		    }
		});
		ArrayList<ArrayList<Integer>> sortedChromosomes = new ArrayList<ArrayList<Integer>>(); 
		for(int i = 0;i < populationSize;i++){
			sortedChromosomes.add(chromosomes.get(sortedList.get(i).getValue()));
		}
		chromosomes.clear();
		chromosomes = sortedChromosomes;
	}
}
