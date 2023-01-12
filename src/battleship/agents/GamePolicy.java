package battleship.agents;

import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;


import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.ShipLengths;
import battleship.model.GameState;

public class GamePolicy implements IGamePolicy {
	private final ShipLengths setup; 
	
	private GameState state = null;
	
	public GamePolicy(ShipLengths setup) {
		this.setup = setup;
	}
	
	@Override
	public void setGameState(GameState state) {
		this.state = state;
	}
	
	@Override
	public Future<Boolean> handle() {
		boolean hasChanged;
	
		switch(state.action) {
		case NOT_STARTED:
			state.getSynchronized((s) -> {
				s.action = GameAction.PLACE_SHIPS;
				s.shipLengths.shipLengths = setup.shipLengths.clone();
				
				s.playerState1.board = new Board();
				s.playerState1.unplacedShipLengths.shipLengths = setup.shipLengths.clone();
				
				s.playerState2.board = new Board();
				s.playerState2.unplacedShipLengths.shipLengths = setup.shipLengths.clone();
			});

			hasChanged = true;
			break;
			
		case PLACE_SHIPS_POLICY:
			state.getSynchronized((s) -> {
				if(state.playerState1.unplacedShipLengths.shipLengths.length == 0 
						&& state.playerState2.unplacedShipLengths.shipLengths.length == 0) 
					
					state.action = GameAction.PLAYER_1_TURN;
				else 
					state.action = GameAction.PLACE_SHIPS;
			});
			
			hasChanged = true;
			break;
		
		case PLAYER_1_TURN_END:
			state.getSynchronized((s) -> {
				if(state.playerState2.board.isDestroyed()) 
					state.action = GameAction.PLAYER_1_WIN;
				else 
					state.action = GameAction.PLAYER_2_TURN;
			});

			hasChanged = true;
			break;
			
		case PLAYER_2_TURN_END:
			state.getSynchronized((s) -> {
				if(state.playerState1.board.isDestroyed()) 
					state.action = GameAction.PLAYER_2_WIN;
				else {
					state.turn++;
					state.action = GameAction.PLAYER_1_TURN;
				}
			});

			hasChanged = true;
			break;
		
		default:
			hasChanged = false;
		}
		
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		future.complete(hasChanged);
		return future;
	}
}
