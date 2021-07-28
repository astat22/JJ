/**
 * @author Jacek Mucha
 * @since 6 grudnia 2013
@date 6 stycznia 2014
 * @version 0.1
 * Klasa obslugujaca archiwum komunikatora JJ.
 */
import java.io.*;
/**
 * Klasa sluzaca do archiwizowania przeprowadzonych rozmow.
 * @author Jacek Mucha
 *
 */
class JJArchive
{
	/**
	 * Plik, do ktorego zapisana bedzie biezaca rozmowa.
	 */
	private File outputFile;
	/**
	 * Plik, w ktorym przechowywane sa ustawienia archiwum.
	 */
	private File settingsFile;
	/**
	 * Nazwa pliku z biezaca rozmowa.
	 */
	private String fileName;
	/**
	 * Obiekt dostepu do pliku z ustawieniami.
	 */
	private RandomAccessFile rafSettings;
	/**
	 * Obiekt dostepu do pliku z biezaca rozmowa.
	 */
	private RandomAccessFile rafOutput;
	/**
	 * Liczba przeprowadzonych rozmow. Odczytywana z  settingsFile
	 * @see settingsFile
	 */
	private int archiveId = -1;
	/**
	 * Konstruktor glowny. Odczytuje ustawienia archiwum i tworzy plik z nowa rozmowa.
	 * @param fileName Nazwa nowego pliku.
	 */
	public JJArchive(String fileName)
	{
		System.out.println("Tworzenie archiwum rozmowy z "+fileName+"...");
		this.fileName = fileName+".txt";
		settingsFile = new File("settings.txt");
		try{ rafSettings = new RandomAccessFile(settingsFile,"rw");} catch(FileNotFoundException e){} //otwieranie pliku z ustawieniami
		try
		{ 
			archiveId = Integer.parseInt(rafSettings.readLine());
			rafSettings.close();
			increaseArchiveId(archiveId);
		} 
		catch(IOException e){}//pobieranie ustawien
		this.fileName += archiveId;
		outputFile = new File(this.fileName);
		try
		{
			rafOutput = new RandomAccessFile(outputFile,"rw");
		}
		catch(FileNotFoundException e){ System.out.println(e);}
	}
	/**
	 * Metoda modyfikujaca plik z ustawieniami archiwum. Zwieksza liczbe wykonanych rozmow o 1.
	 * @param archiveId Liczba wykonanych dotad polaczen.
	 */
	public void increaseArchiveId(int archiveId)
	{
		settingsFile = new File("settings.txt");
		if(settingsFile.delete())
		{
			System.out.println("Plik z ustawieniami archiwum zostal wyczyszczony...");
		}
		settingsFile = new File("settings.txt");
		try{ rafSettings = new RandomAccessFile(settingsFile,"rw");} catch(FileNotFoundException e){}
		try
		{
			rafSettings.writeBytes(Integer.toString(archiveId+1)); 
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
		try{ rafSettings.close(); } catch(IOException e){}
	}
	/**
	 * Metoda sluzaca do zapisania wiadomosci w archiwum
	 * @param message wiadomosc do zapisania
	 * @since 6 stycznia 2014
	 */
	public void saveMessage(String message)
	{
		try
		{
			rafOutput.writeBytes(message+"\n"); 
		}
		catch(IOException e)
		{
			System.out.println(e);
		}
	}
	/**
	 * Metoda wywolywana przy zakonczeniu rozmowy. Konczy prace archiwum, zamykajac wszystkie strumienie.
	 * @since 6 stycznia 2014
	 * @date 11 stycznia 2014
	 */
	public void closeArchive()
	{
		try
		{
			rafOutput.close();
		}
		catch(IOException e){ System.out.println(e);}
	}
}