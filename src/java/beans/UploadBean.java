package beans;

import facades.UserAccountFacade;
import java.util.Scanner;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import models.UserAccount;

/**
 *
 * @author Franck Mamboue
 */
@ManagedBean
@ViewScoped
public class UploadBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private Part publicKey;
    private UserAccount user;
    private String status;
    
    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    public UserAccount getUser() {
        return sessionBean.getUser();
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }
    
    public String getStatus() {
        return status;
    }

    public Part getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Part publicKey) {
        this.publicKey = publicKey;
    }
    
    public void upload() {        
        if (getUser() != null) {
            try {
              String fileContent = new Scanner(publicKey.getInputStream()).useDelimiter("\\A").next();
              userAccountFacade.setPublicKey(user.getId(), fileContent);
              status = "File sucessfully uploaded.";
            } catch (Exception e) {
              status = "Error Uploading file: " + e.toString();
            }
        } else {
            status = "You are not logged in.";
        }
    }
    
    public void validatePublicKey(FacesContext ctx, UIComponent comp, Object value) {
        if (getUser() != null) {
            Part file = (Part)value;
            if (file != null) {
                status = "";
                if (file.getSize() > 1024) {
                  status += "- File too big.\n";
                }
                if (!"text/plain".equals(file.getContentType())) {
                  status += "- " + file.getContentType() + " content was found in the file. Expected text/plain. Please select a text file.";
                }
            }
        } else {
            status = "You are not logged in.";
        }    
    }

}
