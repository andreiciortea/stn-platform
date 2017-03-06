package ro.andreiciortea.stn.platform.eventbus;

import com.google.gson.Gson;

public abstract class StnMessage {
    
    public static final String HEADER_CONTENT_TYPE = "content-type";
    
    
    public StnMessage() {  }
    
    public abstract String getContentType();
    
    public String toJson() {
        return new Gson().toJson(this, this.getClass());
    }
}