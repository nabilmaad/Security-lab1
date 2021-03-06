package beans;

import facades.UserAccountFacade;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import models.UserAccount;

@RequestScoped
@ManagedBean
public class SettingsBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private String name;
    private String age;
    private String status;

    @PostConstruct
    public void init() {
        if(!isLoggedIn()) {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            context.invalidateSession();
            try {
                context.redirect(context.getRequestContextPath() + "/login.xhtml");
            } catch (Exception e) {}
        } else {
            UserAccount user = sessionBean.getUser();
            if(user != null) {
                setName(user.getName());
                setAge(user.getAge().toString());
            } else {
                status = "You are not logged in.";
            }
        }
    }
    
    public UserAccountFacade getUserAccountFacadeFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public void updateName() {
        UserAccount user = sessionBean.getUser();
        
        if (user != null) {
            if(name != null && !"".equals(name)) {
                user.setName(name);
                userAccountFacade.setUserName(user.getId(), name);
                status = "Name successfully updated.";
            } else {
                status = "Please enter your name.";
            }
        } else {
            status = "You are not logged in.";
        }
    }

    public void updateAge() {
        UserAccount user = sessionBean.getUser();
        
        if (user != null) {
            if(isInteger(age)) {
                if(userAccountFacade.isValidAge(Integer.parseInt(age))) {
                    user.setAge(Integer.parseInt(age));
                    userAccountFacade.setUserAge(user.getId(), Integer.parseInt(age));
                    status = "Age successfully updated.";
                } else {
                    status = "Age must be an integer superior to zero.";
                }
            }
        } else {
            status = "You are not logged in.";
        }
    }
    
    public boolean isInteger(String age) {
        try {
            Integer.parseInt(age);
        } catch (NumberFormatException e) {
            status = "Age must be an integer superior to zero.";
            return false;
        }
        return true;
    }

}
