

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import apa.*;
import apa.impl.DefaultCPU;

//known problems:
//	- duplication of processes that are children of two separate nodes (I.E. Two services have a shared child service)
//    this is not a huge problem since prolog ignores duplications anyways.
//
//  - 
//
//
//


public class TestComp {
	final static String OWL_FILE = "facility2.owl";
	final static String BPMN_FILE = "testingServices.bpmn2";
	public static void main(String[] args) throws Exception {
		
		OWLOntologyManager owlm = OWLManager.createOWLOntologyManager();
		File owlFile = new File(OWL_FILE);
		OWLOntology owl = owlm.loadOntologyFromOntologyDocument(owlFile);
		SomeFacility x = new SomeFacility(owl);
		
		//put all the services into an array
		Object[] s;
		s = x.getAllServiceInstances().toArray();
		
		//put all the micro servicese into another array
		Object[] ms;
		ms = x.getAllMServiceInstances().toArray();
		
		//process BPMN and put the results into the services list
		ArrayList<ProcessNode> services = readBPMNSpec(BPMN_FILE);
		
		ProcessNode ss = null; // our starting service
		
		//look at each service and find our starting service
		for(ProcessNode a: services){
			//Might want to find a better way to select the starting node
			if(a.getID().compareTo("StartEvent_1") == 0){
				ss = a; //start node is a
			}
		}
		
		//if we don't find the starting node
		if(ss == null){
			throw(new Exception("start event not set/detected."));
		}
		
			
		//workflow object to manage which service to walk through
		Workflow wf = new Workflow(ss);
		
		//set the current service to the next wf service
		ss = wf.next();
		
		//bunch of strings to build the prolog code.
		//there's so many of them because prolog has to have it's facts next to eachother.
		//and the way we iterate it won't get all of them in one go.
		// for example we get:
		// service(orderprocessing).
		// hasscf(scf, orderprocessing).
		// ...
		// in one loop, but we need
		// service(orderprocessing).
		// service(scheduling).
		// ...etc
		
		String prologCode = "", servicesCode = "",microsCode="", 
			    usesCode = "", producesCode = "", instanceOf = "", mfCFL = "",
			    scfCode = "", madeFromCode = "", madeFromQuantity = "",
			    performAtCode = "", cfdCode= "", mfListString = "";
		
		//what a product is made from
		ArrayList<String> madeFromList = new ArrayList<>();
		
		//is this process a micro service?
		boolean isMicro = false;
		
		//loop services
		do{
			Service thisService = null;
			
			
			//figure out what this service is and if it's a microservice or not
			for(int i = 0; i < s.length; i++){
				if(ss.getName().compareTo(((Service) s[i]).getOwlIndividual().getIRI().getFragment()) == 0){
					thisService = (Service)s[i];
					isMicro = false;
					break;
				}
			}
			if(thisService == null){ // if it's not one of the services in the loop
				//check if it's a micro service
				for(int mm = 0; mm < ms.length; mm++){
					if(ss.getName().compareTo(((Service) ms[mm]).getOwlIndividual().getIRI().getFragment()) == 0){
						thisService = (Service)ms[mm];
						isMicro = true;
						break;
					}
				}
			}
			//if for some reason we still don't know what service it is, just continue to the next one
			if(thisService == null){
				ss = wf.next();
				continue;
			}
			
			//get all the info about this service
			Object[] f = thisService.getPerformAt().toArray();
			Object[] scf = thisService.getHasSCF().toArray();
			Object[] uses = thisService.getUses().toArray();
			Object[] p = thisService.getProduces().toArray();
			String service = thisService.getOwlIndividual().getIRI().getFragment(); //service name
			
			//define service/microservice
			if(!isMicro){
				servicesCode += "service("+service+").\n";
			}else{
				microsCode += "microservice("+service+").\n";
			}
			//if it's a microservice find some CFL services
			if(isMicro){
				String cfl = "";
				Map<OWLDataPropertyExpression, Set<OWLLiteral>> msprop = thisService.getOwlIndividual().getDataPropertyValues(thisService.getOwlOntology());
				Collection<Set<OWLLiteral>> msprop2 = msprop.values();
				for(int q = 0; q < msprop.keySet().toArray().length; q++){
					String property = msprop.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
					if(property.compareTo("CFLabel")==0){
						cfl = msprop2.toArray()[q].toString().split("\"")[1];
					}else{
						System.out.println("!found unexpected property: " + property);
					}
				}
				mfCFL += "hasCFLabel("+service+", "+cfl+ ").\n";
			}
			
			//if this service produces something, create the related 
			if(p.length > 0){
				madeFromList.clear();
				String product = "";
				//produces
				for(int ri = 0; ri < p.length; ri++){
					product = ((Computer)p[ri]).getOwlIndividual().getIRI().getFragment();
					producesCode += "producedBy("+ product +", "+ service +").\n";
					Object[] pc = ((Computer)p[ri]).getMadeFrom().toArray();
					for(int rr = 0; rr < pc.length; rr++){
						// 
						//	This section may cause problems due to it looking for specific data property names "name", "quantity", and "cflabel" 
						// 	
						//
						//
						String material = ((Computer)pc[rr]).getOwlIndividual().getIRI().getFragment();
						Map<OWLDataPropertyExpression, Set<OWLLiteral>> test = ((DefaultCPU)pc[rr]).getOwlIndividual().getDataPropertyValues(((DefaultCPU)pc[rr]).getOwlOntology());
						String indName = "", quantity = "0", cfl = "";
						Collection<Set<OWLLiteral>> test2 = test.values();
						for(int q = 0; q < test.keySet().toArray().length; q++){
							
							//all the strange splits are because the owl stores the data strangely
							
							String property = test.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
							if(property.toLowerCase().compareTo("name")==0){
								indName = test2.toArray()[q].toString().split("\"")[1];
							}else if(property.toLowerCase().compareTo("quantity")==0){
								quantity = test2.toArray()[q].toString().split("\"")[1];
							}else if(property.toLowerCase().compareTo("cflabel")==0){
								cfl = test2.toArray()[q].toString().split("\"")[1];
							}else{
								System.out.println("found unexpected property: " + property);
							}
						}
						
						//save properties to prolog code
						madeFromQuantity += "hasQuantity("+indName+", "+quantity+").\n";
						madeFromCode += "madeFrom("+material+", "+product+").\n" ;
						madeFromList.add(material);
						instanceOf += "instanceOf("+indName+", "+material+").\n"; 
						mfCFL += "hasCFLabel("+ material + ", "+ cfl +").\n";
					}
				}
				String templist = "";
				for(int y =0; y < madeFromList.size(); y++){
					if(y != madeFromList.size()-1){
						templist += madeFromList.get(y) + ", ";
					}else{
						templist += madeFromList.get(y);
					}
				}
				mfListString += "mflist( "+product+", [" + templist + "]).\n";
			}
			//scf
			if(scf.length > 0){
				for(int ri = 0; ri < scf.length; ri++){
					String someSCF = ((SCF)scf[ri]).getOwlIndividual().getIRI().getFragment();
					scfCode += "hasSCF("+someSCF+", "+ service +").\n";
				}
			}
			//uses
			if(uses.length > 0){
				for(int ri = 0; ri < uses.length; ri++){
					String equipment = ((Equipment)uses[ri]).getOwlIndividual().getIRI().getFragment();
					usesCode += "uses("+equipment+", "+service+").\n";
				}
			}
			//location
			for(int z = 0; z < f.length; z++){
				String facility = ((Facility)f[z]).getOwlIndividual().getIRI().getFragment();
				performAtCode += "performedAt("+facility+", "+service+").\n" ;
				Object[] c = ((Facility)f[z]).getHasCFD().toArray();
				for(int cc = 0; cc < c.length; cc++){
					String cfd = ((CFD)c[cc]).getOwlIndividual().getIRI().getFragment();
					cfdCode += "hasCFD("+cfd+", "+facility+"). \n";
				}
			}
			ss = wf.next();
		}while(wf.hasNext());
		prologCode = servicesCode + microsCode + usesCode + scfCode + mfListString + madeFromCode + mfCFL + instanceOf + madeFromQuantity + performAtCode + cfdCode + producesCode;
		
		//can modify this portion to write directly to a file, but for now it's going to console
		
		System.out.println(prologCode.toLowerCase()); //to lowercase because prolog
		
		//these are just some simple rules to connect values
		System.out.println("hasextendedquantity(X, Y) :- instanceof(A, X), hasquantity(A, Y).");
		System.out.println("hosts(X,Y) :- performedat(Y,X).");
		
		//from the paper
		System.out.println("carbon(Obj, CF) :- hascflabel(Obj, CF).");
		System.out.println("carbon(Obj, CF) :- producedby(Obj, PS),\n\thascflabel(PS, CFps),\n\tmflist(Obj, MFL),\n\tget_CF(MFL, CFmfl),\n\tCF is CFps + CFmfl.");
		System.out.println("get_CF([], 0).");
		System.out.println("get_CF([FirstMFL|LastMFL], CFmfl) :-\n\thascflabel(FirstMFL, CFfirst),\n\thasextendedquantity(FirstMFL, Qfirst),\n\tget_CF(LastMFL, CFLast),\n\tCFmfl is CFfirst * Qfirst + CFLast.");
		
		
		
	}
	
	
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
