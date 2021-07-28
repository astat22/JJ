
//Jacek Mucha
//data rozpoczecia 6 grudnia 2013
//ostatnia aktualizacja 18 stycznia 2014
//wersja 1.1
import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.awt.*;
/**Spersonalizowana klasa odpowiedzialna za nawiazywanie polaczenia z serwerem, otwieranie okna rozmow itp.*/
class JJPerson extends JFrame implements WindowListener
{
	/**
	 * Bardziej elegancki sposob wyswietlania wiadomosci w konsoli. 
	 */
	PrintWriter outp = null;
	/**
	 * W celu zapewnienia kompatybilnosci deserializacji.
	 */
	public static final long serialVersionUID = 42L;
	/** Indywidualne imie uzytkownika */
	private String nomen = "Kasia";
	/** Indywidualne nazwisko uzytkownika */
	private String cognomen = "Dêbowa";
	/** Status dostepnosci uzytkownika */
	private boolean ability = false;
	/** Przelacznik informujacy o tym, czy dany uzytkownik jest administratorem. Administrator moze zarzadzac serwerem.*/
	private final boolean isAdmin = true;
	/** Zmienna kontrolujaca zdarzenia w niektorych watkach.
	 *@see #iAm()
	 */
	public static int time = 0;
	/** Dlugosc klucza RSA */
	protected static int RSALength = 512;

	/** Gniazdko do polaczenia z serwerem */
	private Socket socket;
	/** Numer portu do polaczenia z serwerem */
	private int port;
	/** Adres serwera */
	private String server;

