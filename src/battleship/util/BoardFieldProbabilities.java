package battleship.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.IntStream;

import battleship.model.Board;

public final class BoardFieldProbabilities {
	private final long misses;
	private final long hits;
	private final int[] shipLengths;
	
	private final int[][] hitCount = new int[8][8];
	private int trials = 0;
	
	public BoardFieldProbabilities(Board enemyBoard, int... shipLengths) {
		this.misses = enemyBoard.misses();
		this.hits = enemyBoard.hits();
		this.shipLengths = shipLengths;
		
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				long mask = BoardMasks.mask(row, column);
				if((mask & misses) != 0 || (mask & hits) != 0)
					hitCount[row][column] = -1;
			}
		}
			
	}
	
	private boolean filter(Board board) {
		return (board.placedShips & misses) == 0;
	}
	
	private boolean acceptor(Board board) {
		return (~board.placedShips & hits) == 0;
	}
	
	public Board sampleBoard() {
		Board board = new BoardSampler(
				this::filter, this::acceptor, shipLengths).create();
		
		return sampleBoard(board);
	}
	
	public Board sampleBoard(Board board) {
		if(board == null || !filter(board) || !acceptor(board))
			return null;
		
		trials++;
		
		for(int row = 0; row < 8; row++)
			for(int column = 0; column < 8; column++)
				if(board.isShip(row, column) && hitCount[row][column] > -1)
					hitCount[row][column]++;
		
		return board;
	}
	
	public double[][] getFieldProbabilities() {
		double[][] probabilities = new double[8][8];
		double trials, offset;
		
		if(this.trials > 0) {
			trials = this.trials;
			offset = 0d;
		} else {
			double unexploredFields = 8*8 - Long.bitCount(hits) - Long.bitCount(misses);
			double unexploredShips = IntStream.of(shipLengths).sum() - Long.bitCount(hits);
			
			trials = 1d;
			offset = unexploredShips > 0 ? unexploredShips / unexploredFields : 0d;
		}
					
		for(int row = 0; row < 8; row++)
			for(int column = 0; column < 8; column++)
				probabilities[row][column] = Math.max(0, (hitCount[row][column] + offset) / trials);
		
		return probabilities;
	}
	
	public int[] getMostProbableField() {
		ArrayList<int[]> fields = new ArrayList<>();
		
		// randomize search order
		for(int row = 0; row < 8; row++)
			for(int column = 0; column < 8; column++)
				fields.add(new int[] {row, column});
		Collections.shuffle(fields);
		
		// search most probable field in a random order
		int mostHits = -1;
		int[] mostProbableField = null;
		for(int[] field : fields) {
			if(hitCount[field[0]][field[1]] > mostHits) {
				mostHits = hitCount[field[0]][field[1]];
				mostProbableField = field;
			}
		}
		return mostProbableField;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		double[][] probabilities = this.getFieldProbabilities();
		
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				sb.append(String.format("%3.0f", 100*probabilities[row][column])).append("% ");
			}
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
}
