package com.enode.rocketmq.client.ons;

import com.alibaba.rocketmq.common.constant.LoggerName;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

public class OnsAuthSigner {
    public static final Charset defaultCharset = Charset.forName("UTF-8");
    public static final SigningAlgorithm defaultAlgorithm = SigningAlgorithm.HmacSHA1;
    private static final Logger log = LoggerFactory.getLogger(LoggerName.RocketmqAuthorizeLoggerName);
    private static final int CAL_SIGNATURE_FAILED = 10015;
    private static final String CAL_SIGNATURE_FAILED_MSG = "[%s:signature-failed] unable to calculate a request signature. error=%s";

    public static String calSignature(String data, String key) throws AuthenticationException {
        return calSignature(data, key, defaultAlgorithm, defaultCharset);
    }

    public static String calSignature(String data, String key, SigningAlgorithm algorithm, Charset charset) throws AuthenticationException {
        return signAndBase64Encode(data, key, algorithm, charset);
    }

    private static String signAndBase64Encode(String data, String key, SigningAlgorithm algorithm, Charset charset)
            throws AuthenticationException {
        try {
            byte[] signature = sign(data.getBytes(charset), key.getBytes(charset), algorithm);
            return new String(Base64.encodeBase64(signature), defaultCharset);
        } catch (Exception e) {
            String message = String.format(CAL_SIGNATURE_FAILED_MSG, CAL_SIGNATURE_FAILED, e.getMessage());
            log.error(message, e);
            throw new AuthenticationException("CAL_SIGNATURE_FAILED", CAL_SIGNATURE_FAILED, message, e);
        }
    }

    private static byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) throws AuthenticationException {
        try {
            Mac mac = Mac.getInstance(algorithm.toString());
            mac.init(new SecretKeySpec(key, algorithm.toString()));
            return mac.doFinal(data);
        } catch (Exception e) {
            String message = String.format(CAL_SIGNATURE_FAILED_MSG, CAL_SIGNATURE_FAILED, e.getMessage());
            log.error(message, e);
            throw new AuthenticationException("CAL_SIGNATURE_FAILED", CAL_SIGNATURE_FAILED, message, e);
        }
    }

    public static String calSignature(byte[] data, String key) throws AuthenticationException {
        return calSignature(data, key, defaultAlgorithm, defaultCharset);
    }

    public static String calSignature(byte[] data, String key, SigningAlgorithm algorithm, Charset charset) throws AuthenticationException {
        return signAndBase64Encode(data, key, algorithm, charset);
    }

    private static String signAndBase64Encode(byte[] data, String key, SigningAlgorithm algorithm, Charset charset)
            throws AuthenticationException {
        try {
            byte[] signature = sign(data, key.getBytes(charset), algorithm);
            return new String(Base64.encodeBase64(signature), defaultCharset);
        } catch (Exception e) {
            String message = String.format(CAL_SIGNATURE_FAILED_MSG, CAL_SIGNATURE_FAILED, e.getMessage());
            log.error(message, e);
            throw new AuthenticationException("CAL_SIGNATURE_FAILED", CAL_SIGNATURE_FAILED, message, e);
        }
    }

}

