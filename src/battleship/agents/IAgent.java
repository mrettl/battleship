package battleship.agents;

import battleship.model.GameState;

public interface IAgent {
	public boolean handle(GameState state);
}
