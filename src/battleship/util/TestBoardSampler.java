package battleship.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import battleship.model.Board;

class TestBoardSampler {

	@Test
	void test() {
		Board enemyBoard = new Board()
				.addShip(0, 0, 2, true)
				.addShip(0, 6, 2, true);
		
		for(int row = 0; row < 8; row++)
			for(int column = 0; column < 8; column++)
				enemyBoard = enemyBoard.addStrike(row, column);
		
		long hits = enemyBoard.hits();
		long failedStrikes = enemyBoard.misses();
		
		BoardSampler sampler = new BoardSampler(
				(b1) -> (b1.placedShips & failedStrikes) == 0, 
				(b2) -> (~b2.placedShips & hits) == 0, 
				2, 2);
		sampler.setMaxCounter(Integer.MAX_VALUE);
		
		Board reconstructedBoard = sampler.create();
		
		assertEquals(enemyBoard.placedShips, reconstructedBoard.placedShips);
		System.out.println(reconstructedBoard);
	}

}
