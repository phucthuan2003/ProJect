package main;

import java.util.ArrayList;
import main.Sound;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import piece.Bishop;
import piece.King;
import piece.Knight;
import piece.Pawn;
import piece.Piece;
import piece.Queen;
import piece.Rook;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
	public static final int WIDTH = 1100;
	public static final int HEIGHT = 800;
	final int FPS = 60;
	Thread gameThread;
	Board board = new Board();
	Mouse mouse = new Mouse();
	Sound sound = new Sound("move.wav");

	// PIECE
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static ArrayList<Piece> simPieces = new ArrayList<>();
	ArrayList<Piece> promoPieces = new ArrayList<>();
	Piece activeP,checkingP;
	Piece selectedP;
	public static Piece castlingP;

	// COLOR
	public static final int WHITE = 0;
	public static final int BLACK = 1;
	int currentColor = WHITE;

	// Boolean
	boolean canMove;
	boolean validSquare;
	boolean promotion;
	boolean gameover;
	boolean stalemate;

	public GamePanel() {
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.black);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		setPieces();
//		testPromotion();
		
		copyPieces(pieces, simPieces);
	}

	public void launchGame() {
		gameThread = new Thread(this);
		gameThread.start();
	}

	public void setPieces() {
		// white team
		pieces.add(new Pawn(WHITE, 0, 6));
		pieces.add(new Pawn(WHITE, 1, 6));
		pieces.add(new Pawn(WHITE, 2, 6));
		pieces.add(new Pawn(WHITE, 3, 6));
		pieces.add(new Pawn(WHITE, 4, 6));
		pieces.add(new Pawn(WHITE, 5, 6));
		pieces.add(new Pawn(WHITE, 6, 6));
		pieces.add(new Pawn(WHITE, 7, 6));
		pieces.add(new Rook(WHITE, 0, 7));
		pieces.add(new Rook(WHITE, 7, 7));
		pieces.add(new Knight(WHITE, 1, 7));
		pieces.add(new Knight(WHITE, 6, 7));
		pieces.add(new Bishop(WHITE, 2, 7));
		pieces.add(new Bishop(WHITE, 5, 7));
		pieces.add(new Queen(WHITE, 3, 7));
		pieces.add(new King(WHITE, 4, 7));

		// black team
		pieces.add(new Pawn(BLACK, 0, 1));
		pieces.add(new Pawn(BLACK, 1, 1));
		pieces.add(new Pawn(BLACK, 2, 1));
		pieces.add(new Pawn(BLACK, 3, 1));
		pieces.add(new Pawn(BLACK, 4, 1));
		pieces.add(new Pawn(BLACK, 5, 1));
		pieces.add(new Pawn(BLACK, 6, 1));
		pieces.add(new Pawn(BLACK, 7, 1));
		pieces.add(new Rook(BLACK, 0, 0));
		pieces.add(new Rook(BLACK, 7, 0));
		pieces.add(new Knight(BLACK, 1, 0));
		pieces.add(new Knight(BLACK, 6, 0));
		pieces.add(new Bishop(BLACK, 2, 0));
		pieces.add(new Bishop(BLACK, 5, 0));
		pieces.add(new Queen(BLACK, 3, 0));
		pieces.add(new King(BLACK, 4, 0));

	}
