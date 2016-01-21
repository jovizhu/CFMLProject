import apa.MService;
import apa.Service;
import apa.SomeFacility;


public class MicroServiceFacts {


	public static String createMicroServiceFacts(Object[] micro_service_list,
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

}
