package beans;

import exeptions.PasswordAlreadyUsedException;
import facades.UserAccountFacade;
import java.util.Date;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import models.UserAccount;

@RequestScoped
@ManagedBean
public class ChangePasswordBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private String oldPassword;
    private String newPassword;
    private String status;
    
    /**
     * Creates a new instance of ChangePasswordBean
     */
    public ChangePasswordBean() {
    }

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }
    
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void changePassword() {
        UserAccount user = sessionBean.getUser();
        
        if (user != null) {
            if(userAccountFacade.checkPassword(user, oldPassword)) {
                boolean used = false;
                try {
                    userAccountFacade.updateUserPassword(user.getId(), newPassword);
                } catch (PasswordAlreadyUsedException e) {
                    used = true;
                    status = e.toString();
                    return;
                }
                if(!used) {
                    status = "Password successfully changed. Your new password will be active as soon as you logout.";
                } else {
                    status = "An error occured while changing your password.";
                }
            } else {
                status = "The old password didn't match.";
            }
        } else {
            status = "You are not logged in.";
        }
    }
}