//	public void testPromotion() {
//		pieces.add(new Pawn ( WHITE,0,4));
//		pieces.add(new Pawn ( BLACK,0,4));
//	}

	public void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
		target.clear();
		for (int i = 0; i < source.size(); i++) {
			target.add(source.get(i));
		}
	}

	@Override
	public void run() {
		// Gameloop
		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		while (gameThread != null) {
			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;
			if (delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}

	}
	private void update() {
	    if(promotion) {
	        promoting();
	    }
	    else if(gameover == false && stalemate == false){
	        // Mouse button clicked
	        if (mouse.clicked && activeP == null) {
	            // If the activeP is null, check if you can pick up a piece
	            for (Piece piece : simPieces) {
	                if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
	                        && piece.row == mouse.y / Board.SQUARE_SIZE) {
	                    activeP = piece;
	                }
	            }
	            mouse.clicked = false; // Reset the clicked state after handling
	        }
	        // Mouse button dragged or clicked
	        if ((mouse.dragged || mouse.clicked) && activeP != null) {
	            // If the player is holding a piece, simulate the move
	            simulate();
	        }

	        // Mouse button released
	        if (mouse.released) {
	            if (activeP != null && validSquare) {
	                // Move confirmed
	                //Update the piece list in case a piece has been captured and removed during the simulation
	            	sound.playSound(); 
	                copyPieces(simPieces, pieces);
	                activeP.updatePosition();
	                if(castlingP != null) {
	                    castlingP.updatePosition();
	                }
	                if(isKingInCheck() && isCheckmate()) {
	                    gameover = true;
	                }
	                else if(isStalemate() && isKingInCheck() == false) {
	                    stalemate = true;
	                }
	                else { // The game is still going on
	                    if(canPromote()) {
	                        promotion = true;
	                    }
	                    else {
	                        changePlayer();
	                    }
	                }
	            } 
	            else if (activeP != null) {
	                //The move is not valid so reset everything
	                copyPieces(pieces, simPieces);
	                activeP.resetPosition();
	                activeP = null;
	            }
	            mouse.released = false; // Reset the released state after handling
	        }
	    }
	}

	public void simulate() {
	    canMove = false;
	    validSquare = false;

	    //Reset the piece list in every loop
	    // This is basically for restoring the removed piece during the simulation
	    copyPieces(pieces, simPieces);
	    //Reset castling piece's position
	    if(castlingP != null) {
	        castlingP.col = castlingP.preCol;
	        castlingP.x = castlingP.getX(castlingP.col);
	        castlingP = null;
	    }
	    // If a piece is being held, update its position
	    activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
	    activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
	    activeP.col = activeP.getCol(activeP.x);
	    activeP.row = activeP.getRow(activeP.y);
	    // Check if the piece is hovering over a reachable square
	    if (activeP.canMove(activeP.col, activeP.row)) {
	        canMove = true;

	        // if hitting a piece, remove it from list
	        if(activeP.hittingP != null) {
	            simPieces.remove(activeP.hittingP.getIndex());
	        }
	        checkCastling();
	        if(isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
	            validSquare = true;
	        }
	    }
	    if (!mouse.dragged) {
	        mouse.clicked = false; // Reset the clicked state after handling
	        mouse.released = true;
	    }
	}


	
     
//	private void update() {
//	    if(promotion) {
//	        promoting();
//	    }
//	    else if(gameover == false && stalemate == false){
//	        // Mouse button clicked
//	        if (mouse.clicked) {
//	            if (activeP == null) {
//	                // If the activeP is null, check if you can pick up a piece
//	                for (Piece piece : simPieces) {
//	                    if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
//	                            && piece.row == mouse.y / Board.SQUARE_SIZE) {
//	                        activeP = piece;
//	                        // Phát âm thanh khi một quân cờ được nhặt lên
//	                        // Sound pickUpSound = new Sound("move.wav");
//	                        // pickUpSound.playSound();
//	                    }
//	                }
//	            } else {
//	                // If the player is holding a piece, simulate the move
//	                simulate();
//	                // Phát âm thanh khi một quân cờ được di chuyển
//	                // Sound moveSound = new Sound("move.wav");
//	                // moveSound.playSound();
//	                
//	                // Mouse button release
//	                if (validSquare) {
//	                    // Move confirmed
//	                
//	                    //Update the piece list in case a piece has been captured and removed during the simulation
//	                    copyPieces(simPieces, pieces);
//	                    activeP.updatePosition();
//	                    if(castlingP != null) {
//	                        castlingP.updatePosition();
//	                    }
//	                    if(isKingInCheck() && isCheckmate()) {
//	                         gameover = true;
//	                    }
//	                    else if(isStalemate() && isKingInCheck() == false) {
//	                        stalemate = true;
//	                    }
//	                    else { // The game is still going on
//	                        if(canPromote()) {
//	                            promotion = true;
//	                        }
//	                        else {
//	                        changePlayer();
//	                        }
//	                    }
//	                } 
//	                else {
//	                    //The move is not valid so reset everything
//	                    copyPieces(pieces, simPieces);
//	                    activeP.resetPosition();
//	                    activeP = null;
//	                }
//	            }
//	            mouse.clicked = false; // Reset the clicked state after handling
//	        }
//	    }
//	}
   
