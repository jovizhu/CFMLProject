package apa;

import java.net.URI;
import java.util.Collection;
import javax.xml.datatype.XMLGregorianCalendar;

import org.protege.owl.codegeneration.WrappedIndividual;

import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * 
 * <p>
 * Generated by Protege (http://protege.stanford.edu). <br>
 * Source Class: MService <br>
 * @version generated on Wed Jan 20 15:20:39 CST 2016 by jovizhu
 */

public interface MService extends WrappedIndividual {

    /* ***************************************************
     * Property http://www.semanticweb.org/james/ontologies/2015/0/untitled-ontology-15#hasHolisticService
     */
     
    /**
     * Gets all property values for the hasHolisticService property.<p>
     * 
     * @returns a collection of values for the hasHolisticService property.
     */
    Collection<? extends Service> getHasHolisticService();

    /**
     * Checks if the class has a hasHolisticService property value.<p>
     * 
     * @return true if there is a hasHolisticService property value.
     */
    boolean hasHasHolisticService();

    /**
     * Adds a hasHolisticService property value.<p>
     * 
     * @param newHasHolisticService the hasHolisticService property value to be added
     */
    void addHasHolisticService(Service newHasHolisticService);

    /**
     * Removes a hasHolisticService property value.<p>
     * 
     * @param oldHasHolisticService the hasHolisticService property value to be removed.
     */
    void removeHasHolisticService(Service oldHasHolisticService);


    /* ***************************************************
     * Property http://www.semanticweb.org/james/ontologies/2015/0/untitled-ontology-15#hasService
     */
     
    /**
     * Gets all property values for the hasService property.<p>
     * 
     * @returns a collection of values for the hasService property.
     */
    Collection<? extends Service> getHasService();

    /**
     * Checks if the class has a hasService property value.<p>
     * 
     * @return true if there is a hasService property value.
     */
    boolean hasHasService();

    /**
     * Adds a hasService property value.<p>
     * 
     * @param newHasService the hasService property value to be added
     */
    void addHasService(Service newHasService);

    /**
     * Removes a hasService property value.<p>
     * 
     * @param oldHasService the hasService property value to be removed.
     */
    void removeHasService(Service oldHasService);


    /* ***************************************************
     * Common interfaces
     */

    OWLNamedIndividual getOwlIndividual();

    OWLOntology getOwlOntology();

    void delete();

}
