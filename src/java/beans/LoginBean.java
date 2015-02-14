package beans;

import facades.LoginFacade;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import models.UserAccount;

@RequestScoped
@ManagedBean
public class LoginBean extends BaseBean {
    
    @ManagedProperty(value="#{loginFacade}")
    LoginFacade loginFacade;

    private String username;
    private String password;
    private String status;
    
    /**
     * Creates a new instance of LogInBean
     */
    public LoginBean() {
    }

    public LoginFacade getLoginFacade() {
        return loginFacade;
    }

    public void setLoginFacade(LoginFacade loginFacade) {
        this.loginFacade = loginFacade;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void login() {
        UserAccount user = loginFacade.login(username, password);
        
        if (user != null) {
            if(user.getFailedLoginAttempts() >= 10)
                status = "This account has been locked out because the limit for failed login "
                        + "attempts has been reached.\n"
                        + "For assistance, please call (xxx)xxx-xxxx or send an email to "
                        + "some@email.com";
            else {
                try {
                    ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                    context.redirect(context.getRequestContextPath() + "/login_successful.xhtml");
                } catch (Exception e) {}
            }
        } else {
            status = "Login failed. Invalid username or password.";
        }
    }
    
    public void logout() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.invalidateSession();
        try {
        context.redirect(context.getRequestContextPath() + "/logout.xhtml");
        } catch (Exception e) {}
    }
}
