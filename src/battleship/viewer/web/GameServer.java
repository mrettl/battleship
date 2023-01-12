package battleship.viewer.web;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.*;


public class GameServer {
	private HttpServer server;
	
	FileResourceHandler battlefieldHtmlHandler;
	FileResourceHandler battlefieldJSHandler;
	FileResourceHandler w3CssHandler;
	GameHandler gameHandler;
	
	public GameServer(InetSocketAddress address, int backlog) throws IOException {
		this.server = HttpServer.create(address, backlog);
		
		battlefieldHtmlHandler = new FileResourceHandler(
				server, "/", "res/battlefield.html", "text/html");
		battlefieldJSHandler = new FileResourceHandler(
				server, "/battlefield.js", "res/battlefield.js", "text/javascript");
		w3CssHandler = new FileResourceHandler(
				server, "/w3.css", "res/w3.css", "text/css");
		gameHandler = new GameHandler(server, "/app");
	}
	
	public HttpServer getHttpServer() {
		return server;
	}
	
	public void start() {
		server.start();
	}
	
	public void stop(int delay) {
	   System.out.println("server stops in " + delay + " seconds");
	   server.stop(delay);
	   System.out.println("server stoped");
	}
	
}
