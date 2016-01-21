import apa.CFD;
import apa.Facility;
import apa.SomeFacility;


public class FacilityFacts{

	public static String createFacilityFacts(Object[] facility_list,
			SomeFacility factory) {
		String facility_code = new String();
		String cfd_code = new String();
		
		// TODO Auto-generated method stub
		for(int i_f =0; i_f< facility_list.length; i_f++){
			Facility facility = (Facility) facility_list[i_f];
			String facility_name = facility.getOwlIndividual().getIRI().getShortForm();
			facility_code += "facility( "+facility_name.toLowerCase()+").\n";
			
			Object [] facility_cfd_list = facility.getHasCFD().toArray();
			
			for(int i_cfd =0; i_cfd < facility_cfd_list.length; i_cfd++){
				CFD cfd = (CFD)facility_cfd_list[i_cfd];
				String cfd_name = cfd.getOwlIndividual().getIRI().getShortForm();
				cfd_code += "hasCFD( "+facility_name.toLowerCase()+", "+cfd_name.toLowerCase()+").\n";
			}
		}
		
		facility_code += "\n%facility code\n"+cfd_code;
		
		return facility_code;
	}
}
