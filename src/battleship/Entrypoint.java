package battleship;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import battleship.viewer.AppHandler;
import battleship.viewer.FileResourceHandler;

public class Entrypoint {
	public static void main(String... strings) throws IOException {
		   HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		   server.createContext("/", new FileResourceHandler("res/battlefield.html", "text/html"));
		   server.createContext("/battlefield.js", new FileResourceHandler("res/battlefield.js", "text/javascript"));
		   server.createContext("/w3.css", new FileResourceHandler("res/w3.css", "text/css"));
		   server.createContext("/app", new AppHandler());
		   server.setExecutor(null); // creates a default executor
		   server.start();
		   
		   Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler http://localhost:8000/");
		   
		   System.out.println("press any key to stop the server");
		   System.in.read();
		   
		   System.out.println("server stops in 10 seconds");
		   server.stop(10);
		   System.out.println("server stoped");

		   
	}
}
