package battleship.agents;

import java.util.ArrayList;
import java.util.Iterator;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.PlayerState;
import battleship.util.BoardFieldProbabilities;

public class ComputerPlayer implements IPlayer {
	private final boolean isPlayer1;
	private final int difficulty;
	
	public ComputerPlayer(boolean isPlayer1) {
		this(isPlayer1, 100);
	}
	
	public ComputerPlayer(boolean isPlayer1, int difficulty) {
		this.isPlayer1 = isPlayer1;
		this.difficulty = difficulty;
	}
	
	@Override
	public boolean handle(GameState state) {
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
				
				synchronized (state) {
					myState.unplacedShipLengths.shipLengths = new int[0];
					myState.board = board;
					state.action = GameAction.PLACE_SHIPS_POLICY;
				}
				return true;
			} else {
				return false;
			}
		
		case PLAYER_1_TURN:
			if(isPlayer1) {
				int[] field = nextStrike(state, myState, enemyState);
				
				synchronized (state) {
					state.action = GameAction.PLAYER_1_TURN_END;
					enemyState.board = enemyState.board.addStrike(field[0], field[1]);
				}
				return true;
			} else {
				return false;
			}
			
		case PLAYER_2_TURN:
			if(!isPlayer1) {
				int[] field = nextStrike(state, myState, enemyState);
				
				synchronized (state) {
					state.action = GameAction.PLAYER_2_TURN_END;
					enemyState.board = enemyState.board.addStrike(field[0], field[1]);
				}
				return true;
			} else {
				return false;
			}
		
		case PLAYER_1_WIN:
			if(isPlayer1)
				win();
			else
				loose();
			return false;
		
		case PLAYER_2_WIN:
			if(!isPlayer1) 
				win();
			else
				loose();
			return false;
			
		default:
			return false;
		}
	}

	public int[] nextStrike(GameState state, PlayerState myState, PlayerState enemyState) {		
		BoardFieldProbabilities probabilities = new BoardFieldProbabilities(
				enemyState.board, state.shipLengths.shipLengths);
		
		if(myState.playerMemory == null)
			myState.playerMemory = new ArrayList<Board>();
		
		// filter cached boards
		@SuppressWarnings("unchecked")
		ArrayList<Board> cachedBoards = (ArrayList<Board>) myState.playerMemory;
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
		System.out.println("computer player #" + this.hashCode() + 
				" (difficulty=" + difficulty + ") wins the game.");
	}

	public void loose() {
		System.out.println("computer player #" + this.hashCode() + 
				" (difficulty=" + difficulty + ") looses the game.");
	}
}
