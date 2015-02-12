/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import facades.UserAccountFacade;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import models.UserAccount;

/**
 *
 * @author Can Jee
 */
@ManagedBean
@RequestScoped
public class CreateAccountBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;
    
    private String username;
    private String password;
    private String status;

    /**
     * Creates a new instance of CreateAccountBean
     */
    public CreateAccountBean() {
    }

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void createAccount() {
        UserAccount checkDuplicate = userAccountFacade.findByUsername(username, em);
        if (checkDuplicate != null){
            status = "Username already exists in the database, please enter another username.";
        } else {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            if( userAccountFacade.createAccount(username, password) ) {

                try {
                    context.redirect(context.getRequestContextPath() +
                        "/account_creation_successful.xhtml");
                } catch (Exception e) {}
            }
        }
    }
    
}
