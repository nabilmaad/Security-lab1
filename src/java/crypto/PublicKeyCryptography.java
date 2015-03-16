/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.*;
import mail.MailHelper;
import models.UserAccount;
import sun.misc.BASE64Encoder;

/**
 * 
 * @author Joe Prasanna Kumar (edited by Franck Mamboue)
 * 
 * 1. Encrypt the data using a Symmetric Key
 * 2. Encrypt the Symmetric key using the Receivers public key
 * 3. Create a hash of the data to be transmitted
 * 4. Sign the message to be transmitted
 * 5. Send the data to the Receiver via email
 * 6. Decrypt the Symmetric Key using Receiver's private Key
 * 7. Decrypt the data using the Symmetric Key
 * 8. Create a hash of the data decrypted
 * 9. Verify the signature using the Senders public Key
 * 
 */

public class PublicKeyCryptography {
    
        private static final SymmetricEncrypt encryptUtil = new SymmetricEncrypt();
        private static KeyStore ks;
        private static UserAccount sender;
        private static String receiver;
        
        // decryptFile method input
        private static byte[] encryptedFile;
        private static byte[] encryptedSymmetricKey;
        private static byte[] signedHash;
        
        public static boolean sendFile(UserAccount user, String recipient, String fileContent, PrivateKey senderPrivateKey, PublicKey receiverPublicKey) {
            sender = user;
            receiver = recipient;
            if (user == null || recipient == null|| fileContent == null || senderPrivateKey == null || receiverPublicKey == null)
                return false;
            return sendFile(fileContent, senderPrivateKey, receiverPublicKey);
        }
    
