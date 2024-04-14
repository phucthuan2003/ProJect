package piece;

import java.util.List;

import main.Board;
import main.GamePanel;
import main.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
public class Knight extends Piece {
    
	public Knight(int color, int col, int row) {
		super(color,col,row);
		type = Type.KNIGHT;
		if(color == GamePanel.WHITE) {
			image = getImage("/piece/w-knight");
		}
		else {
			image = getImage("/piece/b-knight");
		}
	}
	public List<int[]> getValidMoves() {
	    List<int[]> validMoves = new ArrayList<>();

	    // Các bước đi có thể của quân Mã theo hình chữ L
	    int[][] offsets = {
	        {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
	        {1, -2}, {1, 2}, {2, -1}, {2, 1}
	    };

	    for (int[] offset : offsets) {
	        int targetCol = col + offset[0];
	        int targetRow = row + offset[1];

	        if (canMove(targetCol, targetRow)) {
	            validMoves.add(new int[]{targetCol, targetRow});
	        }
	    }

	    return validMoves;
	}
	public void drawValidMoves(Graphics2D g) {
	    List<int[]> validMoves = getValidMoves();

	    g.setColor(Color.GREEN); // Đặt màu cho các bước đi hợp lệ

	    for (int[] move : validMoves) {
	        int x = getX(move[0]);
	        int y = getY(move[1]);

	        g.fillOval(x + Board.SQUARE_SIZE / 2, y + Board.SQUARE_SIZE / 2, Board.SQUARE_SIZE / 4, Board.SQUARE_SIZE / 4);
	    }
	}


	public boolean canMove(int targetCol,int targetRow) {
		if(isWithinBoard(targetCol, targetRow)) {
			// knight can move if its movement ratio of col and row is 1:2 or 2:1
			if(Math.abs(targetCol - preCol) * Math.abs(targetRow - preRow) == 2) {
				if(isValidSquare(targetCol,targetRow)) {
					return true;
				}
			}
		}
		return false;
	}
	

}
