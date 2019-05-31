package requests;

import java.io.Serializable;

public class StreamResult implements Serializable {

    private String line;

    public StreamResult(String line) {
        this.line = line;
    }

    public String getLine() {
        return line;
    }
}
