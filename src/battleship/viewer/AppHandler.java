package battleship.viewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.sun.net.httpserver.*;

import battleship.agents.ComputerPlayer;
import battleship.agents.GamePolicy;
import battleship.json.JSONReader;
import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipPlacement;


public class AppHandler implements HttpHandler {
	private long sessionCounter = 0L;
	private HashMap<Long, Session> sessions = new HashMap<>();	
	
	@Override
	public void handle(HttpExchange exchange) throws IOException {
        // System.out.println("@AppHanlder: " + exchange.getRequestURI());
        if(!exchange.getRequestURI().getPath().equals("/app"))
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
        
        long sessionId = -1L;
        if(sessionCookie != null) {
        	try {
        		sessionId = Long.parseLong(sessionCookie);
        	} catch(NumberFormatException e) {
        		sessionCookie = null;
        		System.out.println("parseLong(" + sessionCookie + ") failed");
        	}
        }
        	
        if(sessionCookie == null || !sessions.containsKey(sessionId)) {
        	sessionId = ++sessionCounter;
        	responseHeader.set("Set-Cookie", "session=" + sessionId);
        	
        	sessions.put(sessionId, new Session());
        }
        
        // handle session
        Session session = sessions.get(sessionId);
        GameState state = session.state;
        
        //
        InputStream is = exchange.getRequestBody();
        String request = new String(is.readAllBytes());
        
        //
        String query = exchange.getRequestURI().getQuery();
        query = query == null ? "" : query;
        
        if(query.equals("shipLengths")) {
        	state.shipLengths.fromJSON(request);
        } else if(query.equals("start")) {
        	state.action = GameAction.NOT_STARTED;
        	state.turn = 0;
        	GamePolicy policy = session.policy = new GamePolicy(state.shipLengths);
        	policy.handle(state);
        	
        	ComputerPlayer player2 = session.player2 = new ComputerPlayer(false);
        	player2.handle(state);
        	policy.handle(state);
        	
        } else if(query.equals("placeShip")) {
        	ShipPlacement placement = new ShipPlacement().fromJSON(request);
        	
        	List<Integer> shipLengths = 
        			IntStream.of(state.playerState1.unplacedShipLengths.shipLengths)
        			.mapToObj(v -> v)
        			.collect(Collectors.toList());
        	
        	if(shipLengths.remove((Integer)placement.length)) {		
        		Board newBoard = state.playerState1.board.addShip(
        				placement.startRow, 
        				placement.startColumn, 
        				placement.length, 
        				placement.horizontal);
        		
        		if(newBoard != null) {
        			state.playerState1.unplacedShipLengths.shipLengths = 
        					shipLengths.stream().mapToInt(v -> v).toArray();
            		state.playerState1.board = newBoard;
            		state.action = GameAction.PLACE_SHIPS_POLICY;
            		session.policy.handle(state);
        		}
        	}
        } else if(query.equals("strike")) {
        	@SuppressWarnings("unchecked")
			List<Long> coordinates = (List<Long>) new JSONReader().read(request);
        	int row = coordinates.get(0).intValue();
        	int col = coordinates.get(1).intValue();
        	
        	state.playerState2.board = state.playerState2.board.addStrike(row, col);
        	state.action = GameAction.PLAYER_1_TURN_END;
        	
        	session.policy.handle(state);
        	session.player2.handle(state);
        	session.policy.handle(state);
        }

        
        //
        try(OutputStream os = exchange.getResponseBody()) {
        	byte[] response = state.toJSON().getBytes();
        	exchange.sendResponseHeaders(200, response.length);
        	os.write(response);
        }
	}
	
	private static class Session {
		public GameState state;
		public GamePolicy policy;
		public ComputerPlayer player2;
		
		public Session() {
			this.state = new GameState();
			this.policy = null;
			this.player2 = null;
		}
	}
}
