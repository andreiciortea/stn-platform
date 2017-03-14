package ro.andreiciortea.stn.platform.repository;

public interface SparqlRepository {

    String runSelectQuery(String queryString) throws RepositoryException;
    
    boolean runAskQuery(String queryString) throws RepositoryException;
    
    String runConstructQuery(String queryString, String format) throws RepositoryException;
    
    String runDescribeQuery(String queryString, String format) throws RepositoryException;
}
