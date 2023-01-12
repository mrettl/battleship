package battleship;

import java.io.IOException;
import java.net.InetSocketAddress;

import battleship.agents.GameManager;
import battleship.viewer.web.GameServer;

public class Entrypoint {
	public static void main(String... strings) throws IOException, InterruptedException {
		int port = 8000;
		if(strings.length > 0) 
			port = Integer.parseInt(strings[0]);
		
		// start Game Manager
		Thread manager = GameManager.common().startInNewThread();
		
		// create and start web server
		GameServer server = new GameServer(
				new InetSocketAddress("localhost", port), 0);
		server.start();
		
		try {
			// open the game in the web browser (windows only)
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler http://localhost:" + port + "/");
		
		} catch(RuntimeException e) {
			System.out.println("open game: http://localhost:" + port + "/");
		}
		
		// wait for user to stop the server
		System.out.println("press any key to stop the server");
		System.in.read();
		
		GameManager.common().stop();
		server.stop(10);
		manager.join(1_000);
	}
}
