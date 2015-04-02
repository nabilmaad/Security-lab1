package facades;

import exeptions.PasswordAlreadyUsedException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
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
            try {
                // check if new password matches one of the old passwords
                for (HashMap.Entry<byte[], byte[]> entry : user.getOldPasswords().entrySet()) {
                    // encrypt clear text newPassword with salt of the current oldPassword hash
                    byte[] salt = entry.getKey();
                    String saltString = new String(salt, "UTF-8");
                    String checkPass = saltString + newPassword;
                    MessageDigest digest = MessageDigest.getInstance("SHA-512");
                    byte[] checkPassHash = digest.digest(checkPass.getBytes("UTF-8"));

                    if (Arrays.equals(checkPassHash, entry.getValue())) {
                        throw new PasswordAlreadyUsedException("You already used this password.");
                    }
                }
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
                Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            // replace old password with new password
            setPassword(user, newPassword);
            em.persist(user);
            utx.commit();
            return true;
        } catch (NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException e) {
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
            userAccount.getOldPasswords().put(salt, passhash);
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
    
    public boolean setPublicKey(Long userId, String publicKey){
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            
            user.setPublicKey(publicKey);
            em.persist(user);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public boolean setUserName(Long userId, String name){
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            
            user.setName(name);
            em.persist(user);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public boolean setUserAge(Long userId, Integer age){
        try {
            utx.begin();
            String queryString = "SELECT ua FROM UserAccount ua WHERE ua.id = :id";
            Query query = em.createQuery(queryString);
            query.setParameter("id", userId);
            UserAccount user = performQuery(UserAccount.class, query);
            
            if (isValidAge(age)) {
                user.setAge(age);
            } else {
                throw new IllegalArgumentException("Age must be a positive digit.");
            }
            em.persist(user);
            utx.commit();
            return true;
        } catch (Exception e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    public List<UserAccount> getAllUsers() {
        Query query = em.createQuery("SELECT ua FROM UserAccount ua");
        List<UserAccount> result = performQueryList(UserAccount.class, query);
        return result;
    }      
    
    public boolean isValidAge(Integer age) {
        return age>0;
    }

}
