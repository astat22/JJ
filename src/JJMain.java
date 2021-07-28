
//Jacek Mucha
//rozpoczecie 6 grudnia 2013
//ostatnia aktualizacja 6 grudnia 2013
import javax.swing.*; 
/**
 * @author Jacek Mucha
 *  Klasa glowna odpowiedzialna za logowanie.
 */
public class JJMain
{
	/**  Indywidualne haslo dla tego uzytkownika.*/
	private final String password = "123";
	/** Indywidualna zmienna okreslajaca, czy uzytkownik jest administratorem serwera. */
	private static final boolean isAdmin = true;
	/** Obiekt z GUI do utworzenia po wpisaniu poprawnego hasla. */
	private JJPerson me;
	/** Adres serwera przekazywany do JJPerson. */
	public static String server = "localhost";
	/** Bezargumentowy konstruktor glowny. Jego zadaniem jest wyswietlenie okna dialogowego do wpisania hasla i adresu serwera. */
	public JJMain()
	{
		/** Pole tekstu, ktory wprowadzil uzytkownik jako haslo. */
		String checkPass="";
		checkPass = JOptionPane.showInputDialog("Podaj has³o","");
		while(!checkPass.equals(password))
		{
			    checkPass = JOptionPane.showInputDialog("Z³e has³o. Wpisz ponownie.","");
		}
		server = JOptionPane.showInputDialog("Podaj adres serwera",server);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				me = new JJPerson(server,isAdmin);
				//while(true)
				    me.repaint();
			}
		});
	}
	/** Metoda uruchamiajaca program. Wywoluje konstruktor JJMain. */
	public static void main(String[] args)
	{
		/*if(isAdmin)
	    server = new JJServer();*/
		//JJMain main = new JJMain();
		new JJMain();
	}
}