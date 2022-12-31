package battleship.model;


import static battleship.util.BoardMasks.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import battleship.util.BoardMasks;


public final class Board implements IParcelable<Board> {
	public final long placedShips;
	public final long safetyZones;
	
	public final long strikes;
	
	public Board() {
		placedShips = 0L;
		safetyZones = 0L;
		
		strikes = 0L;
	}
	
	private Board(long placedShips, long safetyZones, long strikes) {
		this.placedShips = placedShips;
		this.safetyZones = safetyZones;
		this.strikes = strikes;
	}
	
	public boolean isShip(int row, int column) {
		return get(placedShips, mask(row, column));
	}
	
	public boolean isSafetyZone(int row, int column) {
		return get(safetyZones, mask(row, column));
	}

	public boolean isStrike(int row, int column) {
		return get(strikes, mask(row, column));
	}
	
	public long strikes() {
		return strikes;
	}
	
	public long hits() {
		return strikes & placedShips;
	}
	
	public long misses() {
		return strikes & ~placedShips;
	}
	
	public boolean isHit(int row, int column) {
		return isStrike(row, column) && isShip(row, column);
	}
	
	public boolean isDestroyed() {
		long remainingShips = placedShips & ~strikes;
		return remainingShips == 0;
	}
	
	public Board addShip(int startRow, int startColumn, int length, boolean horizontal) {
		int endRow, endColumn;
		
		if (horizontal) {
			endRow = startRow + 1;
			endColumn = startColumn + length;
		} else {
			endRow = startRow + length;
			endColumn = startColumn + 1;
		}
		
		if (!maskFilledCheckRange(startRow, endRow, startColumn, endColumn))
			return null;
		
		long placedShip = maskFilled(startRow, endRow, startColumn, endColumn);

		if((this.safetyZones & placedShip) != 0)
			return null;
		
		long safetyZone = maskFilled(startRow-1, endRow+1, startColumn-1, endColumn+1)
				& ~maskRectangularCorners(startRow-1, endRow+1, startColumn-1, endColumn+1);
		
		return new Board(
				this.placedShips | placedShip,
				this.safetyZones | safetyZone,
				this.strikes
				);
	}
	
	public Board addStrike(int row, int column) {
		return new Board(
				placedShips,
				safetyZones,
				set(strikes, mask(row, column), true)
			);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (placedShips ^ (placedShips >>> 32));
		result = prime * result + (int) (safetyZones ^ (safetyZones >>> 32));
		result = prime * result + (int) (strikes ^ (strikes >>> 32));
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
		Board other = (Board) obj;
		if (placedShips != other.placedShips)
			return false;
		if (safetyZones != other.safetyZones)
			return false;
		if (strikes != other.strikes)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		char[] ships = BoardMasks.toString(this.placedShips).toCharArray();
		char[] safety = BoardMasks.toString(this.safetyZones).toCharArray();
		char[] strikes = BoardMasks.toString(this.strikes).toCharArray();
		
		char[] board = new char[ships.length];
		for(int i = 0; i < ships.length; i++)
			if(ships[i] == 'X' && strikes[i] == 'X')
				board[i] = 'X';
			else if(ships[i] == 'X')
				board[i] = '#';
			else if(strikes[i] == 'X' && safety[i] == 'X')
				board[i] = 'V';
			else if(strikes[i] == 'X')
				board[i] = 'v';
			else if(safety[i] == 'X')
				board[i] = '.';
			else
				board[i] = safety[i];
		
		return String.copyValueOf(board);
	}
	
	@Override
	public Object toParcelableObject() {
		return Map.of(
				"placedShips", BoardMasks.toBoolean(placedShips),
				"safetyZones", BoardMasks.toBoolean(safetyZones),
				"strikes", BoardMasks.toBoolean(strikes));
	}

