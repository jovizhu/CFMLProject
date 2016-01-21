import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;

import apa.MService;
import apa.Product;
import apa.SomeFacility;


public class ProductFacts {
	public static String createProductFacts(Object[] products, SomeFacility factory) {
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
}
