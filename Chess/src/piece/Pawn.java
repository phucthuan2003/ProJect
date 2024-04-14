package piece;

import java.util.List;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import main.Board;
import main.GamePanel;
import main.Type;

public class Pawn extends Piece {

	public Pawn(int color, int col, int row) {
		super(color, col, row);
		type = Type.PAWN;
		if (color == GamePanel.WHITE) {
			image = getImage("/piece/w-pawn");
		} else {
			image = getImage("/piece/b-pawn");
		}
	}

	public List<int[]> getValidMoves() {
		List<int[]> validMoves = new ArrayList<>();

		// Các bước đi có thể của quân Tốt
		int forward = (color == GamePanel.WHITE) ? -1 : 1;
		int[] offsets = {forward, 2 * forward};

		for (int offset : offsets) {
			int targetCol = col;
			int targetRow = row + offset;

			// Only allow a two square move if the pawn has not moved yet
			if (offset == 2 * forward && moved) {
				continue;
			}

			if (canMove(targetCol, targetRow)) {
				validMoves.add(new int[]{targetCol, targetRow});
			}
		}

		// Kiểm tra các bước đi chéo để ăn quân cờ của đối phương
		int[] diagonalOffsets = {-1, 1};
		for (int offset : diagonalOffsets) {
			int targetCol = col + offset;
			int targetRow = row + forward;

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

	public boolean canMove(int targetCol, int targetRow) {
		if (isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
			// Define the move value based on its color
			int moveValue;
			if (color == GamePanel.WHITE) {
				moveValue = -1;
			} else {
				moveValue = 1;
			}
			// Check the hitting piece
			hittingP = getHittingP(targetCol, targetRow);

			// 1 square movement
			if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
				return true;
				
			}
			// 2 squares movement
			if (targetCol == preCol && targetRow == preRow + moveValue * 2 && hittingP == null && moved == false
					&& pieceIsOnStraightLine(targetCol, targetRow) == false) {
				return true;

			}
			// Diagonal move & capture (if a piece is on a square diagonally in front of it)
			if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null
					&& hittingP.color != color) {
				return true;
			}
			// In passing
			if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue) {
				for(Piece piece : GamePanel.simPieces) {
					if(piece.col == targetCol && piece.row == preRow && piece.twoStepped == true) {
						hittingP = piece;
						return true;
					}
				}
			}
		}
		return false;
	}
}
