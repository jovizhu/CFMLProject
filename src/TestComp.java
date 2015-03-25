

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

public class TestComp {

	public static void main(String[] args) throws Exception {
		
		OWLOntologyManager owlm = OWLManager.createOWLOntologyManager();
		File owlFile = new File("facility2.owl");
		OWLOntology owl = owlm.loadOntologyFromOntologyDocument(owlFile);
		SomeFacility x = new SomeFacility(owl);
		
		//System.out.println(x.getAllFacilityInstances());
		//System.out.println(x.getAllServiceInstances());
		Object[] s;
		s = x.getAllServiceInstances().toArray();
		Object[] ms;
		ms = x.getAllMServiceInstances().toArray();
		/*
		for(int i = 0; i < s.length; i++){
			System.out.println("Service: " + ((Service) s[i]).getOwlIndividual().getIRI().getFragment());
			Object[] f = ((Service)s[i]).getPerformAt().toArray();
			System.out.println("Takes Place At:");
			for(int z = 0; z < f.length; z++){
				System.out.println("  "+((Facility)f[z]).getOwlIndividual().getIRI().getFragment());
				Object[] c = ((Facility)f[z]).getHasCFD().toArray();
				System.out.println("    CFDs:");
				for(int cc = 0; cc < c.length; cc++){
					System.out.println("      " +((CFD)c[cc]).getOwlIndividual().getIRI().getFragment() );
					System.out.println("      " + ((CFD)c[cc]).calculateCFD());
				}
			}
		}*/
		
		ArrayList<ProcessNode> services = readBPMNSpec("testingServices.bpmn2");
		ProcessNode ss = null; // our starting node
		for(ProcessNode a: services){
			if(a.getID().compareTo("StartEvent_1") == 0){
				ss = a; //start node is a
			}
		}
		if(ss == null){
			throw(new Exception("start event not set/detected."));
		}
		//int CFL = 0;
		boolean prologMode = true;
		Workflow wf = new Workflow(ss);
		ss = wf.next();
		String prologCode = "", servicesCode = "",microsCode="", 
			    usesCode = "", producesCode = "", instanceOf = "", mfCFL = "",
			    scfCode = "", madeFromCode = "", madeFromQuantity = "",
			    performAtCode = "", cfdCode= "", mfListString = "";
		ArrayList<String> madeFromList = new ArrayList<>();
		boolean isMicro = false;
		//System.out.println(ss);
		//System.out.println(wf.getQueue());
		do{
			//System.out.println("!!-- " + ss.getName());
			Service thisService = null;
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
			if(!prologMode)
				System.out.println("----------------\nService: " + thisService.getOwlIndividual().getIRI().getFragment()+ "\n----------------");
			Object[] f = thisService.getPerformAt().toArray();
			Object[] scf = thisService.getHasSCF().toArray();
			Object[] uses = thisService.getUses().toArray();
			Object[] p = thisService.getProduces().toArray();
			String service = thisService.getOwlIndividual().getIRI().getFragment();
			if(prologMode){
				if(!isMicro){
					servicesCode += "service("+service+").\n";
				}else{
					microsCode += "microservice("+service+").\n";
				}
			}
			if(isMicro){
				String cfl = "";
				Map<OWLDataPropertyExpression, Set<OWLLiteral>> msprop = thisService.getOwlIndividual().getDataPropertyValues(thisService.getOwlOntology());
				Collection<Set<OWLLiteral>> msprop2 = msprop.values();
				for(int q = 0; q < msprop.keySet().toArray().length; q++){
					String property = msprop.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
					if(property.compareTo("CFLabel")==0){
						cfl = msprop2.toArray()[q].toString().split("\"")[1];
						//System.out.println("ind name: " + indName);
					}else{
						System.out.println("found unexpected property: " + property);
					}
				}
				mfCFL += "hasCFLabel("+service+", "+cfl+ ").\n";
			}
			if(p.length > 0){
				madeFromList.clear();
				String product = "";
				if(!prologMode)
					System.out.println("Produces: ");
				for(int ri = 0; ri < p.length; ri++){
					product = ((Computer)p[ri]).getOwlIndividual().getIRI().getFragment();
					if(prologMode){
						producesCode += "producedBy("+ product +", "+ service +").\n";
					}else{
						System.out.println("  #---------------\n   "+((Computer)p[ri]).getOwlIndividual().getIRI().getFragment());
					}
					Object[] pc = ((Computer)p[ri]).getMadeFrom().toArray();
					if(!prologMode)
						System.out.println("  # Made From");
					for(int rr = 0; rr < pc.length; rr++){
						//hope this works
						// heads up... we'll have to rework this to be much nicer to get the quantity values.
						// this will likely break if individuals' quantity, name properties are changed.
						if(prologMode){
							String material = ((Computer)pc[rr]).getOwlIndividual().getIRI().getFragment();
							Map<OWLDataPropertyExpression, Set<OWLLiteral>> test = ((DefaultCPU)pc[rr]).getOwlIndividual().getDataPropertyValues(((DefaultCPU)pc[rr]).getOwlOntology());
							String indName = "", quantity = "0", cfl = "";
							Collection<Set<OWLLiteral>> test2 = test.values();
							for(int q = 0; q < test.keySet().toArray().length; q++){
								String property = test.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
								if(property.compareTo("name")==0){
									indName = test2.toArray()[q].toString().split("\"")[1];
									//System.out.println("ind name: " + indName);
								}else if(property.compareTo("quantity")==0){
									quantity = test2.toArray()[q].toString().split("\"")[1];
									//System.out.println("quantity: " + 	quantity);
								}else if(property.compareTo("CFLabel")==0){
									cfl = test2.toArray()[q].toString().split("\"")[1];
								}else{
									System.out.println("found unexpected property: " + property);
								}
							}
							madeFromQuantity += "hasQuantity("+indName+", "+quantity+").\n";
							madeFromCode += "madeFrom("+material+", "+product+").\n" ;
							madeFromList.add(material);
							instanceOf += "instanceOf("+indName+", "+material+").\n";
							//TODO: get mfCFL and link it to the individual 
							mfCFL += "hasCFLabel("+ material + ", "+ cfl +").\n";
						}else{
							//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+((Computer)pc[rr]).getOwlIndividual().asOWLNamedIndividual());
							Map<OWLDataPropertyExpression, Set<OWLLiteral>> test = ((DefaultCPU)pc[rr]).getOwlIndividual().getDataPropertyValues(((DefaultCPU)pc[rr]).getOwlOntology());
							String indName = "", quantity = "0", cfl="";
							String material = ((Computer)pc[rr]).getOwlIndividual().getIRI().getFragment();
							Collection<Set<OWLLiteral>> test2 = test.values();
							for(int q = 0; q < test.keySet().toArray().length; q++){
								String property = test.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
								if(property.compareTo("name")==0){
									indName = test2.toArray()[q].toString().split("\"")[1];
									//System.out.println("ind name: " + indName);
								}else if(property.compareTo("quantity")==0){
									quantity = test2.toArray()[q].toString().split("\"")[1];
									//System.out.println("quantity: " + 	quantity);
								}else if(property.compareTo("CFLabel")==0){
									cfl = test2.toArray()[q].toString().split("\"")[1];
								}else{
									System.out.println("found unexpected property: " + property);
								}
							}
							
							//
							madeFromQuantity += "hasQuantity("+indName+", "+quantity+").\n";
							instanceOf += "instanceOf("+indName+", "+product+").\n";
							mfCFL += "hasCFLabel("+ material + ", "+ cfl +").\n";
							System.out.println("  #!! q("+ quantity + ") name:" + indName + " individual: " + material +" CFL:" + cfl );
						}
						//CFL += ((Computere)pc[rr]).getCFValue();
					}
					if(!prologMode)
						System.out.println("  #---------------");
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
			if(scf.length > 0){
				if(!prologMode)
					System.out.println("Has SCF: ");
				
				for(int ri = 0; ri < scf.length; ri++){
					String someSCF = ((SCF)scf[ri]).getOwlIndividual().getIRI().getFragment();
					if(prologMode){
						scfCode += "hasSCF("+someSCF+", "+ service +").\n";
					}else{
						System.out.println("  "+someSCF);
					}
					//CFL += ((SCFe)scf[ri]).getSCFValue();
				}
			}
			if(uses.length > 0){
				if(!prologMode)
					System.out.println("Uses: ");
				for(int ri = 0; ri < uses.length; ri++){
					String equipment = ((Equipment)uses[ri]).getOwlIndividual().getIRI().getFragment();
					if(prologMode){
						usesCode += "uses("+equipment+", "+service+").\n";
					}else{
						System.out.println("  "+equipment);
					}
				}
			}
			if(!prologMode)
				System.out.println("Takes Place At:");
			for(int z = 0; z < f.length; z++){
				String facility = ((Facility)f[z]).getOwlIndividual().getIRI().getFragment();
				if(prologMode){
					performAtCode += "performedAt("+facility+", "+service+").\n" ;
				}else{
					System.out.println("  "+facility);
				}
				Object[] c = ((Facility)f[z]).getHasCFD().toArray();
				if(!prologMode)
					System.out.println("    CFDs:");
				for(int cc = 0; cc < c.length; cc++){
					String cfd = ((CFD)c[cc]).getOwlIndividual().getIRI().getFragment();
					if(prologMode){
						cfdCode += "hasCFD("+cfd+", "+facility+"). \n";
					}else{
						System.out.println("      " + cfd);
					}
					//CFL += ((CFDe)c[cc]).getCFDValue();
					//System.out.println("      " + ((CFD)c[cc]).calculateCFD());
				}
			}
			ss = wf.next();
		}while(wf.hasNext());
		//System.out.println("Final CFL: " + CFL);
		if(prologMode){
			prologCode = servicesCode + microsCode + usesCode + scfCode + mfListString + madeFromCode + mfCFL + instanceOf + madeFromQuantity + performAtCode + cfdCode + producesCode;
			System.out.println(prologCode.toLowerCase());
			System.out.println("hasextendedquantity(X, Y) :- instanceof(A, X), hasquantity(A, Y).");
			System.out.println("hosts(X,Y) :- performedat(Y,X).");
			/*
			 *  carbon(Obj, CF) :- hascflabel(Obj, CF).
				carbon(Obj, CF) :- producedby(Obj, PS),
									hascflabel(PS, CFps),
									mflist(Obj, MFL), 
									get_CF(MFL, CFmfl), 
									CF is CFps + CFmfl.
				
				get_CF([], 0).
				get_CF([FirstMFL|LastMFL], CFmfl) :-
					hascflabel(FirstMFL, CFfirst),
					hasextendedquantity(FirstMFL, Qfirst),
					get_CF(LastMFL, CFLast),
					CFmfl is CFfirst * Qfirst + CFLast.
			 */
			System.out.println("carbon(Obj, CF) :- hascflabel(Obj, CF).");
			System.out.println("carbon(Obj, CF) :- producedby(Obj, PS),\n\thascflabel(PS, CFps),\n\tmflist(Obj, MFL),\n\tget_CF(MFL, CFmfl),\n\tCF is CFps + CFmfl.");
			System.out.println("get_CF([], 0).");
			System.out.println("get_CF([FirstMFL|LastMFL], CFmfl) :-\n\thascflabel(FirstMFL, CFfirst),\n\thasextendedquantity(FirstMFL, Qfirst),\n\tget_CF(LastMFL, CFLast),\n\tCFmfl is CFfirst * Qfirst + CFLast.");
			
		}
		
		
	}
	
	public static ArrayList<ProcessNode> readBPMNSpec(String filename){
		try{ //catch in case the file reader freaks out 
			//long testTime = System.currentTimeMillis();
			File fXmlFile = new File(filename); //get the bpmn2 file
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance(); //build doc builder factory
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder(); // build the doc builder
			Document doc = dBuilder.parse(fXmlFile); //parse the bpmn2 file into a doc
			doc.getDocumentElement().normalize(); // normalize gets rid of junk characters I think
			//testTime = System.currentTimeMillis()-testTime;
			//System.out.println("setting up document time: " + testTime);
			//removing tags you know won't be in the bpmn2 file will increase the loadtime.
			//testTime = System.currentTimeMillis();
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
			//testTime = System.currentTimeMillis()-testTime;
			//System.out.println("grabbing relevent nodes: " + testTime);
			// XML NODES => PROCESS NODES
			//testTime = System.currentTimeMillis();
			List<ProcessNode> pNodes = new ArrayList<>(); //process nodes
			for(Node n: nodes){	//for all the xml nodes we found
				Element e = (Element)n; //convert to an element so we can get the attributes
				//System.out.println(e.getAttribute("name") +"   "+ e.getAttribute("id"));
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
			//testTime = System.currentTimeMillis()-testTime;
			//System.out.println("connecting nodes: " + testTime);
			//print out process nodes
			/*for(ProcessNode a: pNodes){
				if(a.linkCount == 0 && a.links.size() == 0){ //skip empty
					System.out.println(a.getID()+ " :: "+a.getName() +" is a empty node that we're skipping");
					continue;
				}
				System.out.println("--------------"+a.getName()+ "::" + a.getNum() +"----------------");
				System.out.println(a); //shows process node and its children
			}*/
			
			
			return (ArrayList<ProcessNode>) pNodes;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
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
