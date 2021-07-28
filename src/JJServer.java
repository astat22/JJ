//Jacek Mucha
//data rozpoczenia 6 grudnia 2013
//ostatnia aktualizacja 11 stycznia 2014

import java.io.*;
import java.net.*;
import java.util.*;
/**
 * Klasa obslugujaca serwer komunikatora JJ. Przez serwer przechodza wszystkie wiadomosci wysylane miedzy uzytkownikami.
 * @author Jacek Mucha
 * @date 11.01.2014
 */
class JJServer
{
	/**
	 * Obiekt "this"
	 */
	public static JJServer server;
	/**
	 * Wektor podstawowych danych o uzytkownikach, ktorych lista jest pobierana z pliku.
	 */
	public static Vector<JJPersonRecord> users;
	/**
	 * Wektor skladajacy sie z informacji o tym, kto z kim prowadzil rozmowe. Stare rozmowy nie sa kasowane, dlatego mozna odtworzyc historie rozmow.
	 */
	public static Vector<JJDialogRecord> dialogs;
	/**
	 * Gniazdko serwera.
	 */
	public ServerSocket serverSocket;
	/**
	 * Port serwera
	 */
	private int port = 29500;
	//private ObjectOutputStream sOutput;
	//private ObjectInputStream sInput;
	/**
	 * Wektor obslugujacy watki aktywnych klientow. Kazdemu klientowi przydzielony jest watek ze strumieniami wejscia, wyjscia i z indywidualnym gniazdkiem.
	 */
	private Vector<JJClientThread> currentClients;
	/**
	 * Wektor gniazdek klientow. Dzieki niemu mozna wyszukiwac watki klientow po gniazdkach w razie wystapienia bledu.
	 */
	private Vector<Socket> sockets;
	/**
	 * Przelacznik odpowiedzialny za awaryjne wylaczenie akceptacji polaczen przychodzacych od klientow.
	 */
	private boolean serverWorking = true;
	/**
	 * Jedyny konstruktor klasy. Po pierwsze, pobiera z pliku bin\\users.txt loginy wszystkich uzytkownikow. 
	 * Wywoluje konstruktory wektorow i otwiera gniazdko serwera. Uruchamia watek nasluchiwania przez serwer.
	 */
	public JJServer()
	{ 
		try
		{
			try
			{
				//File contactListFile = new File("users.txt");
				Scanner contactListFileScanner = new Scanner(new BufferedReader(new FileReader("users.txt")));
				currentClients = new Vector<JJClientThread>();
				sockets = new Vector<Socket>();
				users = new Vector<JJPersonRecord>();
				dialogs = new Vector<JJDialogRecord>();
				String nextUserLogin = "";
				while(contactListFileScanner.hasNextLine())
				{
					nextUserLogin = contactListFileScanner.nextLine();
					users.add(new JJPersonRecord(nextUserLogin));
				}//dopoki plik niepusty, bierz kolejne linie i wpisuj je jako imiona do wektora kontaktow
				System.out.println("Pobrano liste kontaktow na serwer...");
				serverSocket = new ServerSocket(port);
				System.out.println("Gniazdko serwera utworzone...");
				/**
				 * Watek odpowiedzialny za nasluch sieci, przyjmuje akceptacje polaczen przychodzacych.
				 */
				Thread listen = new Thread()
				{
					/**
					 * Metoda obslugujaca akceptacje przychodzaych polaczen.
					 */
					public void run()
					{
						try
						{
							while(serverWorking)
							{
								synchronized(this) {
									Socket newSocket = serverSocket.accept();
									sockets.add(newSocket);
									JJClientThread newClient = new JJClientThread(newSocket);
									currentClients.add(newClient);
									//newClient.start();
									setLog(newClient.username,true);

								}
							}
						}
						catch(IOException e2){}
					}
				};
				listen.start();
				System.out.println("Rozpoczeto nasluchiwanie...");
				contactListFileScanner.close(); 	
			}
			catch(SocketException e3)
			{
				System.out.println(e3+"Po³¹czenie zresetowano.");
				serverWorking = false;
			} 
		}
		catch(IOException e2){System.out.println(e2);}
	}
	/**
	 * Metoda sluzaca do wyszukiwaniu indeksow klientow po ich gniazdkach.
	 * @param socket Gniazdko poszukiwanego klienta.
	 * @return Indeks w wektorze currentClients
	 * @see currentClients
	 */
	public int getClientNumberBySocket(Socket socket)
	{
		/**
		 * Poszukiwany indeks klienta.
		 */
		int i;
		/**
		 * Przelacznik przerywajacy petle.
		 */
		boolean control = true;
		for(i=0;i<currentClients.size() && control;i++)
		{
			try
			{
				if(currentClients.get(i).getSocket()==socket)
					control = false;
			}
			catch(ArrayIndexOutOfBoundsException e){System.out.println("Brak klientow..."); control = false;}
		}
		return i;
	}
	/**
	 * Metoda wysylajaca liste zalogowanych uzytkownikow do wszystkich zalogowanych uzytkownikow.	
	 */
	public void outerSendTalkables()
	{
		for(int element = 0; element<currentClients.size();element++)
			currentClients.get(element).sendTalkables();
	}
	/**
	 * Metoda wywolywana, gdy od caller przyjdzie dzwonek w celu wywolania adress. Metoda ta przekazuje dzwonek do adresata.
	 * @param caller Nadawca - wywoluje adresata.
	 * @param adress Adresat - do niego przekazywany jest dzwonek.
	 * @param id - identyfikator dialogu u nadawcy.
	 */
	public synchronized void callSecond(String caller, String adress, int id)
	{
		System.out.println("Przekazuje dzwonek do "+adress);
		//JJClientThread called = getThread(adress);
		int called = getThread(adress);
		System.out.println("Znalazlem aktywnego adresata: "+ currentClients.get(called).getUsername());
		currentClients.get(called).sendCall(caller,adress,id);
	}
	/**
	 * Metoda informujaca nadawce rozmowy o niepowodzeniu proby podjecia rozmowy z adresatem. Aktualnie nieuzywana.
	 * @param adress Adresat, ktory nie potwierdzil gotowosci do podjecia rozmowy.
	 */
	public synchronized void callingError(String adress)
	{
		System.out.println("Blad telefonu. Informuje "+adress+" o niepowodzeniu...");
	}
	/**
	 * Metoda wyszukujaca klienta po jego loginie.
	 * @param name Login klienta,
	 * @return Indeks poszukiwanego klienta w wektorze currentClients.
	 * @see currentClients
	 */
	public int getThread(String name)
	{
		int current = 0;
		while(!name.equals(currentClients.get(current).getUsername()))
		{
			current=(current+1)%currentClients.size();
		}
		//return currentClients.get(current);
		return current;
	}
	/**
	 * Metoda wyszukujaca w wektorze users uzytkownika po jego loginie.
	 * @param name login poszukiwanego uzytkownika
	 * @return wizytowka uzytkownika
	 * @see users
	 */
	private JJPersonRecord getPerson(String name)
	{
		int current = 0;
		while(!name.equals(users.get(current).getNomen()))
		{
			current = (current+1)%users.size();
		}
		return users.get(current);
	}
	/**
	 * Metoda ustawiajaca status zalogowania uzytkownika.
	 * @param usersId Login uzytkownika, ktorego status dostepnosci zostaje zmieniony.
	 * @param status Nowy status dostepnosci uzytkownika.
	 */
	private void setLog(String usersId, boolean status)
	{
		int user = 0;
		while(user < users.size() && !usersId.equals(users.get(user).getNomen()))
		{
			user++;
		}
		users.get(user).setAbility(status);
		System.out.println("Ustawiono "+usersId+" na zalogowany: "+status);
	}
	/**
	 * Metoda uruchamiajaca serwer.
	 * @param args
	 */
	public static void main(String[] args)
	{
		server = new JJServer();
	}

