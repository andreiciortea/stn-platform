package ro.andreiciortea.stn.platform.repository;

import ro.andreiciortea.stn.platform.eventbus.RepositoryResponse;

public interface Repository {
    
    RepositoryResponse containsArtifact(String uri);
    
    RepositoryResponse getArtifact(String uri, String format);
    
    RepositoryResponse putArtifact(String artifactUri, String rdfData, String format);
    
    RepositoryResponse deleteArtifact(String uri);
    
    RepositoryResponse runSelectQuery(String queryString);
    
    RepositoryResponse runConstructQuery(String queryString, String format);
}
