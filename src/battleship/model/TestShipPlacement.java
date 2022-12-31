package battleship.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestShipPlacement {

	@Test
	void test() {
		ShipPlacement placement = new ShipPlacement(true, 1, 2, 3);		
		ShipPlacement placement2 = new ShipPlacement();
		placement2.fromJSON(placement.toJSON());
		
		assertEquals(placement.horizontal, placement2.horizontal);
		assertEquals(placement.startRow, placement2.startRow);
		assertEquals(placement.startColumn, placement2.startColumn);
		assertEquals(placement.length, placement2.length);
		
	}

}
