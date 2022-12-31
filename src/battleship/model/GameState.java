package battleship.model;

import java.util.Map;

public class GameState implements IParcelable<GameState> {
	public GameAction action;
	public int turn;
	
	public final ShipLengths shipLengths;
	public final PlayerState playerState1;
	public final PlayerState playerState2;

	
	public GameState() {
		this.action = GameAction.NOT_STARTED;
		this.turn = 0;
		
		this.shipLengths = new ShipLengths();
		this.playerState1 = new PlayerState();
		this.playerState2 = new PlayerState();
	}

	@Override
	public Object toParcelableObject() {
		return Map.of(
				"action", action.toParcelableObject(),
				"turn", turn,
				"shipLengths", shipLengths.toParcelableObject(),
				"playerState1", playerState1.toParcelableObject(),
				"playerState2", playerState2.toParcelableObject()
			);
	}

	@Override
	public GameState fromParcelableObject(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		this.action = this.action.fromParcelableObject(map.get("action"));
		this.turn = ((Long)map.get("turn")).intValue();
		this.shipLengths.fromParcelableObject(map.get("shipLengths"));
		this.playerState1.fromParcelableObject(map.get("playerState1"));
		this.playerState2.fromParcelableObject(map.get("playerState2"));

		return this;
	}

	@Override
	public String toString() {
		return toJSON();
	}
}
