Clustered Keycloak with Remote Infinispan Cache configuration behind haproxy
---

This example provides the configuration to connect Keycloak to an external infinispan cluster.
The external infinispan cluster contains the [cache configurations required by Keycloak](ispn/conf/infinispan-keycloak.xml). 

Keycloak / Wildfly offers multiple options for accessing an external infinispan cluster, e.g. via the 
(deprecated) remote and the recommended hotrod cache store configuration.

This example project contains configurations for both variants:
- [Remote cache store configuration](docker-compose-haproxy-ispn-remote.yml) with the [remote cache cli](cli/0100-onstart-setup-remote-caches.cli) adjustments.
- [HotRod cache store configuration](docker-compose-haproxy-ispn-hotrod.yml) with the [hotrod cache cli](cli/0100-onstart-setup-hotrod-caches.cli) adjustments.

# Setup
## Prepare Infinispan Keystore and Truststore

```
keytool -genkey \
  -alias server \
  -keyalg RSA \
  -keystore ispn-server.jks \
  -keysize 2048 \
  -storepass password \
  -dname "CN=ispn, OU=keycloak, O=tdlabs, L=Saarbrücken, ST=SL, C=DE"

keytool -exportcert \
  -keystore ispn-server.jks \
  -alias server \
  -storepass password \
  -file ispn-server.crt

keytool -importcert \
  -keystore ispn-truststore.jks \
  -storepass password \
  -alias server \
  -file ispn-server.crt \
  -noprompt

rm ispn-server.crt
```

# Run

To run this example see the [readme.md](../readme.md) in the cluster folder.

## Misc
### Patch CA Certs

As of Keycloak image 14.0.0 the used JDK Truststore contains expired certificates which lead to an 
exception during server start. To fix this, we need to remove the expired certificates.

To get rid of exceptions such as:
```
acme-keycloak_1 | 11:32:21,725 WARN  [org.wildfly.extension.elytron] (MSC service thread 1-7) WFLYELY00024: Certificate [soneraclass2rootca] in KeyStore is not valid: java.security.cert.CertificateExpiredException: NotAfter: Tue Apr 06 07:29:40 GMT 2021
acme-keycloak_1 | 	at java.base/sun.security.x509.CertificateValidity.valid(CertificateValidity.java:277)
acme-keycloak_1 | 	at java.base/sun.security.x509.X509CertImpl.checkValidity(X509CertImpl.java:675)
acme-keycloak_1 | 	at java.base/sun.security.x509.X509CertImpl.checkValidity(X509CertImpl.java:648)
acme-keycloak_1 | 	at org.wildfly.extension.elytron@15.0.1.Final//org.wildfly.extension.elytron.KeyStoreService.checkCertificatesValidity(KeyStoreService.java:230)
acme-keycloak_1 | 	at org.wildfly.extension.elytron@15.0.1.Final//org.wildfly.extension.elytron.KeyStoreService.start(KeyStoreService.java:192)
acme-keycloak_1 | 	at org.jboss.msc@1.4.12.Final//org.jboss.msc.service.ServiceControllerImpl$StartTask.startService(ServiceControllerImpl.java:1739)
acme-keycloak_1 | 	at org.jboss.msc@1.4.12.Final//org.jboss.msc.service.ServiceControllerImpl$StartTask.execute(ServiceControllerImpl.java:1701)
acme-keycloak_1 | 	at org.jboss.msc@1.4.12.Final//org.jboss.msc.service.ServiceControllerImpl$ControllerTask.run(ServiceControllerImpl.java:1559)
acme-keycloak_1 | 	at org.jboss.threads@2.4.0.Final//org.jboss.threads.ContextClassLoaderSavingRunnable.run(ContextClassLoaderSavingRunnable.java:35)
acme-keycloak_1 | 	at org.jboss.threads@2.4.0.Final//org.jboss.threads.EnhancedQueueExecutor.safeRun(EnhancedQueueExecutor.java:1990)
acme-keycloak_1 | 	at org.jboss.threads@2.4.0.Final//org.jboss.threads.EnhancedQueueExecutor$ThreadBody.doRunTask(EnhancedQueueExecutor.java:1486)
acme-keycloak_1 | 	at org.jboss.threads@2.4.0.Final//org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1363)
acme-keycloak_1 | 	at java.base/java.lang.Thread.run(Thread.java:829)
```

Copy the cacerts keystore from a running Keycloak container locally
```
docker cp gifted_bhaskara:/etc/pki/ca-trust/extracted/java/cacerts ./ispn/cacerts
chmod u+w cacerts
```

```
keytool -delete -keystore ./ispn/cacerts -alias quovadisrootca -storepass changeit
keytool -delete -keystore ./ispn/cacerts -alias soneraclass2rootca -storepass changeit

chmod u-w ./ispn/cacerts
```

Now mount the fixed `cacerts` into the container via `./ispn/cacerts:/etc/pki/ca-trust/extracted/java/cacerts:z`

## Problems

### Infinispan connect-timeout for remote caches not supported by Keycloak/Wildfly
Cannot configure connect-timeout for remote caches as the configuration attribute is not supported by wildfly, 
but supported by infinispan.

See: https://issues.redhat.com/browse/WFLY-15046

A possible workaround is using [wildfly-clustering-infinispan-extension-patch](/keycloak/patches/wildfly-clustering-infinispan-extension-patch), which 
contains a patched version of `wildfly-clustering-infinispan-extension.jar` with support for configuring `connect-timeouts`.