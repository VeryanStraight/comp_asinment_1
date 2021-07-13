// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 1
 * Name:
 * Username:
 * ID:
 */

/** 
 *  A pair of row and column representing the coordinates of a cell in the warehouse.
 *  Has a method to return the next Position in a given direction.
 *  Because the fields are final (can't be changed), it is safe to make
 *  the fields public.
 *  If  pos is a variable containing a Position, then pos.row and pos.col
 * will be the values of the row and the col in the Position.
 */

public class Position {

    /**
     * Fields containing a row and a column
     */
    public final int row; 
    public final int col;  

    /**
     * Constructor
     */
    Position (int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Return the next position in the specified direction
     */
    public Position next(String direction) {
        if (direction.equals("up"))    return new Position(row-1, col);
        if (direction.equals("down"))  return new Position(row+1, col);
        if (direction.equals("left"))  return new Position(row, col-1);
        if (direction.equals("right")) return new Position(row, col+1);
        return this;
    }
    
    /**
     * Return the column
     */
    public int getCol() {
        return col;
    }
    
    /**
     * Return the row 
     */
    public int getRow() {
        return row;
    }

    /**
     * Return a string with the values of the fields.
     */
    public String toString() {
        return String.format("(%d,%d)", col, row);
    }
    
    /**
     * Return true if the postions are the same
     */
    public boolean isEqual(Position p) { //change to "equals()"
        if(p.getCol() == col && p.getRow() == row){
            return true;
        }
        return false;
    }

	@Override
	public int hashCode() { //append row to col and return result (e.g row=4, col=7, result=74)
		final int prime = 31; 
		int result = 1;
		result = prime * result + col;
		result = prime * result + row;
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
		Position other = (Position) obj;
		if (col != other.col)
			return false;
		if (row != other.row)
			return false;
		return true;
	}
    
    
}
