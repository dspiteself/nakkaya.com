import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.SealedObject;
import javax.crypto.Cipher;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

class seal {

    //name of the algorithm to use
    static String algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC";

    static char[] passPherase = "secretpass".toCharArray();
    static byte[] salt = "a9v5n38s".getBytes();
    static String secretData = "Very Secret Data!!";

    static SealedObject encrypt(String data) throws Exception{
	PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt,20);
	PBEKeySpec pbeKeySpec = new PBEKeySpec(passPherase);
	SecretKeyFactory secretKeyFactory = 
	    SecretKeyFactory.getInstance(algorithm);
	SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

	Cipher cipher = Cipher.getInstance(algorithm);
	cipher.init(Cipher.ENCRYPT_MODE,secretKey,pbeParamSpec);

	return new SealedObject(data,cipher);
    }

    static String decrypt(SealedObject sealedObject) throws Exception{
	PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt,20);
	PBEKeySpec pbeKeySpec = new PBEKeySpec(passPherase);
	SecretKeyFactory secretKeyFactory = 
	    SecretKeyFactory.getInstance(algorithm);
	SecretKey secretKey = secretKeyFactory.generateSecret(pbeKeySpec);

	Cipher cipher = Cipher.getInstance(algorithm);
	cipher.init(Cipher.DECRYPT_MODE,secretKey,pbeParamSpec);
	return (String)sealedObject.getObject(cipher);
    }

    public static void main(String[] args) {
	try{
	    Security.addProvider(new BouncyCastleProvider());

	    SealedObject encryptedString = encrypt(secretData);
	    
	    String decryptedString = decrypt(encryptedString);

	    System.out.println(decryptedString);

	}catch( Exception e ) { 
	    System.out.println(e.toString());
	}
    }
}
