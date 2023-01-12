package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipLengths;

class TestGamePolicy {

	@Test
	void test() throws InterruptedException, ExecutionException {
		GameState state = new GameState();

		GamePolicy policy = new GamePolicy(new ShipLengths(5, 2));
		policy.setGameState(state);
		
		ComputerPlayer player1 = new ComputerPlayer(0);
		player1.setGameState(state);
		player1.setPlayerOrder(true);
		
		ComputerPlayer player2 = new ComputerPlayer(0);
		player2.setGameState(state);
		player2.setPlayerOrder(false);
		
		assertEquals(state.action, GameAction.NOT_STARTED);
		
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLACE_SHIPS);
		
		assertTrue(player1.handle().get());
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLACE_SHIPS);
		
		assertTrue(player2.handle().get());
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLAYER_1_TURN);

		assertTrue(player1.handle().get());
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLAYER_2_TURN);

		assertTrue(player2.handle().get());
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLAYER_1_TURN);
		
		assertTrue(player1.handle().get());

		for(int row = 0; row < 8; row++)
			for(int col = 0; col < 8; col++)
				state.playerState2.board = state.playerState2.board.addStrike(row, col);
		
		assertTrue(policy.handle().get());
		assertEquals(state.action, GameAction.PLAYER_1_WIN);
		assertFalse(policy.handle().get());

	}

}
