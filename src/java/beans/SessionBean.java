package beans;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import models.UserAccount;

@ManagedBean
@SessionScoped
public class SessionBean implements Serializable {
    
    private UserAccount user;

    public SessionBean() {
    }
    
    public UserAccount getUser() {
        return user;
    }
    
    public void setUser(UserAccount user) {
        this.user = user;
    }
    
    public boolean isLoggedIn() {
        return user != null;
    }
}
