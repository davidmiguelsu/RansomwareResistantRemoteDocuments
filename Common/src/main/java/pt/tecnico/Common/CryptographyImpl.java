package pt.tecnico.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
// import static javax.xml.bind.DatatypeConverter.printHexBinary;
import javax.crypto.spec.SecretKeySpec;

public class CryptographyImpl {
    
    public static KeyStore InitializeKeyStore(char[] password, String path) {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");

            File jks = new File(path);
            if(jks.exists()) {
                System.out.println("Keystore exists, will load");
                ks = KeyStore.getInstance("JCEKS");
                ks.load(new FileInputStream(jks), password);
                return ks;
            }
            else {
                System.out.println("Keystore doesn't exist, will create");
                ks.load(null, password);

                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, password);
                return ks;
            }


        } catch (KeyStoreException kse) {
            //TODO: handle exception
        } catch (IOException ioe) {

        } catch (NoSuchAlgorithmException nsae) {

        } catch (CertificateException ce) {

        }
        return null;
    }

    public static void UpdateKeyStore(KeyStore ks, char[] password, String path) {
        File jks = new File(path);
        try {
            if(jks.exists()) {
                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, password);
            }
            else {
                System.out.println("ERROR - KeyStore file not found");
            }
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore exception: " + kse.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("ERROR - KeyStore file not found");
        } catch (NoSuchAlgorithmException nsae) {
            
        } catch (CertificateException ce) {
            
        } catch (IOException ioe) {

        }
    }

    public static KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - Failed to generate key pair");
            return null;
        }
    }

    public static void generateAESKey(String keyPath) throws GeneralSecurityException, IOException {
        // get an AES private key
        System.out.println("Generating AES key ..." );
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        Key key = keyGen.generateKey();
        System.out.println( "Finish generating AES key" );
        byte[] encoded = key.getEncoded();
        // System.out.println("Key:");
        // System.out.println(printHexBinary(encoded));

        System.out.println("Writing key to '" + keyPath + "' ..." );

        FileOutputStream fos = new FileOutputStream(keyPath);
        fos.write(encoded);
        fos.close();
    }

    public static SecretKey generateAESKey() throws GeneralSecurityException, IOException {
        // get an AES private key
        System.out.println("Generating AES key ..." );
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        Key key = keyGen.generateKey();
        System.out.println( "Finish generating AES key" );
        byte[] encoded = key.getEncoded();
        // System.out.println("Key:");
        // System.out.println(printHexBinary(encoded));

        return new SecretKeySpec(encoded, 0, 16, "AES");

        // System.out.println("Writing key to '" + keyPath + "' ..." );

        // FileOutputStream fos = new FileOutputStream(keyPath);
        // fos.write(encoded);
        // fos.close();
    }

    public static Key readAESKey(String keyPath) throws GeneralSecurityException, IOException {
        System.out.println("Reading key from file " + keyPath + " ...");
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();

        return new SecretKeySpec(encoded, 0, 16, "AES");
    }

    public static byte[] encryptAES(String ivString, byte[] plainBytes, Key secretKey) {
        try {
            //TODO: Make this not be always 0
            byte[] ivBytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
            // System.out.println(ivString.getBytes());

            // get a DES cipher object and print the provider
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            System.out.println(cipher.getProvider().getInfo());


            System.out.println("Ciphering ...");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));


            return cipher.doFinal(plainBytes);

        } catch (Exception e) {
            // Pokemon exception handling!
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decryptAES(String ivString, byte[] cipheredBytes, Key secretKey) {
        try {
            //TODO: Make this not be always 0
            byte[] ivBytes = new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
            // System.out.println(ivString.getBytes());

            // get a DES cipher object and print the provider
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            System.out.println(cipher.getProvider().getInfo());


            System.out.println("Deciphering ...");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));


            return cipher.doFinal(cipheredBytes);

        } catch (Exception e) {
            // Pokemon exception handling!
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptRSA(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(Key.getBytes())));
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
            // return new String(Base64.getEncoder().encode(encryptedbytes));
        } catch (Exception e) {
            System.out.println("Error in RSA encryption");
            //TODO: handle exception
            return null;
        }
    }

    public static byte[] decryptRSA(byte[] data, Key key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(Key.getBytes())));
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
            // return new String(Base64.getEncoder().encode(encryptedbytes));
        } catch (Exception e) {
            System.out.println("Error in RSA decryption");
            //TODO: handle exception
            return null;
        }
    }

    public static PrivateKey readPrivateKey(String path) {
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file); 
            byte[] keyBytes = fis.readAllBytes();
            
            PKCS8EncodedKeySpec spec =
              new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (IOException e) {
            //TODO: handle exception
        } catch (Exception e) {

        }
        return null;
    }


    // public static PrivateKey readPrivateKey(KeyStore ks, String keyName, String password) {
    //     char[] pwdArray = password.toCharArray();
    //     try {
    //         ks.getKey(keyName, pwdArray);
    //         // // File file = new File(path);
    //         // FileInputStream fis = new FileInputStream(file); 
    //         // byte[] keyBytes = fis.readAllBytes();
            
    //         PKCS8EncodedKeySpec spec =
    //           new PKCS8EncodedKeySpec(keyBytes);
    //         KeyFactory kf = KeyFactory.getInstance("RSA");
    //         return kf.generatePrivate(spec);
    //     } catch (IOException e) {
    //         //TODO: handle exception
    //     } catch (Exception e) {

    //     }
    //     return null;
    // }
    public static PublicKey readPublicKey(String path) {
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file); 
            byte[] keyBytes = fis.readAllBytes();
    
            X509EncodedKeySpec spec =
              new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            //TODO: handle exception
            return null;
        }
    }

    public static byte[] generateDigitalSignature(byte[] data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
            
        } catch (InvalidKeyException ike) {
            System.out.println("InvalidKeyException error in generateDigitalSignature" + ike.getLocalizedMessage());
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("NoSuchAlgorithmException error in generateDigitalSignature" + nsae.getLocalizedMessage());
        } catch (SignatureException se) {
            System.out.println("SignatureException error in generateDigitalSignature" + se.getLocalizedMessage());
        }
        return null;
    }

    public static boolean verifyDigitalSignature(byte[] message, byte[] signature, PublicKey publicKey) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
    
            sign.initVerify(publicKey);
            sign.update(message);
    
            return sign.verify(signature);
        } catch (InvalidKeyException ike) {
            System.out.println("InvalidKeyException error in verifyDigitalSignature" + ike.getLocalizedMessage());
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("NoSuchAlgorithmException error in verifyDigitalSignature" + nsae.getLocalizedMessage());
        } catch (SignatureException se) {
            System.out.println("SignatureException error in verifyDigitalSignature" + se.getLocalizedMessage());
        }
        return false;
    }

    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}