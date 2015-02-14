package beans;

import exeptions.PasswordAlreadyUsedException;
import facades.UserAccountFacade;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import mail.MailHelper;
import models.UserAccount;
import password.RandomPasswordGenerator;

@RequestScoped
@ManagedBean
public class ResetPasswordBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private String email;
    private boolean disableResetButton;
    private String status;
    
    /**
     * Creates a new instance of ChangePasswordBean
     */
    public ResetPasswordBean() {
        disableResetButton = false;
    }

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }
    
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean getDisableResetButton() {
        return disableResetButton;
    }

    public void setDisableResetButton(boolean disableResetButton) {
        this.disableResetButton = disableResetButton;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.disableResetButton = false;
    }

    public void resetPassword() {
            UserAccount user = userAccountFacade.findByUsername(email, em);
            if (user != null) {
                // Generate a new password
                RandomPasswordGenerator randomPasswordGenerator  = new RandomPasswordGenerator();
                String newPassword = randomPasswordGenerator.generateRandomPassword();
                // Change the password of the current user
                boolean passwordChanged = true;
                try {
                    passwordChanged = userAccountFacade.updateUserPassword(user.getId(), newPassword);
                } catch (PasswordAlreadyUsedException e) {
                    setStatus("An error occured. Please try again.");
                }
                if(passwordChanged) {
                    // Email the user with the new password
                    try {
                        MailHelper.sendEmail(user, newPassword);
                        setStatus("Your new password has been emailed to " + user.getUsername() + ".");
                    } catch (RuntimeException e) {
                        setStatus("An error occured while sending your new password to " + user.getUsername() + ".");
                        Logger.getLogger(MailHelper.class.getName()).log(Level.SEVERE, e.toString());
                    }
                } else {
                    setStatus("An error occured. Please try again.");
                }
            } else {
                setStatus("There is no account associated with this email address.");
            }
    }
}
