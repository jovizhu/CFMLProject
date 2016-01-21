import java.util.LinkedList;

public class Workflow {
	LinkedList<ProcessNode> queue = new LinkedList<>();
	ProcessNode current = null;
	public Workflow(ProcessNode start){
		queue.addLast(start);
		next(); // because it should start with a start node
	}
	
	public LinkedList<ProcessNode> getQueue(){
		return queue;
	}
	
	public boolean hasNext(){
		return (queue.size() > 0);
	}
	
	public ProcessNode next(){
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
	
	public boolean contains(ProcessNode x){
		return recurContains(queue, x);
		
		//return false;
	}
	
	public boolean recurContains(LinkedList<ProcessNode> s, ProcessNode t){
		boolean isThis = false;
		boolean isThat = false;
		if(s.size() == 1){
			return (s.get(0).getName().compareTo(t.getName()) == 0);
		}
		for(ProcessNode x : s){
			if(x.getName().compareTo(t.getName()) == 0){
				isThis = true;
				break;
			}
			if(!isThat){ //if it's not already true
				isThat = recurContains(x.getLinks(), t);
				if(isThat) //this makes sense... probably
					break;
			}else{
				break;//yes
			}
		}
		
		return isThis || isThat;
	}
	

	public static String createWorkflowFacts(Workflow wf,
			String bpmn_procedure_name) {
		String workflow_code = new String();
		
		workflow_code +=" \n%workflow node\n";
		workflow_code +="mServiceList( "+bpmn_procedure_name.toLowerCase()+", [ ";
		while(wf.hasNext()){
			ProcessNode ss = wf.next();
			String node_name = ss.getName();
			if(node_name.compareTo("End") != 0)
				workflow_code +=node_name.toLowerCase()+",";
		}
		workflow_code= workflow_code.substring(0, workflow_code.length()-1)+"]).\n";
		
		System.out.println(workflow_code);
		
		// TODO Auto-generated method stub
		return workflow_code;
		
	}
}
