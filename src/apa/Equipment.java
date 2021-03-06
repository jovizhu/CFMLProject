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
 * Source Class: Equipment <br>
 * @version generated on Wed Jan 20 15:20:39 CST 2016 by jovizhu
 */

public interface Equipment extends WrappedIndividual {

    /* ***************************************************
     * Property http://www.semanticweb.org/james/ontologies/2015/0/untitled-ontology-15#hasCFD
     */
     
    /**
     * Gets all property values for the hasCFD property.<p>
     * 
     * @returns a collection of values for the hasCFD property.
     */
    Collection<? extends CFD> getHasCFD();

    /**
     * Checks if the class has a hasCFD property value.<p>
     * 
     * @return true if there is a hasCFD property value.
     */
    boolean hasHasCFD();

    /**
     * Adds a hasCFD property value.<p>
     * 
     * @param newHasCFD the hasCFD property value to be added
     */
    void addHasCFD(CFD newHasCFD);

    /**
     * Removes a hasCFD property value.<p>
     * 
     * @param oldHasCFD the hasCFD property value to be removed.
     */
    void removeHasCFD(CFD oldHasCFD);


    /* ***************************************************
     * Common interfaces
     */

    OWLNamedIndividual getOwlIndividual();

    OWLOntology getOwlOntology();

    void delete();

}