	/**
	 * Kazdy obiekt tej klasy reprezentuje watek obslugi jednego klienta. Tutaj znajduja sie strumienie we/wy z danym klientem. Odebranie wiadomosci od klienta z dana wartoscia order powoduje wykonanie konkretnych operacji przez serwer. Dotyczy to takze klasy zewnetrznej.
	 * @author Jacek Mucha
	 *
	 */
	class JJClientThread extends Thread
	{
		/**
		 * Gniazdko tworzone dla klienta.
		 */
		private Socket clSocket;
		/**
		 * Strumien wejscia do komunikacji z klientem. 
		 */
		private ObjectInputStream clInput;
		/**
		 * Strumien wyjscia do komunikacji z klientem.
		 */
		private ObjectOutputStream clOutput;
		/**
		 * Login klienta.
		 */
		public String username = "";
		/**
		 * Wiadomosc przychodzaca do serwera.
		 */
		public JJMessage messageIn;
		/**
		 * Wiadomosc wychodzaca do klienta.
		 */
		public JJMessage messageOut;
		/**
		 * Zmienna kontrolujaca aktywnosc uzytkownika
		 */
		public int checkLogin = 0;
		/**
		 * Przelacznik utrzymujacy wykonywanie glownej petli w watku.
		 */
		public boolean dontKillMe = true;
		/**
		 * Przelacznik utrzymujacy wykonywanie petli kontrolujacej dostepnosc uzytkownika.
		 */
		boolean innerWhile = true;
		/**
		 * Przelacznik informujacy o oczekiwaniu na wiadomosc.
		 */
		boolean waitForResponse = false;
		/**
		 * Zmienna do instrukcji warunkowej switch w glownej petli watku klienta, informuje o dzialaniach, jakie ma podjac serwer.
		 */
		int order = -1;
		/**
		 * Konstruktor glowny, otwiera strumienie wejscia i wyjscia polaczenia z klientem i uruchamia watek.
		 * @param socket Gniazdko klienta
		 */
		JJClientThread(Socket socket)
		{
			System.out.println("Nowy klient...");
			clSocket = socket;
			try 
			{
				clOutput = new ObjectOutputStream(clSocket.getOutputStream());
				clInput = new ObjectInputStream(clSocket.getInputStream());
				System.out.println("Utworzono strumienie klienta na serwerze...");
				try
				{
					username = (String) clInput.readObject();
					System.out.println("Zalogowano: "+username+".");
				}
				catch(ClassNotFoundException e3){}
			}
			catch(IOException e)
			{
				System.out.println("Blad strumieni na serwerze w watku klienta.");
			}  
			start();
		}
		/**
		 * Metoda pobierajaca login biezacego uzytkownika.
		 * @return Login biezacego uzytkownika.
		 */
		public String getUsername()
		{
			return username;
		}
		/**
		 * Metoda przesylajaca biezacemu uzytkownikowi liste uzytkownikow.
		 * @param users Stad pobierane sa informacje o uzytkownikach.
		 */
		public synchronized void sendContactList(Vector<JJPersonRecord> users)
		{
			System.out.println("Wysylanie listy kontaktow do "+username+"...");
			JJMessage throwUserName;
			for(int userIterator = 0; userIterator < users.size(); userIterator++)
			{
				throwUserName = new JJMessage(4,users.get(userIterator).getNomen());
				try { 
					//clOutput.writeObject(users.get(userIterator));
					clOutput.writeObject(throwUserName);
					System.out.print(throwUserName.getMessage()+" ");
				}   catch(IOException e2){}
			}
			try { 
				//clOutput.writeObject("end");
				clOutput.writeObject(new JJMessage(5,"end"));
				System.out.println("\nWysylanie zakonczone...");
			} catch(IOException e2){}
		}
		/**
		 * Metoda konczoca prace serwera. Zamyka gniazdko serwera.
		 */
		public void stopServer()
		{
			try{ serverSocket.close(); } catch(IOException e){ System.out.println(e); }
		}
		/**
		 * Metoda zwracajaca gniazdko klienta.
		 * @return Gniazdko biezacego klienta.
		 */
		public Socket getSocket()
		{
			return clSocket;
		}
		/**
		 * Metoda sluzaca do przeslania listy statusow dostepnosci uzytkownikow do biezacego uzytkownika.
		 */
		public synchronized void sendTalkables()
		{
			String isAble = "";
			//System.out.println("Przesylam userowi "+username+" liste zalogowanych...");
			try { clOutput.writeObject(new JJMessage(8)); } catch(IOException e){ System.out.println(e); }
			for(int userIterator = 0; userIterator < users.size(); userIterator++)
			{
				isAble = users.get(userIterator).getAbility() ? "true" : "false";   		
				try{ clOutput.writeObject(new JJMessage(9,isAble) ); } catch(IOException e){ System.out.println(e); }
			}	
			try{ clOutput.writeObject(new JJMessage(10) ); } catch(IOException e){ System.out.println(e); }
		}
		/**
		 * Metoda przekazujaca adresatowi zadanie rozmowy od nadawcy.
		 * @param fromMe Login nadawcy.
		 * @param toYou Login adresata.
		 * @param callerId Indeks rozmowy w wektorz rozmow nadawcy.
		 */
		public void sendCall(String fromMe, String toYou, int callerId)
		{
			System.out.print("Przekazuje adresatowi zadanie rozmowy... ");
			try
			{ 
				/**
				 * Wiadomosc przekazujaca zadanie rozmowy.
				 */
				JJMessage callMessage = new JJMessage(12,fromMe,toYou);
				callMessage.id1 = callerId;
				clOutput.writeObject(callMessage); 
				System.out.print("Przekazano..."); 
			}
			catch(IOException e)
			{ 
				System.out.println(e);
			}
			System.out.println("");
		}
		/**
		 * Glowna metoda obslugujaca watek. Odbiera rozkazy od klientow i wykonuje wszystkie zadania. Sprawdza dostepnosc klienta.
		 */
		public void run()
		{
			boolean youKnowYouLogged = false;
			/**
			 * Wiadomosc, ktorej celem jest potwierdzenie zalogowania.
			 */
			JJMessage hermes;
			synchronized(this)
			{
				//try{
				while(dontKillMe)
				{

					if(order == 1) youKnowYouLogged = true;
					if(!youKnowYouLogged)
					{
						System.out.println("Potwierdzam zalogowanie...");
						try 
						{ 
							hermes = new JJMessage(6);
							clOutput.writeObject(hermes); 
							youKnowYouLogged = true;
						} catch(IOException e) { System.out.println("Nie udalo sie potwierdzic zalogowania.");}
					}
					try
					{
						try
						{
							messageIn = (JJMessage) clInput.readObject();
							//System.out.print("Otrzymano wiadomosc: ");
							order = messageIn.getOrder();
						} catch(ClassNotFoundException e1){ System.out.println(e1);}
					} catch(IOException e2){ System.out.println(e2);}
					switch(order)
					{
					case -1:
						if(dontKillMe)
							setLog(username,false);
						dontKillMe = false;
						innerWhile = false;
						currentClients.remove(getThread(username));
						System.out.println(username+" zerwal polaczenie.");
						//for(int dialogNumber = 0;dialogNumber<dialogs.size();dialogNumber++)
						//{
						/*if(username.equals(dialogs.get(dialogNumber).sender)) //wyslij informacje o zerwaniu polaczenia do osob, z ktorymi rozpoczal rozmowe
								currentClients.get(getThread(dialogs.get(dialogNumber).receiver)).sendContactList(users);
							else if(username.equals(dialogs.get(dialogNumber).receiver)) //wyslij informacje o zerwaniu polaczenia do osob, ktore z nim rozpoczely rozmowe
								currentClients.get(getThread(dialogs.get(dialogNumber).sender)).sendContactList(users);*/
						outerSendTalkables();
						//}
						Thread.currentThread().interrupt();
						//this.stop();
						break;
					case 1:
						System.out.println("Otrzymano zadanie wyslania listy kontaktow do "+username+"...");
						sendContactList(users);
						/**
						 * Watek odpowiedzialny za sprawdzanie dostepnosci biezacego uzytkownika. W przypadku nie otrzymania odpowiedzi, moze uznac klienta za wylogowanego.
						 */
						Thread nowISeeYou = new Thread()
						{
							public void run()
							{
								while(innerWhile)
								{
									if(!dontKillMe)
									{
										currentClients.remove(getThread(username));
										innerWhile = false;
										Thread.currentThread().interrupt();//this.stop();
									}
									try{ sleep(30050); } catch(InterruptedException e){}
									checkLogin++;
									if(checkLogin>2)
									{
										dontKillMe = false;
										innerWhile = false;
										setLog(username,false);
										System.out.println(username+" zerwal polaczenie.");
									}
								}
							}
						};
						nowISeeYou.start();
						break;
					case 7:
						//System.out.println(username+" wciaz obecny");
						checkLogin = 0;
						sendTalkables();
						//for(JJClientThread element : currentClients)
						//    JJClientThread.sendTalkables();
						//outerSendTalkables();
						break;
					case 11:
						/**
						 * Login nadawcy.
						 */
						String fromMe = messageIn.getMessage();
						/** Login adresata. */
						String toYou = messageIn.getAdress();
						System.out.println(fromMe+" dzwoni do "+toYou);
						if(getPerson(toYou).getAbility())
							callSecond(fromMe,toYou,messageIn.id1);
						else
							callingError(fromMe);
						break;
					case 13:
						String fromMe2 = messageIn.getCaller();
						String toYou2 = messageIn.getAdress();
						System.out.println("Otrzymano akceptacje rozmowy "+ fromMe2 +" z "+ toYou2 +"...");
						dialogs.add(new JJDialogRecord(fromMe2,toYou2));
						int caller = getThread(fromMe2);
						try
						{ 
							JJMessage acceptanceGetMessage = new JJMessage(14,"",toYou2,fromMe2);
							acceptanceGetMessage.id1=messageIn.id1;
							acceptanceGetMessage.id2=messageIn.id2;
							currentClients.get(caller).clOutput.writeObject(acceptanceGetMessage);
						} catch(IOException e)
						{ 
							System.out.println(e);
						}
						break;
					case 15: //wyloguj
						System.out.println(username+" wylogowal sie...");
						if(dontKillMe)
						{
							setLog(username,false);
							outerSendTalkables();
						}
						try
						{
							clInput.close();
							clOutput.close();
							clSocket.close();
						}
						catch(IOException e2)
						{
							System.out.println(e2);
						}
						checkLogin = 10;
						System.out.println("Zamykanie watku klienta...");
						dontKillMe = false;
						innerWhile = false;
						//nowISeeYou.interrupt();
						Thread.currentThread().interrupt();
						break;
					case 16:
						String toYou3 = messageIn.getAdress();
						String textMessage = messageIn.textMessageObject.textMessage;
						System.out.println(username+" przesyla wiadomosc tekstowa do "+toYou3+ ":");
						System.out.println("\t"+textMessage);
						try
						{
							currentClients.get(getThread(toYou3)).clOutput.writeObject(messageIn);
							System.out.println("Wiadomosc przekazana...");
							waitForResponse = true;
						}
						catch(IOException ex)
						{
							System.out.println(ex);
						}
						break;
					case 17:
						System.out.println("Potwierdzenie odebrania wiadomosci do "+messageIn.getAdress()+"...");
						try{ currentClients.get(getThread(messageIn.getAdress())).clOutput.writeObject(messageIn);}catch(IOException exception){ System.out.println(exception);}
						break;
					case 18:
						System.out.println("Przesy³anie klucza publicznego do "+messageIn.getAdress()+"...");
						try{ currentClients.get(getThread(messageIn.getAdress())).clOutput.writeObject(messageIn);}catch(IOException exception){ System.out.println(exception);}
						break;
					case 19:
						System.out.println("Przesy³anie klucza publicznego do "+messageIn.getAdress()+"...");
						try{ currentClients.get(getThread(messageIn.getAdress())).clOutput.writeObject(messageIn);}catch(IOException exception){ System.out.println(exception);}
						break;
					case 20:
						System.out.println("Koniec rozmowy "+messageIn.getAdress()+" z "+messageIn.getCaller()+"...");
						try{ currentClients.get(getThread(messageIn.getAdress())).clOutput.writeObject(messageIn);}catch(IOException exception){ System.out.println(exception);}
						break;
					default:
						System.out.println(order);
						break;
					}
					order = -1;
				} 
			}
		}
	}
}