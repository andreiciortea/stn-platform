package ro.andreiciortea.stn.platform.eventbus;

import com.google.gson.Gson;

public class BusMessage {
    
    public String toJson() {
        return new Gson().toJson(this, this.getClass());
    }
}
