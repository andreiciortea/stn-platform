package ro.andreiciortea.stn.platform.repository;

import io.vertx.core.AbstractVerticle;
import io.vertx.serviceproxy.ProxyHelper;

public class RepositoryServiceVerticle extends AbstractVerticle {

    @Override
    public void start() {
        RepositoryService repository = RepositoryService.create(vertx);
        
        ProxyHelper.registerService(RepositoryService.class, 
                vertx, repository, RepositoryService.EVENT_BUS_ADDRESS);
    }
}
