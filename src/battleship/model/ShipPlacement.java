package battleship.model;

import java.util.Map;


public class ShipPlacement implements IParcelable<ShipPlacement> {
	public boolean horizontal;
	public int startRow;
	public int startColumn;
	public int length;
	
	public ShipPlacement() {}
	
	public ShipPlacement(boolean horizontal, int startRow, int startColumn, int length) {
		super();
		this.horizontal = horizontal;
		this.startRow = startRow;
		this.startColumn = startColumn;
		this.length = length;
	}

	@Override
	public Object toParcelableObject() {
		return Map.of(
				"horizontal", horizontal,
				"startRow", startRow,
				"startColumn", startColumn,
				"length", length);
	}

	@Override
	public ShipPlacement fromParcelableObject(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> dict = (Map<String, Object>)obj;
		
		this.horizontal = (boolean)dict.get("horizontal");
		this.startRow = Integer.parseInt(dict.get("startRow").toString());
		this.startColumn = Integer.parseInt(dict.get("startColumn").toString());
		this.length = Integer.parseInt(dict.get("length").toString());
		
		return this;
	}

	@Override
	public String toString() {
		return toJSON();
	}
}