package battleship.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestShipLengths {

	@Test
	void test() {
		ShipLengths ships1 = new ShipLengths(5, 4, 3);
		ShipLengths ships2 = new ShipLengths();
		ships2.fromJSON(ships1.toJSON());
		
		assertArrayEquals(ships1.shipLengths, ships2.shipLengths);
	}

}
