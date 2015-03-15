/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import facades.UserAccountFacade;
import java.util.Scanner;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import models.UserAccount;

/**
 *
 * @author Franck Mamboue
 */
@ManagedBean
@RequestScoped
public class SendFileBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private Part file;
    private UserAccount user;
    private String senderPrivateKey;
    private String receiverPublicKey;
    private String status;

    /**
     * Creates a new instance of CreateAccountBean
     */
    public SendFileBean() {
    }

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    public String getSenderPrivateKey() {
        return senderPrivateKey;
    }

    public void setSenderPrivateKey(String senderPrivateKey) {
        this.senderPrivateKey = senderPrivateKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
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

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }
    
    public void validateFile(FacesContext ctx, UIComponent comp, Object value) {
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
    
    public void send() {        
        if (getUser() != null) {
            String fileContent = "";
            try {
              fileContent = new Scanner(file.getInputStream()).useDelimiter("\\A").next();
              status = fileContent;
            } catch (Exception e) {
              status = "Error Uploading file: " + e.toString();
            }
            status = fileContent + " was successfully sent from " + senderPrivateKey + " to " + receiverPublicKey;
        } else {
            status = "You are not logged in.";
        }
    }

}
