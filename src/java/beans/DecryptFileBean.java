/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import crypto.PublicKeyCryptography;
import facades.UserAccountFacade;
import java.io.IOException;
import java.util.Scanner;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import models.UserAccount;
import org.apache.commons.io.IOUtils;
/**
 *
 * @author Franck Mamboue
 */
@ManagedBean
@RequestScoped
public class DecryptFileBean extends BaseBean {
    
    @ManagedProperty(value="#{userAccountFacade}")
    UserAccountFacade userAccountFacade;

    private UserAccount user;
    private Part encryptedFile;
    private Part encryptedSymmetricKey;
    private Part signedHash;
    private String status;

    /**
     * Creates a new instance of CreateAccountBean
     */
    public DecryptFileBean() {
    }

    public UserAccountFacade getUserAccountFacade() {
        return userAccountFacade;
    }

    public void setUserAccountFacade(UserAccountFacade userAccountFacade) {
        this.userAccountFacade = userAccountFacade;
    }

    public Part getSignedHash() {
        return signedHash;
    }

    public void setSignedHash(Part signedHash) {
        this.signedHash = signedHash;
    }
    
    public UserAccount getUser() {
        this.user = sessionBean.getUser();
        return sessionBean.getUser();
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public Part getEncryptedSymmetricKey() {
        return encryptedSymmetricKey;
    }

    public void setEncryptedSymmetricKey(Part encryptedSymmetricKey) {
        this.encryptedSymmetricKey = encryptedSymmetricKey;
    }
    
    public String getStatus() {
        return status;
    }

    public Part getEncryptedFile() {
        return encryptedFile;
    }

    public void setEncryptedFile(Part encryptedFile) {
        this.encryptedFile = encryptedFile;
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
                  status += "- " + file.getContentType() + " content was found in "+ file.getName() +". Expected text/plain. Please select a text file.";
                }
            }
        } else {
            status = "You are not logged in.";
        }    
    }
    
    public void decrypt() {        
        if (getUser() != null) {
            try {
                if (encryptedFile != null) {
                    byte[] encryptedFileContent =  IOUtils.toByteArray(encryptedFile.getInputStream());
                    if (encryptedSymmetricKey != null) {
                        byte[] encryptedSymmetricKeyContent = IOUtils.toByteArray(encryptedSymmetricKey.getInputStream());
                        if (signedHash != null) {
                        byte[] signedHashContent = IOUtils.toByteArray(signedHash.getInputStream());
                            // Try to send the file to the email entered
                            status = PublicKeyCryptography.decryptFile(encryptedFileContent, encryptedSymmetricKeyContent, signedHashContent);
                        } else {
                            status = "No signed hash was provided.";
                        }
                    } else {
                        status = "No encrypted symmetric key was provided.";
                    }
                } else {
                    status = "No encrypted file was provided.";
                }
            } catch (IOException e) {
              status = "Error Uploading file: " + e.toString();
            }
        } else {
            status = "You are not logged in.";
        }
    }

}
