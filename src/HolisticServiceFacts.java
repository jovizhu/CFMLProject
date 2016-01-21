import apa.Equipment;
import apa.Facility;
import apa.MicroCFD;
import apa.Service;
import apa.SomeFacility;

public class HolisticServiceFacts {

	public static String createHolisticServiceFacts(
			Object[] holistic_service_list, SomeFacility factory) {

		String holistic_service_code = new String();

		String perform_at_code = new String();
		String uses_code = new String();
		String mcfd_code = new String();
		holistic_service_code += "\n%Service Related Code\n";
		for (int i_s = 0; i_s < holistic_service_list.length; i_s++) {

			Service holistic_service = (Service) holistic_service_list[i_s];
			String holistic_service_name = holistic_service.getOwlIndividual()
					.getIRI().getShortForm();

			// exmaple service(orderprocessing).
			holistic_service_code += "service( "
					+ holistic_service_name.toLowerCase() + ").\n";

			Object[] performAts = holistic_service.getPerformAt().toArray();
			Object[] scf = holistic_service.getHasSCF().toArray();
			Object[] uses = holistic_service.getUses().toArray();
			Object[] mcfd = holistic_service.getHasMicroCFD().toArray();
			// Object[] produces = holistic_service.getProduces().toArray();

			// example : performedAt(orderprocessing, [officebuilding]).
			// Must leave a space after [
			perform_at_code += "performedAt( "
					+ holistic_service_name.toLowerCase() + ", [ ";
			for (int i_p = 0; i_p < performAts.length; i_p++) {
				Facility facility = (Facility) performAts[i_p];
				String facility_name = facility.getOwlIndividual().getIRI()
						.getShortForm();
				perform_at_code += facility_name.toLowerCase() + ",";
			}
			perform_at_code = perform_at_code.substring(0,
					perform_at_code.length() - 1)
					+ "]).\n";

			// uses(orderprocessing, []).
			uses_code += "uses( " + holistic_service_name.toLowerCase()
					+ ", [ ";
			for (int i_u = 0; i_u < uses.length; i_u++) {
				Equipment equipment = (Equipment) uses[i_u];
				String equipment_name = equipment.getOwlIndividual().getIRI()
						.getShortForm();
				uses_code += equipment_name.toLowerCase() + ",";
			}
			uses_code = uses_code.substring(0, uses_code.length() - 1)
					+ "]).\n";

			// hasMicroCFD(orderprocessing, 'MicroCFD')

			for (int i_m = 0; i_m < mcfd.length; i_m++) {
				MicroCFD micro_cfd = (MicroCFD) mcfd[i_m];
				String micro_cfd_name = micro_cfd.getOwlIndividual().getIRI()
						.getShortForm();
				mcfd_code += "hasMicroCFD( "
						+ holistic_service_name.toLowerCase() + ", "
						+ micro_cfd_name.toLowerCase() + ").\n";
			}

			// Not support SCF now currently.
		}

		holistic_service_code += "\n%uses code\n" + uses_code;
		holistic_service_code += "\n%perform at code\n" + perform_at_code;
		holistic_service_code += "\n%micro cfd code\n" + mcfd_code;

		System.out.println(holistic_service_code);
		// TODO Auto-generated method stub
		return holistic_service_code;
	}
}
