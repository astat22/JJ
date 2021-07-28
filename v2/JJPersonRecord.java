/**
 * Struktura danych sluzaca do sprawnego dostepu do informacji o klientach na serwerze.
 * @author Jacek Mucha
 *
 */
class JJPersonRecord
{
	/**
	 * Login klienta.
	 */
	private final String nomen;
	/**
	 * Status zalogowania klienta.
	 */
	private boolean ability = false;
	/**
	 * Konstruktor glowny informacji o kliencie.
	 * @param nomen Login klienta.
	 */
	public JJPersonRecord(String nomen)
	{
		this.nomen = nomen;
	}
	/**
	 * Metoda sluzaca do pobierania loginu klienta.
	 * @return Login klienta.
	 */
	public String getNomen()
	{
		return nomen;
	}
	/**
	 * Metoda sluzaca do sprawdzania statusu zalogowania biezacego klienta.
	 * @return Status dostepnosci klienta.
	 */
	public boolean getAbility()
	{
		return ability;
	}
	/**
	 * Metoda sluzaca do ustawiania statusu dostepnosci biezacego klienta.
	 * @param ability Nowy status dostepnosci.
	 */
	public void setAbility(boolean ability)
	{
		this.ability = ability;
	}
}
/**
 * Struktura danych sluzaca do szybkiego dostepu do informacji o prowadzownych rozmowach.
 * @author Jacek Mucha
 *
 */
class JJDialogRecord
{
	/**
	 * Login rozpoczynajacego dialog.
	 */
	public String sender;
	/**
	 * Login osoby, z ktora rozpoczeto dialog.
	 */
	public String receiver;
	/**
	 * Status aktywnosci rozmowy. Gdy zostala zakonczona, przyjmuje wartosc false.
	 */
	public boolean isActive = false;
	/**
	 * Konstruktor glowny, uaktywniajacy rozmowe.
	 * @param sender Login rozpoczynajacego dialog.
	 * @param receiver Login osoby, z ktora rozpoczeto dialog.
	 */
	public JJDialogRecord(String sender, String receiver)
	{
		this.sender = sender;
		this.receiver = receiver;
		isActive = true;
	}
}