        private static boolean sendFile(String fileContent, PrivateKey senderPrivateKey, PublicKey receiverPublicKey) {
            // Get byte data of the file to send
            byte[] fileBytes = fileContent.getBytes();

            // Generate a SecretKey for Symmetric Encryption
            SecretKey senderSecretKey = SymmetricEncrypt.getSecret();

            // 1. Encrypt the file data using the Symmetric Key
            encryptedFile = encryptUtil.encryptData(fileBytes,senderSecretKey,"AES");

            // 2. Encrypt the Symmetric key using the Receivers public key
            try {
                encryptedSymmetricKey = encryptUtil.encryptData(senderSecretKey.getEncoded(),receiverPublicKey,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");

                // 3. Create a hash of the data to be transmitted
                MessageDigest md = MessageDigest.getInstance("SHA-512");
                md.update(fileBytes);
                byte byteMDofDataToTransmit[] = md.digest();

                String strMDofDataToTransmit = new String();
                for (int i = 0; i < byteMDofDataToTransmit.length; i++){
                    strMDofDataToTransmit = strMDofDataToTransmit + Integer.toHexString((int)byteMDofDataToTransmit[i] & 0xFF) ;
                }

                // 3.1 Message to be Signed = Encrypted Secret Key + MAC of the data to be transmitted
                String strMsgToSign = Arrays.toString(encryptedSymmetricKey) + "|" + strMDofDataToTransmit;

                // 4. Sign the message
                Signature mySign = Signature.getInstance("SHA512withRSA");
                mySign.initSign(senderPrivateKey);
                mySign.update(strMsgToSign.getBytes());
                signedHash = mySign.sign();
                
                // 5. Send the the encrypted file, the encrypted symmetric key and the signed hash to the receiver
                FileOutputStream encryptedFileOS, encryptedSymmetricKeyOS, signedHashOS;
                String[] filenames = new String[]{"encrypted-file", "encrypted-symmetric-key", "signed-hash"};
                try {
                    encryptedFileOS = new FileOutputStream(filenames[0]);
                    encryptedFileOS.write(encryptedFile);
                    encryptedFileOS.close();
                    encryptedSymmetricKeyOS = new FileOutputStream(filenames[1]);
                    encryptedSymmetricKeyOS.write(encryptedSymmetricKey);
                    encryptedSymmetricKeyOS.close();
                    signedHashOS = new FileOutputStream(filenames[2]);
                    signedHashOS.write(signedHash);
                    signedHashOS.close();
                } catch (Exception ex) {
                    Logger.getLogger(PublicKeyCryptography.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
                
                // @todo: remove
                BASE64Encoder encoder = new BASE64Encoder();
                System.out.println("Encrypted file: "+encoder.encode(encryptedFile));
                System.out.println("Encrypted key: "+encoder.encode(encryptedSymmetricKey));
                System.out.println("Signed hash: "+encoder.encode(signedHash));

                if (MailHelper.sendEmail(sender, receiver, "CSI439 secure file", "Please find attached a secure file sent by " + sender.getUsername(), filenames))
                    return true;
                
            } catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            }
            return false;
        } 

	/**
         * Use the main function to call the decryptFile or to print the public/private key pairs generated inside of the keystore.
	 * @param args
	 */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException {
        // Assuming that the receiver downloaded the email attachment in the local home directory
        Path path1 = Paths.get(System.getProperty("user.home")+"/encrypted-file");
        encryptedFile = Files.readAllBytes(path1);
        Path path2 = Paths.get(System.getProperty("user.home")+"/encrypted-symmetric-key");
        encryptedSymmetricKey = Files.readAllBytes(path2);
        Path path3 = Paths.get(System.getProperty("user.home")+"/signed-hash");
        signedHash = Files.readAllBytes(path3);        
        decryptFile(encryptedFile, encryptedSymmetricKey, signedHash);
        
      // This code helps printing the sender's private key and the receiver's public key
        
    /*
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	char [] password = "testpwd".toCharArray();
	java.io.FileInputStream fis = new java.io.FileInputStream(System.getProperty("user.home")+"/testkeystore.ks");
        ks.load(fis, password);
        fis.close();

        // Creating an X509 Certificate of the Receiver
        X509Certificate recvcert ;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        recvcert = (X509Certificate)ks.getCertificate("testrecv");
        
        // Getting the Receivers public Key from the Certificate
        PublicKey pubKeyReceiver = recvcert.getPublicKey();
        
        // Getting the local sender's private key from the Certificate
        char[] keypassword = "send123".toCharArray();
        Key myKey =  ks.getKey("testsender", keypassword);
        PrivateKey myPrivateKey = (PrivateKey)myKey;

        // Print them
        BASE64Encoder encoder = new BASE64Encoder();
        System.out.println("Reveicer private key: "+encoder.encode(pubKeyReceiver.getEncoded()));
        System.out.println("Sender private key: "+encoder.encode(myPrivateKey.getEncoded()));
    */   
    }
        
    public static boolean decryptFile(byte[] encryptedFile, byte[] encryptedSymmetricKey, byte[] signedHash) throws FileNotFoundException, IOException, CertificateException {
    try {
        ks = KeyStore.getInstance(KeyStore.getDefaultType());

        // Specify the Keystore where the Receivers certificate has been imported
        char [] password = "testpwd".toCharArray();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(System.getProperty("user.home")+"/testkeystore.ks")) {
            ks.load(fis, password);
        }
        
        // 6. Decrypt the encrypted symmetric key using Receiver's private Key
        // 6.1 Get the private key of the Reveiver from the keystore by providing the password set for the private key while creating the keys using keytool
        char[] recvpassword = "recv123".toCharArray();
        Key recvKey =  ks.getKey("testrecv", recvpassword);
        PrivateKey recvPrivateKey = (PrivateKey)recvKey;

        // 6.2 Decrypt to get the symmetric key
        byte[] byteDecryptWithPrivateKey = encryptUtil.decryptData(encryptedSymmetricKey,recvPrivateKey,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        
        // 7. Decrypt the data using the Symmetric Key
        javax.crypto.spec.SecretKeySpec secretKeySpecDecrypted = new javax.crypto.spec.SecretKeySpec(byteDecryptWithPrivateKey,"AES");
        byte[] byteDecryptText = encryptUtil.decryptData(encryptedFile,secretKeySpecDecrypted,"AES");
        String strDecryptedText = new String(byteDecryptText);
        System.out.println(" Decrypted data is: " +strDecryptedText);

        // 8. Create a hash of the data that was decrypted
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(byteDecryptText);
        byte byteMDofDecryptedText[] = md.digest();

        String strMDofDecryptedText = new String();
        for (int i = 0; i < byteMDofDecryptedText.length; i++){
            strMDofDecryptedText = strMDofDecryptedText + Integer.toHexString((int)byteMDofDecryptedText[i] & 0xFF) ;
        }
        String expectedSignedHash = Arrays.toString(encryptedSymmetricKey) + "|" + strMDofDecryptedText;
        
        // 9 Verify the Signature using the Senders public Key
        // 9.1 Extract the Senders public Key from his certificate
        X509Certificate sendercert;
        sendercert = (X509Certificate)ks.getCertificate("testsender");
        PublicKey pubKeySender = sendercert.getPublicKey();
        
        // 9.2 Verifying the Signature
        Signature myVerifySign = Signature.getInstance("SHA512withRSA");
        myVerifySign.initVerify(pubKeySender);
        myVerifySign.update(expectedSignedHash.getBytes());

        boolean verifySign = myVerifySign.verify(signedHash);
        if (verifySign == false) {
            System.out.println(" Error in validating Signature ");
        } else {
            System.out.println(" Successfully validated Signature ");
        }
	return true;
	} catch(KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | InvalidKeyException | SignatureException e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

}