package requests;

import java.io.Serializable;

public class OrderResult implements Serializable {

    private String title;

    private boolean confirmed = false;

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
