/*orders:
1 do serwera: wyslij liste kontaktow
2 do serwera: ustaw mnie zalogowanym
3 do serwera: dostarcz wiadomoœæ
4 do klienta: lista uzytkownikow cd
5 do klienta: lista uzytkownikow koniec
6 do klienta: potwierdzenie zalogowania
7 do serwera: ciagle jestem
8 do klienta: przesylam liste zalogowanych
9 do klienta: przesylam status i-tego usera
10 do klienta: koniec przesylania statusow dostepnosci
11 do serwera: ja wzywam adresata do rozmowy
12 do adresata: wezwanie do rozmowy, potwierdz gotowosc
13 do serwera: potwierdzam gotowosc
14 do nadawcy: potwierdzam gotowosc adresata
15 do serwera: wyloguj
16 do serwera: wiadomosc tekstowa
17 do serwera/nadawcy: potwierdzenie odbioru
 */
import java.security.Key;
import java.io.Serializable;
/**
 * @author Jacek Mucha
 * Klasa, ktorej obiekty reprezentuja wiadomosci przesylane miedzy uzytkownikiem, a serwerem. Typ wiadomosci okresla zmienna order.
 * @see order
 * @see JJDialog
 * @see JJPerson
 * @see JJServer
 */
class JJMessage implements Serializable
{
	/** Okresla typ przesylanej wiadomosci. (S- do serwera, K - do klienta, N - do nadawcy, A - do adresata)
	 *  1 A wyslij liste kontaktow 
	 *  2 S ustaw mnie zalogowanym
	 *  3 S dostarcz wiadomosc
	 *  4 K kontynuuje przesylanie listy uzytkownikow
	 *  5 K koniec listy uzytkownikow
	 *  6 K potwierdzenie zalogowania
	 *  7 S ciagle jestem zalogowany
	 *  8 K aktualizuje liste zalogowanych uzytkownikow
	 *  9 K przesylam status dostepnosci i-tego usera
	 *  10 K koniec przesylania statusow dostepnosci
	 *  11 S wywoluje adresata do rozmowy
	 *  12 A wywoluje cie do rozmowy, potwierdz gotowosc
	 *  13 S potwierdzam gotowosc do rozpoczecia dialogu
	 *  14 N potwierdzam gotowosc adresata do rozpoczecia dialogu
	 *  15 S wyloguj mnie
	 *  16 S wiadomosc tekstowa
	 *  17 S/N potwierdzenie odbioru wiadomosci tekstowej
	 *  18 S/A przeslanie klucza publicznego
	 *  19 S/N przesylanie klucza publicznego
	 *  20 S/A koniec rozmowy
	 *  @see JJTextMessage
	 */
	private int order = -1;
	public static final long serialVersionUID = 42L;
	/**
	 * Klucz do przeslania. Na poczatku rozmowy uzytkownicy wymieniaja miedzy soba swoje publiczne klucze RSA.
	 */
	private Key key;
	/**
	 * Szybka, niesformatowana wiadomosc tekstowa. Sluzy na przyklad do przesylania loginow uzytkownikow.
	 */
	private String message = "";
	/**
	 * Login adresata wiadomosci.
	 */
	private String adress = "";
	/**
	 * Login nadawcy wiadomosci.
	 */
	private String caller = "";
	/**
	 * Przelacznik informujacy o tym, czy biezaca wiadomosc niesie ze soba zaszyfrowana tresc.
	 */
	public boolean isEncrypted;
	/**
	 * Numer rozmowy w wektorze rozmow u pierwszego uzytkownika (rozpoczynajacego rozmowe).
	 */
	public int id1;
	/**
	 * Numer rozmowy w wektorze rozmow u drugiego uzytkownika (wywolanego).
	 */
	public int id2;
	/**
	 * Obiekt niosacy w sobie sformatowana wiadomosc tekstowa.
	 */
	public JJTextMessage textMessageObject;
	/**
	 * Obiekt niosacy zaszyfrowana sformatowana wiadomosc tekstowa.
	 */
	public JJEncryptedTextMessage encryptedTextMessageObject;
	/**
	 * Metoda sluzaca do pobierania loginu adresata.
	 * @return Login adresata.
	 */
	public String getAdress()
	{
		return adress;
	}
	/**
	 * Metoda sluzaca do ustawiania informacji o indeksie w wektorze rozmow u wywolujacego rozmowe.
	 * @param id Indeks w wektorze rozmow u wywolujacego rozmowe.
	 */
	public void setId1(int id)
	{
		this.id1 = id;
	}
	/**
	 * Metoda sluzaca do ustawiania informacji o indeksie w wektorze rozmow u wywolanego rozmowcy.
	 * @param id Indeks w wektorze rozmow u wywolanego rozmowcy.
	 */
	public void setId2(int id)
	{
		this.id2 = id;
	}
	/**
	 * Metoda odczytujaca tresc rozkazu do wykonania.
	 * @return Polecenie, ktore niesie ze soba wiadomosc.
	 */
	public int getOrder()
	{
		return order;
	}
	/**
	 * Metoda pobierajaca tresc krotkiej wiadomosci.
	 * @return Krotka wiadomosc tekstowa.
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * Metoda zwracajaca login wywolujacego rozmowe.
	 * @return Login wywolujacego rozmowe.
	 */
	public String getCaller()
	{
		return caller;
	}
	/**
	 * Najprostszy konstruktor uzywany, gdy wiadomosc niesie ze soba tylko rozkaz.
	 * @param order Polecenie do przekazania klientowi/serwerowi.
	 */
	public JJMessage(int order) //kiedy wystarczy wskazac operacje do wykonania
	{
		this.order = order;
	}
	/**
	 * Konstruktor uzywany do przekazania, poza rozkazem, takze pewnych krotkich informacji tekstowych.
	 * @param order Polecenie do wykonania.
	 * @param message Informacja o poleceniu.
	 */
	public JJMessage(int order, String message)
	{
		this.order = order;
		this.message = message;
	}
	/**
	 * Konstruktor wiadomosci posredniej, kiedy nie wystarczy tylko przekazac polecenia serwerowi, lecz takze wskazac, gdzie nalezy przeslac wiadomosc dalej.
	 * @param order Polecenie do wykonania.
	 * @param message Informacja o poleceniu.
	 * @param adress Adresat, ktoremu nalezy przeslac dalej wiadomosc.
	 */
	public JJMessage(int order, String message, String adress) //kiedy trzeba wskazaæ adresata
	{
		this.order = order;
		this.message = message;
		this.adress = adress;
	}
	/**
	 * Konstruktor wiadomosci posredniej, kiedy nie wystarczy tylko przekazac polecenia serwerowi, lecz takze wskazac, gdzie nalezy przeslac wiadomosc dalej i informacje o nadawcy.
	 * @param order Polecenie do wykonania.
	 * @param message Informacja o poleceniu.
	 * @param adress Adresat, ktoremu nalezy przeslac dalej wiadomosc.
	 * @param caller Login nadawcy.
	 */
	public JJMessage(int order, String message, String adress, String caller) //kiedy trzeba wskazaæ adresata
	{
		this.order = order;
		this.message = message;
		this.adress = adress;
		this.caller = caller;
	}
	/**
	 * Konstruktor uzywany przy przesylaniu kluczy publicznych.
	 * @param order Polecenie do wykonania.
	 * @param key Przesylany klucz publiczny.
	 * @param adress Adresat klucza.
	 * @param caller Nadawca klucza.
	 */
	public JJMessage(int order, Key key,String adress, String caller)
	{
		this.order = order;
		this.key = key;
		this.adress = adress;
		this.caller = caller;
	}
	/**
	 * Metoda sluzaca do pobierania klucza z wiadomosci
	 * @return Klucz publiczny niesiony w wiadomosci.
	 */
	public Key getKey()
	{
		return key;
	}
}
/**
 * Klasa, ktorej obiekty sluza do przenoszenia specyficznych typow wiadomosci, a mianowicie sformatowanych wiadomosci tekstowych.
 * @author Jacek Mucha
 *
 */
