package com.qianzhui.enode.rocketmq.client.ons;

import java.nio.charset.Charset;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


/**
 * copy from spas
 *
 * @auther lansheng.zj
 */
public class SpasSigner {
    public static final Charset UTF8 = Charset.forName("UTF-8");


    public static String sign(String data, String key) throws SpasException {
        return signAndBase64Encode(data, key, SigningAlgorithm.HmacSHA1);
    }


    public static String sign(String data, String key, SigningAlgorithm algorithm) throws SpasException {
        return signAndBase64Encode(data, key, algorithm);
    }


    public static String sign(byte[] data, String key) throws SpasException {
        return signAndBase64Encode(data, key, SigningAlgorithm.HmacSHA1);
    }


    public static String sign(byte[] data, String key, SigningAlgorithm algorithm) throws SpasException {
        return signAndBase64Encode(data, key, algorithm);
    }


    private static String signAndBase64Encode(String data, String key, SigningAlgorithm algorithm)
            throws SpasException {
        try {
            byte[] signature = sign(data.getBytes(UTF8), key.getBytes(UTF8), algorithm);
            return new String(Base64.encodeBase64(signature));
        } catch (Exception e) {
            throw new SpasException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }


    private static String signAndBase64Encode(byte[] data, String key, SigningAlgorithm algorithm)
            throws SpasException {
        try {
            byte[] signature = sign(data, key.getBytes(UTF8), algorithm);
            return new String(Base64.encodeBase64(signature));
        } catch (Exception e) {
            throw new SpasException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }


    private static byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) throws SpasException {
        try {
            Mac mac = Mac.getInstance(algorithm.toString());
            mac.init(new SecretKeySpec(key, algorithm.toString()));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new SpasException("Unable to calculate a request signature: " + e.getMessage(), e);
        }
    }
}
