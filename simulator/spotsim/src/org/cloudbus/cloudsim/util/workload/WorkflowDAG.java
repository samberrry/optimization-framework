package org.cloudbus.cloudsim.util.workload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class WorkflowDAG {

	private int vertices;
    private int edges;
    private Bag<Job>[] adj;
    private Job[] nodes;
    private Bag<Job>[] parents;
    private long MaxCriticalPathWeightFirstLevel;
    
    /**
     * Create an empty digraph with V vertices.
     */
    public WorkflowDAG(int V) {
        if (V < 0) throw new RuntimeException("Number of vertices must be nonnegative");
        this.vertices = V;
        this.edges = 0;
        adj = (Bag<Job>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
            adj[v] = new Bag<Job>();
        }
        parents = (Bag<Job>[]) new Bag[V];
        for (int v = 0; v < V; v++) {
        	parents[v] = new Bag<Job>();
        }
        
        nodes = new Job[V];
        
    }
    
    /**
     * Return the number of vertices in the digraph.
     */
    public int V() {
        return this.vertices;
    }

   /**
     * Return the number of edges in the digraph.
     */
    public int E() {
        return this.edges;
    }

    public Bag<Job>[] adj(){
    	return adj;
    }
   /**
     * Add the directed edge v-w to the digraph.
     */
    public void addEdge(int v, Job w) {
    	nodes[w.getIntId()] = w;
        adj[v].add(w);
        this.edges++;
    }
    /**
     * Add the directed edge v-w to the digraph.
     */
    public void addEdge(Job v, Job w) {
    	int fromIndex = v.getIntId();
    	int toIndex = w.getIntId();
    	nodes[fromIndex] = v;
    	nodes[toIndex] = w;
    	
    	adj[fromIndex].setNodeInfo(v);
        adj[fromIndex].add(w);
        
        parents[toIndex].setNodeInfo(w);
        parents[toIndex].add(v);
        
        this.edges++;
    }
    /**
     * Add the directed edge v-w to the digraph.
     */
    public void removeEdge(int v, Job w) {
    	adj[v].remove(w);
    	
    	parents[w.getIntId()].remove(getNode(v));
    	this.edges--;
    }

   /**
     * Return the list of neighbors of vertex v as in Iterable.
     */
    public Bag<Job> adj(int v) {
        return adj[v];
    }
    
    /**
     * Return the list of neighbors of vertex v as in Iterable.
     */
    public ArrayList<Integer> adjList(int v) {
        ArrayList<Integer> retList = new ArrayList<>();
        for(Job task : adj[v]){
        	retList.add(task.getIntId());
        }
        return retList;
    }
    
    /**
     * Return the list of neighbors of vertex v as in Iterable as a linked hash set.
     */
    public Set<Integer> adjSet(int v) {
        Set<Integer> retList = new LinkedHashSet<>();
        for(Job task : adj[v]){
        	retList.add(task.getIntId());
        }
        return retList;
    }
    
    /**
     * Return the list of children of vertex v as in a ArrayList.
     */
    public ArrayList<Integer> getChildren(int v) {
        ArrayList<Integer> retList = new ArrayList<>();
        for(Job task : adj[v]){
        	retList.add(task.getIntId());
        }
        return retList;
    }
    
    /**
     * Return the list of parents of vertex v as in a ArrayList.
     */
    public ArrayList<Integer> getParents(int v) {
        ArrayList<Integer> retList = new ArrayList<>();
        for(Job task : parents[v]){
        	retList.add(task.getIntId());
        }
        return retList;
    }
    
    /**
     * Return the critical parents of vertex v.
     */
    public Job getCriticalParent(int v) {
        long criticalWght=0;
        Job criticalParent = null;
        for(Job task : parents[v]){
        	if(criticalWght <= task.getCriticalPathWeight()){
        		criticalWght = task.getCriticalPathWeight();
        		criticalParent = task;
        	}
        }
        return criticalParent;
    }
    /**
     * Return the critical Child of vertex v.
     */
    public Job getCriticalChild(int v) {
        long criticalWght=0;
        Job criticalChild = null;
        for(Job task : adj[v]){
        	if(criticalWght < task.getCriticalPathWeight()){
        		criticalWght = task.getCriticalPathWeight();
        		criticalChild = task;
        	}
        }
        return criticalChild;
    }
    /**
     * Return the critical Child of vertex v.
     */
    public Job getCriticalChild(int v, ArrayList<Integer> nodeList) {
        long criticalWght=0;
        Job criticalChild = null;
        for(Job task : adj[v]){
        	if(nodeList.contains(task.getIntId())){
        		if(criticalWght < task.getCriticalPathWeight()){
        			criticalWght = task.getCriticalPathWeight();
        			criticalChild = task;
        		}
        	}
        }
        return criticalChild;
    }
    
    /**
     * Return the list of children as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getChildren(ArrayList<Integer> returnList) {

    		ArrayList<Integer> dummyList = new ArrayList<>();
    		for(Integer intV : returnList){
    			dummyList.addAll(getChildren(intV));
    		}
    		removeDuplicates(dummyList);
    		
    		return dummyList;
    }
    
    /**
     * Return the list of parents as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getParents(ArrayList<Integer> returnList) {

		ArrayList<Integer> dummyList = new ArrayList<>();
		for(Integer intV : returnList){
			dummyList.addAll(getParents(intV));
		}
		removeDuplicates(dummyList);
		
		return dummyList;
    }
    
    /**
     * Return the list of unassigned children as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getUnassignedChildren(ArrayList<Integer> returnList) {

		ArrayList<Integer> dummyList = new ArrayList<>();
		ArrayList<Integer> parList = getAllChildren(returnList);
		for(Integer node : parList){
			if(!getNode(node).isAssigned()){
				dummyList.add(node);
			}
		}
		removeDuplicates(dummyList);
		
		return dummyList;
    }
    
    
    /**
     * Return the list of unassigned parents as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getUnassignedParents(ArrayList<Integer> returnList) {

		ArrayList<Integer> dummyList = new ArrayList<>();
		ArrayList<Integer> parList = getAllParents(returnList);
		for(Integer node : parList){
			if(!getNode(node).isAssigned()){
				dummyList.add(node);
			}
		}
		removeDuplicates(dummyList);
		Collections.reverse(dummyList);
		return dummyList;
    }
    
    /**
     * Return the list of all possible child nodes as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getAllChildren(ArrayList<Integer> returnList) {
    	ArrayList<Integer> tempList = returnList;
    	ArrayList<Integer> fullTempList = new ArrayList<>();
    	while(tempList.size()!= 0 ){
    		tempList = getChildren(tempList);
    		removeDuplicates(tempList);
    		fullTempList.addAll(tempList);
    	}
    	return fullTempList;
    }
    
    /**
     * Return the list of all possible parent nodes as an ArrayList of all the nodes in the returnList.
     */
    public ArrayList<Integer> getAllParents(ArrayList<Integer> returnList) {
    	ArrayList<Integer> tempList = returnList;
    	ArrayList<Integer> fullTempList = new ArrayList<>();
    	while(tempList.size()!= 0 ){
    		tempList = getParents(tempList);
    		removeDuplicates(tempList);
    		fullTempList.addAll(tempList);
    	}
    	return fullTempList;
    }
    
    //Computes the Latest Finish time of a node
    public long computeLFT(int v, long D){
    	
    	ArrayList<Integer> children = getChildren(v);
    	long minLFT = D;
    	if(children.size()==0){
    		getNode(v).setLatestFinishTime(D);
    		return D;
    	}else{
    		for(Integer child : children){
    			Job tempNode = getNode(child);
    			long value = tempNode.getLatestFinishTime() - tempNode.getLength() - getNode(v).getEdge(child);
    			//System.out.println("LFT " + tempNode.getLatestFinishTime() + " exec " + tempNode.getExecutionTime() + " trans time " + getNode(v).getEdge(child) + " value " + value + " from " + v + " D " + child);
    			if( value <= minLFT){
    				minLFT = value;
    			}
    		}
    		//System.out.println(" Node " + v + " LFT " + minLFT);
    		getNode(v).setLatestFinishTime(minLFT);
    		return minLFT;
    	}
    //	return -1;
    }
       
    public ArrayList<Integer> getFirstLevel(){
    	ArrayList<Integer> firstLevelNodes = new ArrayList<>();
    	
    	for(int i =0; i < nodes.length ; i++){
    		ArrayList<Integer> parentNodes = getParents(i);
    		if(parentNodes.size() == 0){
    			firstLevelNodes.add(nodes[i].getIntId());
    		}
    	}
    	removeDuplicates(firstLevelNodes);
    	
    	return firstLevelNodes;
    }
    
    
    public long setMaxCriticalPathWeightFirstLevel(){
    	long maxStartTime = 0;
		for(Integer parent : this.getFirstLevel()){
			Job parentJob = this.getNode(parent);
			if( maxStartTime < parentJob.getCriticalPathWeight()){
				maxStartTime = parentJob.getCriticalPathWeight();
			}
		}
		return maxStartTime;
    }
    
    public long getMaxCriticalPathWeightFirstLevel(){
    	if(this.MaxCriticalPathWeightFirstLevel == 0){
    		this.MaxCriticalPathWeightFirstLevel = setMaxCriticalPathWeightFirstLevel();
    	}
    	return this.MaxCriticalPathWeightFirstLevel;
    }
    
    public ArrayList<Integer> getLastLevel(){
    	ArrayList<Integer> lastLevelNodes = new ArrayList<>();
    	
    	for(int i =0; i < nodes.length ; i++){
    		ArrayList<Integer> childNodes = getChildren(i);
    		if(childNodes.size() == 0){
    			lastLevelNodes.add(nodes[i].getIntId());
    		}
    	}
    	removeDuplicates(lastLevelNodes);
    	return lastLevelNodes;
    }
    
    public ArrayList<Integer> getNextLevel(ArrayList<Integer> prevLevel){
    	ArrayList<Integer> nextLevelNodes = new ArrayList<>();
    	ArrayList<Integer> remNodes = new ArrayList<>();
    	for(Integer td : prevLevel){
    		nextLevelNodes.addAll(getChildren(td));
    	}
   	
    	ArrayList<Integer> returnList= getAllChildren(nextLevelNodes);
    	remNodes.addAll(returnList);

    	nextLevelNodes.removeAll(remNodes);
    	
    	removeDuplicates(nextLevelNodes);

    	return nextLevelNodes;
    }
    
    public ArrayList<Integer> getPrevLevel(ArrayList<Integer> prevLevel){
    	ArrayList<Integer> prevLevelNodes = new ArrayList<>();
    	ArrayList<Integer> remNodes = new ArrayList<>();
    	for(Integer td : prevLevel){
    		prevLevelNodes.addAll(getParents(td));
    	}
    	
    	ArrayList<Integer> returnList= getAllParents(prevLevelNodes);
    	remNodes.addAll(returnList);

    	prevLevelNodes.removeAll(remNodes);
    	
    	removeDuplicates(prevLevelNodes);

    	return prevLevelNodes;
    }
    
    public long getLongestPath(int startNode, int endNode){
    	long maxvalue=0;
    	ArrayList<Integer> path = new ArrayList<>();
    	path.add(startNode);
		ArrayList<ArrayList<Integer>> allPaths = new ArrayList<ArrayList<Integer>>();
    	dfs(startNode, endNode, path, allPaths);
    	for(ArrayList<Integer> singlePath : allPaths){
    		long pathValue = 0;
    		for(int i=0; i < singlePath.size() ; i++){
    			int node = singlePath.get(i);
    			Job tempNode = getNode(node);
    			pathValue += tempNode.getReqRunTime();
    			if(i+1 < singlePath.size()){
    				pathValue += getNode(node).getEdge(singlePath.get(i+1));
    			}
    		}
    		if(maxvalue <= pathValue){
    			maxvalue = pathValue;
    		}
    	}
    	return maxvalue;
    }
    
    public long getShortestPath(int startNode, int endNode){
    	long maxvalue=Long.MAX_VALUE;
    	ArrayList<Integer> path = new ArrayList<>();
    	path.add(startNode);
		ArrayList<ArrayList<Integer>> allPaths = new ArrayList<ArrayList<Integer>>();
    	dfs(startNode, endNode, path, allPaths);
    	for(ArrayList<Integer> singlePath : allPaths){
    		long pathValue = 0;
    		for(int i=0; i < singlePath.size() ; i++){
    			int node = singlePath.get(i);
    			Job tempNode = getNode(node);
    			pathValue += tempNode.getReqRunTime();
    			if(i+1 < singlePath.size()){
    				pathValue += getNode(node).getEdge(singlePath.get(i+1));
    			}
    		}
    		if(pathValue <= maxvalue){
    			maxvalue = pathValue;
    		}
    	}
    	
    	return maxvalue;
    	
    }
    
   public void dfs(int startNode, int endNode, ArrayList<Integer> path, ArrayList<ArrayList<Integer>> allPaths){
	   if( startNode == endNode){
		   ArrayList<Integer> toAdd = new ArrayList<Integer>(path);
		   allPaths.add(toAdd);
		   return;
	   }else{
		   for(Job taskNode : adj(startNode)){
			   int nodeId = taskNode.getIntId();
			   if(!path.contains(nodeId)){
				   path.add(nodeId);
				   dfs(nodeId,endNode, path, allPaths);
				   path.remove((Object)nodeId);
			   }
		   }
	   }
   }
   
   public void createFistNLastNodes(){
   	
   	Bag<Job>[] tempAdj;
       Job[] tempNodes;
       Bag<Job>[] tempParents;
       int index = vertices+2;
       
   	for (int v = 0; v < V(); v++){
               getNode(v).incrId();
   	}
       
       tempAdj = (Bag<Job>[]) new Bag[index];
       for (int v = 0; v < index; v++) {
       	tempAdj[v] = new Bag<Job>();
       }
       tempParents = (Bag<Job>[]) new Bag[index];
       for (int v = 0; v < index; v++) {
       	tempParents[v] = new Bag<Job>();
       }
       
       tempNodes = new Job[index];
       
       tempNodes[0] = new Job(0, 0, 0, 0, 0, 0, 0);
    	//	   new Job(0, -1, 0);
       
       for(int i =1; i <=vertices ;i++) {
       	nodes[i-1].incrTaskIDonEdges();
       	tempNodes[i] = nodes[i-1];
       }
       for(int i =1; i <=vertices ;i++) {
       	tempAdj[i] = adj[i-1];
       	tempParents[i] = parents[i-1];
       }
       
     //  tempNodes[index-2] = new TaskData(index-2, -1, 0);
       
       ArrayList<Integer> firstLevel = getFirstLevel();
       
       for(Integer i : firstLevel){
       	int fromIndex = 0;
       	int toIndex = i;
       	//tempNodes[fromIndex] = tempNodes[0];
       	//tempNodes[toIndex] = tempNodes[i];
       	//System.out.println(toIndex + " from " + fromIndex );
       	tempAdj[fromIndex].setNodeInfo(tempNodes[0]);
       	tempAdj[fromIndex].add(nodes[toIndex-1]);
           
           tempParents[toIndex].setNodeInfo(tempNodes[i]);
           tempParents[toIndex].add(tempNodes[0]);
       }
       
       //adding task and edges for exit node
       tempNodes[index-1] = new Job(index-1, 0, 0, 0, 0, 0, 0);
    		   //new TaskData(index-1, -1, 0);
       
       firstLevel = getLastLevel();
       
       for(Integer i : firstLevel){
       	int fromIndex = i;
       	int toIndex = index-1;
       	//tempNodes[fromIndex] = tempNodes[0];
       //	tempNodes[toIndex] = tempNodes[i];
       	
       	tempAdj[fromIndex].setNodeInfo(tempNodes[0]);
       	tempAdj[fromIndex].add(tempNodes[toIndex]);
           
           tempParents[toIndex].setNodeInfo(tempNodes[i]);
          tempParents[toIndex].add(tempNodes[fromIndex]);
       }
       
       this.nodes = tempNodes;
       this.adj = tempAdj;
       this.parents = tempParents;
       this.vertices = index;
       
   }
   
   public void deleteFistNLastNodes(){
   	
   	Bag<Job>[] tempAdj;
       Job[] tempNodes;
       Bag<Job>[] tempParents;
       int index = vertices-2;
       
       tempNodes = new Job[index];
      
       ArrayList<Integer> lastLevel = getLastLevel();
       ArrayList<Integer> lastLevelParents = new ArrayList<>();
      	lastLevelParents.addAll(getParents(lastLevel));

      	for(Integer intr : lastLevelParents){
      		//System.out.println("Removing from "+ intr + " to " + nodes[V-1].getID());
      		removeEdge(intr, nodes[vertices-1]);
      	}
       
      	for(Integer intr : getChildren(0)){
      		//System.out.println("Removing from "+ 0 + " to " + nodes[intr].getID());
      		removeEdge(0, nodes[intr]);
      	}
       for(int i =0; i < index ;i++) {
       	nodes[i+1].decrTaskIDonEdges();
       	tempNodes[i] = nodes[i+1];
       }
   	
   	tempAdj = (Bag<Job>[]) new Bag[index];
       for (int v = 0; v < index; v++) {
       	tempAdj[v] = adj[v+1];
       }
       tempParents = (Bag<Job>[]) new Bag[index];
       for (int v = 0; v < index; v++) {
       	tempParents[v] = parents[v+1];
       }
       
       this.nodes = tempNodes;
       this.adj = tempAdj;
       this.parents = tempParents;
       this.vertices = index;
       
       for (int v = 0; v < V(); v++){
       	getNode(v).decrId();
       }

	}
   
   
   
   
   public void printDAG() {
		
		for (int v = 0; v < this.V(); v++){
		   Job parentJob = getNode(v);
           for (Job w : this.adj(v)){
               System.out.println(v + "->" + w.getIntId() + " Parent Job length " + parentJob.getLength() + " ChildJob length " + w.getLength());
               System.out.println("Data size to transfer " + parentJob.getEdge(w.getIntId()));
           }
   		}
		
	}
      
    /**
     * Remove duplicates in a given list.
     */
    private void removeDuplicates(ArrayList<Integer> levelNodes) {
		HashSet hs = new HashSet();
    	hs.addAll(levelNodes);
    	levelNodes.clear();
    	
    	levelNodes.addAll(hs);
	}
     
    /**
     * Return the list of nodes.
     */
    public Job[] getNodes() {
    	return nodes;
    }

    /**
     * Assigns a set of nodes
     */
    public void setNodes(Job[] nodes) {
    	this.nodes = nodes;
    }
    
    /**
     * Returns a node at vertex v
     */
    public Job getNode(int v){
    	return nodes[v];
    }
    
    /**
     * Return a string representation of the digraph.
     */
    public String toString() {
        StringBuilder s = new StringBuilder();
        String NEWLINE = System.getProperty("line.separator");
        s.append(vertices + " " + edges + NEWLINE);
        for (int v = 0; v < vertices; v++) {
            s.append(v + ": ");
            for (Job w : adj[v]) {
                s.append(w + " ");
            }
            s.append(NEWLINE);
        }
        return s.toString();
    }
    
}
