// Copyright 2012 Citrix Systems, Inc. Licensed under the
package com.cloud.utils.netty;

public class TrustAllManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
    }
    
    public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
          return true;
    }
    
    public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
          return true;
    }
    
    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
    throws java.security.cert.CertificateException {
          return;
    }
    
    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
    throws java.security.cert.CertificateException {
          return;
    }
}