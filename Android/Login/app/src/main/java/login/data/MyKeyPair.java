package vs.in.de.uni_ulm.mreuter.login.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.openssl.PEMKeyPair;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

/**
 * Created by mreuter on 13/02/15.
 */
public class MyKeyPair implements Parcelable{
    private String jointHash;
    private String user;
    private String site;
    private byte[] pubKeyData;
    private byte[] privKeyData;
    private String created;
    private KeyPair keyPair;

    // Basic constructor for saving new keys.
    public MyKeyPair(String jointHash) {
        this.jointHash = jointHash;
        generateKeys();
        created = new Date().toString();
    }

    // Constructor for keys obtained from the database, that provide more thorough information.
    public MyKeyPair(String jointHash, String user, String site, byte[] pubKeyData, byte[] privKeyData, String created) {

        PrivateKey privKey = null;
        PublicKey pubKey = null;

        this.jointHash = jointHash;
        this.user = (user == null) ? "N/A" : user;
        this.site = (site == null) ? "N/A": site;
        this.pubKeyData = pubKeyData;
        this.privKeyData = privKeyData;
        this.created = created;

        // Convert byte string from database into actual keys
        try {
            // Create new key factory. Spongy Castle is being used as security provider to ensure
            // all algorithms are supported.
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA",
                    new org.spongycastle.jce.provider.BouncyCastleProvider());
            // Get private key.
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(privKeyData);
            privKey = keyFactory.generatePrivate(encodedKeySpec);
            // Get public key.
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyData);
            pubKey = keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Make keys usable
        if(privKey != null && pubKey != null)
            keyPair = new KeyPair(pubKey, privKey);
    }

    // Generate new private and public key when keys are created or changed. ECDSA keys with the
    // curve "prime256v1" are being used.
    public void generateKeys() {

        // The named curve "prime256v1" (i.e. NIST Curve P-256) is being used for compatibility reasons.
        // It is supposed to be both reasonably fast and secure.
        ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
        KeyPairGenerator keyGen = null;
        try {
            // Using SC for Spongy Castle rather than Bouncy Castle to avoid classloader conflicts.
            keyGen = KeyPairGenerator.getInstance("ECDSA", "SC");
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            keyGen.initialize(ecSpec, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        if(keyGen != null) {
            keyPair = keyGen.generateKeyPair();

            // Make keys ready to be saved in database.
            pubKeyData = keyPair.getPublic().getEncoded();
            privKeyData = keyPair.getPrivate().getEncoded();
        } else {
            Log.d("MeyKeyPair", "keyGen null");
        }
    }

    public String getJointHash() {

        return jointHash;
    }

    public void setJointHash(String jointHash) {

        this.jointHash = jointHash;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    public String getSite() {

        return site;
    }

    public void setSite(String site) {

        this.site = site;
    }

    public byte[] getPubKeyData() {

        return pubKeyData;
    }

    public byte[] getPrivKeyData() {

        return privKeyData;
    }

    public String getCreated() {

        return created;
    }

    public PublicKey getPublicKey() {

        return keyPair.getPublic();
    }

//    //TODO ONLY FOR TESTING
//    public PrivateKey getPrivateKey() {
//
//        return keyPair.getPrivate();
//    }
//    //TODO ONLY FOR TESTING
//    public KeyPair getKeys() {
//        return keyPair;
//    }

    // Sign a plain text (e.g. a nonce). SHA-256 is being used as hash method.
    public byte[] sign(String plain){
        try {
            Signature signature = Signature.getInstance("SHA256withECDSA", "SC");
            signature.initSign(keyPair.getPrivate());
            signature.update(plain.getBytes("UTF-8"));
            return signature.sign();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean verify(byte[] signature, String plain) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA", "SC");
            ecdsaVerify.initVerify(keyPair.getPublic());
            ecdsaVerify.update(plain.getBytes("UTF-8"));
            return ecdsaVerify.verify(signature);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;

    }

    // Convert public key to PEM format for openssl compatibility.
    public String pubToPEM () {
        StringWriter sw = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
        try {
            pemWriter.writeObject(keyPair.getPublic());
            pemWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

//    public String privToPEM () {
//        StringWriter sw = new StringWriter();
//        JcaPEMWriter pemWriter = new JcaPEMWriter(sw);
//        try {
//            pemWriter.writeObject(keyPair.getPrivate());
//            pemWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return sw.toString();
//    }
//
//    public String toPem(KeyPair keyPair){
//        StringWriter stringWriter = new StringWriter();
//        JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter);
//        try {
//            pemWriter.writeObject(keyPair);
//            pemWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return stringWriter.toString();
//    }
//
//    public void fromPEM (byte[] data){
//        try {
//            File tmp = File.createTempFile("tmp", null);
//            FileOutputStream fileOutputStream = new FileOutputStream(tmp);
//            fileOutputStream.write(data);
//
//            PEMParser pemParser = new PEMParser(new FileReader(tmp));
//            Object object = pemParser.readObject();
//            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("SC");
//
//            keyPair = converter.getKeyPair((PEMKeyPair) object);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }

    // Necessary for serialization process
    public static final Parcelable.Creator<MyKeyPair> CREATOR = new Creator<MyKeyPair>() {
    @Override
    public MyKeyPair createFromParcel(Parcel source) {
        return new MyKeyPair(source.readString(), source.readString(), source.readString(),
                source.createByteArray(), source.createByteArray(), source.readString());

    }

    // Necessary for serialization process
     @Override
        public MyKeyPair[] newArray(int size) {

            return new MyKeyPair[size];
        }
    };

    // Necessary for serialization process
    @Override
    public int describeContents() {

        return 0;
    }

    // Necessary for serialization process
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(jointHash);
        dest.writeString(user);
        dest.writeString(site);
        dest.writeByteArray(pubKeyData);
        dest.writeByteArray(privKeyData);
        dest.writeString(created);
    }
}
