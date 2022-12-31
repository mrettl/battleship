package battleship.util;

import static battleship.util.BoardMasks.*;
import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;


class TestBoardMasks {

	@Test
	void testShiftRow() {
		assertEquals(0xFF00L, shiftRow(0xFFL, 1));
		assertEquals(0xFF00000000000000L, shiftRow(0x100FFL, 7));
		assertEquals(0L, shiftRow(~0L, 8));
		assertEquals(0L, shiftRow(~0L, 1000));
		
		assertEquals(0xFFL, shiftRow(0xFF00L, -1));
		assertEquals(0xFFL, shiftRow(0xFF00000100000000L, -7));
		assertEquals(0L, shiftRow(~0L, -8));
		assertEquals(0L, shiftRow(~0L, -1000));
	}
	
	@Test
	void testShiftColumn() {
		assertEquals(0x0202020202020202L, shiftColumn(COLUMN_MASKS[0] + 0b1000_0000L, 1));
		assertEquals(0x8080808080808080L, shiftColumn(~0L, 7));
		assertEquals(0L, shiftColumn(~0L, 8));
		assertEquals(0L, shiftColumn(~0L, 1000));
		
		assertEquals(0x0101010101010101L, shiftColumn(COLUMN_MASKS[1] + 0b0000_0001L, -1));
		assertEquals(0x0101010101010101L, shiftColumn(~0L, -7));
		assertEquals(0L, shiftColumn(~0L, -8));
		assertEquals(0L, shiftColumn(~0L, -1000));
	}
	
	@Test
	void testMaskFilledCheckRange() {
		assertTrue(maskFilledCheckRange(0, 0, 0, 0));
		assertTrue(maskFilledCheckRange(8, 8, 8, 8));
		assertTrue(maskFilledCheckRange(0, 8, 0, 8));
		
		assertFalse(maskFilledCheckRange(7, 9, 0, 1));
		assertFalse(maskFilledCheckRange(0, 1, 7, 9));
		
		assertFalse(maskFilledCheckRange(-1, 0, 0, 1));
		assertFalse(maskFilledCheckRange(0, 1, -1, 0));
		
		assertFalse(maskFilledCheckRange(1, 0, 0, 1));
		assertFalse(maskFilledCheckRange(0, 1, 1, 0));
	}
	
	@Test
	void testMaskFilled() {
		assertEquals(0L, maskFilled(0, 0, 0, 0));
		
		assertEquals(
				shiftRow(0b1000_0000, 0) |
				shiftRow(0b1000_0000, 1) |
				shiftRow(0b1000_0000, 2) |
				shiftRow(0b1000_0000, 3) |
				shiftRow(0b1000_0000, 4) |
				shiftRow(0b1000_0000, 5) |
				shiftRow(0b1000_0000, 6) |
				shiftRow(0b1000_0000, 7), 
				maskFilled(0, 8, 7, 8));
		
		assertEquals(
				shiftRow(0b0000_0000, 0) |
				shiftRow(0b0000_0000, 1) |
				shiftRow(0b0000_0000, 2) |
				shiftRow(0b0000_0000, 3) |
				shiftRow(0b0001_1111, 4) |
				shiftRow(0b0000_0000, 5) |
				shiftRow(0b0000_0000, 6) |
				shiftRow(0b0000_0000, 7), 
				maskFilled(4, 5, 0, 5));
		
		assertEquals(0x303030303030303L, maskFilled(-1, 9, -1, 2));
	}
	
	@Test
	void testMaskRectangularCorners() {
		assertEquals(
				(ROW_MASKS[0] | ROW_MASKS[7]) & (COLUMN_MASKS[0] | COLUMN_MASKS[7]), 
				maskRectangularCorners(0, 8, 0, 8));

		assertEquals(
				ROW_MASKS[5] & COLUMN_MASKS[2], 
				maskRectangularCorners(-1, 6, -1, 3));
		
		assertEquals(0L, maskRectangularCorners(0, 8, -1, 9));
		assertEquals(0L, maskRectangularCorners(-1, 9, 0, 8));
	}
	
	@Test
	void testGetSetMaskCoordinate() {
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				long m = mask(row, column);
				
				assertArrayEquals(
					new int[] {row, column}, 
					coordinate(m)
				);
				
				assertTrue(get(~0L, m));
				assertFalse(get(0L, m));
				
				assertFalse(get(set(~0L, m, false), m));
				assertTrue(get(set(0L, m, true), m));

				assertEquals(1, Long.bitCount(~0L ^ set(~0L, m, false)));
				assertEquals(0, Long.bitCount(~0L ^ set(~0L, m, true)));
				assertEquals(1, Long.bitCount(0L ^ set(0L, m, true)));
				assertEquals(0, Long.bitCount(0L ^ set(0L, m, false)));
			}
		}
		
		assertArrayEquals(null, coordinate(0L));
		assertEquals(0L, mask(0, 8));
	}
	
	@Test
	void testToString() {
		assertEquals(
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			"XXXX" + "XXXX" + System.lineSeparator() +
			System.lineSeparator(), 
			BoardMasks.toString(~0L)
			);
	}
	
	@Test
	void testToBoolean() {
		long pattern = set(0, mask(4, 5), true);
		boolean[][] array = toBoolean(pattern);
		long pattern2 = fromBoolean(array);
		
		assertEquals(pattern, pattern2);
	}
	
	@Test
	void testToJsonFromJson() {
		long pattern = set(0, mask(4, 5), true);
		String test = toJson(pattern);
		long pattern2 = fromJson(test);
		
		assertEquals(pattern, pattern2);
	}
}
