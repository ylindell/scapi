package edu.biu.scapi.midLayer;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import edu.biu.scapi.primitives.generators.SecretKeyGeneratorSpi;

/**
 * This utility class generates a SecretKey in a single step.
 * There are two possibilities for creation. One requires the size of the key -a value greater than zero-, and the other does not.
 * Both require the name of the algorithm for which to generate the key and a source of randomness. 
 *   
 * @author Cryptography and Computer Security Research Group Department of Computer Science Bar-Ilan University (Yael Ejgenberg)
 *
 */
public class SecretKeyGeneratorUtil {
	
	/**
	 * This static function generates a SecretKey. It looks for a default provider implementation for the algorithm name requested.
	 * If found then uses it. Otherwise it creates the Key using {@link}SecretKeyGeneratorSpi {@link}.
	 * This function should be used when the key size is known.
	 * It requires a key size greater than zero, otherwise it throws NegativeArraySizeException.
	 * 
	 * @param keySize The size of the requested key in bits.
	 * @param algName The name of the algorithm for which to generate the key.
	 * @param random  The source of randomness to use.
	 * @throws NegativeArraySizeException
	 **/
	static public SecretKey generateKey(int keySize, String algName, SecureRandom random){
		//if the key size is zero or less - throw exception
		if (keySize <= 0){
				throw new NegativeArraySizeException("key size must be greater than 0");
			}
		SecretKey secretKey = null;
		try {
			//Get a default provider KeyGenerator.
			KeyGenerator kGen = KeyGenerator.getInstance(algName);	
			kGen.init(keySize, random);
			secretKey = kGen.generateKey();
		} catch (NoSuchAlgorithmException e) {		
			//Couldn't find default provider implementation-> use SecretKeyGeneratorSpi
			SecretKeyGeneratorSpi keyGen = new SecretKeyGeneratorSpi();
			keyGen.engineInit(keySize, random);
			secretKey = keyGen.engineGenerateKey();
		} 
		return secretKey;

	}
	/**
	 * This static function generates a SecretKey. It looks for a default provider implementation for the algorithm name requested.
	 * If found then uses it. Otherwise it creates the Key using {@link}SecretKeyGeneratorSpi {@link}.
	 * This function is useful if there is a default key size for the requested algorithm, 
	 * and there is a default provider implementation for it. 
	 *
	 * @param algName The name of the algorithm for which to generate the key.
	 * @param random  The source of randomness to use.
	 * @throws NoSuchAlgorithmException  
	 **/
	static public SecretKey generateKey(String algName, SecureRandom random) throws NoSuchAlgorithmException{
		SecretKey secretKey = null;
		KeyGenerator kGen = KeyGenerator.getInstance(algName);
		kGen.init( random); //The generator will use its default size.
		secretKey = kGen.generateKey(); 
		return secretKey;

	}

}