class JJTextMessage implements Serializable
{
	/**
	 * Przesylana wiadomosc tekstowa.
	 */
	public String textMessage;
	public static final long serialVersionUID = 42L;
	/**
	 * Wartosc parametru Red przy kodowaniu koloru RGB
	 */
	public int red; 
	/**
	 * Wartosc parametru Green przy kodowaniu koloru RGB
	 */
	public int green;
	/**
	 * Wartosc parametru Blue przy kodowaniu koloru RGB
	 */
	public int blue;
	/**
	 * Wielkosc czcionki
	 */
	public int size;
	/** 
	 * Przelacznik okreslajacy, czy czcionka ma byc pogrubiona
	 */
	public boolean b;
	/**
	 * Przelacznik okreslajacy, czy wiadomosc ma byc pisana kursywa
	 */
	public boolean i;
	/**
	 * Przelacznik okreslajacy, czy tekst wiadomosci ma byc podkreslony.
	 */
	public boolean u;
	/**
	 * Przelacznik okreslajacy,czy wiadomosc jest pisana w skladni latex'a.
	 */
	public boolean latex;
	/**
	 * Konstruktor glowny, niosacy informacje o formatowaniu tekstu.
	 * @param textMessage Tresc przesylana.
	 * @param red Wartosc czerwieni w kodowaniu RGB.
	 * @param green Wartosc zieleni w kodowaniu RGB.
	 * @param blue Wartosc blekitu w kodowaniu RGB.
	 * @param size Rozmiar czcionki.
	 * @param b Pogrubienie.
	 * @param i Kursywa.
	 * @param u Podreslenie.
	 * @param latex Wiadomosc bedzie odczytywana jak wzor w latex'u.
	 */
	public JJTextMessage(String textMessage, int red, int green, int blue, int size, boolean b, boolean i, boolean u, boolean latex)
	{
		this.textMessage = textMessage;
	}
}
class JJEncryptedTextMessage implements Serializable
{
	/**
	 * Tresc zaszyfrowanej wiadomosci.
	 */
	public byte[] encryptedTextMessage;
	public static final long serialVersionUID = 42L;
	/**
	 * Wartosc parametru Red przy kodowaniu koloru RGB
	 */
	public int red; 
	/**
	 * Wartosc parametru Green przy kodowaniu koloru RGB
	 */
	public int green;
	/**
	 * Wartosc parametru Blue przy kodowaniu koloru RGB
	 */
	public int blue;
	/**
	 * Wielkosc czcionki
	 */
	public int size;
	/** 
	 * Przelacznik okreslajacy, czy czcionka ma byc pogrubiona
	 */
	public boolean b;
	/**
	 * Przelacznik okreslajacy, czy wiadomosc ma byc pisana kursywa
	 */
	public boolean i;
	/**
	 * Przelacznik okreslajacy, czy tekst wiadomosci ma byc podkreslony.
	 */
	public boolean u;
	/**
	 * Przelacznik okreslajacy,czy wiadomosc jest pisana w skladni latex'a.
	 */
	public boolean latex;
	/**
	 * Konstruktor glowny, niosacy informacje o formatowaniu tekstu.
	 * @param textMessage Tresc przesylana.
	 * @param red Wartosc czerwieni w kodowaniu RGB.
	 * @param green Wartosc zieleni w kodowaniu RGB.
	 * @param blue Wartosc blekitu w kodowaniu RGB.
	 * @param size Rozmiar czcionki.
	 * @param b Pogrubienie.
	 * @param i Kursywa.
	 * @param u Podreslenie.
	 * @param latex Wiadomosc bedzie odczytywana jak wzor w latex'u.
	 */
	public JJEncryptedTextMessage(byte[] textMessage, int red, int green, int blue, int size, boolean b, boolean i, boolean u, boolean latex)
	{
		this.encryptedTextMessage = textMessage;
	}
}