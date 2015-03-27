/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.io.FileOutputStream;
import java.io.IOException;
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
 * This program assumes that public/private key pairs were generated for Alice (sender) and Bob (receiver). 
 * See https://www.owasp.org/index.php/Digital_Signature_Implementation_in_Java
 * 
 */

public class PublicKeyCryptography {
    
        private static final SymmetricEncrypt encryptUtil = new SymmetricEncrypt();
        private static KeyStore KEYSTORE;
        private static UserAccount SENDER;
        private static String RECEIVER_EMAIL;
        
        // Crypto variables
        public static String ALGORITHM = "SHA-512";
        public static String ENCRYPTION_STANDARD = "AES";
                
        // KeyStore variables
        public static String KEYSTORE_LOCATION = System.getProperty("user.home")+"/keystore.ks";
        public static String KEYSTORE_PASSWORD = "testpwd";
        public static String SENDER_ALIAS = "alice";
        public static String SENDER_PASSWORD = "send123";
        public static String RECEIVER_ALIAS = "bob";
        public static String RECEIVER_PASSWORD = "rcv123";
        
        // decryptFile method input
        private static byte[] encryptedFile;
        private static byte[] encryptedSymmetricKey;
        private static byte[] signedHash;
        
        public static boolean sendFile(UserAccount user, String recipient, String fileContent, PrivateKey senderPrivateKey, PublicKey receiverPublicKey) {
            SENDER = user;
            RECEIVER_EMAIL = recipient;
            if (user == null || recipient == null || fileContent == null || senderPrivateKey == null || receiverPublicKey == null)
                return false;
            return sendFile(fileContent, senderPrivateKey, receiverPublicKey);
        }
    
        private static boolean sendFile(String fileContent, PrivateKey senderPrivateKey, PublicKey receiverPublicKey) {
            // Get byte data of the file to send
            byte[] fileBytes = fileContent.getBytes();

            // Generate a SecretKey for Symmetric Encryption
            SecretKey senderSecretKey = SymmetricEncrypt.getSecret();

            // 1. Encrypt the file data using the Symmetric Key
            encryptedFile = encryptUtil.encryptData(fileBytes,senderSecretKey,ENCRYPTION_STANDARD);

            // 2. Encrypt the Symmetric key using the Receivers public key
            try {
                encryptedSymmetricKey = encryptUtil.encryptData(senderSecretKey.getEncoded(),receiverPublicKey,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");

                // 3. Create a hash of the data to be transmitted
                MessageDigest md = MessageDigest.getInstance(ALGORITHM);
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
                String[] filenames = new String[]{"encrypted-file.txt", "encrypted-symmetric-key.txt", "signed-hash.txt"};
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

                if (MailHelper.sendEmail(SENDER, RECEIVER_EMAIL, "CSI439 secure file", "Please find attached a secure file sent by " + SENDER.getUsername(), filenames))
                    return true;
                
            } catch(NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
                Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            }
            return false;
        } 

	/**
         * Prints the public keys for Alice and Bob
	 * @param args
	 */
    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, KeyStoreException, CertificateException, UnrecoverableKeyException {
        // Getting keystore
        KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());
	char [] password = KEYSTORE_PASSWORD.toCharArray();
	java.io.FileInputStream fis = new java.io.FileInputStream(KEYSTORE_LOCATION); // Assuming the keystore file is located in the home directory
        KEYSTORE.load(fis, password);
        fis.close();

        // Creating an X509 Certificate
        X509Certificate certSender, certReceiver;
        certSender = (X509Certificate)KEYSTORE.getCertificate(SENDER_ALIAS);
        certReceiver = (X509Certificate)KEYSTORE.getCertificate(RECEIVER_ALIAS);

        // Getting public Key from the Certificate
        PublicKey pubKeySender = certSender.getPublicKey();
        PublicKey pubKeyReceiver = certReceiver.getPublicKey();
        
        // Print them
        BASE64Encoder encoder = new BASE64Encoder();
        System.out.println("Alice's Public Key (Sender): \n\n"+encoder.encode(pubKeySender.getEncoded())+"\n");
        System.out.println("Bob's Public Key (Receiver): \n\n"+encoder.encode(pubKeyReceiver.getEncoded())+"\n");
    
    }
        
    public static String decryptFile(byte[] encryptedFile, byte[] encryptedSymmetricKey, byte[] signedHash) {
    String status = "";
    try {
        KEYSTORE = KeyStore.getInstance(KeyStore.getDefaultType());

        // Specify the Keystore where the Receivers certificate has been imported
        char [] password = KEYSTORE_PASSWORD.toCharArray();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(KEYSTORE_LOCATION)) {
            KEYSTORE.load(fis, password);
        }
        
        // 6. Decrypt the encrypted symmetric key using Receiver's private Key
        // 6.1 Get the private key of the Reveiver from the keystore by providing the password set for the private key while creating the keys using keytool
        char[] recvpassword = RECEIVER_PASSWORD.toCharArray();
        Key recvKey =  KEYSTORE.getKey(RECEIVER_ALIAS, recvpassword);
        PrivateKey recvPrivateKey = (PrivateKey)recvKey;

        // 6.2 Decrypt to get the symmetric key
        byte[] byteDecryptWithPrivateKey = encryptUtil.decryptData(encryptedSymmetricKey,recvPrivateKey,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        
        // 7. Decrypt the data using the Symmetric Key
        javax.crypto.spec.SecretKeySpec secretKeySpecDecrypted = new javax.crypto.spec.SecretKeySpec(byteDecryptWithPrivateKey,ENCRYPTION_STANDARD);
        byte[] byteDecryptText = encryptUtil.decryptData(encryptedFile,secretKeySpecDecrypted,ENCRYPTION_STANDARD);
        String strDecryptedText = new String(byteDecryptText);
        status +="- Decrypted data is: \n\n" +strDecryptedText+ "\n";

        // 8. Create a hash of the data that was decrypted
        MessageDigest md = MessageDigest.getInstance(ALGORITHM);
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
        sendercert = (X509Certificate)KEYSTORE.getCertificate(SENDER_ALIAS);
        PublicKey pubKeySender = sendercert.getPublicKey();
        
        // 9.2 Verifying the Signature
        Signature myVerifySign = Signature.getInstance("SHA512withRSA");
        myVerifySign.initVerify(pubKeySender);
        myVerifySign.update(expectedSignedHash.getBytes());

        boolean verifySign = myVerifySign.verify(signedHash);
        if (verifySign == false) {
            status += "- Signature is not validated.";
        } else {
            status+= "- Successfully validated Signature :)";
        }
	} catch(KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException | InvalidKeyException | SignatureException e) {
            Logger.getLogger(UserAccount.class.getName()).log(Level.SEVERE, null, e);
            status += "- Error while decrypting file: " + e.getMessage();
        }
        return status;
    }

}