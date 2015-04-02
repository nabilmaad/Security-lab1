package beans;

import facades.UserAccountFacade;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import models.UserAccount;

@ManagedBean
@ViewScoped
public class BrowseKeysBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private UserAccount user;
    private List<UserAccount> userList;
    private String status;
    
    @PostConstruct
    public void init() {
        if(!isLoggedIn()) {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.invalidateSession();
        try {
            context.redirect(context.getRequestContextPath() + "/login.xhtml");
        } catch (Exception e) {}
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
    
    public List<UserAccount> getUserList() {
        List<UserAccount> all = userAccountFacade.getAllUsers();
        userList = new ArrayList<>();
        for (UserAccount u : all) {
            List<String> allowedList = u.getAllowedUsers();
            if (allowedList.contains(getUser().getUsername())) {
                userList.add(u);
            }
        }
        if (userList.isEmpty()) {
            UserAccount user = new UserAccount();
            user.setUsername("No user allowed you to see their public key.");
            user.setPublicKey("N/A");
            userList = new ArrayList<UserAccount>();
            userList.add(user);
        }
        for (UserAccount u : userList) {
            if (u.getPublicKey() == null) {
                u.setPublicKey("No public key");
            }
        }
        return userList;
    }
    
    public String getStatus() {
        return status;
    }

}
