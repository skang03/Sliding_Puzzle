/**
 * 
 */
package edu.wm.cs.cs301.slidingpuzzle;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Sungwon Kang 
 *
 */
public class SimplePuzzleState implements PuzzleState {
	/** 
	 * class variables set up
	 * position is the matrix that stores position of the tiles, 
	 * parentstate is the previous state used undo the moves,
	 * operation is the direction it moved from the parentstate,
	 * and pathlength is how many moves it went through from the start
	 */
    private int[][] position;
    private SimplePuzzleState parentstate;
	private Operation operation;
	private int pathlength;
	
	/**
	 *  default constructor class
	 */
	SimplePuzzleState(){
		
	}
	
	/** 
	 * constructor used for move method 
	 */
	SimplePuzzleState(int[][] position, SimplePuzzleState parentstate, Operation operation, int pathlength) {
		this.position = position; 
		this.parentstate = parentstate;
		this.operation = operation;
		this.pathlength = pathlength;
	}
	
	/**
	 *  setToInitialState creates a new board by making a matrix and putting numbers into it in order with
	 * 0's in the end
	 */
	@Override
	public void setToInitialState(int dimension, int numberOfEmptySlots) {
		int tilenumber = 1;
		position = new int[dimension][dimension]; //creating a matrix
		for(int i = 0; i < dimension; i++){ //iterate through and numbers
			for(int j = 0; j < dimension; j++){
				position[i][j] = tilenumber++; 
			}
		}
       for(int i = 0; i < numberOfEmptySlots; i++){
    	   position[dimension-1][dimension-i-1] = 0; //iterate backwards on the last line and add 0's. 
    	   //Does not work if numerOfEemptySlots is greater than dimension.
       }
	}

	
	@Override
	public int getValue(int row, int column) {
		return position[row][column]; //returns position of tile at the specified coordinate.
	}
	
	
	@Override
	public PuzzleState getParent() {
		return parentstate; //returns parentstate
	}

	
	@Override
	public Operation getOperation() {
		return operation; //returns operation
	}


	@Override
	public int getPathLength() {
		return pathlength; //returns pathlength
	}

	/** 
	 * there are four types of moves: moveright, moveleft, moveup, and movedown.
	 * each move operation works by copying the non-zero value from its tile to the tile located in the corresponding direction, 
	 * and replacing its original tile with a zero.
	 */
	@Override
	public PuzzleState move(int row, int column, Operation op) {
		
		int[][] newposition = new int[4][4];
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				newposition[i][j] = position[i][j]; //create a new position matrix to modify 
			}
		}
		if (op == Operation.MOVERIGHT){ // in the PuzzleGameGUI, it only sends MOVERIGHT as the operation even when it is not moving right.
			// Therefore, if we receive MOVERIGHT, we do not trust it and instead we look around and find empty space(s) and move accordingly.
			// We look at MOVERIGHT first in case MOVERIGHT was the real direction.
			if(column != 3 && newposition[row][column + 1] == 0){
				op = Operation.MOVERIGHT;
				newposition[row][column + 1] = newposition[row][column]; //copy the value from its tile to the tile on the right of it.
				newposition[row][column] = 0; //replace the original tile's value with 0
			}
			else if(column != 0 && newposition[row][column - 1] == 0){
				op = Operation.MOVELEFT;
				newposition[row][column - 1] = newposition[row][column];
				newposition[row][column] = 0;
			}
			
			else if(row != 3 && newposition[row + 1][column] == 0){
				op = Operation.MOVEDOWN;
				newposition[row + 1][column] = newposition[row][column];
				newposition[row][column] = 0;
			}
			
			else if(row != 0 && newposition[row - 1][column] == 0){
				op = Operation.MOVEUP;
				newposition[row - 1][column] = newposition[row][column];
				newposition[row][column] = 0;
			}
			else return null;
		}
		
		else return flipmove(row, column, op); //If MOVERIGHT was not what it received, then it did not come from the PuzzleGameGUI and therefore
		//we can trust the operation received so we use flipmove which moves based on the operation received.
		
		PuzzleState newpuzzlestate = new SimplePuzzleState(newposition, this, op, pathlength + 1); //we increment pathlength by 1.
		return newpuzzlestate;
	}
	
	/** 
	 * actual moving algorithm works the same as the move() method but instead without the fear of getting a wrong operation.
	 */
	
	private SimplePuzzleState flipmove(int row, int column, Operation trueop) {
		
		int[][] newposition = new int[4][4];
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				newposition[i][j] = position[i][j];
			}
		}
		
		if(trueop == Operation.MOVELEFT){
			newposition[row][column - 1] = newposition[row][column];
			newposition[row][column] = 0;
		}
		
		else if(trueop == Operation.MOVERIGHT){ //we still have move right because we use the flipmove in this class.
			newposition[row][column + 1] = newposition[row][column];
			newposition[row][column] = 0;
		}
		
		else if(trueop == Operation.MOVEDOWN){
			newposition[row + 1][column] = newposition[row][column];
			newposition[row][column] = 0;
		}
		
		else if(trueop == Operation.MOVEUP){
			newposition[row - 1][column] = newposition[row][column];
			newposition[row][column] = 0;
		}
		
		else return null;
		SimplePuzzleState newpuzzlestate = new SimplePuzzleState(newposition, this, trueop, pathlength + 1); //increment pathlength by 1
		return newpuzzlestate;
	}

