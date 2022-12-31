package battleship.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import battleship.model.Board;

class TestBoardFieldProbabilities {

	@Test
	void test() {
		Board enemyBoard = new Board()
				.addShip(6, 2, 4, true)
				.addStrike(0, 0).addStrike(1, 1).addStrike(2, 2).addStrike(3, 3)
				.addStrike(4, 4).addStrike(5, 5).addStrike(6, 6).addStrike(7, 7)
				.addStrike(4, 0).addStrike(5, 1).addStrike(6, 2).addStrike(7, 3)
				.addStrike(5, 2);
		
		BoardFieldProbabilities bfp = new BoardFieldProbabilities(enemyBoard, 4);
		
		assertNotNull(bfp.getMostProbableField());
		System.out.println(bfp);
		
		for(int sample = 0; sample < 100; sample++)
			bfp.sampleBoard();
		
		assertArrayEquals(new int[] {6, 3}, bfp.getMostProbableField());
		assertEquals(1d, bfp.getFieldProbabilities()[6][3], 1e-3);
		
		System.out.println(bfp);
	}

}
