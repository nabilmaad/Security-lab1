package beans;

import exeptions.PasswordAlreadyUsedException;
import facades.UserAccountFacade;
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
    private String confirm;
    private boolean disableChangeButton;
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
    
    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }
    
    public boolean getDisableChangeButton() {
        return disableChangeButton;
    }

    public void setDisableChangeButton(boolean disableChangeButton) {
        this.disableChangeButton = disableChangeButton;
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
                boolean passwordPreviouslyUsed = false;
                boolean passwordChanged = true;
                try {
                    passwordChanged = userAccountFacade.updateUserPassword(user.getId(), newPassword);
                } catch (PasswordAlreadyUsedException e) {
                    passwordPreviouslyUsed = true;
                    status = "You already used this password before.";
                }
                if(!passwordPreviouslyUsed) {
                    if (passwordChanged) {
                    status = "Password successfully changed. Your new password will be active as soon as you logout.";
                    disableChangeButton = true;
                    } else {
                    status = "An error occured while changing your password. Please try again.";
                    }
                }
            } else {
                status = "The old password didn't match.";
            }
        } else {
            status = "You are not logged in.";
        }
    }
}
