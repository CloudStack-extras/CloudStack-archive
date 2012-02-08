package com.cloud.utils.security;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.cloud.utils.Ternary;

public class CertificateHelper {
	public static byte[] buildAndSaveKeystore(String alias, String cert, String privateKey, String storePassword) throws KeyStoreException, CertificateException, 
		NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		KeyStore ks = buildKeystore(alias, cert, privateKey, storePassword);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ks.store(os, storePassword != null ? storePassword.toCharArray() : null);
		os.close();
		return os.toByteArray();
	}
	
	public static byte[] buildAndSaveKeystore(List<Ternary<String, String, String>> certs, String storePassword) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, InvalidKeySpecException {
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null, storePassword != null ? storePassword.toCharArray() : null);

		//name,cert,key
		for (Ternary<String, String, String> cert : certs) {
			if (cert.third() == null) {
				Certificate c = buildCertificate(cert.second());
				ks.setCertificateEntry(cert.first(), c);
			} else {
				Certificate[] c = new Certificate[certs.size()];
				int i = certs.size();
				for (Ternary<String, String, String> ct : certs) {
					c[i - 1] = buildCertificate(ct.second());
					i--;
				}
			    ks.setKeyEntry(cert.first(), buildPrivateKey(cert.third()), storePassword != null ? storePassword.toCharArray() : null, c );
			}
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ks.store(os, storePassword != null ? storePassword.toCharArray() : null);
		os.close();
		return os.toByteArray();
	}
	
	public static KeyStore loadKeystore(byte[] ksData, String storePassword) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		assert(ksData != null);
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new ByteArrayInputStream(ksData), storePassword != null ? storePassword.toCharArray() : null);

		return ks;
	}
	
	public static KeyStore buildKeystore(String alias, String cert, String privateKey, String storePassword) throws KeyStoreException, CertificateException, 
		NoSuchAlgorithmException, InvalidKeySpecException, IOException {
	
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(null, storePassword != null ? storePassword.toCharArray() : null);
		Certificate[] certs = new Certificate[1];
		certs[0] = buildCertificate(cert);
	    ks.setKeyEntry(alias, buildPrivateKey(privateKey), storePassword != null ? storePassword.toCharArray() : null, certs );
		return ks;
	}

	public static Certificate buildCertificate(String content) throws CertificateException {
		assert(content != null);
		
		BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		return cf.generateCertificate(bis);
	}

	public static Key buildPrivateKey(String base64EncodedKeyContent) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    PKCS8EncodedKeySpec  keysp = new PKCS8EncodedKeySpec (Base64.decodeBase64(base64EncodedKeyContent));
	    return kf.generatePrivate (keysp);
	}
}
