package battleship.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.stringtree.json.JSONReader;
import org.stringtree.json.JSONWriter;

public class BoardMasks {
	
	public final static long[] ROW_MASKS;
	public final static long [] COLUMN_MASKS;
	
	private final static long[] SHIFT_COLUMN_MASK;
	
	
	static {
		ROW_MASKS = new long[8];
		for(int row = 0; row < 8; row++)
			ROW_MASKS[row] = shiftRow(0b1111_1111L, row);
		
		COLUMN_MASKS = new long[8];
		for(int column = 0; column < 8; column++)
			COLUMN_MASKS[column] = 0x0101010101010101L << column;  // shift column
		
		SHIFT_COLUMN_MASK = new long[9];
		for(int shift = 0; shift < 9; shift++)
			for(int column = 0; column < 8 - shift; column++)
				SHIFT_COLUMN_MASK[shift] |= COLUMN_MASKS[column];
	}
	
	public static long shiftRow(long pattern, int rows) {
		if (rows*rows >= 8*8)
			return 0L;
		else if (rows >= 0)
			return pattern << (8*rows);
		else
			return pattern >>> (-8*rows);
	}
	
	public static long shiftColumn(long pattern, int columns) {		
		if (columns*columns >= 8*8)
			return 0L;
		else if (columns >= 0)
			return (pattern & SHIFT_COLUMN_MASK[columns]) << columns;
		else
			return (pattern >>> -columns) & SHIFT_COLUMN_MASK[-columns];
	}
	
	public static boolean maskFilledCheckRange(int startRow, int endRow, int startColumn, int endColumn) {
		return -1 < startRow && startRow <= endRow && endRow <= 8 &&
				-1 < startColumn && startColumn <= endColumn && endColumn <= 8;
	}
	
	public static long maskFilled(int startRow, int endRow, int startColumn, int endColumn) {
		startRow = Math.max(0, startRow);
		endRow = Math.min(8, endRow);
		startColumn = Math.max(0, startColumn);
		endColumn = Math.min(8, endColumn);
		
		int rows = endRow - startRow;
		int columns = endColumn - startColumn;
		
		long full = ~0L;
		long sliced = shiftColumn(shiftRow(full, rows-8), columns-8);
		long aligned = shiftColumn(shiftRow(sliced, startRow), startColumn);
		return aligned;
	}
	
	public static long maskRectangularCorners(int startRow, int endRow, int startColumn, int endColumn) {
		return (
				(-1 < startRow && startRow < 8 ? ROW_MASKS[startRow] : 0L) |
				(0 < endRow && endRow <= 8 ? ROW_MASKS[endRow-1] : 0L)
			) & (
				(-1 < startColumn && startColumn < 8 ? COLUMN_MASKS[startColumn] : 0L) |
				(0 < endColumn && endColumn <= 8 ? COLUMN_MASKS[endColumn-1] : 0L)
			);
	}
	
	public static long mask(int row, int column) {
		if (-1 < row && row < 8 && -1 < column && column < 8)
			return ROW_MASKS[row] & COLUMN_MASKS[column];
		else
			return 0L;
	}
	
	public static int[] coordinate(long mask) {
		if (mask == 0L)
			return null;
		
		int id = Long.numberOfTrailingZeros(mask);
		int row = id / 8;
		int column = id % 8;
		return new int[] {row, column};
	}
	
	public static boolean get(long pattern, long mask) {
		return (pattern & mask) != 0L;
	}
	
	public static long set(long pattern, long mask, boolean value) {
		if(value)
			return pattern | mask;
		else
			return pattern & ~mask;
	}
	
	public static String toString(long pattern) {
		StringBuilder sb = new StringBuilder(9*9);
		
		for(int row = 0; row < 8; row++) {
			for(int column = 0; column < 8; column++) {
				if (get(pattern, mask(row, column)))
					sb.append('X');
				else
					sb.append(' ');
			}
			sb.append(System.lineSeparator());
		}
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}
	
	public static boolean[][] toBoolean(long pattern) {
		boolean[][] array = new boolean[8][8];
		for(int row = 0; row < 8; row++)
			for(int column = 0; column < 8; column++)
				array[row][column] = get(pattern, mask(row, column));
		
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static long fromBoolean(Object array) {
		Function<Object, List<Object>> arrayToList = (a) -> {
			if(a.getClass().isArray()) {
				int length = Array.getLength(a);
		        ArrayList<Object> l = new ArrayList<>(length);
		        for(int i = 0; i < length; i++)
		        	l.add(Array.get(a, i));
		        
		        return l;
			} else {
				return (List<Object>) a;
			}
		};
        
		
		List<Object> rows = arrayToList.apply(array);
		
		long pattern = 0L;
		for(int row = 0; row < 8; row++) {
			List<Object> columns = arrayToList.apply(rows.get(row));
			
			for(int column = 0; column < 8; column++)
				pattern = set(pattern, mask(row, column), (boolean)columns.get(column));
		}
		return pattern;
	}
	
	
	public static String toJson(long pattern) {
		boolean[][] array = toBoolean(pattern);
		return new JSONWriter().write(array);
	}
	
	
	public static long fromJson(String json) {
		Object a = new JSONReader().read(json);
		return fromBoolean(a);
	}
}
