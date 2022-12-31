package battleship.model;

public enum GameAction implements IParcelable<GameAction> {
	NOT_STARTED,
	PLACE_SHIPS,
	PLACE_SHIPS_POLICY,
	PLAYER_1_TURN,
	PLAYER_1_TURN_END,
	PLAYER_2_TURN,
	PLAYER_2_TURN_END,
	PLAYER_1_WIN,
	PLAYER_2_WIN;

	@Override
	public Object toParcelableObject() {
		return this.name();
	}

	@Override
	public GameAction fromParcelableObject(Object obj) {
		return GameAction.valueOf((String)obj);
	}	
}