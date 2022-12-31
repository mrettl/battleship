package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import battleship.model.Board;
import battleship.model.GameAction;
import battleship.model.GameState;

class TestComputerPlayer {

	@Test
	void test() {
		ComputerPlayer player1 = new ComputerPlayer(true, 10);
		ComputerPlayer player2 = new ComputerPlayer(false, 10);
		GameState state = new GameState();
		state.shipLengths.shipLengths = new int[] {3};
	
		state.action = GameAction.NOT_STARTED;	
		assertFalse(player1.handle(state));
		
		state.action = GameAction.PLACE_SHIPS;
		state.playerState1.board = state.playerState2.board = new Board();
		state.playerState1.unplacedShipLengths.shipLengths
			= state.playerState2.unplacedShipLengths.shipLengths
			= state.shipLengths.shipLengths;
		
		assertTrue(player1.handle(state));
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		assertArrayEquals(state.playerState1.unplacedShipLengths.shipLengths, new int[0]);
		assertEquals(3, Long.bitCount(state.playerState1.board.placedShips));
		
		assertArrayEquals(state.playerState2.unplacedShipLengths.shipLengths, new int[] {3});
		assertEquals(0, Long.bitCount(state.playerState2.board.placedShips));
		assertFalse(player1.handle(state));
		
		state.action = GameAction.PLACE_SHIPS;
		assertTrue(player2.handle(state));
		assertEquals(state.action, GameAction.PLACE_SHIPS_POLICY);
		assertArrayEquals(state.playerState2.unplacedShipLengths.shipLengths, new int[0]);
		assertEquals(3, Long.bitCount(state.playerState2.board.placedShips));

		state.action = GameAction.PLAYER_1_TURN;
		assertFalse(player2.handle(state));
		assertTrue(player1.handle(state));
		assertEquals(GameAction.PLAYER_1_TURN_END, state.action);
		assertTrue(state.playerState1.board.strikes == 0L);
		assertTrue(state.playerState2.board.strikes != 0L);
		
		state.action = GameAction.PLAYER_2_TURN;
		assertFalse(player1.handle(state));
		assertTrue(player2.handle(state));
		assertEquals(GameAction.PLAYER_2_TURN_END, state.action);
		assertTrue(state.playerState1.board.strikes != 0L);
		assertTrue(state.playerState2.board.strikes != 0L);
		
		state.action = GameAction.PLAYER_2_WIN;
		assertFalse(player1.handle(state));
		assertFalse(player2.handle(state));
		
		state.action = GameAction.PLAYER_1_WIN;
		assertFalse(player1.handle(state));
		assertFalse(player2.handle(state));
	}
}
