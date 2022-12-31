package battleship.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import javax.naming.LimitExceededException;

import battleship.model.Board;
import battleship.model.ShipPlacement;

public class BoardSampler {
	private final static Random RANDOM = ThreadLocalRandom.current();
	
	private int[] shipLengths;
	private Board initialBoard;
	private Predicate<Board> filter;
	private Predicate<Board> acceptor;
	private int maxCount = 10_000;
	
	private int counter = 0;
	private List<List<ShipPlacement>> placements;
	
	public BoardSampler(Predicate<Board> filter, Predicate<Board> acceptor, int... shipLengths) {
		this(new Board(), filter, acceptor, shipLengths);
	}
	
	public BoardSampler(Board initialBoard, Predicate<Board> filter, Predicate<Board> acceptor, int... shipLengths) {
		this.initialBoard = initialBoard;
		this.filter = filter;
		this.acceptor = acceptor;
		this.shipLengths = shipLengths;
	}
	
	public void setMaxCounter(int maxCount) {
		this.maxCount = maxCount;
	}
	
	public Board create() {
		// reset counter
		counter = 0;
		
		// begin with the longest ship; shorter ships are easier to arrange
		Arrays.sort(shipLengths);
		for(int i = 0, j = shipLengths.length-1; i < j; i++, j--) {
			int length = shipLengths[i];
			shipLengths[i] = shipLengths[j];
			shipLengths[j] = length;
		}
		
		// list all possible placements for all ship
		placements = new ArrayList<>();
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
			Collections.shuffle(shipPlacements, RANDOM);
			
			if(shipPlacements.isEmpty())
				return null;  // no possible placement for this ship length (too long?)
		}
		
		// iterate in depth first order through all possible ship placements
		try {
			return createFilteredRandomBoardPerDepthFirstSearch_Recursion(initialBoard, 0);
		} catch(LimitExceededException e) {
			// intended exception -> no warning
			return null;
		}
		
	}
	
	
	private Board createFilteredRandomBoardPerDepthFirstSearch_Recursion(Board board, int index) throws LimitExceededException {
		if(board == null || placements.size() == index)
			return board;
				
		for(ShipPlacement placement : placements.get(index)) {
			if(maxCount <= ++counter)
				throw new LimitExceededException();
				
			Board newBoard = board.addShip(
					placement.startRow, 
					placement.startColumn, 
					placement.length, 
					placement.horizontal);
			
			if(newBoard == null || !filter.test(newBoard))
				continue;
			
			newBoard = createFilteredRandomBoardPerDepthFirstSearch_Recursion(newBoard, index+1);
			
			if(newBoard != null && acceptor.test(newBoard))
				return newBoard;  // found a valid board
		}
		
		return null;
	}
}
