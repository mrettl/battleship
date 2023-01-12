package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;

class TestComputerPlayer {

	@Test
	void test() throws InterruptedException, ExecutionException {
		GameState state = new GameState();

		ComputerPlayer player1 = new ComputerPlayer(10);
		player1.setPlayerOrder(true);
		player1.setGameState(state);
		
		ComputerPlayer player2 = new ComputerPlayer(10);
		player2.setPlayerOrder(false);
		player2.setGameState(state);
		
		state.shipLengths.shipLengths = new int[] {3};
		state.action = GameAction.NOT_STARTED;	
		assertFalse(player1.handle().get());
		
		state.action = GameAction.PLACE_SHIPS;
		state.playerState1.board = state.playerState2.board = new Board();
		state.playerState1.unplacedShipLengths.shipLengths
			= state.playerState2.unplacedShipLengths.shipLengths
			= state.shipLengths.shipLengths;
		
		assertTrue(player1.handle().get());
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		assertArrayEquals(state.playerState1.unplacedShipLengths.shipLengths, new int[0]);
		assertEquals(3, Long.bitCount(state.playerState1.board.placedShips));
		
		assertArrayEquals(state.playerState2.unplacedShipLengths.shipLengths, new int[] {3});
		assertEquals(0, Long.bitCount(state.playerState2.board.placedShips));
		assertFalse(player1.handle().get());
		
		state.action = GameAction.PLACE_SHIPS;
		assertTrue(player2.handle().get());
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		assertArrayEquals(state.playerState2.unplacedShipLengths.shipLengths, new int[0]);
		assertEquals(3, Long.bitCount(state.playerState2.board.placedShips));

		state.action = GameAction.PLAYER_1_TURN;
		assertFalse(player2.handle().get());
		assertTrue(player1.handle().get());
		assertEquals(GameAction.PLAYER_1_TURN_END, state.action);
		assertTrue(state.playerState1.board.strikes == 0L);
		assertTrue(state.playerState2.board.strikes != 0L);
		
		state.action = GameAction.PLAYER_2_TURN;
		assertFalse(player1.handle().get());
		assertTrue(player2.handle().get());
		assertEquals(GameAction.PLAYER_2_TURN_END, state.action);
		assertTrue(state.playerState1.board.strikes != 0L);
		assertTrue(state.playerState2.board.strikes != 0L);
		
		state.action = GameAction.PLAYER_2_WIN;
		assertFalse(player1.handle().get());
		assertFalse(player2.handle().get());
		
		state.action = GameAction.PLAYER_1_WIN;
		assertFalse(player1.handle().get());
		assertFalse(player2.handle().get());
	}
}