/** 
 * the flip method makes it possible to drag a value to a blank tile around it. You can only drag to a valid locations thanks to the PuzzleGameGUI.
 */
	@Override
	public PuzzleState flip(int startRow, int startColumn, int endRow, int endColumn) {
		int currentRow = startRow;
		int currentColumn = startColumn;
		SimplePuzzleState newstate = new SimplePuzzleState(this.position, this.parentstate, this.operation, this.pathlength);
		
		while(currentRow != endRow || currentColumn != endColumn){
			if(currentRow != 3 && this.position[currentRow + 1][currentColumn] == 0 && currentRow < endRow){
				newstate = newstate.flipmove(currentRow, currentColumn, Operation.MOVEDOWN);
				startRow = currentRow;
				currentRow++;
			}
			else if(currentRow != 0 && this.position[currentRow - 1][currentColumn] == 0 && currentRow > endRow){
				newstate = newstate.flipmove(currentRow, currentColumn, Operation.MOVEUP);
				startRow = currentRow;
				currentRow--;	
			}
			else if(currentColumn != 3 && this.position[currentRow][currentColumn + 1] == 0 && currentColumn < endColumn){
				newstate = newstate.flipmove(currentRow, currentColumn, Operation.MOVERIGHT);
				startColumn = currentColumn;
				currentColumn++;
			}
			else if(currentColumn != 0 && this.position[currentRow][currentColumn - 1] == 0 && currentColumn > endColumn){
				newstate = newstate.flipmove(currentRow, currentColumn, Operation.MOVELEFT);
				startColumn = currentColumn;
				currentColumn--;
			}
        }
		return (PuzzleState) newstate;
	}

/** 
 * shuffle works by getting a random number that tells which way to go.
 */
	@Override
	public PuzzleState shuffleBoard(int pathLength) {
		SimplePuzzleState newstate = new SimplePuzzleState(this.position, this.parentstate, this.operation, this.pathlength);
		int[][] zeropositions = new int[16][2];
		int a = 0;
		for(int i = 0; i < 4; i++){
			for(int j = 0; j < 4; j++){
				if(newstate.position[i][j] == 0){
					zeropositions[a][0] = i;
					zeropositions[a][1] = j;
					a++; //this is the number of empty tiles in the matrix.
				}
			}
		}
		/** 
		 * picks a random zero from its array and gets a position
		*/
		for(int i = 0; i < pathLength; i++){
			int whichzero = (int) Math.floor((a*Math.random())); //this picks which zero 
	    	int row = zeropositions[whichzero][0];
	    	int column = zeropositions[whichzero][1];
	    	int direction = (int) Math.floor((4*Math.random())); 
	    	
	    	//if randomly selected direction is impossible then these if statements reverse it.
	    	if(direction == 0 && row == 3){
	    		direction++; 
	    	}
	    	
	    	else if(direction == 1 && row == 0){
	    		direction--;
	    	}
	    	
	    	else if(direction == 2 && column == 3){
	    		direction++;
	    	}
	    	
	    	else if(direction == 3 && column  == 0){
	    		direction--;
	    	}
	    	
	    	
	    	if (direction == 0 && row != 3){
		    	newstate = newstate.flipmove(row + 1, column, Operation.MOVEUP);
		    	zeropositions[whichzero][0] = row + 1;
		
		    }
		    
		    else if (direction == 1 && row != 0){
		    	newstate = newstate.flipmove(row - 1, column, Operation.MOVEDOWN);
		    	zeropositions[whichzero][0] = row - 1;
		    }
		    
		    else if (direction == 2 && column != 3){
		    	newstate = newstate.flipmove(row, column + 1, Operation.MOVELEFT);
		    	zeropositions[whichzero][1] = column + 1;
		    }
		    
		    else if (direction == 3 && column != 0){
		    	newstate = newstate.flipmove(row, column - 1, Operation.MOVERIGHT);
		    	zeropositions[whichzero][1] = column - 1;
		    }
		}
		return (PuzzleState) newstate;
	}
		
	/**
	 * checks if certain position is 0 
	 */
	@Override
	public boolean isEmpty(int row, int column) {
		if(this.position[row][column] == 0){
			return true;
		}
		return false;
	}
	
	/** 
	 * generated by Eclipse
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.deepHashCode(position);
		return result;
	}
	/**
	 * checks if the position matrices of two puzzlestates are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimplePuzzleState other = (SimplePuzzleState) obj;
		if (!Arrays.deepEquals(position, other.position))
			return false;
		return true;
	}
	
}
