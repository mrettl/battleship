package battleship.agents;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import battleship.model.GameState;

public class GameManager implements Runnable {
	private final static GameManager DEFAULT_MANAGER = new GameManager();
	
	Thread managerThread = null;
	int manageIntervalMillis = 1_000;
	AtomicBoolean isManaging = new AtomicBoolean();
	AtomicLong sessionId = new AtomicLong(0);
	Map<GameSession, ?> sessions = Collections.synchronizedMap(new WeakHashMap<>());
	
	public static GameManager common() {
		return DEFAULT_MANAGER;
	}
	
	public void setManageInterval(int intervalMillis) {
		this.manageIntervalMillis = intervalMillis;
	}
	
	public GameSession addSession(GameState state, IGamePolicy policy, IPlayer player1, IPlayer player2) {
		policy.setGameState(state);
		
		player1.setGameState(state);
		player1.setPlayerOrder(true);
		
		player2.setGameState(state);
		player2.setPlayerOrder(false);
		
		long sessionId = this.sessionId.getAndIncrement();
		GameSession newSession = new GameSession(sessionId, state, policy, player1, player2);
		sessions.put(newSession, null);
		
		return newSession;
	}
	
	public boolean removeSession(GameSession session) {
		return sessions.remove(session) != null;
	}
	
	public long countPendingSessions() {
		return sessions
			.keySet()
			.stream()
			.filter(v -> v.isPending())
			.count();
	}
	
	@Override
	public void run() {
		isManaging.set(true);
		while(isManaging.get()) {
			for(int agentId = 0; agentId < 3; agentId++) {
				final int finalAgentId = agentId;
				sessions
				.keySet()
				.stream()
				.filter(v -> v.isPending())
				.forEach(v -> v.handleAgent(finalAgentId));
			}
			
			if(countPendingSessions() == 0) {
				synchronized (this) {
					this.notifyAll();
					try {
						this.wait(manageIntervalMillis);
					} catch (InterruptedException e) {
						isManaging.set(false);
					}
				}
				
			}
		}
	}
	
	public void stop() {
		isManaging.set(false);
		
	}
	
	public void signalPendingSessions(int timeoutMillis) {
		synchronized (this) {
			this.notify();
			try {
				this.wait(timeoutMillis);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	
	public Thread startInNewThread() {
		if(managerThread != null && managerThread.isAlive())
			return managerThread;
		
		managerThread = new Thread(this);
		managerThread.setName("manager");
		managerThread.setPriority(Thread.MIN_PRIORITY);
		managerThread.start();
		
		return managerThread;
	}
	
	public class GameSession {
		private final long sessionId;
		
		private final GameState state;
		private final IAgent[] agents;
		private Future<Boolean>[] hasChanged; 
		
		@SuppressWarnings("unchecked")
		public GameSession(long sessionId, GameState state, IAgent... agents) {
			this.sessionId = sessionId;
			
			this.state = state;
			this.agents = agents;
			this.hasChanged = new Future[agents.length];
		}
		
		public GameState getState() {
			return state;
		}
		
		public boolean isFinished() {
			boolean finished = true;
			for(Future<Boolean> future: hasChanged)
				try {
					finished &= future != null && future.isDone() && !future.get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
					finished = true;
					break;
				}
			
			return finished;
		}
		
		public boolean isPending() {
			boolean ready = true;
			for(Future<Boolean> future: hasChanged)
				ready &= future == null || future.isDone();
			
			return ready && !isFinished();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (sessionId ^ (sessionId >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GameSession other = (GameSession) obj;
			if (getEnclosingInstance() != other.getEnclosingInstance())
				return false;
			if (sessionId != other.sessionId)
				return false;
			return true;
		}

		private GameManager getEnclosingInstance() {
			return GameManager.this;
		}
		
		private void handleAgent(int agentId) {
			hasChanged[agentId] = agents[agentId].handle();
		}
	}
}
