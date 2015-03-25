import java.util.LinkedList;

public class Workflow {
	LinkedList<ProcessNode> queue = new LinkedList<>();
	ProcessNode current;
	Workflow(ProcessNode start){
		queue.addLast(start);
		next(); // because it should start with a start node
	}
	
	LinkedList<ProcessNode> getQueue(){
		return queue;
	}
	
	boolean hasNext(){
		return (queue.size() > 0);
	}
	
	ProcessNode next(){
		if(queue.size() > 0){
			current = queue.removeFirst();
			if(current.getLinks().size() > 0){
				for(int x = 0; x < current.getLinks().size(); x++){//current.getLinks().size(); x++){
					queue.addLast(current.getLinks().get(x));
				}
			}
			return current;
		}else{
			return null;
		}
	}
}
