package battleship.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestPlayerState {

	@Test
	void test() {
		PlayerState playerState1 = new PlayerState();
		playerState1.unplacedShipLengths.shipLengths = new int[] {5, 4, 3, 2, 2};
		playerState1.board = new Board().addShip(1, 1, 3, false);
		
		PlayerState playerState2 = new PlayerState();
		playerState2.fromJSON(playerState1.toJSON());
		
		assertEquals(
				playerState1.board, 
				playerState2.board);
		assertArrayEquals(
				playerState1.unplacedShipLengths.shipLengths, 
				playerState2.unplacedShipLengths.shipLengths);
	}

}
