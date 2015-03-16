/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package beans;

import crypto.PublicKeyCryptography;
import static crypto.PublicKeyCryptography.decryptFile;
import facades.UserAccountFacade;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import models.UserAccount;
import sun.misc.BASE64Decoder;

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
    private String receiverEmail;
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

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }
    
    public UserAccount getUser() {
        this.user = sessionBean.getUser();
        return sessionBean.getUser();
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
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
            try {
                if (file != null) {
                    String fileContent = new Scanner(file.getInputStream()).useDelimiter("\\A").next();
                    if (receiverEmail != null && !"".equals(receiverEmail)) {
                        if (receiverPublicKey != null && !"".equals(receiverPublicKey)) {
                            // Open the keystore
                            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                            char [] password = "testpwd".toCharArray();
                            try (java.io.FileInputStream fis = new java.io.FileInputStream(System.getProperty("user.dir")+"/testkeystore.ks")) {
                                ks.load(fis, password);
                            }
                            // Get local user's private key
                            char[] keypassword = "send123".toCharArray();
                            Key myKey =  ks.getKey("testsender", keypassword);
                            PrivateKey myPrivateKey = (PrivateKey)myKey;

                            // Parse the public key entered by the user in the interface
                            BASE64Decoder decoder = new BASE64Decoder();
                            KeyFactory kf = KeyFactory.getInstance("RSA");
                            X509EncodedKeySpec pub = new X509EncodedKeySpec(decoder.decodeBuffer(receiverPublicKey));
                            PublicKey pubKeyReceiver = kf.generatePublic(pub);

                            // Try to send the file to the email entered
                            boolean success = PublicKeyCryptography.sendFile(getUser(), receiverEmail, fileContent, myPrivateKey, pubKeyReceiver);
                            if (success) {
                                status = "File sent successfully.";
                            }
                        } else {
                            status = "No public key was entered.";
                        }
                    } else {
                        status = "No email was entered.";
                    }
                } else {
                    status = "No file was provided.";
                }
            } catch (IOException | InvalidKeySpecException | NoSuchAlgorithmException | KeyStoreException | CertificateException | UnrecoverableEntryException e) {
              status = "Error Uploading file: " + e.toString();
            }
        } else {
            status = "You are not logged in.";
        }
    }

}
