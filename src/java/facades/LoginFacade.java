package facades;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import models.UserAccount;

@ManagedBean
@SessionScoped
public class LoginFacade extends BaseFacade {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }
    
    public UserAccount login(String username, String password) {
        UserAccount account = userAccountFacade.findByUsername(username, em);
        
        if (account != null) {
            if(userAccountFacade.checkPassword(account, password)) {
                sessionBean.setUser(account);
                return account;
            } else if(account.getFailedLoginAttempts() >= 10) {
                // Lock out user account if wrong password is entered 10 times
                return account;
            }
        }
        return null;
    }
    
}
