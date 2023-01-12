package battleship;

import java.io.IOException;
import java.net.InetSocketAddress;

import battleship.agents.GameManager;
import battleship.viewer.web.GameServer;

public class Entrypoint {
	public static void main(String... strings) throws IOException, InterruptedException {
		// start Game Manager
		Thread manager = GameManager.common().startInNewThread();
		
		// create and start web server
		GameServer server = new GameServer(
				new InetSocketAddress("localhost", 8000), 0);
		server.start();
		
		try {
			// open the game in the web browser (windows only)
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler http://localhost:8000/");
		
		} catch(RuntimeException e) {
			System.out.println("open game: http://localhost:8000/");
		}
		
		// wait for user to stop the server
		System.out.println("press any key to stop the server");
		System.in.read();
		
		System.out.println("server stops in 10 seconds");
		GameManager.common().stop();
		server.stop(10);
		manager.join(1_000);
		System.out.println("server stoped");
	}
}
