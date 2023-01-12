package battleship.agents;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.PlayerState;
import battleship.model.ShipPlacement;

public class AsyncPlayer implements IPlayer {

	private boolean isPlayer1;
	private GameState state;
	
	private CompletableFuture<Boolean> hasPlacedShips = null;
	private CompletableFuture<Boolean> myTurn = null;
	
	public AsyncPlayer() {
		this.isPlayer1 = true;
		this.state = null;
	}
	
	@Override
	public void setPlayerOrder(boolean isPlayer1) {
		this.isPlayer1 = isPlayer1;
	}
	
	@Override
	public void setGameState(GameState state) {
		this.state = state;
	}

	@Override
	public Future<Boolean> handle() {
		switch(state.action) {
		case PLACE_SHIPS:
			PlayerState myState = isPlayer1 ? state.playerState1 : state.playerState2;
			if(myState.unplacedShipLengths.shipLengths.length > 0)
				return hasPlacedShips = new CompletableFuture<>();
			else
				break;
			
		case PLAYER_1_TURN: 
			if(isPlayer1)
				return myTurn = new CompletableFuture<Boolean>();
			else
				break;
			
		case PLAYER_2_TURN:
			if(!isPlayer1)
				return myTurn = new CompletableFuture<Boolean>();
			else
				break;

		default:
		}
		
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		future.complete(false);
		return future;
	}
	
	public boolean isWaitingForShipPlacement() {
		return hasPlacedShips != null && !hasPlacedShips.isDone();
	}
	
	public boolean placeShip(ShipPlacement placement) {
		if(hasPlacedShips == null)
			return false;
		
		state.getSynchronized((s) -> {
			PlayerState myState = isPlayer1 ? s.playerState1 : s.playerState2;

        	List<Integer> shipLengths = 
        			IntStream.of(myState.unplacedShipLengths.shipLengths)
        			.mapToObj(v -> v)
        			.collect(Collectors.toList());
        	
        	if(shipLengths.remove((Integer)placement.length)) {		
        		Board newBoard = myState.board.addShip(
        				placement.startRow, 
        				placement.startColumn, 
        				placement.length, 
        				placement.horizontal);
        		
        		if(newBoard != null) {
        			myState.unplacedShipLengths.shipLengths = 
        					shipLengths.stream().mapToInt(v -> v).toArray();
        			myState.board = newBoard;
            		state.action = GameAction.PLACE_SHIPS_POLICY;
        		}
        	}
	
        	hasPlacedShips.complete(true);
		});
		
		hasPlacedShips = null;
		return true;
	}
	
	public boolean isWaitingForStrike() {
		return myTurn != null && !myTurn.isDone();
	}
	
	public boolean strike(int row, int col) {
		if(myTurn == null)
			return false;
		
		state.getSynchronized((s) -> {
			PlayerState enemyState;
			if(isPlayer1) {
				enemyState = s.playerState2;
				s.action = GameAction.PLAYER_1_TURN_END;
			} else {
				enemyState = s.playerState1;
				s.action = GameAction.PLAYER_2_TURN_END;
			}
        	enemyState.board = enemyState.board.addStrike(row, col);
        	myTurn.complete(true);
		});
		
		myTurn = null;
		return true;
	}
}
