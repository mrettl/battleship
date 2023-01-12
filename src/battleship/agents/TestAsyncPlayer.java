package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipPlacement;

class TestAsyncPlayer {
	@Test
	void test() throws InterruptedException, ExecutionException {

		GameState state = new GameState();
		Future<Boolean> hasChanged;

		AsyncPlayer player1 = new AsyncPlayer();
		player1.setPlayerOrder(true);
		player1.setGameState(state);
		
		AsyncPlayer player2 = new AsyncPlayer();
		player2.setPlayerOrder(false);
		player2.setGameState(state);
		
		//
		state.shipLengths.shipLengths = new int[] {1};
		state.action = GameAction.NOT_STARTED;
		
		//
		hasChanged = player1.handle();
		assertFalse(player1.isWaitingForShipPlacement());
		assertFalse(player1.isWaitingForStrike());
		assertTrue(hasChanged.isDone());
		assertFalse(hasChanged.get());
		
		//
		state.action = GameAction.PLACE_SHIPS;
		state.playerState1.board = state.playerState2.board = new Board();
		state.playerState1.unplacedShipLengths.shipLengths
			= state.playerState2.unplacedShipLengths.shipLengths
			= state.shipLengths.shipLengths;
		
		//
		hasChanged = player1.handle();
		assertTrue(player1.isWaitingForShipPlacement());
		assertFalse(player1.isWaitingForStrike());
		
		player1.placeShip(new ShipPlacement(false, 0, 0, 1));
		assertTrue(state.playerState1.board.isShip(0, 0));
		assertTrue(hasChanged.get());
		assertFalse(player1.isWaitingForShipPlacement());
		assertFalse(player1.isWaitingForStrike());
		
		//
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		state.action = GameAction.PLACE_SHIPS;

		//		
		hasChanged = player2.handle();
		assertTrue(player2.isWaitingForShipPlacement());
		assertFalse(player2.isWaitingForStrike());
		
		player2.placeShip(new ShipPlacement(false, 1, 1, 1));
		assertTrue(state.playerState2.board.isShip(1, 1));
		assertTrue(hasChanged.get());
		assertFalse(player2.isWaitingForShipPlacement());
		assertFalse(player2.isWaitingForStrike());
		
		//
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		state.action = GameAction.PLAYER_1_TURN;
		
		//
		hasChanged = player1.handle();
		assertTrue(player1.isWaitingForStrike());
		player1.strike(0, 0);
		
		//
		assertEquals(state.action, GameAction.PLAYER_1_TURN_END);
		state.action = GameAction.PLAYER_2_TURN;
		
		//
		hasChanged = player2.handle();
		assertTrue(player2.isWaitingForStrike());
		player2.strike(0, 0);
		
		//
		assertEquals(state.action, GameAction.PLAYER_2_TURN_END);
		assertTrue(state.playerState1.board.isDestroyed());
		state.action = GameAction.PLAYER_2_WIN;
		
		//
		assertFalse(player1.handle().get());
		assertFalse(player2.handle().get());
	}

}
