package ro.andreiciortea.stn.platform.artifact;

import java.util.List;

import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

import io.vertx.core.MultiMap;
import ro.andreiciortea.stn.vocabulary.STNCore;

public class UserAccountModel extends DigitalArtifactModel {
    
    public static final String USER_ACCOUNT_CONTAINER_PATH = "/users/";
    
    @Override
    public String getContainerPath() {
        return USER_ACCOUNT_CONTAINER_PATH;
    }
    
    @Override
    public List<Statement> getRequiredStatements(String artifactUri) {
        List<Statement> list = super.getRequiredStatements(artifactUri);
        
        list.add(ResourceFactory.createStatement(
                    ResourceFactory.createResource(artifactUri), 
                    RDF.type, 
                    STNCore.UserAccount
                )
            );
        
        return list;
    }
    
    @Override
    public String getConstructCollectionQuery(MultiMap params) {
//        System.out.println("Returning owned by");
        
        String ownerUri = params.get("ownedBy");
        
//        System.out.println("Owner URI: " + ownerUri);
        
        // TODO
        
        String query = "CONSTRUCT {"
                    + "?accountUri <" + RDF.type + "> <" + STNCore.UserAccount + "> ."
//                    + "?accountUri <" + STNCore.source + "> ?holderUri ."
//                    + "?accountUri <" + STNCore.target + "> <" + ownerUri + "> ."
                    + "}"
                    + "WHERE {"
                    + "?accountUri <" + RDF.type + "> <" + STNCore.UserAccount + "> ."
                    + "?accountUri <" + STNCore.heldBy + "> ?holderUri ."
                    + "?holderUri <" + STNCore.ownedBy + "> <" + ownerUri + "> ."
                    + "}";
        
//        System.out.println(query);
        
        return query;
    }
}
