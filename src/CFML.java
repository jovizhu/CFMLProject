

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
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

public class CFML {
	
	
	
	 static String OWL_FILE = "WayRad_Computer_9000.owl"; 
	 static String PRODUCT_NAME = "WayRad_Computer_9000";
	 static String PROLOG_FILE = "output.pl";
	 static String RULE_FILE = "CFML_rule.pl";
	 
	public static void main(String[] args) throws Exception {
		
		if(args.length ==0 ){
			System.out.println(" CFML WayRad_Computer_9000.owl  facts.pl");
		}else if(args.length < 2){
			System.out.println("Usage CFML owlfile out_put");
		}else if(args.length == 2){
			OWL_FILE = args[0];
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
		
		Object [] facility;
		facility = factory.getAllFacilityInstances().toArray();
		
		Object [] equipment;
		equipment = factory.getAllEquipmentInstances().toArray();
		
		Product final_product = null;
		
		for(int i=0; i<product.length; i++){
			String pr_name = ((Product)product[i]).getOwlIndividual().getIRI().getShortForm();
			if(pr_name.equals(PRODUCT_NAME)){
				final_product= (Product)product[i];
			}
		}
		
		String product_code= ProductFacts.createProductFacts(product, factory);
		String holistic_service_code = HolisticServiceFacts.createHolisticServiceFacts(holistic_service, factory);
		String micro_service_code = MicroServiceFacts.createMicroServiceFacts(micro_service, factory);
		String facility_code = FacilityFacts.createFacilityFacts(facility, factory);
		String equipment_code = EquipmentFacts.createEquipmentFacts(equipment, factory);
		
		// only the 1st produced by works
		Object [] bpmn_produres = final_product.getProducedBy().toArray();
		String bpmn_procedure_name  = ((MService)bpmn_produres[0]).getOwlIndividual().getIRI().getShortForm();
		String bpmn_file = bpmn_procedure_name+".bpmn";
		//(Product) final_product[0].toString()
		//process BPMN and put the results into the services list
		ArrayList<ProcessNode> services = BPMNParser.readBPMNSpec(bpmn_file);
		
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
		String workflow_code = Workflow.createWorkflowFacts( wf, bpmn_procedure_name);
		
		String prologCode = product_code + holistic_service_code + micro_service_code + facility_code + equipment_code + workflow_code;
		
		//can modify this portion to write directly to a file, but for now it's going to console
		
		System.out.println(prologCode); //to lowercase because prolog
		
		
		// output to a file added by jovi Nov 17, 2015
		File file = new File(PROLOG_FILE);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(prologCode);
	
		bw.write("\n\n%*****************************\n"
				+ "%***********rules**************\n\n");
		FileReader fr = new FileReader(RULE_FILE);
		BufferedReader br = new BufferedReader(fr);
		String line;
		while((line = br.readLine()) != null){
			bw.write(line+"\n");
		}
		br.close();
		bw.close();
		fr.close();
		fw.close();
		System.out.println("Done");
	}
	
	


	

}
