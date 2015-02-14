package facades;

import exeptions.PasswordAlreadyUsedException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import models.UserAccount;

@ManagedBean
@SessionScoped
public class UserAccountFacade extends BaseFacade {
    
    public boolean createAccount(String username, String password) {
        try {
            utx.begin();
            
            UserAccount account = new UserAccount();
            
            account.setUsername(username);
            setPassword(account, password);
            
            
            em.persist(account);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public boolean updateUserPassword(Long userId, String newPassword) throws PasswordAlreadyUsedException {
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            // encrypt/salt newPassword
            try {
                byte[] salt = user.getSalt();
                String saltString = new String(salt, "UTF-8");
                String checkPass = saltString + newPassword;
                MessageDigest digest = MessageDigest.getInstance("SHA-512");
                byte[] checkPassHash = digest.digest(checkPass.getBytes("UTF-8"));
                // check if it matches one of the old passwords
                for (HashMap.Entry<Date, byte[]> entry : user.getOldPasswords().entrySet()) {
                    Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, new String(entry.getValue(), "UTF-8"));
                    Logger.getLogger(UserAccount.class.getName()).log(Level.INFO, new String(checkPassHash, "UTF-8"));
                    Logger.getLogger(UserAccount.class.getName()).log(Level.INFO, "new space");
                    Logger.getLogger(UserAccount.class.getName()).log(Level.INFO, "new space");

                    if (Arrays.equals(checkPassHash, entry.getValue())) {
                        Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, "ALREADY USED");
                        throw new PasswordAlreadyUsedException("You already used this password."); 
                    }
                }
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
                Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, ex);
            }
            // replace old password with new password
            setPassword(user, newPassword);
            em.persist(user);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public boolean setPassword(UserAccount userAccount, String password) {
        try {
            // randomly generate salt value
            final Random r = new SecureRandom();
            byte[] salt = new byte[64];
            r.nextBytes(salt);
            String saltString = new String(salt, "UTF-8");
            // hash password using SHA-256 algorithm
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            String saltedPass = saltString+password;
            byte[] passhash = digest.digest(saltedPass.getBytes("UTF-8"));
            userAccount.setSalt(salt);
            userAccount.setPassword(passhash);
            // put current password in oldpassword Hash
            userAccount.getOldPasswords().put(new Date(), passhash);
            return true;
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException | RuntimeException ex) {
            return false;
        }
    }

    public boolean checkPassword(UserAccount userAccount, String password) {
        try {
            byte[] salt = userAccount.getSalt();
            String saltString = new String(salt, "UTF-8");
            String checkPass = saltString + password;
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] checkPassHash = digest.digest(checkPass.getBytes("UTF-8"));
            if (Arrays.equals(checkPassHash, userAccount.getPassword())) {
                // Reset failed password attempts to zero if not locked out
                if(userAccount.getFailedLoginAttempts() < 10) {
                    userAccount.setFailedLoginAttempts(0);
                    resetFailedAttempts(userAccount.getId());
                    return true;
                }
            } else {
                // Increment failed login attempts
                userAccount.setFailedLoginAttempts(incrementFailedAttempts(userAccount.getId()));
            }
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public boolean resetFailedAttempts(Long userId)
    {
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            
            user.setFailedLoginAttempts(0);
            em.persist(user);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public int incrementFailedAttempts(Long userId){
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            em.persist(user);
            utx.commit();
            return user.getFailedLoginAttempts();
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return 0;
        }
    }
    
    public UserAccount findByUsername(String username, EntityManager em) {
        Query query = em.createQuery("SELECT ua FROM UserAccount ua WHERE ua.username = :username");
        query.setParameter("username", username);
        UserAccount result = performQuery(UserAccount.class, query);
        return result;
    }      
    
}
