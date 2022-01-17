package pt.tecnico.Common;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
// import static javax.xml.bind.DatatypeConverter.printHexBinary;
import javax.crypto.spec.SecretKeySpec;

public class CryptographyImpl {

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

    public static byte[] encryptFileAES(String ivString, byte[] plainBytes, Key secretKey) {
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

    public static byte[] decryptFileAES(String ivString, byte[] cipheredBytes, Key secretKey) {
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