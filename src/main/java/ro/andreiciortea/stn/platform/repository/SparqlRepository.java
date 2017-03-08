package ro.andreiciortea.stn.platform.repository;

import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;

public interface SparqlRepository {

    // TODO: refactor
    
    RepositoryResponse runSelectQuery(String queryString);
    
    RepositoryResponse runAskQuery(String queryString);
    
    RepositoryResponse runConstructQuery(String queryString, String format);
    
    RepositoryResponse runDescribeQuery(String queryString, String format);
}
