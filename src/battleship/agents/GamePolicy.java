package battleship.agents;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.ShipLengths;
import battleship.model.GameState;

public class GamePolicy implements IGamePolicy {
	private final ShipLengths setup; 
	
	public GamePolicy(ShipLengths setup) {
		this.setup = setup;
	}
	
	@Override
	public boolean handle(GameState state) {
		synchronized (state) {
			switch(state.action) {
			case NOT_STARTED:
				state.action = GameAction.PLACE_SHIPS;
				state.shipLengths.shipLengths = setup.shipLengths.clone();
				
				state.playerState1.board = new Board();
				state.playerState1.unplacedShipLengths.shipLengths = setup.shipLengths.clone();
				
				state.playerState2.board = new Board();
				state.playerState2.unplacedShipLengths.shipLengths = setup.shipLengths.clone();
				
				return true;
				
			case PLACE_SHIPS_POLICY:
				if(state.playerState1.unplacedShipLengths.shipLengths.length == 0 
						&& state.playerState2.unplacedShipLengths.shipLengths.length == 0) 
					
					state.action = GameAction.PLAYER_1_TURN;
				else 
					state.action = GameAction.PLACE_SHIPS;
				
				return true;
			
			case PLAYER_1_TURN_END:
				if(state.playerState2.board.isDestroyed()) 
					state.action = GameAction.PLAYER_1_WIN;
				else 
					state.action = GameAction.PLAYER_2_TURN;

				return true;
				
			case PLAYER_2_TURN_END:
				if(state.playerState1.board.isDestroyed()) 
					state.action = GameAction.PLAYER_2_WIN;
				else {
					state.turn++;
					state.action = GameAction.PLAYER_1_TURN;
				}
					

				return true;
			
			default:
				return false;
			}
		}
	}
}
