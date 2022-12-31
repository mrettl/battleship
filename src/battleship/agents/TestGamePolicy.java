package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipLengths;

class TestGamePolicy {

	@Test
	void test() {
		GamePolicy policy = new GamePolicy(new ShipLengths(5, 2));
		ComputerPlayer player1 = new ComputerPlayer(true, 0);
		ComputerPlayer player2 = new ComputerPlayer(false, 0);
		
		GameState state = new GameState();
		assertEquals(state.action, GameAction.NOT_STARTED);
		
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLACE_SHIPS);
		
		assertTrue(player1.handle(state));
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLACE_SHIPS);
		
		assertTrue(player2.handle(state));
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLAYER_1_TURN);

		assertTrue(player1.handle(state));
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLAYER_2_TURN);

		assertTrue(player2.handle(state));
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLAYER_1_TURN);
		
		assertTrue(player1.handle(state));

		for(int row = 0; row < 8; row++)
			for(int col = 0; col < 8; col++)
				state.playerState2.board = state.playerState2.board.addStrike(row, col);
		
		assertTrue(policy.handle(state));
		assertEquals(state.action, GameAction.PLAYER_1_WIN);
		assertFalse(policy.handle(state));

	}

}
