package pt.tecnico.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
// import static javax.xml.bind.DatatypeConverter.printHexBinary;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.x509.X509V3CertificateGenerator;

public class CryptographyImpl {
    
    public static KeyStore InitializeKeyStore(char[] password, String path) {
        try {
            KeyStore ks = KeyStore.getInstance("JCEKS");

            File jks = new File(path);
            if(jks.exists()) {
                System.out.println("Keystore exists, will load");
                ks = KeyStore.getInstance("JCEKS");
                FileInputStream fis = new FileInputStream(jks);
                ks.load(fis , password);
                fis.close();
                return ks;
            }
            else {
                System.out.println("Keystore doesn't exist, will create");
                ks.load(null, password);

                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, password);
                fos.close();
                return ks;
            }


        } catch (KeyStoreException kse) {
            System.out.println("KeyStore error: " + kse.getLocalizedMessage());
        } catch (IOException ioe) {
            try {
                FileInputStream fiss = new FileInputStream( new File(path));
                System.out.println(fiss.readAllBytes());
            } catch (Exception e) {
                System.out.println("IO error 2: " + e.getMessage());
            }
            System.out.println("IO error: " + ioe.getMessage());
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("No algorithmn JCEKS, somehow: " + nsae.getLocalizedMessage());
        } catch (CertificateException ce) {
            System.out.println("Certificate error: " + ce.getLocalizedMessage());
        }
        return null;
    }

    public static void UpdateKeyStore(KeyStore ks, char[] password, String path) {
        File jks = new File(path);
        try {
            if(jks.exists()) {
                FileOutputStream fos = new FileOutputStream(jks);
                ks.store(fos, password);
                fos.close();
            }
            else {
                System.out.println("ERROR - KeyStore file not found");
            }
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore exception: " + kse.getMessage());
        } catch (FileNotFoundException fnfe) {
            System.out.println("ERROR - File not found: " + fnfe.getMessage());
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - No such algorithm: " + nsae.getMessage());
        } catch (CertificateException ce) {
            System.out.println("ERROR - Certificate exception:" + ce.getMessage());
        } catch (IOException ioe) {
            System.out.println("ERROR - IO: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR - IO: " + e.getMessage());
        }
    }

    public static void CreateNewKeyStore(KeyStore ks, char[] password, String path) {
        File jks = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(jks);
            ks.store(fos, password);
            fos.close();
        } catch (KeyStoreException kse) {
            System.out.println("ERROR - KeyStore exception: " + kse.getMessage());
        } catch (FileNotFoundException fnfe) {
            System.out.println("ERROR - File not found: " + fnfe.getMessage());
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("ERROR - No such algorithm: " + nsae.getMessage());
        } catch (CertificateException ce) {
            System.out.println("ERROR - Certificate exception:" + ce.getMessage());
        } catch (IOException ioe) {
            System.out.println("ERROR - IO: " + ioe.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR - IO: " + e.getMessage());
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

        return new SecretKeySpec(encoded, 0, 16, "AES");

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


            System.out.println("Deciphering ...");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));


            return cipher.doFinal(cipheredBytes);

        } catch (InvalidKeyException ike) {
            // Pokemon exception handling!
            System.out.println("No key/wrong key associated: " + ike.getMessage());
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
            System.out.println("Error in RSA encryption. " + e.getLocalizedMessage());
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
            System.out.println("Error in RSA decryption. " + e.getLocalizedMessage());
            return null;
        }
    }

    public static PrivateKey readPrivateKey(String path) {
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file); 
            byte[] keyBytes = fis.readAllBytes();
            fis.close();

            PKCS8EncodedKeySpec spec =
              new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            System.out.println("Failed to read private key: " + e.getMessage());
            return null;
        }
    }


    public static PublicKey readPublicKey(String path) {
        try {
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file); 
            byte[] keyBytes = fis.readAllBytes();
    
            fis.close();
            X509EncodedKeySpec spec =
              new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (Exception e) {
            System.out.println("Failed to read public key: " + e.getMessage());
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

    @SuppressWarnings("deprecation")
    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String certificateDN, String certificateName)  {
        X509Certificate cert = null;

        
        X509V3CertificateGenerator v3CertGen =  new X509V3CertificateGenerator();
        v3CertGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
        v3CertGen.setIssuerDN(new X509Principal(certificateDN));
        v3CertGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24));
        v3CertGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)));
        v3CertGen.setSubjectDN(new X509Principal(certificateDN));
        v3CertGen.setPublicKey(keyPair.getPublic());
        v3CertGen.setSignatureAlgorithm("SHA256WithRSA");
        try {
            cert = v3CertGen.generate(keyPair.getPrivate());
            // saveCert(cert, keyPair.getPrivate());
            return cert;
        } catch (Exception e) {
            System.out.println("Unable to generate X509 certificate!");
            return null;
        }
    }

    public static byte[] GenerateSHA3Digest(byte[] message) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            return digest.digest(message);
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("Failed to create digest");
            return null;
        }
    }

    public static byte[] GenerateSaltedSHA3Digest(byte[] message, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            digest.update(salt);
            return digest.digest(message);
        } catch (NoSuchAlgorithmException nsae) {
            System.out.println("Failed to create digest");
            return null;
        }
    }

    public static byte[] GenerateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
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