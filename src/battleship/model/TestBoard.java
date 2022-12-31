package battleship.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import battleship.util.BoardMasks;


class TestBoard {
	@Test
	void testIsShip() {
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				Board board = new Board().addShip(row, column, 1, false);
				
				
				for(int row2 = 0; row2 < 8; row2++)
					for(int column2 = 0; column2 < 8; column2++) 
						if(row == row2 && column == column2)
							assertTrue(board.isShip(row2, column2));
						else
							assertFalse(board.isShip(row2, column2));
			}
		}
	}
	
	@Test
	void testIsSafetyZone() {
		Board board = new Board().addShip(4, 4, 1, false);
		
		assertFalse(board.isSafetyZone(5, 5));
		assertTrue(board.isSafetyZone(4, 5));
	}
	
	@Test
	void testStrikeIsStrike() {
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				Board board = new Board().addStrike(row, column);
				
				
				for(int row2 = 0; row2 < 8; row2++)
					for(int column2 = 0; column2 < 8; column2++) 
						if(row == row2 && column == column2)
							assertTrue(board.isStrike(row2, column2));
						else
							assertFalse(board.isStrike(row2, column2));
			}
		}
	}
	
	@Test
	void testIsHitIsDestroyed() {
		Board board = new Board()
				.addShip(4, 1, 3, true)
				.addShip(0, 7, 4, false);
		
		assertFalse(board.isDestroyed());
		
		assertFalse(board.isStrike(0, 0));
		board = board.addStrike(0, 0);
		assertTrue(board.isStrike(0, 0));
		assertFalse(board.isHit(0, 0));
		assertFalse(board.isHit(4, 1));
		assertFalse(board.isDestroyed());
		
		board = board
				.addStrike(4, 1).addStrike(4, 2).addStrike(4, 3)
				.addStrike(0, 7).addStrike(1, 7).addStrike(2, 7).addStrike(3, 7);
		
		assertTrue(board.isHit(4, 1));
		assertTrue(board.isDestroyed());
	}
	
	@Test
	void testHashCodeEquals() {
		Board b1 = new Board().addShip(0, 0, 1, false).addStrike(0, 1);
		Board b2 = new Board().addShip(0, 0, 1, false).addStrike(0, 1);
		Board b3 = new Board().addShip(0, 0, 1, false);
		Board b4 = new Board().addStrike(0, 1);
		
		assertEquals(b1, b1);
		assertEquals(b1, b2);
		assertEquals(b1.hashCode(), b2.hashCode());
		
		assertNotEquals(b1, b3);
		assertNotEquals(b1, b4);
	}
	
	@Test
	void testAddShip() {
		Board emptyBoard = new Board();
		
		assertNull(emptyBoard.addShip(0, 0, 9, true));
		assertNull(emptyBoard.addShip(0, 0, 9, false));
		
		assertNull(emptyBoard.addShip(-1, 0, 2, false));
		assertNull(emptyBoard.addShip(0, -1, 2, false));
		
		assertNull(emptyBoard.addShip(-1, 0, 2, true));
		assertNull(emptyBoard.addShip(0, -1, 2, true));
		
		Board board1 = emptyBoard.addShip(5, 2, 4, true);
		assertEquals(
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"  XX" + "XX  " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				System.lineSeparator(), 
				BoardMasks.toString(board1.placedShips)
				);
		assertEquals(
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				"  XX" + "XX  " + System.lineSeparator() +
				" XXX" + "XXX " + System.lineSeparator() +
				"  XX" + "XX  " + System.lineSeparator() +
				"    " + "    " + System.lineSeparator() +
				System.lineSeparator(), 
				BoardMasks.toString(board1.safetyZones)
				);
		
		assertNull(board1.addShip(0, 5, 6, false));
		assertNull(board1.addShip(0, 5, 5, false));
		
		assertNull(board1.addShip(6, 5, 2, false));
		assertNull(board1.addShip(6, 5, 2, false));
		
		Board board2 = board1.addShip(0, 0, 8, false);
		assertEquals(
				"X   " + "    " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				"X XX" + "XX  " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				"X   " + "    " + System.lineSeparator() +
				System.lineSeparator(), 
				BoardMasks.toString(board2.placedShips)
				);
		assertEquals(
				"XX  " + "    " + System.lineSeparator() +
				"XX  " + "    " + System.lineSeparator() +
				"XX  " + "    " + System.lineSeparator() +
				"XX  " + "    " + System.lineSeparator() +
				"XXXX" + "XX  " + System.lineSeparator() +
				"XXXX" + "XXX " + System.lineSeparator() +
				"XXXX" + "XX  " + System.lineSeparator() +
				"XX  " + "    " + System.lineSeparator() +
				System.lineSeparator(), 
				BoardMasks.toString(board2.safetyZones)
				);
		
	}
	
	@Test
	void testCreateRandomBoardTrialError() {
		Board b = Board.createRandomBoardPerTrialAndError(
				1, 1, 1, 1, 
				1, 1, 1, 1,
				2, 2, 3, 3);
		assertNotNull(b);
		System.out.println(b);
	}
	
	@Test
	void testCreateRandomBoardDFS() {
		Board b = Board.createRandomBoardPerDepthFirstSearch(
				1, 1, 1, 1, 
				1, 1, 1, 1,
				2, 2, 3, 3);
		assertNotNull(b);
		System.out.println(b);
	}
	
	@Test
	void testParcelable() {
		Board board1 = new Board().addShip(0, 0, 2, true);
		Board board2 = new Board().fromJSON(board1.toJSON());
		
		assertEquals(board1, board2);
	}
}
