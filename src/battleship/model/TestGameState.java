package battleship.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestGameState {

	@Test
	void test() {
		GameState state = new GameState();
		
		state.action = GameAction.PLACE_SHIPS;
		state.turn = 15;
		state.shipLengths.shipLengths = new int[] {5, 4, 3};
		
		state.playerState1.unplacedShipLengths.shipLengths = new int[] {5, 4, 3, 2, 2};
		state.playerState1.board = new Board();
		
		state.playerState2.unplacedShipLengths.shipLengths = new int[] {};
		state.playerState2.board = Board.createRandomBoardPerDepthFirstSearch(
				new int[] {5, 4, 3, 2, 2});
		
		String text = state.toJSON();
		GameState state2 = new GameState();
		state2.fromJSON(text);
		
		assertEquals(state.action, state2.action);
		
		assertEquals(
				state.playerState1.board, 
				state2.playerState1.board);
		assertArrayEquals(
				state.playerState1.unplacedShipLengths.shipLengths, 
				state2.playerState1.unplacedShipLengths.shipLengths);

		assertEquals(
				state.playerState2.board, 
				state2.playerState2.board);
		assertArrayEquals(
				state.playerState2.unplacedShipLengths.shipLengths, 
				state2.playerState2.unplacedShipLengths.shipLengths);
		
		System.out.println(text);
	}

}
