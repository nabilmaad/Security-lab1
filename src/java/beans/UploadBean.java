package beans;

import facades.UserAccountFacade;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import models.UserAccount;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

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
        this.user = sessionBean.getUser();
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
              BASE64Decoder decoder = new BASE64Decoder();
              KeyFactory kf = KeyFactory.getInstance("RSA");
              X509EncodedKeySpec pub = new X509EncodedKeySpec(decoder.decodeBuffer(fileContent));
              PublicKey pubKeyReceiver = kf.generatePublic(pub);
              BASE64Encoder encoder = new BASE64Encoder();
              userAccountFacade.setPublicKey(getUser().getId(), encoder.encode(pubKeyReceiver.getEncoded()));
              status = "File sucessfully uploaded.";
            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
              status = "Error Uploading file: " + e.toString();
              if (e.getClass().equals(InvalidKeySpecException.class)) {
                  status = "Error Uploading file: The key you are trying to upload is invalid.";
              }
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
