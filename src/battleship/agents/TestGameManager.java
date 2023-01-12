package battleship.agents;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import battleship.agents.GameManager.GameSession;
import battleship.model.GameAction;
import battleship.model.GameState;
import battleship.model.ShipLengths;
import battleship.model.ShipPlacement;

class TestGameManager {
	static GameManager manager = GameManager.common();
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		manager.startInNewThread();
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		manager.stop();
	}

	@Test
	void test() {
		GameSession session1 = manager.addSession(
				new GameState(),
				new GamePolicy(new ShipLengths(5, 4, 3, 2)),
				new ComputerPlayer(10),
				new ComputerPlayer(10)
				);
		
		final AsyncPlayer asyncPlayer1 = new AsyncPlayer();
		final AsyncPlayer asyncPlayer2 = new AsyncPlayer();
		
		GameSession session2 = manager.addSession(
				new GameState(),
				new GamePolicy(new ShipLengths(1)),
				asyncPlayer1,
				asyncPlayer2
				);
		
		manager.signalPendingSessions(2000);
		
		assertTrue(session1.isFinished());
		assertFalse(session2.isFinished());
		assertEquals(GameAction.PLACE_SHIPS, session2.getState().action);
		
		Runnable playSession2 = () -> {
			if(asyncPlayer1.isWaitingForShipPlacement())
				asyncPlayer1.placeShip(new ShipPlacement(false, 0, 0, 1));
			else if(asyncPlayer2.isWaitingForShipPlacement())
				asyncPlayer2.placeShip(new ShipPlacement(false, 0, 0, 1));
			else if(asyncPlayer1.isWaitingForStrike()) 
				asyncPlayer1.strike(0, 0);
		};
		
		
		playSession2.run();
		manager.signalPendingSessions(20);
		playSession2.run();
		manager.signalPendingSessions(20);

		assertEquals(GameAction.PLAYER_1_TURN, session2.getState().action);
		
		playSession2.run();
		manager.signalPendingSessions(20);
		
		assertEquals(GameAction.PLAYER_1_WIN, session2.getState().action);
	}

}
