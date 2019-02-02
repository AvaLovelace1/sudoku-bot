package sudokubot;

import java.util.Scanner;

import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;

public class SudokuBot {

	private static final int BOARD_SIZE = 9;
	private static final int LEVEL = (int) Math.sqrt(BOARD_SIZE);
	// region location calibration things
	private static final int TILE_SIZE = 35;
	private static final int GAP_SIZE = 1;
	private static final Pattern CALENDAR_FILE = new Pattern("resources/calendar");
	private static Pattern NUMBER_FILES[][] = new Pattern[2][10];
	private static final Screen SCREEN = new Screen(1);
	private static Region[][] TILES = new Region[BOARD_SIZE][BOARD_SIZE];
	private static final Scanner in = new Scanner(System.in);
	private static final double DELAY_VALUE = 0;

	public SudokuBot() {

		Settings.DelayBeforeDrag = DELAY_VALUE;
		Settings.DelayBeforeDrop = DELAY_VALUE;
		Settings.MoveMouseDelay = (float) DELAY_VALUE;
		Settings.DelayValue = DELAY_VALUE;

		// read image files for number recognition
		for (int i = 0; i <= 9; i++) {
			NUMBER_FILES[0][i] = new Pattern("resources/light/" + i);
			NUMBER_FILES[1][i] = new Pattern("resources/dark/" + i);
		}
		for (Pattern p : NUMBER_FILES[0]) {
			p.similar((float) 0.8);
		}
		for (Pattern p : NUMBER_FILES[1]) {
			p.similar((float) 0.8);
		}
		CALENDAR_FILE.similar((float) 0.9);

		Region calendar;
		try {
			// define tile regions in relation to the calendar in upper right corner
			calendar = SCREEN.find(CALENDAR_FILE);
			TILES[0][0] = new Region(calendar.x - 258, calendar.y + 132, TILE_SIZE, TILE_SIZE);
			for (int i = 0; i < BOARD_SIZE; i++) {
				for (int j = 0; j < BOARD_SIZE; j++) {
					if (TILES[i][j] == null) {
						if (j == 0) {
							int x = TILES[i - 1][j].x;
							int y = TILES[i - 1][j].y + TILE_SIZE
									+ (i % LEVEL == 0 ? GAP_SIZE * (i / LEVEL * LEVEL) : 0);
							TILES[i][j] = new Region(x, y, TILE_SIZE, TILE_SIZE);
						} else {
							int x = TILES[i][j - 1].x + TILE_SIZE
									+ (j % LEVEL == 0 ? GAP_SIZE * (j / LEVEL * LEVEL) : 0);
							int y = TILES[i][j - 1].y;
							TILES[i][j] = new Region(x, y, TILE_SIZE, TILE_SIZE);
						}
					}
				}
			}

		} catch (FindFailed e) {
			e.printStackTrace();
		}

	}
	
	// read board using image recognition

	private int[][] readBoard(Region[][] tiles) {			
		
		int board[][] = new int[BOARD_SIZE][BOARD_SIZE];
		double highScore, score;
		int best;

		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {			
				
				highScore = 0;
				score = 0;
				best = 0;
				int dark = (i / 3 + j / 3) % 2;
				for (int k = 1; k < NUMBER_FILES[dark].length; k++) {
					Match m = tiles[i][j].exists(NUMBER_FILES[dark][k], 0);
					if (m != null) {
						score = m.getScore();
						if (score > highScore) {
							best = k;
							highScore = score;
						}
					}
				}
				board[i][j] = best;
			}
		}

		System.out.println();
		for (int is[] : board) {
			for (int i : is) {
				System.out.print(i + " ");
			}
			System.out.println();
		}

		return board;
	}
	
	// read board through stdin
	private int[][] readBoardInput() {
		int board[][] = new int[BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				board[i][j] = in.nextInt();
			}
		}
		return board;
	}

	// check if a board is valid
	private boolean check(int i, int j, int arr[][]) {
		for (int k = 0; k < BOARD_SIZE; k++) {
			if ((k != j && arr[i][k] == arr[i][j]) || (k != i && arr[k][j] == arr[i][j])) {
				return false;
			}
		}
		int i0 = i / LEVEL * LEVEL, j0 = j / LEVEL * LEVEL;
		for (int k = 0; k < LEVEL; k++) {
			for (int l = 0; l < LEVEL; l++) {
				if ((i0 + k != i || j0 + l != j) && arr[i0 + k][j0 + l] == arr[i][j]) {
					return false;
				}
			}
		}
		return true;
	}

	private Pair getPos(int n) {
		return new Pair(n / BOARD_SIZE, n % BOARD_SIZE);
	}

	public void run() {

		// read in board
		int board[][] = readBoard(TILES);
		boolean isFixed[][] = new boolean[BOARD_SIZE][BOARD_SIZE];

		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (board[i][j] != 0) {
					isFixed[i][j] = true;
				}
			}
		}

		// backtracking to solve sudoku
		int pos = 0;
		while (pos < Math.pow(BOARD_SIZE, 2)) {
			Pair x = getPos(pos);
			if (!isFixed[x.first][x.second]) {
				board[x.first][x.second]++;
				if (board[x.first][x.second] > BOARD_SIZE) {
					board[x.first][x.second] = 0;
					do {
						pos--;
						x = getPos(pos);
					} while (isFixed[x.first][x.second]);
				} else if (check(x.first, x.second, board)) {
					pos++;
				}
			} else {
				pos++;
			}
		}

		// play numbers 
		try {
			SCREEN.click(TILES[0][0]);
			SCREEN.click(TILES[0][0]);
		} catch (FindFailed e1) {
			e1.printStackTrace();
		}
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if (!isFixed[i][j]) {
					try {
						SCREEN.click(TILES[i][j]);
						SCREEN.type(Integer.toString(board[i][j]));
					} catch (FindFailed e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
