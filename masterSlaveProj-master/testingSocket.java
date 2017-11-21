import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class testingSocket {
	/*static ServerSocket serverSocket;
	static Socket socket;*/
	static String slaveSelected, targetIp, targetPort, numberOfConnetions, url;
	static boolean keepAlive, upFakeUrl;
	static Socket socketForFakeUrl;
	public static ArrayList<Socket> connected = new ArrayList<>();
	static ServerSocket serverSocketForFakeUrl;
	
	public static void main(String [] args) throws IOException, NoSuchAlgorithmException {
		/*serverSocket = new ServerSocket(3000);
		while(true) {
			Socket socket = serverSocket.accept();
			List<String> lines = Files.readAllLines( Paths.get("L:\\sjsu\\cmpe273\\workSpace\\masterSlaveProj3\\src\\index.html"));
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			out.println("HTTP/1.1 200 OK");
			out.println("Content-Type: Text/html");
			out.println("\r\n");
			for(int k=0; k<lines.size();k++) {
				out.println(lines.get(k));
			}
			out.flush();
			out.close();
		}*/
		//socket.close();
		
		serverSocketForFakeUrl = new ServerSocket(3000);
		upFakeUrl=true;
		while(upFakeUrl) {
			socketForFakeUrl = serverSocketForFakeUrl.accept();
			socketForFakeUrl.setKeepAlive(true);
			
			InputStream is = socketForFakeUrl.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(is)); 
			String line = in.readLine();
			if(line!=null && (line.contains(".html") || line.contains(" / "))) {
				
				String key="CMPE206_project3_maulik.bhatt_012421019";
		        MessageDigest messageDigest=MessageDigest.getInstance("MD5");
		        messageDigest.update(key.getBytes(),0,key.length());
		        String md5String = new BigInteger(1,messageDigest.digest()).toString(16);
			       
				List<String> lines;
				if(line.contains(".html")) {
					lines = Files.readAllLines( Paths.get(line.split(" ")[1].replace("/", "")));								
				} else {
					lines = Files.readAllLines( Paths.get("index.html"));
				}
				PrintWriter out = new PrintWriter(socketForFakeUrl.getOutputStream());
				out.println("HTTP/1.1 200 OK");
				out.println("Content-Type: Text/html");
				out.println("\r\n");
				String temp = "";
				for(int k=0; k<lines.size();k++) {
					temp = "http://www.google.com:80";
					out.println(lines.get(k).replace("md5String",md5String).replace("provideUrl",temp));
				}
				out.flush();
				out.close();
			} else if(line!=null && (line.contains("sitemap.xml") || line.contains("robots.txt"))) {
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
		}
	}
}