

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import apa.impl.*;
//known problems:
//	- duplication of processes that are children of two separate nodes (I.E. Two services have a shared child service)
//    this is not a huge problem since prolog ignores duplications anyways.
//
//  - 
//
//
//


public class CFML {
	
	 //static String OWL_FILE = "facility2.owl";
	 static String OWL_FILE = "WayRad_Computer_9000.owl"; 
	 static String PRODUCT_NAME = "WayRad_Computer_9000";
	 //static String BPMN_FILE = "testingServices.bpmn2";
	 static String PROLOG_FILE = "output.pl";
	 
	public static void main(String[] args) throws Exception {
		
		if(args.length ==0 ){
			System.out.println(" TestComp WayRad_Computer_9000.owl  facts.pl");
		}else if(args.length < 3){
			System.out.println("Usage TestComp owlfile out_put");
		}else if(args.length == 3){
			OWL_FILE = args[0];
			//BPMN_FILE = args[1];
			PROLOG_FILE= args[1];
		}
		
		
		OWLOntologyManager owlm = OWLManager.createOWLOntologyManager();
		File owlFile = new File(OWL_FILE);
		OWLOntology owl = owlm.loadOntologyFromOntologyDocument(owlFile);
		SomeFacility factory = new SomeFacility(owl);
		
		//put all the services into an array
		Object[] holistic_service;
		holistic_service = factory.getAllServiceInstances().toArray();
		
		//put all the micro servicese into another array
		Object[] micro_service;
		micro_service = factory.getAllMServiceInstances().toArray();
		
		Object [] product;
		product = factory.getAllProductInstances().toArray();
		
		Product final_product = null;
		
		for(int i=0; i<product.length; i++){
			String pr_name = ((Product)product[i]).getOwlIndividual().getIRI().getShortForm();
			if(pr_name.equals(PRODUCT_NAME)){
				final_product= (Product)product[i];
			}
		}
		
		createProductFacts(product, factory);
		createHolisticServiceFacts(holistic_service, factory);
		createMicroServiceFacts(micro_service, factory);
		
		// only the 1st produced by works
		Object [] bpmn_produres = final_product.getProducedBy().toArray();
		String bpmn_procedure_name  = ((MService)bpmn_produres[0]).getOwlIndividual().getIRI().getShortForm();
		String bpmn_file = bpmn_procedure_name+".bpmn";
		//(Product) final_product[0].toString()
		//process BPMN and put the results into the services list
		ArrayList<ProcessNode> services = readBPMNSpec(bpmn_file);
		
		ProcessNode ss = null; // our starting service
		
		//look at each service and find our starting service
		for(ProcessNode a: services){
			//Might want to find a better way to select the starting node
			if(a.getName().compareTo("Start") == 0){
				ss = a; //start node is a
			}
		}
		
		//if we don't find the starting node
		if(ss == null){
			throw(new Exception("start event not set/detected."));
		}
		
			
		//workflow object to manage which service to walk through
		Workflow wf = new Workflow(ss);
		
		createWorkflowCode( wf, bpmn_procedure_name);
		
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
			MService thisMService = null;
			
			//figure out what this service is and if it's a microservice or not
			/*for(int i = 0; i < s.length; i++){
				if(ss.getName().compareTo(((Service) s[i]).getOwlIndividual().getIRI().getFragment()) == 0){
					thisService = (Service)s[i];
					isMicro = false;
					break;
				}
			}*/
			
			if(thisMService == null){ 
				// if it's not one of the services in the loop
				//check if it's a micro service
				for(int mm = 0; mm < micro_service.length; mm++){
					if(ss.getName().compareTo(((MService) micro_service[mm]).getOwlIndividual().getIRI().getShortForm()) == 0){
						thisMService = (MService)micro_service[mm];
						isMicro = true;
						break;
					}
				}
			}
			
			//if for some reason we still don't know what service it is, just continue to the next one
			if(thisMService == null){
				ss = wf.next();
				continue;
			}
			
			/*
			Service holistic_service =(Service) thisMService.getHasHolisticService().toArray()[0];
			//get all the info about this service
			Object[] performAts = holistic_service.getPerformAt().toArray();
			Object[] scf = holistic_service.getHasSCF().toArray();
			Object[] uses = holistic_service.getUses().toArray();
			Object[] produces = holistic_service.getProduces().toArray();
			
			String service_name = thisMService.getOwlIndividual().getIRI().getShortForm(); //service name
			*/
			
			//define service/microservice
			/*
			if(!isMicro){
				servicesCode += "service("+service_name+").\n";
			}else{
				microsCode += "microservice("+service_name+").\n";
			}*/
			
			//if it's a microservice find some CFL services
			if(isMicro){
				String cfl = "";
				Map<OWLDataPropertyExpression, Set<OWLLiteral>> msprop = thisMService.getOwlIndividual().getDataPropertyValues(thisMService.getOwlOntology());
				Collection<Set<OWLLiteral>> msprop2 = msprop.values();
				for(int q = 0; q < msprop.keySet().toArray().length; q++){
					String property = msprop.keySet().toArray()[q].toString().split("#")[1].replaceAll("[^A-Za-z0-9]", "");
					if(property.compareTo("CFLabel")==0){
						System.out.println("property founded :"+property);
						cfl = msprop2.toArray()[q].toString().split("\"")[1];
					}else{
						System.out.println("!found unexpected property: " + property);
					}
				}
				//mfCFL += "hasCFLabel("+service+", "+cfl+ ").\n";
			}
			
			/*
			//if this service produces something, create the related 
			if(produces.length > 0){
				madeFromList.clear();
				String product = "";
				//produces
				for(int ri = 0; ri < produces.length; ri++){
					product = ((Product)produces[ri]).getOwlIndividual().getIRI().getFragment();
					producesCode += "producedBy("+ product +", "+ service +").\n";
					Object[] pc = ((Product)produces[ri]).getMadeFrom().toArray();
					for(int rr = 0; rr < pc.length; rr++){
						// 
						//	This section may cause problems due to it looking for specific data property names "name", "quantity", and "cflabel" 
						// 	
						//
						//
						String material = ((Product)pc[rr]).getOwlIndividual().getIRI().getFragment();
						Map<OWLDataPropertyExpression, Set<OWLLiteral>> test = ((Product)pc[rr]).getOwlIndividual().getDataPropertyValues(((Product)pc[rr]).getOwlOntology());
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
			}*/
			
			/*
			//scf
			if(scf.length > 0){
				for(int ri = 0; ri < scf.length; ri++){
					String someSCF = ((SCF)scf[ri]).getOwlIndividual().getIRI().getFragment();
					scfCode += "hasSCF("+someSCF+", "+ service +").\n";
				}
			}*/
			/*
			//uses
			if(uses.length > 0){
				for(int ri = 0; ri < uses.length; ri++){
					String equipment = ((Equipment)uses[ri]).getOwlIndividual().getIRI().getFragment();
					usesCode += "uses("+equipment+", "+service+").\n";
				}
			}*/
			/*
			//location
			for(int z = 0; z < performAts.length; z++){
				String facility = ((Facility)performAts[z]).getOwlIndividual().getIRI().getFragment();
				performAtCode += "performedAt("+facility+", "+service+").\n" ;
				Object[] c = ((Facility)performAts[z]).getHasCFD().toArray();
				for(int cc = 0; cc < c.length; cc++){
					String cfd = ((CFD)c[cc]).getOwlIndividual().getIRI().getFragment();
					cfdCode += "hasCFD("+cfd+", "+facility+"). \n";
				}
			}
			*/
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
		
		
		// output to a file added by jovi Nov 17, 2015
		File file = new File(PROLOG_FILE);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(prologCode.toLowerCase());
		bw.write("hasextendedquantity(X, Y) :- instanceof(A, X), hasquantity(A, Y).\n");
		bw.write("hosts(X,Y) :- performedat(Y,X).\n");
		
		//from the paper
		bw.write("carbon(Obj, CF) :- hascflabel(Obj, CF).\n");
		bw.write("carbon(Obj, CF) :- producedby(Obj, PS),\n\thascflabel(PS, CFps),\n\tmflist(Obj, MFL),\n\tget_CF(MFL, CFmfl),\n\tCF is CFps + CFmfl.\n");
		bw.write("get_CF([], 0).\n");
		bw.write("get_CF([FirstMFL|LastMFL], CFmfl) :-\n\thascflabel(FirstMFL, CFfirst),\n\thasextendedquantity(FirstMFL, Qfirst),\n\tget_CF(LastMFL, CFLast),\n\tCFmfl is CFfirst * Qfirst + CFLast.\n");	
		bw.close();

		System.out.println("Done");
	}
	
	
	private static String createWorkflowCode(Workflow wf,
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


	private static String createMicroServiceFacts(Object[] micro_service_list,
			SomeFacility factory) {
		String micro_service_code = new String();
		String has_holistic_code = new String();
		
		micro_service_code += "\n%Micro Service Related Code\n";
		for (int i_s = 0; i_s < micro_service_list.length; i_s++) {

			MService micro_service = (MService) micro_service_list[i_s];
			String micro_service_name = micro_service.getOwlIndividual().getIRI().getShortForm();
			
			//exmaple microService(morderprocessing).
			micro_service_code += "microService( "+micro_service_name.toLowerCase()+").\n";
			
			// exmaple hasHolisticService(morderprocessing, orderprocessing).
			Object [] holisticServices = micro_service.getHasHolisticService().toArray();
			for(int i_h =0; i_h<holisticServices.length; i_h++){
				Service holistic_service = (Service)holisticServices[i_h];
				String holistic_service_name = holistic_service.getOwlIndividual().getIRI().getShortForm();
				has_holistic_code += "hasHolisticService( "+micro_service_name.toLowerCase()+", "+holistic_service_name.toLowerCase()+").\n";
			}
			
		}

		micro_service_code += "\n%has Holistic Service Code\n"+has_holistic_code;
		System.out.println(micro_service_code);
		return micro_service_code;

	}


	private static String createHolisticServiceFacts(Object[] holistic_service_list,
			SomeFacility factory) {
		
		String holistic_service_code = new String();
		
		String perform_at_code = new String();
		String uses_code = new String();
		String facility_cfd_code = new String();
		String equipment_cfd_code = new String();
		String facility_code = new String();
		String equipment_code = new String();
		String mcfd_code = new String();
		holistic_service_code += "\n%Service Related Code\n";
		for (int i_s = 0; i_s < holistic_service_list.length; i_s++) {
			
			Service holistic_service = (Service) holistic_service_list[i_s];
			String holistic_service_name = holistic_service.getOwlIndividual().getIRI().getShortForm();
			
			// exmaple service(orderprocessing).
			holistic_service_code += "service( "+holistic_service_name.toLowerCase()+").\n";
			
			Object[] performAts = holistic_service.getPerformAt().toArray();
			Object[] scf = holistic_service.getHasSCF().toArray();
			Object[] uses = holistic_service.getUses().toArray();
			Object[] mcfd = holistic_service.getHasMicroCFD().toArray();
			//Object[] produces = holistic_service.getProduces().toArray();
			
			// example : performedAt(orderprocessing, [officebuilding]).
			// Must leave a space after [
			perform_at_code += "performedAt( "+holistic_service_name.toLowerCase()+", [ ";
			for(int i_p =0; i_p < performAts.length; i_p++){
				Facility facility = (Facility)performAts[i_p];
				String facility_name = facility.getOwlIndividual().getIRI().getShortForm();
				Object [] facility_cfd_list = facility.getHasCFD().toArray();
				for(int i_f =0; i_f < facility_cfd_list.length; i_f++){
					CFD facility_cfd = (CFD)facility_cfd_list[i_f];
					String facility_cfd_name = facility_cfd.getOwlIndividual().getIRI().getShortForm();
					facility_cfd_code += "hasCFD( "+facility_name.toLowerCase()+", "+facility_cfd_name.toLowerCase()+").\n";
				}
				perform_at_code +=facility_name.toLowerCase()+",";
				facility_code += "facility( "+facility_name.toLowerCase()+").\n";
			}
			perform_at_code = perform_at_code.substring(0, perform_at_code.length()-1)+"]).\n";
			
			//uses(orderprocessing, []).
			uses_code += "uses( "+holistic_service_name.toLowerCase()+", [ ";
			for(int i_u =0; i_u < uses.length; i_u++){
				Equipment equipment = (Equipment)uses[i_u];
				String equipment_name = equipment.getOwlIndividual().getIRI().getShortForm();
				
				Object [] equipment_cfd_list = equipment.getHasCFD().toArray();
				for(int i_e =0; i_e < equipment_cfd_list.length; i_e++){
					CFD equipment_cfd = (CFD)equipment_cfd_list[i_e];
					String equipment_cfd_name = equipment_cfd.getOwlIndividual().getIRI().getShortForm();
					equipment_cfd_code += "hasCFD( "+equipment_name.toLowerCase()+", "+equipment_cfd_name.toLowerCase()+").\n";
				}
				
				uses_code +=equipment_name.toLowerCase()+",";
				equipment_code += "equipment( "+equipment_name.toLowerCase()+").\n";
				
			}
			uses_code = uses_code.substring(0, uses_code.length()-1)+"]).\n";
			
			//hasMicroCFD(orderprocessing, 'MicroCFD')
			
			for(int i_m =0; i_m < mcfd.length; i_m++){
				MicroCFD micro_cfd = (MicroCFD)mcfd[i_m];
				String micro_cfd_name = micro_cfd.getOwlIndividual().getIRI().getShortForm();
				mcfd_code += "hasMicroCFD( "+holistic_service_name.toLowerCase()+", "+micro_cfd_name.toLowerCase()+").\n";
			}
			
			// Not support SCF now currently.
		}
		
		holistic_service_code += "\n%facility code\n"+facility_code;
		holistic_service_code += "\n%facility cfd code\n"+facility_cfd_code;
		
		holistic_service_code += "\n%equipment code\n"+equipment_code;
		holistic_service_code += "\n%equipment cfd code\n"+equipment_cfd_code;
		holistic_service_code += "\n%uses code\n"+uses_code;
		holistic_service_code += "\n%perform at code\n"+perform_at_code;
		holistic_service_code += "\n%micro cfd code\n"+mcfd_code;
		
		System.out.println(holistic_service_code);
		// TODO Auto-generated method stub
		return holistic_service_code;	
	}

	private static String createProductFacts(Object[] products, SomeFacility factory) {
		// TODO Auto-generated method stub
		String products_code = new String();
		String made_from_code = new String();
		String quantity_code = new String();
		String cf_label_code = new String();
		
		products_code += "\n%product related code\n";
		
		for (int ri = 0; ri < products.length; ri++) {

			// producedBy
			Product pr = (Product) products[ri];
			Object[] pr_workflow = pr.getProducedBy().toArray();
			String pr_name = pr.getOwlIndividual().getIRI().getShortForm();

			for (int i = 0; i < pr_workflow.length; i++) {
				String pr_workflow_name = ((MService) pr_workflow[i])
						.getOwlIndividual().getIRI().getShortForm();
				products_code += "producedBy(" + pr_name.toLowerCase() + ", "
						+ pr_workflow_name.toLowerCase() + "). \n";
			}

			Object[] pr_madefrom_list = pr.getMadeFrom().toArray();

			if (pr_madefrom_list.length > 0) {
				// need to leave a space after the [
				// Important!!!!!!!!!!!!!!!!!!!
				made_from_code += "mfList(" + pr_name.toLowerCase() + ", [ ";

				for (int rr = 0; rr < pr_madefrom_list.length; rr++) {

					Product pr_madefrom = (Product) pr_madefrom_list[rr];
					//
					// This section may cause problems due to it looking for
					// specific data property names "name", "quantity", and
					// "cflabel"
					//
					String made_from_name = pr_madefrom.getOwlIndividual()
							.getIRI().getShortForm();

					Map<OWLDataPropertyExpression, Set<OWLLiteral>> test = pr_madefrom
							.getOwlIndividual().getDataPropertyValues(
									pr_madefrom.getOwlOntology());

					String indName = "", quantity = "0", cfl = "";
					Collection<Set<OWLLiteral>> test2 = test.values();
					for (int q = 0; q < test.keySet().toArray().length; q++) {

						// all the strange splits are because the owl stores the
						// data strangely

						String property = test.keySet().toArray()[q].toString()
								.split("#")[1].replaceAll("[^A-Za-z0-9]", "");
						if (property.toLowerCase().compareTo("name") == 0) {
							indName = test2.toArray()[q].toString().split("\"")[1];
						} else if (property.toLowerCase().compareTo("quantity") == 0) {
							quantity = test2.toArray()[q].toString()
									.split("\"")[1];
						} else if (property.toLowerCase().compareTo("cflabel") == 0) {
							cfl = test2.toArray()[q].toString().split("\"")[1];
						} else {
							System.out.println("found unexpected property: "
									+ property);
						}
					}

					// save properties to prolog code
					made_from_code += " " + made_from_name.toLowerCase() + ",";
					quantity_code += "hasQuantity( " + pr_name.toLowerCase() + ", "
							+ made_from_name.toLowerCase() + ", " + quantity.toLowerCase() + ").\n";
					cf_label_code += "hasCFLabel( " + made_from_name.toLowerCase()+", "+cfl.toLowerCase()+").\n";
				}

				made_from_code = made_from_code.substring(0,
						made_from_code.length() - 1)
						+ "]).\n";
			}
		}
		
		products_code += "\n%made from code\n"+made_from_code;
		products_code += "\n%quantity code\n"+quantity_code;
		products_code += "\n%cf label code\n"+cf_label_code;
		System.out.println(products_code);
		
		return products_code;
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
