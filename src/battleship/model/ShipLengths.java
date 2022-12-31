package battleship.model;

import java.util.List;

public class ShipLengths implements IParcelable<ShipLengths> {
	public int[] shipLengths;
	
	public ShipLengths() {
		this(new int[0]);
	}
	
	public ShipLengths(int... shipLengths) {
		this.shipLengths = shipLengths;
	}
	
	@Override
	public Object toParcelableObject() {
		return shipLengths;
	}

	@Override
	public ShipLengths fromParcelableObject(Object obj) {
		@SuppressWarnings("unchecked")
		List<Long> array = (List<Long>) obj;
		shipLengths = array.stream().mapToInt(v -> v.intValue()).toArray();
		return this;
	}
	
	@Override
	public String toString() {
		return toJSON();
	}
}
