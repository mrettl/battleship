package battleship.viewer.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.stringtree.json.JSONReader;

import com.sun.net.httpserver.*;

import battleship.agents.AsyncPlayer;
import battleship.agents.ComputerPlayer;
import battleship.agents.GameManager;
import battleship.agents.GamePolicy;
import battleship.agents.IPlayer;
import battleship.agents.GameManager.GameSession;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipLengths;
import battleship.model.ShipPlacement;


public class GameHandler implements HttpHandler {
	private GameManager manager = GameManager.common();
	
	private int maxDelayMillis = 500;
	private HashMap<UUID, WebSession> sessions = new HashMap<>();
	private final String webPath;
	
	public GameHandler(HttpServer server, String webPath) {
		this.webPath = webPath;
		server.createContext(this.webPath, this);
	}
	
	public void setMaxResponseDelay(int maxDelay) {
		this.maxDelayMillis = maxDelay;
	}
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
        if(!exchange.getRequestURI().getPath().equals(webPath))
        	return;
        
        Headers requestHeader = exchange.getRequestHeaders();
        Headers responseHeader = exchange.getResponseHeaders();
        
        // handle session cookie
        String cookies = "";
        for(String c : requestHeader.getOrDefault("Cookie", List.of()))
        	cookies += c;
        
        String sessionCookie = Stream.of(cookies.split(";"))
        		.map(c -> c.strip().split("="))
        		.filter(c -> c[0].strip().equals("session"))
        		.map(c -> c[1].strip())
        		.findAny().orElse(null);
        
        UUID sessionId = null;
        if(sessionCookie != null) {
        	try {
        		sessionId = UUID.fromString(sessionCookie);
        	} catch(IllegalArgumentException e) {
        		e.printStackTrace();
        		sessionId = null;
        	}
        }
        	
        if(sessionCookie == null || !sessions.containsKey(sessionId)) {
        	sessionId = UUID.randomUUID();
        	responseHeader.set("Set-Cookie", "session=" + sessionId.toString());
        	
        	sessions.put(sessionId, new WebSession());
        }
        
        // handle session
        WebSession session = sessions.get(sessionId);
        GameState state = session.state;
        
        //
        InputStream is = exchange.getRequestBody();
        String request = new String(is.readAllBytes());
        
        //
        String query = exchange.getRequestURI().getQuery();
        query = query == null ? "" : query;
        
        if(query.equals("reset")) {
        	session.reset();
        	
        } else if(query.equals("shipLengths")) {
        	state.shipLengths.fromJSON(request);
        	
        } else if(query.equals("start")) {
        	@SuppressWarnings("unchecked")
			Map<String, Object> settings = (Map<String, Object>) new JSONReader().read(request);
        	session.start((Boolean)settings.get("isPlayer1"));
        	
        } else if(query.equals("placeShip") && session.player != null) {
        	ShipPlacement placement = new ShipPlacement().fromJSON(request);
        	session.player.placeShip(placement);
        	
        } else if(query.equals("strike") && session.player != null) {
        	@SuppressWarnings("unchecked")
			List<Long> coordinates = (List<Long>) new JSONReader().read(request);
        	int row = coordinates.get(0).intValue();
        	int col = coordinates.get(1).intValue();
        	
        	session.player.strike(row, col);
        }
        
        // wait until the next players' turn or until the max delay
        if(session.gameSession != null)
        	manager.signalPendingSessions(maxDelayMillis);
        
        //
    	byte[] response = state.applySynchronized((s) -> s.toJSON().getBytes());
        try(OutputStream os = exchange.getResponseBody()) {
        	exchange.sendResponseHeaders(200, response.length);
        	os.write(response);
        }
	}
	
	private class WebSession {
		public GameState state;
		public GameSession gameSession;
		public AsyncPlayer player;
		
		public WebSession() {
			state = new GameState();
			gameSession = null;
			player = null;
		}
		
		public void reset() {
			ShipLengths previousConfig = state.shipLengths;
			state = new GameState();
			state.shipLengths.shipLengths = previousConfig.shipLengths;
			
			player = null;
		}
		
		public void start(boolean isPlayer1) {
			state.getSynchronized((s) -> {
				state.action = GameAction.NOT_STARTED;
				state.turn = 0;
			});
			
			AsyncPlayer client = player = new AsyncPlayer();
			IPlayer opposite = new ComputerPlayer();
			
			gameSession = manager.addSession(
					this.state, 
					new GamePolicy(this.state.shipLengths),
					isPlayer1 ? client : opposite, 
					isPlayer1 ? opposite : client);
		}
	}
}
