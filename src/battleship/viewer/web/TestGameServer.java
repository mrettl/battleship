package battleship.viewer.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.stringtree.json.JSONWriter;

import battleship.agents.GameManager;
import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipLengths;
import battleship.model.ShipPlacement;
import battleship.util.BoardMasks;

class TestGameServer {
	static GameServer server;
	static Thread managerThread;
	
	@BeforeAll
	static void setup() throws IOException {
		server = new GameServer(new InetSocketAddress("localhost", 8000), 0);
		server.gameHandler.setMaxResponseDelay(0);
		server.start();
		
		GameManager manager = GameManager.common();
		manager.setManageInterval(0);
		managerThread = manager.startInNewThread();
	}
	
	@AfterAll
	static void tearDown() throws InterruptedException {
		server.stop(1);
		GameManager.common().stop();
		managerThread.join(1000);
	}
	
	static HttpURLConnection openConnection(String url, String method) throws IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod(method);
		con.setUseCaches(false);
		con.setConnectTimeout(1000);
		con.setReadTimeout(1000);
		
		return con;
	}
	
	@Test
	void testHtml() throws IOException {
		HttpURLConnection con = openConnection(
				"http://localhost:8000", 
				"GET");
		
		assertEquals(200, con.getResponseCode());
		
		try(InputStream is = con.getInputStream()) {
			String response = new String(is.readAllBytes());
			assertTrue(response.startsWith("<!DOCTYPE html"));
		}
	}
	
	@Test
	void testJs() throws IOException {
		HttpURLConnection con = openConnection(
				"http://localhost:8000/battlefield.js", 
				"GET");
		
		assertEquals(200, con.getResponseCode());
		
		try(InputStream is = con.getInputStream()) {
			String response = new String(is.readAllBytes());
			assertTrue(response.length() > 0);
		}
	}
	
	@Test
	void testCss() throws IOException {
		HttpURLConnection con = openConnection(
				"http://localhost:8000/w3.css", 
				"GET");
		
		assertEquals(200, con.getResponseCode());
		
		try(InputStream is = con.getInputStream()) {
			String response = new String(is.readAllBytes());
			assertTrue(response.length() > 0);
		}
	}
	
	@Test
	void testIco() throws IOException {
		assertThrows(SocketTimeoutException.class, () -> {
			HttpURLConnection con = openConnection(
					"http://localhost:8000/icon.ico", 
					"GET");
			
			con.getResponseCode();
		});
	}
	
	@Test
	void testApp() throws IOException, InterruptedException {
		GameState state = new GameState();
		final String sessionCookie;
		
		// receive empty game state
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app", 
					"GET");
			
			assertEquals(200, con.getResponseCode());
			
			sessionCookie = con.getHeaderFields().get("Set-cookie").get(0);
			assertTrue(sessionCookie.startsWith("session"));
			
			try(InputStream is = con.getInputStream()) {
				state.fromJSON(new String(is.readAllBytes()));
				assertEquals(GameAction.NOT_STARTED, state.action);
			}
		}
		
		
		Runnable getState = () -> {
			HttpURLConnection con;
			try {
				con = openConnection(
						"http://localhost:8000/app", 
						"POST");
				con.setRequestProperty("Cookie", sessionCookie);

				assertEquals(200, con.getResponseCode());
				
				try(InputStream is = con.getInputStream()) {
					state.fromJSON(new String(is.readAllBytes()));
				}
				
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue(false);
			}
		};
		
		// add ship length
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app?shipLengths", 
					"POST");
			con.setRequestProperty("Cookie", sessionCookie);
			con.setDoOutput(true);

			try(OutputStream os = con.getOutputStream()) {
				os.write(new ShipLengths(1).toJSON().getBytes());
			}
			
			assertEquals(200, con.getResponseCode());
			
			Thread.sleep(500);
			getState.run();
			
			assertArrayEquals(new int[] {1}, state.shipLengths.shipLengths);
		}
		
		// start game
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app?start", 
					"POST");
			con.setRequestProperty("Cookie", sessionCookie);
			
			assertEquals(200, con.getResponseCode());			

			Thread.sleep(500);
			getState.run();
			
			assertEquals(GameAction.PLACE_SHIPS, state.action);
		}
		
		// place ship
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app?placeShip", 
					"POST");
			con.setRequestProperty("Cookie", sessionCookie);
			con.setDoOutput(true);

			try(OutputStream os = con.getOutputStream()) {
				os.write(new ShipPlacement(
						true, 1, 0, 1
					).toJSON().getBytes());
			}
			
			assertEquals(200, con.getResponseCode());
			
			Thread.sleep(500);
			getState.run();
			
			assertEquals(
					new Board().addShip(1, 0, 1, true),
					state.playerState1.board
				);
			assertEquals(GameAction.PLAYER_1_TURN, state.action);
		}
		
		// strike
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app?strike", 
					"POST");
			con.setRequestProperty("Cookie", sessionCookie);
			con.setDoOutput(true);
			
			int[] enemyShip = BoardMasks.coordinate(state.playerState2.board.placedShips);
			
			try(OutputStream os = con.getOutputStream()) {
				os.write(new JSONWriter().write(enemyShip).getBytes());
			}
			
			assertEquals(200, con.getResponseCode());

			Thread.sleep(500);
			getState.run();

			assertEquals(GameAction.PLAYER_1_WIN, state.action);
		}
		
		// reset
		{
			HttpURLConnection con = openConnection(
					"http://localhost:8000/app?reset", 
					"POST");
			con.setRequestProperty("Cookie", sessionCookie);

			assertEquals(200, con.getResponseCode());

			Thread.sleep(500);
			getState.run();

			assertEquals(GameAction.NOT_STARTED, state.action);
		}
	}
}