//	public boolean mouseClickedOnActivePiece(MouseEvent event) {
//	    int col = event.getX() / Board.SQUARE_SIZE;
//	    int row = event.getY() / Board.SQUARE_SIZE;
//
//	    if (activeP != null && activeP.row == row && activeP.col == col) {
//	        return true;
//	    }
//
//	    return false;
//	}
//	public void handleMouseClick() {
//	    if (mouse.clicked) {
//	        int clickedCol = mouse.x / Board.SQUARE_SIZE;
//	        int clickedRow = mouse.y / Board.SQUARE_SIZE;
//
//	        for (Piece piece : pieces) {
//	            if (piece.color == currentColor && piece.col == clickedCol && piece.row == clickedRow) {
//	                Piece selectedP = piece;
//	                mouse.clicked = false; // Reset the clicked status after handling
//	                break;
//	            }
//	        }
//	    }
//	}
//	private void update1() {
//		if(promotion) {
//			promoting();
//		}
//		else if(gameover == false && stalemate == false){
//			// Mouse button pressed
//			if (mouse.pressed) {
//			    if (activeP == null) {
//			        // If the activeP is null, check if you can pick up a piece
//			        for (Piece piece : simPieces) {
//			            if (piece.color == currentColor && piece.col == mouse.x / Board.SQUARE_SIZE
//			                    && piece.row == mouse.y / Board.SQUARE_SIZE) {
//			                activeP = piece;
//			                // Phát âm thanh khi một quân cờ được nhặt lên
////			                Sound pickUpSound = new Sound("move.wav");
////			                pickUpSound.playSound();
//			            }
//			        }
//			    } else {
//			        // If the player is holding a piece, simulate the move
//			        simulate();
//			        // Phát âm thanh khi một quân cờ được di chuyển
////			        Sound moveSound = new Sound("move.wav");
////			        moveSound.playSound();
//			    }
//			}
//
//
//			// Mouse button release
//			if (mouse.pressed == false) {
//				if (activeP != null) {
//					if (validSquare) {
//						
//						// Move confirmed
//					
//						//Update the piece list in case a piece has been captured and removed during the simulation
//						copyPieces(simPieces, pieces);
//						activeP.updatePosition();
//						if(castlingP != null) {
//							castlingP.updatePosition();
//						}
//						if(isKingInCheck() && isCheckmate()) {
//						     gameover = true;
//						}
//						else if(isStalemate() && isKingInCheck() == false) {
//							stalemate = true;
//						}
//						else { // The game is still going on
//							if(canPromote()) {
//								promotion = true;
//							}
//							else {
//							changePlayer();
//	             			}
//						}
//					} 
//					else {
//						//The move is not valid so reset everything
//						copyPieces(pieces, simPieces);
//						activeP.resetPosition();
//						activeP = null;
//					}
//				}
//			}
//		}
//	} 
//	public void simulate1() {
//
//		canMove = false;
//		validSquare = false;
//		
//		//Reset the piece list in every loop
//		// This is basically for restoring the removed piece during the simulation
//        copyPieces(pieces, simPieces);
//        //Reset castling piece's position
//        if(castlingP != null) {
//        	castlingP.col = castlingP.preCol;
//        	castlingP.x = castlingP.getX(castlingP.col);
//        	castlingP = null;
//        }
//		// If a piece is being held, update its position
//		activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
//		activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
//		activeP.col = activeP.getCol(activeP.x);
//		activeP.row = activeP.getRow(activeP.y);
//		// Check if the piece is hovering over a reachable square
//		if (activeP.canMove(activeP.col, activeP.row)) {
//			canMove = true;
//			
//			// if hitting a piece, remove it from list
//			if(activeP.hittingP != null) {
//				simPieces.remove(activeP.hittingP.getIndex());
//			}
//			checkCastling();
//			if(isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
//				validSquare = true;
//			}
//		}
//
//	}
//	public void simulate() {
//	    if (mouse.clicked) {
//	        canMove = false;
//	        validSquare = false;
//
//	        //Reset the piece list in every loop
//	        // This is basically for restoring the removed piece during the simulation
//	        copyPieces(pieces, simPieces);
//	        //Reset castling piece's position
//	        if(castlingP != null) {
//	            castlingP.col = castlingP.preCol;
//	            castlingP.x = castlingP.getX(castlingP.col);
//	            castlingP = null;
//	        }
//	        // If a piece is being held, update its position
//	        activeP.x = mouse.x - Board.HALF_SQUARE_SIZE;
//	        activeP.y = mouse.y - Board.HALF_SQUARE_SIZE;
//	        activeP.col = activeP.getCol(activeP.x);
//	        activeP.row = activeP.getRow(activeP.y);
//	        // Check if the piece is hovering over a reachable square
//	        if (activeP.canMove(activeP.col, activeP.row)) {
//	            canMove = true;
//
//	            // if hitting a piece, remove it from list
//	            if(activeP.hittingP != null) {
//	                simPieces.remove(activeP.hittingP.getIndex());
//	            }
//	            checkCastling();
//	            if(isIllegal(activeP) == false && opponentCanCaptureKing() == false) {
//	                validSquare = true;
//	            }
//	        }
//	        mouse.clicked = false; // Reset the clicked state after handling
//	    }
//	}

	private boolean isIllegal(Piece king) {
		if(king.type == Type.KING) {
			for(Piece piece : simPieces) {
				if(piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
					return true;
				}
			}
		}
		return false;
	}
	private boolean opponentCanCaptureKing() {
	    Piece king = getKing(false);
	    if (king == null) {
	        // Handle the case when king is null
	        return false;
	    }
	    for(Piece piece : simPieces) {
	        if(piece.color != king.color && piece.canMove(king.col, king.row)) {
	            return true;
	        }
	    }
	    return false;
	}

	private boolean isKingInCheck() {
		Piece king = getKing(true);
		if(activeP.canMove(king.col, king.row)) {
			checkingP = activeP;
			return true;
		}
		else {
			checkingP = null;
		}
		return false;
	}
	private Piece getKing(boolean opponent) {
		Piece king = null;
		for(Piece piece : simPieces) {
			if(opponent) {
				if(piece.type == Type.KING && piece.color != currentColor) {
					king = piece;
				}
			}
			else {
				if(piece.type == Type.KING && piece.color == currentColor) {
					king = piece;
				}
			}
		}
		return king;
	}
	private boolean isCheckmate() {
		Piece king = getKing(true);
		if(kingCanMove(king)) {
		   return false;	
		}
		else {
			// But you still have a chance!!!
			// Check if you can block the attack with your piece
			//Check the position of the checking piece and the king in check
			int colDiff = Math.abs(checkingP.col - king.col);
			int rowDiff = Math.abs(checkingP.row - king.row);
			if(colDiff == 0) {
				//The checking piece is attacking vertically
				if(checkingP.row<king.row) {
					//The checking piece is above the king
					for(int row = checkingP.row; row<king.row;row++) {
						for(Piece piece : simPieces) {
							if(piece!= king && piece.color != currentColor && piece.canMove(checkingP.col,  row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//The checking piece is blow the king
					for(int row = checkingP.row; row > king.row;row--) {
						for(Piece piece : simPieces) {
							if(piece!= king && piece.color != currentColor && piece.canMove(checkingP.col,  row)) {
								return false;
							}
						}
					}
				}
				
			}
			else if(rowDiff == 0) {
		    	//The checking piece is attacking horizontally
				if(checkingP.col < king.col) {
					// The checking piece is to the left
					for(int col = checkingP.col; col<king.col;col++) {
						for(Piece piece : simPieces) {
							if(piece!= king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				if(checkingP.col > king.col) {
					// the checking piece is to the right
					for(int col = checkingP.col; col>king.col;col--) {
						for(Piece piece : simPieces) {
							if(piece!= king && piece.color != currentColor && piece.canMove(col, checkingP.row)) {
								return false;
							}
						}
					}
				}
				
			}
			else if(colDiff == rowDiff){
				//The checking piece is attacking diagonally
				if(checkingP.row < king.row) {
					//The checking piece is above the king
					if(checkingP.col < king.col) {
						// The checking piece is in the upper left
						for(int col = checkingP.col, row = checkingP.row; col < king.col;col++,row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if(checkingP.col > king.col) {
						//The checking piece is in the upper right
						for(int col = checkingP.col, row = checkingP.row; col > king.col;col--,row++) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
				if(checkingP.row > king.row) {
					//The checking piece is below the king
					if(checkingP.col < king.col) {
						// The checking piece is in the lower left
						for(int col = checkingP.col, row = checkingP.row; col < king.col;col++,row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
					if(checkingP.col > king.col) {
						// The checking piece is in the lower right
						for(int col = checkingP.col, row = checkingP.row; col >  king.col;col--,row--) {
							for(Piece piece : simPieces) {
								if(piece != king && piece.color != currentColor && piece.canMove(col, row)) {
									return false;
								}
							}
						}
					}
				}
			}
			else {
				// The checking piece is Knight
			}
		}
		return true;
	}
	private boolean kingCanMove(Piece king) {
		// Simulate if there is any square where the king can move to
		if(isValidMove(king, -1, -1)) {return true;}
		if(isValidMove(king, 0, -1)) {return true;}
		if(isValidMove(king, 1, -1)) {return true;}
		if(isValidMove(king, -1, 0)) {return true;}
		if(isValidMove(king, 1, 0)) {return true;}
		if(isValidMove(king, -1, 1)) {return true;}
		if(isValidMove(king, 0, 1)) {return true;}
		if(isValidMove(king, 1, 1)) {return true;}
		return false;
	}
	private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
		boolean isValidMove = false;
		// Update the king's position for a second
		king.col += colPlus;
		king.row += rowPlus;
		
		if(king.canMove(king.col,king.row)) {
			if(king.hittingP != null) {
				simPieces.remove(king.hittingP.getIndex());
			}
			if(isIllegal(king) == false) {
				isValidMove = true;
			}
		}
		//Reset the king position and restore the remove piece
		king.resetPosition();
		copyPieces(pieces,simPieces);
		return isValidMove;
	}
//	public void endTurn() {
//	    // Switch the current player
//	    currentColor = (currentColor == WHITE) ? BLACK : WHITE;
//
//	    // Check if the opponent has any valid moves
//	    boolean hasValidMoves = false;
//	    for (Piece piece : simPieces) {
//	        if (piece.color == currentColor) {
//	            for (int colPlus = -1; colPlus <= 1; colPlus++) {
//	                for (int rowPlus = -1; rowPlus <= 1; rowPlus++) {
//	                    if (isValidMove(piece, colPlus, rowPlus)) {
//	                        hasValidMoves = true;
//	                        break;
//	                    }
//	                }
//	                if (hasValidMoves) {
//	                    break;
//	                }
//	            }
//	        }
//	        if (hasValidMoves) {
//	            break;
//	        }
//	    }
//	
//
//	    // If the opponent has no valid moves, the game is over
//	    if (!hasValidMoves) {
//	        gameover = true;
//	        String winner = (currentColor == WHITE) ? "Black" : "White";
//	        Component frame = null;
//			JOptionPane.showMessageDialog(frame, winner + " wins!");
//	    }
//	}

	private boolean isStalemate() {
		int count = 0;
		//Count the number of piece
		for(Piece piece : simPieces) {
			if(piece.color!= currentColor) {
				count++;
			}
		}
		// If only one piece is left
		if(count == 1) {
			if(kingCanMove(getKing(true)) == false){
				return true;
			}
		}
		return false;
	}
	private void checkCastling() {
		if(castlingP != null) {
			if(castlingP.col == 0) {
				castlingP.col += 3;
				
			}
			else if(castlingP.col == 7) {
				castlingP.col -= 2;
			}
			castlingP.x = castlingP.getX(castlingP.col);
		}
	}
    private void changePlayer() {
    	if(currentColor == WHITE) {
    		currentColor = BLACK;
    		//Reset black's two stepped status
    		for(Piece piece : pieces) {
    			if(piece.color == BLACK) {
    				piece.twoStepped = false;
    			}
    		}
    	
    	}else {
    		currentColor = WHITE;
    		//Reset white's two stepped status
    		for(Piece piece : pieces) {
    			if(piece.color == WHITE) {
    				piece.twoStepped = false;
    			}
    		}
    	}
    	activeP = null;
    }
    private boolean canPromote() {
    	if(activeP.type == Type.PAWN ) {
    		if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7) {
    			promoPieces.clear();
    			promoPieces.add(new Rook(currentColor,9,2));
    			promoPieces.add(new Knight(currentColor,9,3));
    			promoPieces.add(new Bishop(currentColor,9,4));
    			promoPieces.add(new Queen(currentColor,9,5));
    			return true;
    			
    		}
    	}
    	return false;
    }
    private void promoting() {
    	if(mouse.clicked) {
    		for(Piece piece : promoPieces) {
    			if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE) {
    				switch(piece.type) {
    				case ROOK : simPieces.add(new Rook(currentColor, activeP.col, activeP.row)); break;
    				case KNIGHT : simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
    				case BISHOP : simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
    				case QUEEN : simPieces.add(new Queen(currentColor, activeP.col, activeP.row)); break;
    				default: break;
    				}
    				simPieces.remove(activeP.getIndex());
    				copyPieces(simPieces, pieces);
    				activeP = null;
    				promotion = false;
    				changePlayer();
    			}
    		}
    	}
    }
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// Board
		board.draw(g2);

		// Pieces
		for (Piece p : simPieces) {
			p.draw(g2);
		}
//		if(selectedP != null){
		if (activeP != null) {
		
			if(canMove) {
				  
			if(isIllegal(activeP) || opponentCanCaptureKing()) {
				g2.setColor(Color.gray);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						Board.SQUARE_SIZE);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				
			}
			else {
				g2.setColor(Color.white);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
				g2.fillRect(activeP.col * Board.SQUARE_SIZE, activeP.row * Board.SQUARE_SIZE, Board.SQUARE_SIZE,
						Board.SQUARE_SIZE);
				g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			}

			}
			if (activeP instanceof Knight) {
	            ((Knight) activeP).drawValidMoves(g2);
			}
			if (activeP instanceof Bishop) {
	            ((Bishop) activeP).drawValidMoves(g2);
			}
			if (activeP instanceof King) {
	            ((King) activeP).drawValidMoves(g2);
			}
			if (activeP instanceof Pawn) {
	            ((Pawn) activeP).drawValidMoves(g2);
			}
			if (activeP instanceof Rook) {
	            ((Rook) activeP).drawValidMoves(g2);
			}
			if (activeP instanceof Queen) {
	            ((Queen) activeP).drawValidMoves(g2);
			}
			// Draw the active piece is the end so it won't be hidden by the board or the color square
			activeP.draw(g2);
		}
//	}
		// Status messages
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setFont(new Font("Book Antiqua", Font.PLAIN, 40));
		g2.setColor(Color.white);
		
		if(promotion) {
			g2.drawString("Promote to:", 840, 150);
			for(Piece piece : promoPieces) {
				g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
						Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
			}
		}
		else {
			if(currentColor == WHITE) {
				g2.drawString("White's turn", 840, 550);
				if(checkingP != null && checkingP.color == BLACK) {
					g2.setColor(Color.red);
					g2.drawString("The King", 840, 550);
					g2.drawString("is in check", 840, 700);
				}
			}
			else {
				g2.drawString("Black's turn", 840, 250);
				if(checkingP != null && checkingP.color == WHITE) {
					g2.setColor(Color.red);
					g2.drawString("The King", 840, 100);
					g2.drawString("is in check", 840, 150);
				}
			}
		}
		if(gameover) {
			String s = "";
			if(currentColor == WHITE) {
				s = "White wins";
			}
			else {
				s = "Black wins";
			}
			g2.setFont(new Font("Arial", Font.PLAIN,90));
			g2.setColor(Color.green);
			g2.drawString(s, 200, 420);
		}
		if(stalemate) {
			g2.setFont(new Font("Arial", Font.PLAIN,90));
			g2.setColor(Color.lightGray);
			g2.drawString("Stalemate", 200, 420);
		}
		}
	

}