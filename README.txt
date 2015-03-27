Here are the steps that I used to test my application:

1- From the home directory, generated private and public keys for Alice then for Bob (for this step, you only need to copy keystore.ks from 'pathToProject/Security-lab2/src/java/crypto/keystore.ks' to your home directory. In my case, it was '/Users/Franck/'. Key generation steps are at the bottom of the page)
2- Used PublicKeyCryptography.java’s main program to print Alice and Bob public keys
3- Copied pasted these keys into separate files
4- Created their accounts on the web
5- Uploaded their public keys by:
    - logging into their respective accounts
    - selecting “Upload your public key”
    - choosing the respective files from step 3
    - clicking on the “upload” button
6- Logged into Alice’s account
7- Clicked on the “Send a Secure File” link
8- Uploaded a text file
9- Entered the email where I wanted to received the encrypted files
10- Clicked on the “Browse Public Keys” link
11- Copied Bob’s public key
12- Pasted the key into the proper field and clicked on the “Send” button

After receiving the email:

1- Saved the email attachments
2- Logged into Bob’s account
3- Clicked on the “Decrypt a Secure File” link
4- Uploaded the 3 files received in the email and clicked on the “Decrypt” button

-----------------------

Key Generation Using Java's Keytool:

For Alice:

From the home directory, run: keytool -genkey -alias alice -keystore keystore.ks -keyalg RSA

Enter keystore password: testpwd

What is your first and last name?

[Unknown]:  Alice Sender

What is the name of your organizational unit?

[Unknown]:  IT

What is the name of your organization?

[Unknown]:  ABC Inc

What is the name of your City or Locality?

[Unknown]:  LA

What is the name of your State or Province?

[Unknown]:  CA

What is the two-letter country code for this unit?

[Unknown]:  US

Is CN=Alice Sender, OU=IT, O=ABC Inc, L=LA, ST=CA, C=US correct?

[no]:  y

Enter key password for <testsender>

(RETURN if same as keystore password):  send123

For BOB:

From the home directory, run: keytool -genkey -alias bob -keystore keystore.ks -keyalg RSA

Enter keystore password: testpwd What is your first and last name?

[Unknown]:  Bob Receiver

What is the name of your organizational unit?

[Unknown]:  HR

What is the name of your organization?

[Unknown]:  ABC Inc

What is the name of your City or Locality?

[Unknown]:  SFO

What is the name of your State or Province?

[Unknown]:  CA

What is the two-letter country code for this unit?

[Unknown]:  US

Is CN=Bob Receiver, OU=HR, O=ABC Inc, L=SFO, ST=CA, C=US correct?

[no]:  y

Enter key password for <testrecv>

(RETURN if same as keystore password):  recv123
