/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package crypto;

import java.security.*;
import java.security.cert.*;
import javax.crypto.*;
import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;


/**
 * 
 * @author Joe Prasanna Kumar
 * 
 * 1. Encrypt the data using a Symmetric Key
 * 2. Encrypt the Symmetric key using the Receivers public key
 * 3. Create a Message Digest of the data to be transmitted
 * 4. Sign the message to be transmitted
 * 5. Send the data over to an unsecured channel
 * 6. Validate the Signature
 * 7. Decrypt the message using Recv private Key to get the Symmetric Key
 * 8. Decrypt the data using the Symmetric Key
 * 9. Compute MessageDigest of data + Signed message
 * 10.Validate if the Message Digest of the Decrypted Text matches the Message Digest of the Original Message
 * 
 * 
 */

public class PublicKeyCryptography {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	SymmetricEncrypt encryptUtil = new SymmetricEncrypt();
	String strDataToEncrypt = "Hello World";
	byte[] byteDataToTransmit = strDataToEncrypt.getBytes();

	// Generating a SecretKey for Symmetric Encryption
	SecretKey senderSecretKey = SymmetricEncrypt.getSecret();
	
	//1. Encrypt the data using a Symmetric Key
	byte[] byteCipherText = encryptUtil.encryptData(byteDataToTransmit,senderSecretKey,"AES");
	String strCipherText = new BASE64Encoder().encode(byteCipherText);
	
	
	//2. Encrypt the Symmetric key using the Receivers public key
	try{
		// 2.1 Specify the Keystore where the Receivers certificate has been imported
	KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
	char [] password = "testpwd".toCharArray();
	java.io.FileInputStream fis = new java.io.FileInputStream("/home/Joebi/workspace/OWASP_Crypto/org/owasp/crypto/testkeystore.ks");
    ks.load(fis, password);
    fis.close();
    
	// 2.2 Creating an X509 Certificate of the Receiver
    X509Certificate recvcert ;
    MessageDigest md = MessageDigest.getInstance("SHA512");
    recvcert = (X509Certificate)ks.getCertificate("testrecv");
    // 2.3 Getting the Receivers public Key from the Certificate
    PublicKey pubKeyReceiver = recvcert.getPublicKey();
    
    // 2.4 Encrypting the SecretKey with the Receivers public Key
    byte[] byteEncryptWithPublicKey = encryptUtil.encryptData(senderSecretKey.getEncoded(),pubKeyReceiver,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");
    String strSenbyteEncryptWithPublicKey = new BASE64Encoder().encode(byteEncryptWithPublicKey);
        
    // 3. Create a Message Digest of the Data to be transmitted
    md.update(byteDataToTransmit);
	byte byteMDofDataToTransmit[] = md.digest();
	
	String strMDofDataToTransmit = new String();
	for (int i = 0; i < byteMDofDataToTransmit.length; i++){
		strMDofDataToTransmit = strMDofDataToTransmit + Integer.toHexString((int)byteMDofDataToTransmit[i] & 0xFF) ;
             }
	
    // 3.1 Message to be Signed = Encrypted Secret Key + MAC of the data to be transmitted
	String strMsgToSign = strSenbyteEncryptWithPublicKey + "|" + strMDofDataToTransmit;
    
    // 4. Sign the message
    // 4.1 Get the private key of the Sender from the keystore by providing the password set for the private key while creating the keys using keytool
	char[] keypassword = "send123".toCharArray();
    Key myKey =  ks.getKey("testsender", keypassword);
    PrivateKey myPrivateKey = (PrivateKey)myKey;
    
    // 4.2 Sign the message
    Signature mySign = Signature.getInstance("SHA512withRSA");
    mySign.initSign(myPrivateKey);
    mySign.update(strMsgToSign.getBytes());
    byte[] byteSignedData = mySign.sign();
        
	// 5. The Values byteSignedData (the signature) and strMsgToSign (the data which was signed) can be sent across to the receiver
	
	// 6.Validate the Signature
    // 6.1 Extracting the Senders public Key from his certificate
	X509Certificate sendercert ;
	sendercert = (X509Certificate)ks.getCertificate("testsender");
    PublicKey pubKeySender = sendercert.getPublicKey();
    // 6.2 Verifying the Signature
    Signature myVerifySign = Signature.getInstance("SHA512withRSA");
    myVerifySign.initVerify(pubKeySender);
    myVerifySign.update(strMsgToSign.getBytes());
    
    boolean verifySign = myVerifySign.verify(byteSignedData);
    if (verifySign == false)
    {
    	System.out.println(" Error in validating Signature ");
    }
    
    else
    	System.out.println(" Successfully validated Signature ");

    // 7. Decrypt the message using Recv private Key to get the Symmetric Key
    char[] recvpassword = "recv123".toCharArray();
    Key recvKey =  ks.getKey("testrecv", recvpassword);
    PrivateKey recvPrivateKey = (PrivateKey)recvKey;
    
    // Parsing the MessageDigest and the encrypted value
    String strRecvSignedData = new String (byteSignedData);
    String[] strRecvSignedDataArray = new String [10];
    strRecvSignedDataArray = strMsgToSign.split("|");
    int intindexofsep = strMsgToSign.indexOf("|");
    String strEncryptWithPublicKey = strMsgToSign.substring(0,intindexofsep);
    String strHashOfData = strMsgToSign.substring(intindexofsep+1);

    // Decrypting to get the symmetric key
    byte[] bytestrEncryptWithPublicKey = new BASE64Decoder().decodeBuffer(strEncryptWithPublicKey);
    byte[] byteDecryptWithPrivateKey = encryptUtil.decryptData(byteEncryptWithPublicKey,recvPrivateKey,"RSA/ECB/OAEPWithSHA1AndMGF1Padding");
    
    // 8. Decrypt the data using the Symmetric Key
    javax.crypto.spec.SecretKeySpec secretKeySpecDecrypted = new javax.crypto.spec.SecretKeySpec(byteDecryptWithPrivateKey,"AES");
    byte[] byteDecryptText = encryptUtil.decryptData(byteCipherText,secretKeySpecDecrypted,"AES");
    String strDecryptedText = new String(byteDecryptText);
    System.out.println(" Decrypted data is " +strDecryptedText);
    
    // 9. Compute MessageDigest of data + Signed message
    MessageDigest recvmd = MessageDigest.getInstance("SHA512");
    recvmd.update(byteDecryptText);
	byte byteHashOfRecvSignedData[] = recvmd.digest();

	String strHashOfRecvSignedData = new String();
		
	for (int i = 0; i < byteHashOfRecvSignedData.length; i++){
		strHashOfRecvSignedData = strHashOfRecvSignedData + Integer.toHexString((int)byteHashOfRecvSignedData[i] & 0xFF) ;
             }
	// 10. Validate if the Message Digest of the Decrypted Text matches the Message Digest of the Original Message
	if (!strHashOfRecvSignedData.equals(strHashOfData))
	{
		System.out.println(" Message has been tampered ");
	}
	
	}
	
	catch(Exception exp)
	{
		System.out.println(" Exception caught " + exp);
		exp.printStackTrace();
	}
	
	
	}

}