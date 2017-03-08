package ro.andreiciortea.stn.platform.eventbus;

import com.google.gson.Gson;

public abstract class BusMessage {
    
    public static final String HEADER_CONTENT_TYPE = "content-type";
    
    
    public BusMessage() {  }
    
    public abstract String getContentType();
    
    public String toJson() {
        return new Gson().toJson(this, this.getClass());
    }
}
