package battleship.model;

import java.util.Map;

public class PlayerState implements IParcelable<PlayerState> {
	public final ShipLengths unplacedShipLengths;
	public Board board;
	
	/**
	 * memory free to use by the player (e.g. for caching)
	 */
	public transient Object playerMemory;
	
	public PlayerState() {
		this.unplacedShipLengths = new ShipLengths();
		this.board = new Board();
	}
	
	@Override
	public Object toParcelableObject() {
		return Map.of(
				"unplacedShipLengths", unplacedShipLengths.toParcelableObject(),
				"board", board.toParcelableObject());
	}

	@Override
	public PlayerState fromParcelableObject(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		this.unplacedShipLengths.fromParcelableObject(map.get("unplacedShipLengths"));
		this.board = (Board)this.board.fromParcelableObject(map.get("board"));
		return this;
	}

	@Override
	public String toString() {
		return toJSON();
	}
}