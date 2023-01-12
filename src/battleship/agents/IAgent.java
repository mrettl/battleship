package battleship.agents;

import java.util.concurrent.Future;

import battleship.model.GameState;

/**
 * 
 * An Agent changes the state of the game.
 * 
 * @author Matthias Rettl
 *
 */
public interface IAgent {
	
	/**
	 * Defines the {@link GameState}
	 * @param state {@link GameState}
	 */
	public void setGameState(GameState state);
	
	/**
	 * The agent might update the {@link GameState} defined previously {@link #setGameState(GameState)}.
	 * The returned {@link Future} signals if changes were made to the {@link GameState}.
	 * The game must not proceed while the Future is not done.
	 * 
	 * The {@link GameState} must be defined by {@link #setGameState(GameState)}
	 * before this function is called.
	 * 
	 * @return true if the {@link GameState} has been changed
	 */
	public Future<Boolean> handle();
}
