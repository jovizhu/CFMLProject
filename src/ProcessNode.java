

import java.util.LinkedList;

public class ProcessNode {
	LinkedList<ProcessNode> links;
	String name, id;
	int linkCount = 0;
	static int globalNum = 0;
	ProcessNode(String name, String id){
		links = new LinkedList<>();
		this.name = name;
		this.id = id;
		globalNum++;
	}
	
	ProcessNode next(){
		if(links.size() > 0){
			return links.getFirst();
		}else{
			return null;
		}
	}
	
	LinkedList<ProcessNode> getLinks(){
		return links;
	}
	
	void addNodeLink(ProcessNode n){
		//System.out.println(n.getName() + " was added to " + this.name);
		n.incrementCount();
		links.add(n);
	}
	String getID(){
		return id;
	}
	String getName(){
		return name;
	}
	int getNum(){
		return globalNum;
	}
	boolean removeNodeLink(String id){
		for(int x = 0; x < links.size(); x++){
			if(id.compareTo(links.get(x).getID())==0){
				ProcessNode n = links.remove(x);
				n.decrementCount();
				return true;
			}
		}
		return false;
	}
	
	void incrementCount(){
		linkCount++;
	}
	void decrementCount(){
		linkCount--;
	}
	public String toString(){
		String childrenString = "";
		for(int x = 0; x < links.size(); x++){
			childrenString += "  + "+ links.get(x).getName() + "\n";
		}
		return " - " + getName() + "\n" + childrenString;
	}
}