	/** Strumien obiektow wysylanych do serwera. W programie JJ wysyla sie glownie obiekty klasy JJMessage.
	 *@see JJMessage
	 */
	public ObjectOutputStream sOutput;
	/** Strumien wejscia obiektow odbieranych z serwera. W programie JJ sa to glownie obiekty typu JJMessage. 
	 *@see JJMessage
	 */
	public ObjectInputStream sInput;
	/** Obiekt odpowiedzialny za nasluchiwanie serwera. */
	public ListenFromServer lst;
/**
 * Panel, na ktorym umieszczone sa buttony z nazwiskami uzytkownikow pobranymi z serwera.
 */
	public JPanel guiPanel;
	/**
	 * Panel, na ktorym wyswietlane sa statusy dostepnosci uzytkownikow.
	 */
	public JPanel statusPanel;
	/** Wektor z nazwami uzytkownikow znajdujacych sie na buttonach
	 *@see contactListButtons
	 */
	public static Vector<String> contactListButtonsNames;
	/** Wektor z przelacznikami okreslajacymi dostepnosc uzytkownikow */
	public static Vector<Boolean> contactListAbility;
	/** Wektor przyciskow rozpoczynajacych rozmowe z danym uzytkownikiem. 
	 * @see #createGUI() 
	 */
	public static Vector<JButton> contactListButtons;
	/**
	 * Glowny pasek menu.
	 */
	private JMenuBar mb;
	/**
	 * Pierwsze menu z najwazniejszymi opcjami, jak ustawianie dlugosci klucza RSA.
	 */
	private JMenu opcje;
	private JMenuItem menuRSA;
	private JMenuItem adminItem;
	/** Wektor prowadzonych rozmow.
	 * @see JJDialog
	 */
	public static Vector<JJDialog> allDialogs;
	/** Watek kontrolujacy czas wykonania programu.
	 * @see time
	 * @see #startReporting() 
	 */
	public static Thread ProcessIDentifier;
	/**
	 * Obiekt odpowiedzialny za wywolywanie akcji przez buttony.
	 * @see #call(String)
	 */
	public ActionListener buttonListener;
public ActionListener menuListener;
	/**
	 * Glowny konstruktor odpowiedzialny za stworzenie interfejsu graficznego i polaczanie z serwerem
	 * @param server adres serwera przekazany w klasie JJMain
	 */
	public JJPerson(String server, boolean isAdmin)
	{
		guiPanel = new JPanel();
		try
		{
			outp = new PrintWriter(new OutputStreamWriter(System.out,"Cp852"),true);
		}
		catch(UnsupportedEncodingException e)
		{ 
			System.out.println(e);
			outp = new PrintWriter(new OutputStreamWriter(System.out),true);
		}
		statusPanel = new JPanel()
		{
			public static final long serialVersionUID = 42L;
			/**
			 * Metoda kolorujaca kolka oznaczajace statusy dostepnosci uzytkownikow.
			 */
				public void paint(Graphics g)
				{
					//g.clearRect(0, 0, getSize().width,  getSize().height);
					for(int i=0; i<contactListAbility.size(); i++)
					{
						if(contactListAbility.get(i))
							g.setColor(Color.GREEN);
						else
							g.setColor(Color.RED);
						g.fillOval(5,25+40*i,10,10);
					}
				}
		};
		statusPanel.setBounds(245,20,25,530);
		statusPanel.setLayout(null);
		add(statusPanel);
		this.server = server;
		contactListButtonsNames = new Vector<String>();
		contactListAbility = new Vector<Boolean>();
		contactListButtons = new Vector<JButton>();
		allDialogs = new Vector<JJDialog>();
		menuListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				if(ev.getSource()==menuRSA)
					RSALength = Integer.parseInt(JOptionPane.showInputDialog("Podaj d³ugoœæ w bitach klucza RSA",RSALength));
			}
		};
		mb = new JMenuBar();
		opcje = new JMenu("Opcje");
		menuRSA = new JMenuItem("D³ugoœæ klucza RSA");
		if(isAdmin) 
		{
			adminItem = new JMenuItem("Panel administracyjny");
			opcje.add(adminItem);
			adminItem.addActionListener(menuListener);
		}
		opcje.add(menuRSA);
		menuRSA.addActionListener(menuListener);
		mb.add(opcje);
		setJMenuBar(mb);
		addWindowListener(this);
		guiPanel.setLayout(null);
		guiPanel.setBounds(0,20,245,530);
		getContentPane().add(guiPanel);
		setSize(270,550);
		setLayout(null);
		setResizable(false);
		setTitle("JJ "+nomen+" "+cognomen);
		//setVisible(true);
		boolean loginControl = start();
		outp.println("Wystartowa³em...");
		if(!loginControl)
		{
			System.out.println("Niepowodzenie logowania.");
			dispose();
		}
	}
	/**
	 * Metoda zwracajaca imie uzytkownika.
	 * @return Zwraca login uzytkownika.
	 */
	public String getNomen()
	{
		return nomen;
	}
	/**
	 * Po zalogowaniu wywolywana jest metoda startReporting(), ktora tworzy watek informujacy serwer co jakis czas, ze uzytkownik wciaz jest zalogowany.
	 * @see ProcessIDentifier
	 * @see time
	 * @see #iAm()
	 */
	public void startReporting()
	{   
		ProcessIDentifier = new Thread()
		{
			public void run()
			{
				while(true)
					try
				{
						//if(time%6==0)
						iAm();	//co 30 sekund poinformuj, ze jestes zalogowany. jednak lepiej co 5 sekund
						time++;
						sleep(5000);
				} catch(InterruptedException e){ System.out.println(e); }	
			}
		};
		ProcessIDentifier.start();
	}
	/**
	 * Metoda informujaca serwer o dostepnosci uzytkownika. Wysyla JJMessage z order=7.
	 * @see JJMessage
	 */
	public void iAm()
	{
		//System.out.println("Informuje serwer, ze ciagle jestem zalogowany...");
		try { sOutput.writeObject(new JJMessage(7)); } catch(IOException e){ System.out.println(e); }
	}
	/**
	 * Metoda zwracajaca nazwisko zalogowanego uzytkownika.
	 * @return zwraca nazwisko zalogowanego uzytkownika
	 */
	public String getCognomen()
	{
		return cognomen;
	}
	/**
	 * Metoda zwracajaca status dostepnosci uzytkownika.
	 * @return przelacznik informujacy o tym, czy uzytkownik jest dostepny.
	 */
	public boolean isLogged()
	{
		return ability;
	}
	/**
	 * Metoda ustawiajaca przelacznik zalogowania uzytkownika.
	 * @param ability nowy status dostepnosci
	 */
	public void setAbility(boolean ability)
	{
		this.ability = ability;
	}
	/**
	 * Metoda sluzaca do sprawdzania, czy biezacy uzytkownik jest administratorem serwera.
	 * @return "czy jestem aministratorem?"
	 */
	public boolean isAdmin()
	{
		return isAdmin;
	}
	/**
	 * Metoda zwracajaca status dostepnosci danego uzytkownika
	 * @param name imie poszukiwanego
	 * @return status poszukiwanego
	 */
	public boolean findUserAbility(String name)
	{
		/**
		 * Przelacznik kontrolujacy wykonywanie sie petli.
		 */
		boolean control = true;
		/**
		 * Indeks poszukiwanego uzytkownika.
		 */
		int gotIt = -1;
		/**
		 * Iterator petli.
		 */
		int userNumber;
		for(userNumber = 0; userNumber<contactListButtonsNames.size() && control;userNumber++)
		{
			if(name.equals(contactListButtonsNames.get(userNumber)))
			{
				control = false;
				gotIt = userNumber;
			}
		}
		try
		{
			return contactListAbility.get(gotIt);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{ 
			return false; 
		}
	}
	/**
	 * Metoda sluzaca do wysylania wiadomosci. Wiadomosc to obiekt klasy JJMessage.
	 * @see JJMessage
	 * @see sOutput
	 * @param m wiadomosc do wyslania
	 */
	public void outputMessage(JJMessage m)
	{
		try 
		{ 
			sOutput.writeObject(m); 
		} 
		catch(IOException exception)
		{ 
			System.out.println("Niepowodzenie przy wysylaniu wiadomosci:\n"+m.textMessageObject.textMessage);
			System.out.println(exception);
		}
	}
/**
 * Metoda sluzaca do nawiazywania polaczenia z serwerem.
 * @see JJServer
 * @see sInput
 * @see sOutput
 * @see port
 * @see server
 * @see socket
 * @see lst
 * @return Zwraca informacje o powodzeniu nawiazania polaczenia z serwerem.
 */
	public boolean start()
	{
		//if(isAdmin())
		//    server = "localhost";
		port = 29500;
		System.out.println("£¹czê z "+server+" port "+port);
		try
		{
			socket = new Socket(server, port);
			System.out.println("Gniazdko otworzone...");
		}
		catch(Exception e)
		{
			outp.println("B³¹d przy otwieraniu gniazdka.");	    
			return false;
		}
		try
		{
			sInput = new ObjectInputStream(socket.getInputStream());
			outp.println("Strumieñ wejœcia ustanowiony...");
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			outp.println("Strumieñ wyjœcia ustanowiony...");
		}
		catch(IOException e)
		{
			outp.println("B³¹d przy otwieraniu strumieni.");
			return false;
		}
		lst = new ListenFromServer();
		lst.start();
		outp.println("Nas³uchujê...");
		try
		{
			sOutput.writeObject(nomen+" "+cognomen);
			outp.println("Wysy³am dane do zalogowania...");
		}
		catch(IOException e)
		{
			outp.println("B³¹d przy logowaniu.");
			return false;
		}
		return true;
	}
/**
 * Metoda sluzaca do ustawiania widocznosci glownego okna i wykonywania innych, podrzednych czynnosci z tym zwiazanych.
 * @param visible czy okno ma byc widoczne?
 */
	public void setVisibleMainWindow(boolean visible)
	{
		this.setVisible(visible);
	}
/**
 * Metoda sluzaca do odswiezania statusow dostepnosci w glownym interfejsie graficznym uzytkownika
 */
	public void refreshAbilities()
	{
		statusPanel.repaint();
		guiPanel.repaint();
	}

/**
 * Metoda zwracajaca numer dialogu z wektora allDialogs, wybieranie po id dialogu.
 * @see allDialogs
 * @param id kazdy dialog ma swoje id, ktore niekoniecznie zgadza sie z pozycja w wektorze
 * @return indeks w wektorze allDialogs dialogu z id tkim samym, jak wartosc przekazanego parametru.
 */
	public int getDialog(int id)
	{
		boolean gotIt = false;
		int itIs = 0;
		for(int current = 0; current < allDialogs.size() && !gotIt; current++)
			if(allDialogs.get(current).getId()==id)
			{
				gotIt = true;
				itIs = current;
			}
		return itIs;
	}
/**
 * Metoda wywolujaca do rozmowy adresata callThisPerson
 * @param callThisPerson adresat rozmowy
 * @see sOutput
 */
	public synchronized void call(String callThisPerson)
	{
		System.out.println("Dzwonie do: "+callThisPerson);
		try
		{ 
			/**
			 * Wiadomosc "dzwonek" mowiaca serwerowi, kogo ma wywolac do rozmowy.
			 */
			JJMessage callMessage = new JJMessage(11,nomen+" "+cognomen, callThisPerson);
			callMessage.id1 = lst.getDialogId();
			sOutput.writeObject(callMessage); 
			lst.dialogId++;
		} 
		catch(IOException e)
		{ 
			System.out.println(e);
		}
		lst.callNow(true);
	}
	/**
	 * Metoda sluzaca do umieszczenia w interfejsie graficznym przyciskow z loginami uzytkownikow i obsluge zdarzen przez nie wywolywanych.
	 */
	public void createGUI()
	{
		buttonListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				int currentButton = 0;
				for(;currentButton<contactListButtons.size() && !(event.getSource()==contactListButtons.get(currentButton)); currentButton++){}
				if(contactListAbility.get(currentButton))
				{
					System.out.println("Wybrano przycisk: "+contactListButtonsNames.get(currentButton));
					String tempUsername1 = contactListButtonsNames.get(currentButton), tempUsername2 = nomen+" "+cognomen;
					if(!tempUsername1.equals(tempUsername2))
						call(contactListButtonsNames.get(currentButton));
					else
					{
						System.out.println("Chcesz porozmawiaæ sam ze sob¹? To urocze!");
						JOptionPane.showMessageDialog(null,"Chcesz rozmawiaæ sam ze sob¹? To urocze!","Schizofrenic error",JOptionPane.ERROR_MESSAGE);

					}
					
				}
				else
				{
					System.out.println("Dialog z osoba "+contactListButtonsNames.get(currentButton)+" nie jest mozliwy.");
					//JDialog notAvailible;
					JOptionPane.showMessageDialog(null,"Dialog z osoba "+contactListButtonsNames.get(currentButton)+" nie jest mozliwy.","U¿ytkownik niedostêpny",JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		for(int buttonNumber = 0; buttonNumber< contactListButtonsNames.size(); buttonNumber++)
		{
			contactListButtons.add(new JButton(contactListButtonsNames.get(buttonNumber)));
			contactListButtons.get(buttonNumber).addActionListener(buttonListener);
			contactListButtons.get(buttonNumber).setBounds(30,10+40*buttonNumber,200,40);
			guiPanel.add(contactListButtons.get(buttonNumber));
			//contactListButtons.get(buttonNumber).setVisible(true);
			//add(contactListButtons.get(buttonNumber));
		}
		setVisible(true);
		guiPanel.repaint();
		outp.println("\nPomyœlnie utworzono GUI...");
	}
/**
 * Nadpisanie metody zamykajacej okno w celu zabicia aktywnych watkow i wylogowania z serwera.
 */
	public void windowClosing(WindowEvent event)
	{
		//poinformuj serwer
		setAbility(false);
		outp.println("Wylogowywanie...");
		//lst.stop();
		try
		{
			sOutput.writeObject( new JJMessage(15));
		} 
		catch(IOException e){ System.out.println(e);}
		outp.println("Zamykanie okien z rozmowami...");
		lst.closeAllDialogs();
		outp.println("Zatrzymywanie watkow...");
		lst.stopThread();
		outp.println("Zamykanie strumieni...");
		try
		{
			sOutput.close();
			sInput.close();
		}
		catch(IOException e3)
		{
			System.out.println(e3);
		}
		outp.println("Zamykanie gniazdka...");
		try
		{
			socket.close();
		}
		catch(IOException e2)
		{
			System.out.println(e2);
		}
		outp.println("Czyszczenie wektora rozmow...");
		allDialogs.clear();
		this.setVisible(false);
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		java.awt.Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING));
		//this.dispose();
	}
	public void windowClosed(WindowEvent e){}
	public void windowOpened(WindowEvent e){}
	public void windowIconified(WindowEvent e){}
	public void windowDeiconified(WindowEvent e){}
	public void windowActivated(WindowEvent e){}
	public void windowDeactivated(WindowEvent e){}
	/**
	 * Klasa wewnetrzna odpowiedzialna za nasluch z serwera.
	 * @author Jacek Mucha
	 *
	 */
	class ListenFromServer extends Thread
	{
		/**
		 * 	Przelacznik okreslajacy, czy kontynuowac watek lst
		 *	
		 */
		boolean keepRunning = true;
		/**
		 * Wiadomosc odczytywana ze strumienia wejscia z serwera lub wysylana do serwera.
		 */
		JJMessage newMessage = new JJMessage(0,"?","?");
		/**
		 * Liczba okreslajaca ile dialogow zostalo rozpoczetych (+ niepowodzenia w rozpoczeciu dialogow) 
		 */
		public int dialogId = 0;
		/**
		 * Przelacznik okreslajacy, czy biezacy uzytkownik wykonuje polaczenie. Aktualnie nie wykorzystywana, ale istotna dla rozwoju projektu.
		 */
		public boolean calling = false;
		/**
		 * Rozkaz wysylany/odbierany z serwera. Okreslenie, jaka liczba oznacza jaki rozkaz, zostalo podane w opisie klasy JJMessage. -1 oznacza brak rozkazu lub blad.
		 * @see JJMessage
		 */
		int order = -1;
		/**
		 * Ustawia przelacznik calling na wartosc podana w signal
		 * @param signal nowa wartosc dla calling
		 * @see calling
		 */
		public void callNow(boolean signal)
		{
			calling = signal;
		}
/**
 * Metoda zwracajaca wartosc zmiennej dialogId
 * @return liczba prob rozpoczecia dialogu + dialogi przychodzace.
 */
		public int getDialogId()
		{
			return dialogId;
		}
/**
 * Nadpisanie metody obslugujacej watek. Ponizsza metoda odbiera wiadomosci JJMessage od JJServer i na ich podstawie wykonuje polecenia.
 * @see #getAbilityList()
 * @see #getContactList()
 * @see #affirmation(int, int, String, String)
 * @see #receiveAffirmation(JJMessage)
 */
		public void run()
		{
			while(keepRunning)
			{
				//System.out.print(".");
				synchronized(this)
				{
					try{ try{
						newMessage = (JJMessage) sInput.readObject();
						//System.out.print("Wiadomosc od serwera: ");
						order = newMessage.getOrder();
					} 
					catch(IOException e1)
					{ 
						keepRunning = false; //zatrzymaj watek
						System.out.println("Blad przy odbieraniu wiadomosci od serwera.");
						JOptionPane.showMessageDialog(null,"B³¹d przy odbieraniu wiadomoœci od serwera. Po³¹czenie zostanie przerwane.\n"+e1,"IOException",JOptionPane.ERROR_MESSAGE);
					} } catch(ClassNotFoundException e2){ System.out.println(e2);}
					switch(order)
					{
					case 6:
						System.out.println("Zalogowano...");
						getContactList();
						startReporting();
						break;
					case 8:
						getAbilityList();
						break;
					case 12:
						System.out.print("Przyszlo nowe polaczenie od: ");
						String fromMe = newMessage.getMessage();
						System.out.println(fromMe);
						String toYou = newMessage.getAdress();
						allDialogs.add(new JJDialog(toYou,fromMe,server,port,dialogId,false,newMessage.id2));
						dialogAccepted(fromMe,toYou,newMessage.id1,dialogId);
						dialogId++;
						break;
					case 14:
						System.out.println("Adresat gotowy do rozpoczecia dialogu...");
						String fromMe3 = newMessage.getCaller();
						String toYou3 = newMessage.getAdress();
						allDialogs.add(new JJDialog(fromMe3,toYou3,server,port,newMessage.id1,true,newMessage.id2));
						allDialogs.get(newMessage.id1).isCaller = true;
						allDialogs.get(newMessage.id1).sendPublicKey(true);
						break;
					case 16:
						System.out.println("Otrzymano wiadomosc tekstowa...");
						String decryptedTextFromMessage;
						if(!newMessage.isEncrypted)
							allDialogs.get(newMessage.id2).setLabels(newMessage.getCaller()+"> "+newMessage.textMessageObject.textMessage,newMessage.textMessageObject.b,newMessage.textMessageObject.i,newMessage.textMessageObject.u);
						else
						{
							decryptedTextFromMessage = new String(allDialogs.get(newMessage.id2).securityModule.decrypt(newMessage.encryptedTextMessageObject.encryptedTextMessage));
							allDialogs.get(newMessage.id2).setLabels(newMessage.getCaller()+"> "+decryptedTextFromMessage,newMessage.textMessageObject.b,newMessage.textMessageObject.i,newMessage.textMessageObject.u);
						}
						affirmation(newMessage.id1,newMessage.id2,newMessage.getAdress(),newMessage.getCaller()); //potwierdz, ze dialog id2 odebral wiadomosc
						break;
					case 17:
						System.out.println("Potwierdzenie odebrania wiadomosci tekstowej...");
						receiveAffirmation(newMessage);
						break;
					case 18:
						System.out.println("Otrzymano klucz publiczny...");
						allDialogs.get(newMessage.id2).securityModule.setEncryptionKey(newMessage.getKey());
						allDialogs.get(newMessage.id2).sendPublicKey(false);
						break;
					case 19:
						allDialogs.get(newMessage.id2).securityModule.setEncryptionKey(newMessage.getKey());
						break;
					case 20:
						allDialogs.get(newMessage.id2).endDialog();
						break;
					default:
						break;
					}
				}
				order = -1;
				//try{ this.wait(10); } catch(InterruptedException e){ System.out.println(e);}
			}
		}
/**
 * Metoda, ktora po otrzymaniu potwierdzenia otrzymania wiadomosci od adresata, przekazuje oczekujacej rozmowie informacje o potwierdzeniu odbioru.
 * @param affirmationMessage Wiadomosc potwierdzajaca nawiazanie polaczenia.
 */
		public void receiveAffirmation(JJMessage affirmationMessage)
		{
			/**
			 * Numer dialogu w wektorze rozmow.
			 */
			int dialogNumber = getDialog(affirmationMessage.id1);
			allDialogs.get(dialogNumber).dontWait();
		}
/**
 * Metoda wysylajaca potwierdzenie otrzymania wiadomosci od interlokutora.
 * @param senderId numer dialogu w wektorze dialogow u nadawcy
 * @param receiverId numer dialogu w wektorze dialogow u adresata
 * @param senderName login nadawcy
 * @param receiverName login adresata
 */
		public void affirmation(int senderId, int receiverId,String senderName, String receiverName)
		{
			try
			{
				/**
				 * Wiadomosc potwierdzajaca otrzymanie wiadomosci tekstowej.
				 */
				JJMessage affMessage = new JJMessage(17,"",receiverName,senderName);
				affMessage.id1 = senderId;
				affMessage.id2 = receiverId;
				sOutput.writeObject(affMessage);
			}
			catch(IOException exception){ System.out.println(exception);}
		}
/**
 * Metoda informujaca serwer o zaakceptowaniu wezwania do rozmowy, ktora nadeszla.
 * @param caller Nadawca.
 * @param me Login biezacego uzytkownika.
 * @param hisId Id rozmowy nadawcy.
 * @param id Id rozmowy biezacego uzytkownika.
 */
		public void dialogAccepted(String caller, String me, int hisId, int id)
		{
			System.out.println("Rozmowa "+id+" zaakceptowana. Informuje serwer...");
			try
			{ 
				JJMessage acceptMessage = new JJMessage(13,"",me,caller);
				acceptMessage.id1 = hisId;
				acceptMessage.id2 = id;
				sOutput.writeObject(acceptMessage); 
			} 
			catch(IOException e)
			{ 
				System.out.println(e);
			}
		}
/**
 * Metoda zatrzymujaca watek nasluchiwania serwera.
 */
		public void stopThread()
		{
			keepRunning = false;
			Thread.currentThread().interrupt();
		}
/**
 * Metoda wysylajaca do serwera zadanie wyslania listy kontaktow do biezacego uzytkownika i odbierajaca te liste.
 */
		public void getContactList()
		{
			System.out.println("Pobieranie listy kontaktow...");
			boolean get=true;
			String localName="";
			try
			{
				sOutput.writeObject(new JJMessage(1));
			} catch(IOException e) {}
			while(get)
			{
				try
				{
					try
					{
						//localName = ((JJPersonRecord) sInput.readObject()).getNomen();
						localName = ((JJMessage) sInput.readObject()).getMessage() ;
						System.out.print(localName+" ");
					}
					catch(ClassNotFoundException e2)
					{
						get = false;
					}
				}
				catch(IOException e)
				{
					get = false;
				}
				if(!"end".equals(localName))
				{
					contactListButtonsNames.add(localName);
					contactListAbility.add(false);
				}
				else
					get = false;
			}
			createGUI();
		}
/**
 * Metoda wysylajaca do serwera zadanie wyslania statusow dostepnosci uzytkownikow i odbierajaca ich liste.
 */
		public synchronized void getAbilityList()
		{
			//System.out.println("Pobieranie statusow dostepnosci uzytkownikow...");
			JJMessage currentMessage = new JJMessage(0); //pusta wiadomosc
			boolean getNext = true;
			int userIterator = 0;
			while(getNext)
			{
				try {try{ currentMessage = (JJMessage) sInput.readObject(); } catch(IOException e){ System.out.println(e);} } catch(ClassNotFoundException e2){System.out.println(e2);}
				if(currentMessage.getOrder()==10)
					getNext = false;
				else if(currentMessage.getOrder()==9)
					if("true".equals(currentMessage.getMessage()))
						contactListAbility.set(userIterator,true);
					else
						contactListAbility.set(userIterator,false);
				userIterator++; 
			}
			refreshAbilities();
			closeDeactivatedDialogs();
		}
		/**
		 * Metoda zamykajaca rozmowy z wylogowanymi uzytkownikami
		 */
		public void closeDeactivatedDialogs()
		{
			outp.println("Zamykam nieaktywne rozmowy (o ile istniej¹)...");
			for(int dialogNumber = 0; dialogNumber<allDialogs.size(); dialogNumber++)
			{
				if(allDialogs.get(dialogNumber).isAccepted)// && !allDialogs.get(dialogNumber).isDeactivated)
				{
					if( !findUserAbility( allDialogs.get(dialogNumber).you ) )
					{
						//allDialogs.get(dialogNumber).archive.closeArchive();
						outp.println(allDialogs.get(dialogNumber).you+" wylogowa³ siê, wiêc rozmowa zosta³a zakoñczona...");
						allDialogs.get(dialogNumber).endDialog();
					}
				}
			}
		}
		/**
		 * Metoda konczaca wszystkie rozmowy.
		 */
		public void closeAllDialogs()
		{
			outp.println("Zamykanie wszystkich rozmów...");
			for(int dialogNumber = 0; dialogNumber<allDialogs.size(); dialogNumber++)
			{
				if(allDialogs.get(dialogNumber).isAccepted && !allDialogs.get(dialogNumber).isDeactivated )
				{
					//allDialogs.get(dialogNumber).archive.closeArchive();
					allDialogs.get(dialogNumber).endDialog();
					//allDialogs.get(dialogNumber).isActive = false;
				}
			}
		}
	}
/**
 * Klasa obslugujaca kazda pojedyncza rozmowe. Odpowiada za wysylanie wiadomosci tekstowych. Kazdy obiekt JJDialog jest obslugiwany przez swoje indywidualne obiekty klasy JJArchive i JJKrypto.
 * @author Jacek Mucha
 *
 */
	class JJDialog extends JFrame implements WindowListener
	{
		/**
		 * Szeroko pojeta wiadomosc do wyslania.
		 */
		public JJMessage message;
/**
 * Modul kryptograficzny przechowujacy i generujacy klucze (prywatny, publiczny, publiczny rozmowcy(ten ostatni jest odbierany, a nie generowany)), szyfrujacy i deszyfrujacy wiadomosci.
 */
		public JJKrypto securityModule;
		//private String myMessage;
/** 
 * Login wlasciciela obiektu JJDialog.
 */
		private String me;
		/**
		 * Login interlokutora w dialogu.
		 */
		private String you;
		/**
		 * Stala sluzaca do poprawnej serializacji przesylanych danych.
		 */
		public static final long serialVersionUID = 42L;
		/**
		 * Liczba okreslajaca numer rozpoczetej rozmowy u biezacego uzytkownika.
		 */
		private int id;
		/**
		 * Liczba okreslajaca numer rozpoczetej rozmowy u interlokutora.
		 */
		private int hisId;
		//private ObjectOutputStream sOutput;
		//private ObjectInputStream sInput;
		/**
		 * Pole do wpisywania wiadomosci do wyslania.
		 */
		private JTextArea writeText;
		/**
		 * Przycisk sluzacy do wysylania wiadomosci do rozmowcy.
		 * @see writeText
		 */
		private JButton sendMessageButton;
		/**
		 * Obiekt obslugujacy archiwizowanie wiadomosci.
		 */
		private JJArchive archive;
		//private Queue<String> messageQueue;
		/**
		 * Obszary wyswietlania wiadomosci dla biezacego uzytkownika.
		 */
		private JLabel[] textLabels;
		/**
		 * Przelacznik informujacy o tym, czy biezacy uzytkownik rozpoczal rozmowe, czy to z nim rozpoczeto rozmowe.
		 */
		public boolean isCaller = false;
		/**
		 * Wartosc parametru Red przy kodowaniu koloru RGB
		 */
		private int red; 
		/**
		 * Wartosc parametru Green przy kodowaniu koloru RGB
		 */
		private int green;
		/**
		 * Wartosc parametru Blue przy kodowaniu koloru RGB
		 */
		private int blue;
		/**
		 * Wielkosc czcionki
		 */
		private int size;
		/** 
		 * Przelacznik okreslajacy, czy czcionka ma byc pogrubiona
		 */
		private boolean b;
		/**
		 * Przelacznik okreslajacy, czy wiadomosc ma byc pisana kursywa
		 */
		private boolean i;
		/**
		 * Przelacznik okreslajacy, czy tekst wiadomosci ma byc podkreslony.
		 */
		private boolean u;
		/**
		 * Przelacznik okreslajacy,czy wiadomosc jest pisana w skladni latex'a.
		 */
		private boolean latex;
		/**
		 * Przelacznik uzywany po wyslaniu wiadomosci. Przyjmuje wartosc true, dopoki rozmowca nie potwierdzi odebrania wiadomosci.
		 */
		private boolean waitForResponse = false;
		/**
		 * Przelacznik okreslajacy, czy biezaca wiadomosc ma byc zaszyfrowana.
		 */
		private boolean conversationEncrypted;
		/**
		 * Przelacznik okreslajacy, czy rozmowa jest aktywna, tzn. czy zostala poprawnie rozpoczeta i nie zostala zakonczona.
		 */
		public boolean isDeactivated;
		/**
		 * Pole wyboru opcji szyfrowania wiadomosci
		 */
		private JCheckBox isConversationEncrypted;
		/**
		 * Pole wyboru pogrubiania czcionki
		 */
		private JCheckBox isTextBold;
		/**
		 * Pole wyboru pisania kursyw¹.
		 */
		private JCheckBox isTextItalic;
		/**
		 * Pole wyboru podkreslenia tekstu.
		 */
		private JCheckBox isTextUnderlined;
		/**
		 * Pole wyboru okreslajace, czy wiadomosc jest pisana w latex'u.
		 */
		private JCheckBox isTextLatexEquation;
		/**
		 * Przelacznik okreslajacy, czy rozmowa zostala zaakceptowana przez rozmowce.
		 */
		public boolean isAccepted = false;

		/**
		 * Glowny konstruktor klasy odpowiedzialnej za obslugiwanie rozmowy.
		 * @param me Login biezacego uzytkownika.
		 * @param you Login rozmowcy/
		 * @param server Adres serwera.
		 * @param port Numer portu.
		 * @param id Indeks w wektorze allDialogs biezacego uzytkownika.
		 * @param isCaller Przelacznik okreslajacy, czy biezacy uzytkownik jest rozpoczynajacym rozmowe.
		 * @param hisId Indeks w wektorze allDialogs adresata.
		 * @see allDialogs
		 */
		public JJDialog(String me, String you, String server, int port, int id, boolean isCaller, int hisId)
		{
			System.out.println("Tworzenie okna rozmowy z "+you);
			isDeactivated = false;
			isAccepted = true;
			securityModule = new JJKrypto(RSALength);
			this.me = me;
			this.you = you;
			this.id = id;
			this.hisId = hisId;
			this.isCaller = isCaller;
			/**
			 * Obiekt obslugujacy akcje po kliknieciu buttona "wyslij"
			 * @see sendMessageButton
			 */
			ActionListener buttonActionListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					if(event.getSource()==sendMessageButton)
					{
						if(!waitForResponse)
							if(conversationEncrypted)
								sendEncryptedTextMessage();
							else
								sendTextMessage();
						else
						{
							System.out.println("Oczekiwanie na potwierdzenie odbioru.");
						}
					}
				}
			};
			/**
			 * Obiekt obslugujacy komponenty inne niz buttony w JJDialog.
			 */
			ActionListener otherJJDialogComponentsListener = new ActionListener()
			{
				public void actionPerformed(ActionEvent componentEvent)
				{
					conversationEncrypted = isConversationEncrypted.isSelected();
					b = isTextBold.isSelected();
					i = isTextItalic.isSelected();
					u = isTextUnderlined.isSelected();
					latex = isTextLatexEquation.isSelected();
				}
			};
			setTitle(you);
			setLayout(null);

			archive = new JJArchive(you);

			writeText = new JTextArea();
			writeText.setBounds(10,350,530,50);
			add(writeText);	
			
			isConversationEncrypted = new JCheckBox("Szyfruj wiadomoœæ");
			isConversationEncrypted.addActionListener(otherJJDialogComponentsListener);
			isConversationEncrypted.setBounds(20,400,200,20);
			add(isConversationEncrypted);
			
			isTextBold = new JCheckBox("Pogrubienie");
			isTextBold.addActionListener(otherJJDialogComponentsListener);
			isTextBold.setBounds(20,420,200,20);
			add(isTextBold);
			
			isTextItalic = new JCheckBox("Kursywa");
			isTextItalic.addActionListener(otherJJDialogComponentsListener);
			isTextItalic.setBounds(20,440,200,20);
			add(isTextItalic);
			
			isTextUnderlined = new JCheckBox("Podkreœlenie");
			isTextUnderlined.addActionListener(otherJJDialogComponentsListener);
			isTextUnderlined.setBounds(20,460,200,20);
			add(isTextUnderlined);
			
			isTextLatexEquation = new JCheckBox("Kod Latex'a");
			isTextLatexEquation.addActionListener(otherJJDialogComponentsListener);
			isTextLatexEquation.setBounds(20,480,200,20);
			add(isTextLatexEquation);		
			
			sendMessageButton = new JButton("Wyœlij");
			sendMessageButton.setBounds(390,400,100,20);
			sendMessageButton.addActionListener(buttonActionListener);
			add(sendMessageButton);

			textLabels = new JLabel[5];
			for(int i=0;i<textLabels.length; i++)
				textLabels[i] = new JLabel();
			textLabels[0].setBounds(10,20,530,50);
			textLabels[1].setBounds(10,70,530,50);	    
			textLabels[2].setBounds(10,120,530,50);
			textLabels[3].setBounds(10,170,530,50);
			textLabels[4].setBounds(10,220,530,50);
			for(int i = 0; i<textLabels.length; i++)
				add(textLabels[i]);
			setSize(550,535);
			//setVisibleMainWindow(false);
			setResizable(false);
			setVisible(true);
		}
		/**
		 * Metoda obslugujaca zakonczenie rozmowy. Zamyka archiwum i okno.
		 */
		public void endDialog()
		{
			outp.println("Otrzymano ¿¹danie zakoñczenia rozmowy z"+you+"...");
			archive.closeArchive();
			message = new JJMessage(20,"",you,me);
			message.setId1(id);
			message.setId2(hisId);
			isDeactivated = true;
			outputMessage(message);
			this.setVisible(false);
			this.dispose();
		}
		/**
		 * Metoda zwracajaca login jednego z rozmowcow.
		 * @param who true oznacza biezacego uzytkownika, false - rozmowce
		 * @return login jednego z uzytkownikow.
		 */
		public String getInterlocutor(boolean who)
		{
			return who? me:you;
		}
		/**
		 * Metoda sluzaca do pozyskiwania id rozmowy biezacego uzytkownika.
		 * @see id
		 * @see allDialogs
		 * @return Numer rozmowy w wektorze allDialogs
		 */
		public int getId()
		{
			return id;
		}
		/**
		 * Metoda wywolywana po otrzymaniu potwierdzenia odebrania wiadomosci tekstowej, zwalnia program z obowiazku oczekiwania na to potwierdzenie.
		 * @see waitForResponse
		 */
		public void dontWait()
		{
			System.out.println("Koniec czekania na potwierdzenie odbioru wiadomosci...");
			waitForResponse = false;
		}
		/**
		 * Metoda ustawiajaca nowy tekst wiadomosci w polach widocznych w okienku i przesuwajaca poprzednie wiadomosci wyzej.
		 * @param newText Tekst nowej wiadomosci.
		 * @param newB Formatowanie tekstu: pogrubienie.
		 * @param newI Formatowanie tekstu: kursywa.
		 * @param newU Formatowanie tekstu: podkreslenie.
		 */
		public void setLabels(String newText, boolean newB, boolean newI, boolean newU)
		{
			for(int label = 0; label < textLabels.length-1; label++)
			{
				textLabels[label].setFont(textLabels[label+1].getFont());
				textLabels[label].setText(textLabels[label+1].getText());
			}
			textLabels[textLabels.length-1].setText(newText);
		}
		public void windowClosing(WindowEvent event)
		{
			//setVisibleMainWindow(true);
			endDialog();
		}
		public void windowClosed(WindowEvent e){}
		public void windowOpened(WindowEvent e){}
		public void windowIconified(WindowEvent e)
		{
			//setVisibleMainWindow(true);
		}
		public void windowDeiconified(WindowEvent e)
		{
			//setVisibleMainWindow(false);	    
		}
		public void windowActivated(WindowEvent e){}
		public void windowDeactivated(WindowEvent e){}
		/**
		 * Metoda sluzaca do przeslania wlasnego klucza publicznego do rozmowcy.
		 * @param first Okresla, czy wysylasz tekst jako pierwszy, czy wczesniej odebrales klucz publiczny od rozmowcy.
		 */
		public synchronized void sendPublicKey(boolean first)
		{
			int order= first ? 18:19; 
			System.out.println("Wysy³anie klucza publicznego...");
			JJMessage keyMessage = new JJMessage(order,securityModule.getPublicKey(),you,me);
			keyMessage.setId1(id);
			keyMessage.setId2(hisId);
			outputMessage(keyMessage);
		}
		/**
		 * Metoda sluzaca do wysylania zaszyfrowanej wiadomosci do rozmowcy.
		 */
		public synchronized void sendEncryptedTextMessage()
		{
			/**
			 * Tresc wysylanej wiadomosci.
			 */
			String textMessage = writeText.getText();
			outp.println("Wiadomoœæ do wys³ania: "+textMessage);
			if(!"".equals(textMessage))
			{
				message = new JJMessage(16,"",you,me);
				message.setId1(id);
				message.setId2(hisId);
				message.isEncrypted = true;
				message.encryptedTextMessageObject = new JJEncryptedTextMessage(securityModule.encrypt(textMessage),red,green,blue,size,b,i,u,latex);
				message.textMessageObject = new JJTextMessage(">>zaszyfrowane<<",red,green,blue,size,b,i,u,latex);
				outp.println("Wysy³anie zaszyfrowanej wiadomoœci tekstowej...");
				archive.saveMessage(textMessage);
				writeText.setText("");
				outputMessage(message);
				waitForResponse = true;
				setLabels(nomen+" "+cognomen+"> "+textMessage,b,i,u);
			}
			else
			{
				outp.println("Nie mo¿esz wys³aæ pustej wiadomoœci.");
			}
		}
		/**
		 * Metoda sluzaca do przeslania rozmowcy niezaszyfrowanej wiadomosci tekstowej.
		 */
		public synchronized void sendTextMessage()
		{
			String textMessage = writeText.getText();
			System.out.println("Wiadomosc do wyslania: "+textMessage);
			if(!"".equals(textMessage))
			{
				message = new JJMessage(16,"",you,me);
				message.setId1(id);
				message.setId2(hisId);
				message.isEncrypted = false;
				message.textMessageObject = new JJTextMessage(textMessage,red,green,blue,size,b,i,u,latex);
				System.out.println("Wysylanie wiadomosci tekstowej...");
				archive.saveMessage(textMessage);
				writeText.setText("");
				outputMessage(message);
				waitForResponse = true;
				setLabels(nomen+" "+cognomen+"> "+textMessage,b,i,u);
			}
			else
			{
				outp.println("Nie mo¿esz wys³aæ pustej wiadomoœci.");
			}
		}
	}
}

