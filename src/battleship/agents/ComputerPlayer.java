package battleship.agents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.PlayerState;
import battleship.util.BoardFieldProbabilities;

public class ComputerPlayer implements IPlayer {
	private int difficulty;
	
	private boolean isPlayer1;
	private GameState state;
	
	private boolean hasPrintedResult = false;
	private ArrayList<Board> cachedBoards;

	
	public ComputerPlayer() {
		this(100);
	}
	
	public ComputerPlayer(int difficulty) {
		this.difficulty = difficulty;
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
		boolean hasChanged = false;
		PlayerState myState;
		PlayerState enemyState;
		
		if(isPlayer1) {
			myState = state.playerState1;
			enemyState = state.playerState2;
		} else {
			myState = state.playerState2;
			enemyState = state.playerState1;
		}
		
		switch(state.action) {
		case PLACE_SHIPS:
			if(myState.unplacedShipLengths.shipLengths.length > 0) {
				Board board = Board.createRandomBoardPerDepthFirstSearch(
						myState.board,
						myState.unplacedShipLengths.shipLengths);
				
				state.getSynchronized((s) -> {
					myState.unplacedShipLengths.shipLengths = new int[0];
					myState.board = board;
					state.action = GameAction.PLACE_SHIPS_POLICY;
				});
				
				hasChanged = true;
			}
			break;
		
		case PLAYER_1_TURN:
			if(isPlayer1) {
				int[] field = nextStrike(state, myState, enemyState);
				
				state.getSynchronized((s) -> {
					state.action = GameAction.PLAYER_1_TURN_END;
					enemyState.board = enemyState.board.addStrike(field[0], field[1]);
				});
				
				hasChanged = true;
			}
			break;
			
		case PLAYER_2_TURN:
			if(!isPlayer1) {
				int[] field = nextStrike(state, myState, enemyState);
				
				state.getSynchronized((s) -> {
					state.action = GameAction.PLAYER_2_TURN_END;
					enemyState.board = enemyState.board.addStrike(field[0], field[1]);
				});
				
				hasChanged = true;
			}
			break;
		
		case PLAYER_1_WIN:
			if(isPlayer1)
				win();
			else
				loose();
			break;
		
		case PLAYER_2_WIN:
			if(!isPlayer1) 
				win();
			else
				loose();
			break;
		
		default:
		}
		
		CompletableFuture<Boolean> future = new CompletableFuture<>();
		future.complete(hasChanged);
		return future;
	}

	public int[] nextStrike(GameState state, PlayerState myState, PlayerState enemyState) {		
		BoardFieldProbabilities probabilities = new BoardFieldProbabilities(
				enemyState.board, state.shipLengths.shipLengths);
		
		if(cachedBoards == null)
			cachedBoards = new ArrayList<Board>();
		
		// filter cached boards
		Iterator<Board> iterator = cachedBoards.iterator();
		while(iterator.hasNext())
			if(probabilities.sampleBoard(iterator.next()) == null)
				iterator.remove();
		
		// sample new boards
		for(int sample = 0; sample < difficulty; sample++) {
			Board sampledBoard = probabilities.sampleBoard();
			if(sampledBoard != null)
				cachedBoards.add(sampledBoard);
		}
		
		return probabilities.getMostProbableField();
	}
	
	public void win() {
		if(hasPrintedResult)
			return;
		
		hasPrintedResult = true;
		System.out.println("computer player #" + this.hashCode() + 
				" (difficulty=" + difficulty + ") wins the game.");
	}

	public void loose() {
		if(hasPrintedResult)
			return;
		
		hasPrintedResult = true;
		System.out.println("computer player #" + this.hashCode() + 
				" (difficulty=" + difficulty + ") looses the game.");
	}
}
