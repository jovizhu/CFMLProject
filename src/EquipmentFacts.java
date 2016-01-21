import apa.CFD;
import apa.Equipment;
import apa.SomeFacility;


public class EquipmentFacts {
	
	public static String createEquipmentFacts(Object[] equipment_list,
			SomeFacility factory) {
		String equipment_code = new String();
		
		String cfd_code = new String();
		
		// TODO Auto-generated method stub
		for(int i_e =0; i_e< equipment_list.length; i_e++){
			Equipment equipment = (Equipment) equipment_list[i_e];
			String equipment_name = equipment.getOwlIndividual().getIRI().getShortForm();
			equipment_code += "equipment( "+equipment_name.toLowerCase()+").\n";
			
			Object [] equipment_cfd_list = equipment.getHasCFD().toArray();
			
			for(int i_cfd =0; i_cfd < equipment_cfd_list.length; i_cfd++){
				CFD cfd = (CFD)equipment_cfd_list[i_cfd];
				String cfd_name = cfd.getOwlIndividual().getIRI().getShortForm();
				cfd_code += "hasCFD( "+equipment_name.toLowerCase()+", "+cfd_name.toLowerCase()+").\n";
			}
		}
		
		equipment_code += cfd_code;
		
		// TODO Auto-generated method stub
		return equipment_code;
	}
}