	@Override
	public Board fromParcelableObject(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) obj;
		return new Board(
				BoardMasks.fromBoolean(map.get("placedShips")),
				BoardMasks.fromBoolean(map.get("safetyZones")),
				BoardMasks.fromBoolean(map.get("strikes"))
			);
	}
	
	public static Board createRandomBoardPerTrialAndError(int... shipLengths) {
		return createRandomBoardPerTrialAndError(new Board(), shipLengths);
	}
	
	public static Board createRandomBoardPerTrialAndError(Board current, int... shipLengths) {
		Random rnd = ThreadLocalRandom.current();

		// begin with the longest ship; shorter ships are easier to arrange
		Arrays.sort(shipLengths);
		for(int i = 0, j = shipLengths.length-1; i < j; i++, j--) {
			int length = shipLengths[i];
			shipLengths[i] = shipLengths[j];
			shipLengths[j] = length;
		}
		
		// randomly add ships, until they don't collide
		for(int trial = 0; trial < 100_000_000; trial++) {
			Board board = new Board(
					current.placedShips,
					current.safetyZones,
					current.strikes
				);
			
			for(int shipLength : shipLengths) {
				boolean horizontal = rnd.nextBoolean();
				int startRow = rnd.nextInt(horizontal ? 8 : 9 - shipLength);
				int startColumn = rnd.nextInt(horizontal ? 9 - shipLength : 8);
				
				board = board.addShip(
						startRow, 
						startColumn, 
						shipLength, 
						horizontal
					);
				
				if(board == null)
					break; // collision detected -> next try
			}
			
			if(board != null) 
				return board;  // all ships added -> return this board
			
			if((trial % 10_000) == 0 && trial > 0)
				System.out.println(trial);
		}
		
		return null;
	}
	
	public static Board createRandomBoardPerDepthFirstSearch(int... shipLengths) {
		return createRandomBoardPerDepthFirstSearch(new Board(), shipLengths);
	}
	
	public static Board createRandomBoardPerDepthFirstSearch(Board current, int... shipLengths) {
		Random rnd = ThreadLocalRandom.current();

		// begin with the longest ship; shorter ships are easier to arrange
		Arrays.sort(shipLengths);
		for(int i = 0, j = shipLengths.length-1; i < j; i++, j--) {
			int length = shipLengths[i];
			shipLengths[i] = shipLengths[j];
			shipLengths[j] = length;
		}
		
		// list all possible placements for all ship
		List<List<ShipPlacement>> placements = new ArrayList<>();
		for(int shipLength : shipLengths) {
			List<ShipPlacement> shipPlacements = new ArrayList<>();
			placements.add(shipPlacements);
			
			for(boolean horizontal : new boolean[] {true, false}) {
				for(int startRow = horizontal ? 7 : 8 - shipLength; startRow > -1; startRow--) {
					for(int startColumn = horizontal ? 8 - shipLength: 7; startColumn > -1; startColumn--) {
						ShipPlacement placement = new ShipPlacement();
						placement.horizontal = horizontal;
						placement.startRow = startRow;
						placement.startColumn = startColumn;
						placement.length = shipLength;
						shipPlacements.add(placement);
					}
				}
			}
			
			// randomize ship placements
			Collections.shuffle(shipPlacements, rnd);
			
			if(shipPlacements.isEmpty())
				return null;  // no possible placement for this ship length (too long?)
		}
		
		// iterate in depth first order through all possible ship placements
		return createRandomBoardPerDepthFirstSearch_Recursion(current, placements, 0);
	}
	
	private static Board createRandomBoardPerDepthFirstSearch_Recursion(Board board, List<List<ShipPlacement>> placements, int index) {
		if(board == null || placements.size() == index)
			return board;
				
		for(ShipPlacement placement : placements.get(index)) {
			Board newBoard = board.addShip(
					placement.startRow, 
					placement.startColumn, 
					placement.length, 
					placement.horizontal);
			
			newBoard = createRandomBoardPerDepthFirstSearch_Recursion(newBoard, placements, index+1);
			
			if(newBoard != null)
				return newBoard;  // found a valid board
		}
		
		return null;
	}
}
