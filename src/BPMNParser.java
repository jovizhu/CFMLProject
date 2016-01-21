import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class BPMNParser {
	// this parses our workflow from bpmn
	public static ArrayList<ProcessNode> readBPMNSpec(String filename){
		try{ //catch in case the file reader freaks out 
			
			File fXmlFile = new File(filename); //get the bpmn2 file
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); //build doc builder factory
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); // build the doc builder
			Document doc = dBuilder.parse(fXmlFile); //parse the bpmn2 file into a doc
			doc.getDocumentElement().normalize(); // normalize gets rid of junk characters I think
			
			
			//removing tags you know won't be in the bpmn2 file will increase the loadtime.
			List<Node> nodes = searchDoc(doc,
								new String[]{  			//these are the tags that it looks for in the xml
								 "bpmn2:startEvent",		//!!! these are nodes. not connections
								 "bpmn2:endEvent",
								 "bpmn2:intermediateThrowEvent",
								 "bpmn2:intermediateCatchEvent",
								 "bpmn2:boundaryEvent",
								 "bpmn2:dataObject",
								 "bpmn2:exclusiveGateway",
								 "bpmn2:inclusiveGateway",
								 "bpmn2:parallelGateway",
								 "bpmn2:eventBasedGateway",
								 "bpmn2:complexGateway",
								 "bpmn2:dataStoreReference",
								 "bpmn2:dataInput",
								 "bpmn2:scriptTask",
								 "bpmn2:task",
								 "bpmn2:manualTask",
								 "bpmn2:businessRuleTask",
								 "bpmn2:receiveTask",
								 "bpmn2:sendTask",
								 "bpmn2:serviceTask",
								 "bpmn2:userTask"
								});
			
			
			// XML NODES => PROCESS NODES
			List<ProcessNode> pNodes = new ArrayList<>(); //process nodes
			for(Node n: nodes){	//for all the xml nodes we found
				Element e = (Element)n; //convert to an element so we can get the attributes
				pNodes.add( new ProcessNode(e.getAttribute("name"), e.getAttribute("id") )  ); //convert to process nodes 
			}
			
			//finding the solid lines and connecting parent->child
			NodeList flowList = doc.getElementsByTagName("bpmn2:sequenceFlow"); //find all the sequenceFlow xml nodes
			
			for(int z = 0; z<flowList.getLength(); z++){ //for each sequence flow
				Element x = (Element)flowList.item(z); //convert to an Element to get attributes
				String sourceString = x.getAttribute("sourceRef"); //get the sourceReference
				String targetString = x.getAttribute("targetRef"); //get the targetReference
				ProcessNode t = null, s = null;
				for(ProcessNode n: pNodes){ //for all of the process nodes we have
					if(sourceString.compareTo(n.getID())==0){ //if its id is the same as the sourceReference
						s = n;										//assign it as our source node
					}else if(targetString.compareTo(n.getID())==0){ // if its id is the same as our targetReference 
						t = n;											//assign it as our target node
					}
				}
				if(t != null && s != null){ //if we have both a target and a source
					s.addNodeLink(t);  			//make the target a child of the source
				}
			}
			
			//these are the dotted lines
			NodeList dataList = doc.getElementsByTagName("bpmn2:dataOutputAssociation"); //find all the output associations
			for(int i = 0; i < dataList.getLength();i++){                                 //iterate through them all
				NodeList xdataList = ((Element)dataList.item(i)).getChildNodes();           //create a list of each link
				Element parent = (Element)dataList.item(i).getParentNode();                 //get the parent 
				String sourceString = parent.getAttribute("id");							//get the parents id set as source
				String targetString = "~!~!~!~"; 											//some default it'll never be				
				ProcessNode t = null, s = null;
				for(int ii = 0; ii<xdataList.getLength();ii++){								//iterate through the list
					//System.out.println(targetString = xdataList.item(ii).getTextContent());
					if(	xdataList.item(ii).getNodeName().compareTo("bpmn2:targetRef") == 0){ //check if it's the target
						targetString = xdataList.item(ii).getTextContent();					 //set as target
						break;						//break early if possible
					}
				}
				for(ProcessNode n: pNodes){								//iterate through our processes
					if(sourceString.compareTo(n.getID())==0){			//check if it's our source
						s = n;												//set it as source
					}else if(targetString.compareTo(n.getID())==0){		//check if target
						t = n;												//set it as our target
					}
				}
				if(t != null && s != null){		//if we have both
					s.addNodeLink(t);				//make target a child of the source
				}
			}
			
			//connects boundary events to parent
			NodeList boundaryList = doc.getElementsByTagName("bpmn2:boundaryEvent"); //find all the output associations
			for(int z = 0; z<boundaryList.getLength(); z++){ //for each sequence flow
				Element x = (Element)boundaryList.item(z); //convert to an Element to get attributes
				String sourceString = x.getAttribute("attachedToRef"); //get the sourceReference
				String targetString = x.getAttribute("id"); //get the targetReference
				ProcessNode t = null, s = null;
				for(ProcessNode n: pNodes){ //for all of the process nodes we have
					if(sourceString.compareTo(n.getID())==0){ //if its id is the same as the sourceReference
						s = n;										//assign it as our source node
					}else if(targetString.compareTo(n.getID())==0){ // if its id is the same as our targetReference 
						t = n;											//assign it as our target node
					}
				}
				if(t != null && s != null){ //if we have both a target and a source
					s.addNodeLink(t);  			//make the target a child of the source
				}
			}
			//more dotted lines... they connect differently from the previous set of dotted lines
			dataList = doc.getElementsByTagName("bpmn2:dataInputAssociation");   //find all input associations
			for(int i = 0; i < dataList.getLength();i++){						//iterate through them
				NodeList xdataList = ((Element)dataList.item(i)).getChildNodes(); //put the children into a list
				Element parent = (Element)dataList.item(i).getParentNode();			//get the parent
				String targetString = parent.getAttribute("id");					//parent is the target (opposite of dataoutput iteration)
				String sourceString = "-1!!";										//set as some default it'll never be
				ProcessNode t = null, s = null;
				for(int ii = 0; ii<xdataList.getLength();ii++){						//iterate through list of children
					if(	xdataList.item(ii).getNodeName().compareTo("bpmn2:sourceRef") == 0){ //check if source ref
						sourceString = xdataList.item(ii).getTextContent();				//set as the source ref
					}
				}
				for(ProcessNode n: pNodes){									//iterate through each of the process nodes
					if(sourceString.compareTo(n.getID())==0){				//look for source in process nodes
						s = n;
					}else if(targetString.compareTo(n.getID())==0){        //look for target in processnodes
						t = n;
					}
				}
				if(t != null && s != null){		//if we have target and source
					s.addNodeLink(t);				//make target a child of source
				}
			}
			
			
			return (ArrayList<ProcessNode>) pNodes;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	//just reads all the different types of tags returns the node
		public static ArrayList<Node> searchDoc(Document doc, String[] searchList){
			List<Node> ret = new ArrayList<>(); // create a list to return
			NodeList nList = null; // nodelist that the document finds
			for(String tag: searchList){ //iterate through each of the terms
				nList = doc.getElementsByTagName(tag); //get xml nodes with bpmn2:startEvent tag
				for(int z = 0; z<nList.getLength(); z++){
					ret.add(nList.item(z)); //put the xml nodes into the list
				}
			}
			return (ArrayList<Node>)ret; //return all the xml nodes
		}
}
