package ro.andreiciortea.stn.platform.repository;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;


public interface SparqlRepository {

    ResultSet runSelectQuery(String queryString) throws RepositoryException;
    
    boolean runAskQuery(String queryString) throws RepositoryException;
    
    Model runConstructQuery(String queryString, String format) throws RepositoryException;
    
    Model runDescribeQuery(String queryString, String format) throws RepositoryException;
}
