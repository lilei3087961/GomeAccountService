package com.gome.gomeaccountservice.utils;

import java.security.MessageDigest;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
//import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import android.util.Base64;

public class DesUtil {
    public static void main(String[] args) throws Exception{
            System.out.println(encrypt3DES("1@1.com"));
        System.out.println(encrypt3DES("123456"));

    }
    public  static final String KEY = "GOME_IUVACCOUNT_KEY";
    public final static String MD5(String pwd) {  
        //用于加密的字符  
        char md5String[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',  
                'A', 'B', 'C', 'D', 'E', 'F' };  
        try {  
            //使用平台的默认字符集将此 String 编码为 byte序列，并将结果存储到一个新的 byte数组中  
            byte[] btInput = pwd.getBytes();  
               
            //信息摘要是安全的单向哈希函数，它接收任意大小的数据，并输出固定长度的哈希值。  
            MessageDigest mdInst = MessageDigest.getInstance("MD5");  
               
            //MessageDigest对象通过使用 update方法处理数据， 使用指定的byte数组更新摘要  
            mdInst.update(btInput);  
               
            // 摘要更新之后，通过调用digest（）执行哈希计算，获得密文  
            byte[] md = mdInst.digest();  
               
            // 把密文转换成十六进制的字符串形式  
            int j = md.length;  
            char str[] = new char[j * 2];  
            int k = 0;  
            for (int i = 0; i < j; i++) {   //  i = 0  
                byte byte0 = md[i];  //95  
                str[k++] = md5String[byte0 >>> 4 & 0xf];    //    5   
                str[k++] = md5String[byte0 & 0xf];   //   F  
            }  
               
            //返回经过加密后的字符串  
            return new String(str);  
               
        } catch (Exception e) {  
            return null;  
        }  
    }  
    /**
     *
     * @param value 待解密字符串
     * @return
     * @throws Exception
     */
    public static String decrypt3DES(String value) throws Exception {
        return decrypt3DES(value,KEY);
    }

    /**
     *
     * @param value 待加密字符串
     * @return
     * @throws Exception
     */
    public static String encrypt3DES(String value) throws Exception {
        return encrypt3DES(value,KEY);

    }
    /**
     *
     * @param value 待解密字符串
     * @param key 原始密钥字符串
     * @return
     * @throws Exception
     */
    public static String decrypt3DES(String value, String key) throws Exception {
        //byte[] b = decryptMode(getKeyBytes(key), Base64.decode(value));
    	byte[] b = decryptMode(getKeyBytes(key), Base64.decode(value, Base64.DEFAULT));
        return new String(b);
    }

    /**
     *
     * @param value 待加密字符串
     * @param key 原始密钥字符串
     * @return
     * @throws Exception
     */
    public static String encrypt3DES(String value, String key) throws Exception {
        String str = byte2Base64(encryptMode(getKeyBytes(key), value.getBytes()));
        return str;

    }

    /**
     * 计算24位长的密码byte值,首先对原始密钥做MD5算hash值，再用前8位数据对应补全后8位
     * @param strKey
     * @return
     * @throws Exception
     */
    public static byte[] getKeyBytes(String strKey) throws Exception {
        if (null == strKey || strKey.length() < 1)
            throw new Exception("key is null or empty!");
        java.security.MessageDigest alg = java.security.MessageDigest.getInstance("MD5");
        alg.update(strKey.getBytes());
        byte[] bkey = alg.digest();
//        System.out.println("md5key.length=" + bkey.length);
//        System.out.println("md5key=" + byte2hex(bkey));
        int start = bkey.length;
        byte[] bkey24 = new byte[24];
        for (int i = 0; i < start; i++) {
            bkey24[i] = bkey[i];
        }
        for (int i = start; i < 24; i++) {//为了与.net16位key兼容
            bkey24[i] = bkey[i - start];
        }
//        System.out.println("byte24key.length=" + bkey24.length);
//        System.out.println("byte24key=" + byte2hex(bkey24));
        return bkey24;
    }
    private static final String Algorithm = "DESede"; //定义 加密算法,可用 DES,DESede,Blowfish

    /**
     *
     * @param keybyte 加密密钥，长度为24字节
     * @param src 为被加密的数据缓冲区（源）
     * @return
     */
    public static byte[] encryptMode(byte[] keybyte, byte[] src) {
        try {
            //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm); //加密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.ENCRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;

    }
    /**
     *
     * @param keybyte 加密密钥，长度为24字节
     * @param src 加密后的缓冲区
     * @return
     */
    public static byte[] decryptMode(byte[] keybyte, byte[] src) {
        try { //生成密钥
            SecretKey deskey = new SecretKeySpec(keybyte, Algorithm);
            //解密
            Cipher c1 = Cipher.getInstance(Algorithm);
            c1.init(Cipher.DECRYPT_MODE, deskey);
            return c1.doFinal(src);
        } catch (java.security.NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        } catch (javax.crypto.NoSuchPaddingException e2) {
            e2.printStackTrace();
        } catch (java.lang.Exception e3) {
            e3.printStackTrace();
        }
        return null;
    }



    //转换成base64编码
    public static String byte2Base64(byte[] b) {
        return Base64.encodeToString(b, 0, b.length, Base64.DEFAULT);//Base64.encode(b);
    }

    //转换成十六进制字符串
    public static String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
            if (n < b.length - 1)
                hs = hs + ":";
        }
        return hs.toUpperCase();
    }

}
  