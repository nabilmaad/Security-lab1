package beans;

import facades.UserAccountFacade;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import models.UserAccount;

@ManagedBean
@ViewScoped
public class BrowseKeysBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private List<UserAccount> userList;
    private String status;
    
    public BrowseKeysBean () {

    }
    
    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    public List<UserAccount> getUserList() {
        userList = userAccountFacade.getAllUsers();
        if (userList == null) {
            UserAccount user = new UserAccount();
            user.setUsername("No users");
            user.setPublicKey("No public keys");
            userList = new ArrayList<UserAccount>();
            userList.add(user);
        }
        for (UserAccount user : userList) {
            if (user.getPublicKey() == null) {
                user.setPublicKey("No public key");
            }
        }
        return userList;
    }
    
    public String getStatus() {
        return status;
    }

}
