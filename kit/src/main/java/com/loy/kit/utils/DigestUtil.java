package com.loy.kit.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 摘要工具类
 *
 * @author Loy
 * @time 2021/4/8 10:40
 * @des
 */
public class DigestUtil {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private static final char[] HEX_UPPER_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final char[] HEX_LOWER_CHARS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     * 签名策略
     */
    public  static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /*
        MD2
        MD5
        SHA-1
        SHA-224
        SHA-256
        SHA-384
        SHA-512
        SHA-512/224
        SHA-512/256
    */
    public enum Digest {
        MD5,
        SHA1,

    }

    public enum Transformation {
        RSA("RSA/None/PKCS1Padding"),
        DES("DES/None/PKCS1Padding"),
        AES("AES/None/PKCS1Padding");

        // Android 和 Java 在默认
        private final String javaTransformation;

        Transformation(String transformation) {
            this.javaTransformation = transformation;
        }
    }

    public static byte[] getUTF8Bytes(String src) {
        return (src == null || src.length() == 0) ? new byte[0] : src.getBytes(UTF8);
    }

    public static String getUTF8String(byte[] src) {
        return (src == null || src.length == 0) ? "" : new String(src, UTF8);
    }

    public static String encodeToHMacSHA256String(String secret, String message){
        try {
            Mac hmacSha256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            hmacSha256.init(secret_key);
            byte[] bytes = hmacSha256.doFinal(message.getBytes());
            return bytes2HexString(bytes, false);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String encodeToMD5String(String src) {
        return encodeToMD5String(src, false);
    }

    public static String encodeToMD5String(String src, boolean isUpperCase) {
        byte[] bytes = getUTF8Bytes(src);
        byte[] md5Bytes = md5(bytes);
        return bytes2HexString(md5Bytes, isUpperCase);
    }

    private static byte[] md5(byte[] bytes) {
        return digest(bytes, Digest.MD5);
    }

    private static byte[] digest(byte[] bytes, Digest digest) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance(digest.name());
            return md.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String bytes2HexString(final byte[] bytes, boolean isUpperCase) {
        if (bytes != null && bytes.length > 0) {
            char[] chars = isUpperCase ? HEX_UPPER_CHARS : HEX_LOWER_CHARS;
            int len = bytes.length;
            char[] ret = new char[len << 1];
            for (int i = 0, j = 0; i < len; i++) {
                ret[j++] = chars[(bytes[i] >> 4) & 0x0f];
                ret[j++] = chars[bytes[i] & 0x0f];
            }
            return new String(ret);
        }
        return "";
    }

    public static byte[] hexString2Bytes(String hexString) {
        if (EmptyUtil.isStringEmpty(hexString))
            return new byte[0];
        int len = hexString.length();
        if (len % 2 != 0) {
            hexString = "0" + hexString;
            len = len + 1;
        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] ret = new byte[len >> 1];
        for (int i = 0; i < len; i += 2) {
            ret[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));
        }
        return ret;
    }

    private static int hex2Dec(final char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 公钥加密
     * @param data 待加密数据
     * @param keyBytes 公钥
     * @return 加密数据
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] keyBytes) {
        return rsaTemplate(data, keyBytes, true, true);
    }

    /**
     * 私钥解密
     * @param data 加密数据
     * @param keyBytes 私钥
     * @return 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] keyBytes) {
        return rsaTemplate(data, keyBytes, false, false);
    }

    /**
     * 私钥加密
     * @param data 待加密数据
     * @param keyBytes 私钥
     * @return 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] keyBytes) {
        return rsaTemplate(data, keyBytes, true, false);
    }

    /**
     * 公钥解密
     * @param data 加密数据
     * @param keyBytes 公钥
     * @return 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] keyBytes) {
        return rsaTemplate(data, keyBytes, false, true);
    }

    /**
     * 私钥签名
     * @param data 待签名数据
     * @param keyBytes 私钥
     * @return 签名数据
     */
    public static byte[] signByPrivateKey(byte[] data, byte[] keyBytes) {
        try {
            PrivateKey key = (PrivateKey) getKey(keyBytes, false);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(key);
            signature.update(data);
            return signature.sign();
        } catch (InvalidKeyException | SignatureException |
                NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 公钥验证签名
     * @param data 签名前数据
     * @param keyBytes 公钥
     * @param sign 签名后数据
     * @return 是否签名一致, 验证私钥归属
     */
    public static boolean verifyByPublicKey(byte[] data, byte[] keyBytes, byte[] sign) {
        try {
            PublicKey key = (PublicKey) getKey(keyBytes, true);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initVerify(key);
            signature.update(data);
            return signature.verify(sign);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] encryptByDES(byte[] data, byte[] keyBytes) throws Exception{
        Cipher cipher = Cipher.getInstance(Transformation.DES.javaTransformation);
        DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
        return cipher.doFinal(data);
    }

    public static byte[] decryptByDES(byte[] data, byte[] keyBytes) throws Exception {
        Cipher cipher = Cipher.getInstance(Transformation.DES.javaTransformation);
        DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        SecretKey secretKey = keyFactory.generateSecret(desKeySpec);
        IvParameterSpec iv = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        return cipher.doFinal(data);
    }

    public static byte[] encryptByAES(byte[] data, byte[] keyBytes) throws Exception{
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(keyBytes));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decryptByAES(byte[] data, byte[] keyBytes) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128, new SecureRandom(keyBytes));
        SecretKey secretKey = kgen.generateKey();
        byte[] enCodeFormat = secretKey.getEncoded();
        SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    // 公钥加密 <-> 私钥解密 | 私钥签名  -> 公钥验证
    public static byte[] rsaTemplate(byte[] data, byte[] keyBytes, boolean isEncrypt, boolean isPublic) {
        try {
            Cipher cipher = Cipher.getInstance(Transformation.RSA.javaTransformation);
            Key key = getKey(keyBytes, isPublic);
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException |
                NoSuchPaddingException | InvalidKeyException |
                BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Key getKey(byte[] keyBytes, boolean isPublic) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance(Transformation.RSA.name());
        Key key = null;
        KeySpec keySpec = null;
        if (isPublic) {
            keySpec = new X509EncodedKeySpec(keyBytes);
            key = keyFactory.generatePublic(keySpec);
        } else {
            keySpec = new PKCS8EncodedKeySpec(keyBytes);
            key = keyFactory.generatePrivate(keySpec);
        }
        return key;
    }
}
