package beans;

import facades.UserAccountFacade;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import models.UserAccount;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Franck Mamboue
 */
@ManagedBean
@ViewScoped
public class AllowedBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private UserAccount user;
    private String status;
    private List<UserAccount> userList;
    public List<String> allowedUsers;
    
    @PostConstruct
    public void init() {
        if(!isLoggedIn()) {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.invalidateSession();
        try {
            context.redirect(context.getRequestContextPath() + "/login.xhtml");
        } catch (Exception e) {}
        } else {
            allowedUsers = userAccountFacade.getAllowedUsers();
        }
    }
    
    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    public UserAccount getUser() {
        this.user = sessionBean.getUser();
        return sessionBean.getUser();
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }
    
    public String getStatus() {
        return status;
    }
    
    public List<String> getAllowedUsers() {
        return allowedUsers;
    }
    
    public void setAllowedUsers(List<String> allowedUsers) {
        this.allowedUsers = allowedUsers;
    }
    
    public List<UserAccount> getUserList() {
        userList = userAccountFacade.getAllUsers();
        return userAccountFacade.removeUserFromList(userList, getUser().getUsername());
    }
    
    public void submit() {        
        if (getUser() != null) {
            userAccountFacade.setAllowedUsers(getUser().getId(), allowedUsers);
            status = "Sucessfully updated list of allowed users.";
        } else {
            status = "You are not logged in.";
        }
    }

}
