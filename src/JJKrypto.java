import java.io.UnsupportedEncodingException;
import java.security.*;

import javax.crypto.*;
/**
 * Klasa odpowiedzialna za szyfrowanie przesylanych wiadomosci tekstowych.
 * @author Jacek Mucha
 *
 */
public class JJKrypto 
{
	/**
	 * Dlugosc klucza RSA w bitach.
	 */
	private int RSALength = 4096;
	/**
	 * Generator kluczy w algorytmie RSA.
	 */
	private KeyPairGenerator kpg;
	/**
	 * Para klucz publiczny-prywatny biezacego uztytkownika.
	 */
	private KeyPair key;
	/**
	 * Klucz publiczny biezacego uzytkownika.
	 */
	private Key pubKey;
	/**
	 * Klucz prywatny biezacego uzytkownika. Sluzy do deszyfracji odebranych wiadomosci.
	 */
	private Key privKey;
	/**
	 * Klucz publiczny rozmowcy. Sluzy do zaszyfrowywania wiadomosci.
	 */
	private Key pubKeyForEncryption;
	/**
	 * Konstruktor glowny. Tworzy pare kluczy publicznego i prywatnego dla biezacego uzytkownika.
	 * @param RSALength Dlugosc kluczy w bitach.
	 */
	public JJKrypto(int RSALength)
	{
		this.RSALength = RSALength;
		try
		{
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(this.RSALength);
			key = kpg.generateKeyPair();
			pubKey = key.getPublic();
			privKey = key.getPrivate();
		}
		catch(NoSuchAlgorithmException e){System.out.println(e);}
	}
	/**
	 * Metoda odbierajaca i zapisujaca klucz publiczny rozmowcy.
	 * @param receivedKey Odebrany klucz publiczny rozmowcy.
	 */
	public void setEncryptionKey(Key receivedKey)
	{
		pubKeyForEncryption = receivedKey;
	}
	/**
	 * Metoda sluzaca do pobierania klucza publicznego biezacego uzytkownika.
	 * @return Zwraca klucz publiczny biezacego uzytkownika.
	 */
	public Key getPublicKey()
	{
		return pubKey;
	}
	/**
	 * Metoda sluzaca do szyfrowania kluczem publicznym rozmowcy tekstu, ktory ma byc owemu rozmowcy wyslany.
	 * @see pubKeyForEncryption
	 * @param text tekst do zaszyfrowania.
	 * @return Szyfrogram w postaci tablicy bitow.
	 */
	public byte[] encrypt(String text)
	{
		/**
		 * Szyfrogram.
		 */
		byte[] cipherText = null;
		try
		{
			final Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE,pubKeyForEncryption);
			cipherText = cipher.doFinal(text.getBytes());
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		return cipherText;
	}
	/**
	 * Metoda sluzaca do odszyfrowywania odebranej od rozmowcy wiadomosci.
	 * @see privKey
	 * @param text Odebrana zaszyfrowana wiadomosc w postaci tablicy bitow.
	 * @return Odszyfrowana wiadomosc.
	 */
	public String decrypt(byte[] text)
	{
		byte[] decryptedText = null;
		String message = "";
		try
		{
			try
			{
				final Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privKey);
				decryptedText = cipher.doFinal(text);
				message = new String(decryptedText,"UTF-8");

			}
			catch(UnsupportedEncodingException e)
			{
				message = new String(decryptedText);
			}
		}
		catch(Exception e2){ System.out.println(e2);}
		return message;
	}
}
