---
title: Password Based Encryption Using AES
layout: post
tags: java encryption
---

In a recent engagement, i needed to encrypt an object then write it to
disk. I wanted to use [Advanced Encryption
Standard(AES)](http://en.wikipedia.org/wiki/Advanced_Encryption_Standard), 
but unfortunately Java does not provide a way to use password based
encryption with AES. 

[Bouncy Castle](http://www.bouncycastle.org/) does provide the required
libraries that will allow us to use password based encryption with AES.


In this scenario, i wanted the user to provide the pass phrase.

    //name of the algorithm to use
    static String algorithm = "PBEWITHSHA256AND128BITAES-CBC-BC";

    static char[] passPherase = "secretpass".toCharArray();
    static byte[] salt = "a9v5n38s".getBytes();
    static String secretData = "Very Secret Data!!";

In real world you should generate the salt randomly. We need to create a
key spec based on the user input then create a cipher for encrypting or
decrypting the data.

#### Encrypting the data

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

#### Decrypting the data back again

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

Using a SealedObject you can basically encrypt any object you want, and
serialize it to a file.

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

Download [code](/code/java/seal.java)
