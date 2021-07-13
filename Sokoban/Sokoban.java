// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

/* Code for COMP103 - 2021T2, Assignment 1
 * Name:
 * Username:
 * ID:
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

/** 
 * Sokoban
 */

public class Sokoban {

    private Cell[][] cells;             // the array representing the warehouse
    private int rows;                   // the height of the warehouse
    private int cols;                   // the width of the warehouse
    private int level = 1;              // current level 

    private Position workerPos;         // the position of the worker
    private String workerDir = "left";  // the direction the worker is facing
    
    private Deque<ActionRecord> actionRecord = new ArrayDeque<ActionRecord>(); //record ot the acion taken
    private Deque<ActionRecord> redoRecord = new ArrayDeque<ActionRecord>(); //record of the undon actions for the redo button
    
    private Deque<Position> path = new ArrayDeque<Position>(); // list of the found path
    private ArrayList<Position> checkedCells = new ArrayList<Position>(); // list of the cells that have been checked
    
    private Deque<ActionRecord> directionsPath = new ArrayDeque<ActionRecord>(); //the found path as directions


    /** 
     *  Constructor: load the 0th level.
     */
    public Sokoban() {
        doLoad();
    }

    /** 
     *  Moves the worker in the given direction, if possible.
     *  If there is box in front of the Worker and a space in front of the box,
     *  then push the box.
     *  Otherwise, if the worker can't move, do nothing.
     */
    public void moveOrPush(String direction) {
        workerDir = direction;                       // turn worker to face in this direction
        redoRecord.clear();

        Position nextP = workerPos.next(direction);  // where the worker would move to
        Position nextNextP = nextP.next(direction);  // where a box would be pushed to

        // is there a box in that direction which can be pushed?
        if ( cells[nextP.row][nextP.col].hasBox() &&
        cells[nextNextP.row][nextNextP.col].isFree() ) { 
            push(direction);
            actionRecord.push(new ActionRecord("push", direction));
            
            if (isSolved()) { reportWin(); }
        }
        // is the next cell free for the worker to move into?
        else if ( cells[nextP.row][nextP.col].isFree() ) { 
            move(direction);
            actionRecord.push(new ActionRecord("move", direction));
        }
    }

    /**
     * Moves the worker into the new position (guaranteed to be empty) 
     * @param direction the direction the worker is heading
     */
    public void move(String direction) {
        drawCell(workerPos);                   // redisplay cell under worker
        workerPos = workerPos.next(direction); // put worker in new position
        drawWorker();                          // display worker at new position

        Trace.println("Move " + direction);    // for debugging
    }
    
    /**
     * Undoes the prevous move
     * 
     */
    public void undo() {
        if(actionRecord.isEmpty()){
            return;
        }
        ActionRecord action = actionRecord.pop();     //gets the last action
        String RecordedDirection = action.direction();
        
        String direction = opposite(RecordedDirection);
      
        workerDir = RecordedDirection;               // turn worker to face in the orignal direction 
        
        if(action.isPush()){
            pull(direction); 
            redoRecord.push(new ActionRecord("push", RecordedDirection));
        }else{
            move(direction);
            redoRecord.push(new ActionRecord("move", RecordedDirection));
        }
    }
    
    /**
     * Redoes the prevous move
     * 
     */
    public void redo() {
        if(redoRecord.isEmpty()){ 
            return;
        }
        ActionRecord action = redoRecord.pop();     //gets the last action
        String direction = action.direction();
      
        workerDir = direction;               // turn worker to face in the orignal direction 
        
        if(action.isPush()){
            push(direction); 
            actionRecord.push(new ActionRecord("push", direction));
        }else{
            move(direction);
            actionRecord.push(new ActionRecord("move", direction));
        }
    }

    /**
     * Push: Moves the Worker, pushing the box one step 
     *  @param direction the direction the worker is heading
     */
    public void push(String direction) {
        Position boxPos = workerPos.next(direction);   // where box is
        Position newBoxPos = boxPos.next(direction);   // where box will go

        cells[boxPos.row][boxPos.col].removeBox();     // remove box from current cell
        cells[newBoxPos.row][newBoxPos.col].addBox();  // place box in its new position

        drawCell(workerPos);                           // redisplay cell under worker
        drawCell(boxPos);                              // redisplay cell without the box
        drawCell(newBoxPos);                           // redisplay cell with the box

        workerPos = boxPos;                            // put worker in new position
        drawWorker();                                  // display worker at new position

        Trace.println("Push " + direction);   // for debugging
    }

    /**
     * Pull: (could be useful for undoing a push)
     *  move the Worker in the direction,
     *  pull the box into the Worker's old position
     */
    public void pull(String direction) {
        String opposite = opposite(direction);
        
        Position boxPos = workerPos.next(opposite);   // where box is
        Position newWorkerPos = workerPos.next(direction);   // where worker will go
        
        cells[boxPos.row][boxPos.col].removeBox();     // remove box from current cell
        cells[workerPos.row][workerPos.col].addBox();  // place box in its new position
        
        drawCell(workerPos);                           // redisplay cell under worker
        drawCell(boxPos);                              // redisplay cell without the box

        workerPos = newWorkerPos;                            // put worker in new position
        drawWorker();                                  // display worker at new position

        Trace.println("Pull " + direction);   // for debugging
    }
    
    /**
     * 
     *  
     */
    public Boolean findPath(Position inital, Position end) {// not working 
        checkedCells.add(inital);
        path.push(inital);
        
        //returns true if the end is found
        if(inital.isEqual(end)){
            return true;
        }
        
        for(int i=0; i<4; i++){
            //go through nabours
            Position neighbor;
            if(i==0){neighbor=inital.next("left");}
            else if(i==1){neighbor=inital.next("up");}
            else if(i==2){neighbor=inital.next("right");}
            else{neighbor=inital.next("down");}
            UI.println(neighbor);
           
            //checks that the nabour is free and hasn't been checked
            if(!cells[neighbor.row][neighbor.col].isFree() || checkedCells.contains(neighbor)){
                continue;
            }
            
            if(findPath(neighbor, end)){//return true when the end is found
                return true; 
            }
        }
        
        path.pop();
        
        return false;
    }
   
    //creat a method for turning list of postions in to diretions.
    /*public void PostionsToDirections() {
        int size = path.size();
        
        for(int i=size-1; i>1; i++){
            String Direction="";
            
            Position first = path.get(i);
            Position second = path.get(i-1);
            
            int colF = first.getCol();
            int colS = second.getCol();
            
            int rowF = first.getRow();
            int rowS = second.getRow();
            
            if((rowF == rowS) && (colF == colS+1)){Direction="right";}
            if((rowF == rowS) && (colF == colS-1)){Direction="left";}
            if((rowF == rowS+1) && (colF == colS)){Direction="down";}
            if((rowF == rowS-1) && (colF == colS)){Direction="up";}
            
            directionsPath.push(new ActionRecord("move", Direction));
        }  
    }*/
    
    public void PostionsToDirections() {
        int size = path.size();
        Position first = path.pop();
        
        for(int i=1; i>size; i++){
            String Direction="";
            Position second = path.pop();
            
            int colF = first.getCol();
            int colS = second.getCol();
            
            int rowF = first.getRow();
            int rowS = second.getRow();
            
            if((rowF == rowS) && (colF+1 == colS)){Direction="right";}
            if((rowF == rowS) && (colF-1 == colS)){Direction="left";}
            if((rowF+1 == rowS) && (colF == colS)){Direction="down";}
            if((rowF-1 == rowS) && (colF == colS)){Direction="up";}
            
            directionsPath.push(new ActionRecord("move", Direction));
            
            first = second;
        }        
    }
    
    public Position findPostionMouse(double x, double y){
        int col = (int)((x-LEFT_MARGIN)/(CELL_SIZE));
        int row = (int)((y-TOP_MARGIN)/(CELL_SIZE));
        
        if(cells[row][col].isFree()){
            return (new Position(row, col));
        }else{
            return null;
        }
    }
    
    public void doMouse(String action, double x, double y){
        if(action.equals("released")){
            Position endPostion = findPostionMouse(x, y); //find the postion clicked on
            
            if(endPostion != null){
                UI.println("path not found");
                if(findPath(workerPos, endPostion)){//find a path to end point
                    UI.println("path found");
                    PostionsToDirections();//convrt list of postions to stack of directions
                    while(!directionsPath.isEmpty()){
                        ActionRecord move = directionsPath.pop();     //gets the next action
                        String direction = move.direction();
      
                        workerDir = direction;               // turn worker to face in the direction 
                        move(direction); //moves the worker
                        UI.sleep(50);
                    }
                }
                UI.println("loop end");
            }
        }
    }

    //creat a doMoues that calls findPath and if true converst so actions and dose the list of actions
    
    /**
     * Report a win by flickering the cells with boxes
     */
    public void reportWin(){
        for (int i=0; i<12; i++) {
            for (int row=0; row<cells.length; row++)
                for (int column=0; column<cells[row].length; column++) {
                    Cell cell=cells[row][column];

                    // toggle shelf cells
                    if (cell.hasBox()) {
                        cell.removeBox();
                        drawCell(row, column);
                    }
                    else if (cell.isEmptyShelf()) {
                        cell.addBox();
                        drawCell(row, column);
                    }
                }

            UI.sleep(100);
        }
    }

    /** 
     *  Returns true if the warehouse is solved, 
     *  i.e., all the shelves have boxes on them 
     */
    public boolean isSolved() {
        for(int row = 0; row<cells.length; row++) {
            for(int col = 0; col<cells[row].length; col++)
                if(cells[row][col].isEmptyShelf())
                    return  false;
        }

        return true;
    }

    /** 
     * Returns the direction that is opposite of the parameter
     * useful for undoing!
     */
    public String opposite(String direction) {
        if ( direction.equals("right")) return "left";
        if ( direction.equals("left"))  return "right";
        if ( direction.equals("up"))    return "down";
        if ( direction.equals("down"))  return "up";
        throw new RuntimeException("Invalid  direction");
    }


    // Drawing the warehouse
    private static final int LEFT_MARGIN = 40;
    private static final int TOP_MARGIN = 40;
    private static final int CELL_SIZE = 25;

    /**
     * Draw the grid of cells on the screen, and the Worker 
     */
    public void drawWarehouse() {
        UI.clearGraphics();
        // draw cells
        for(int row = 0; row<cells.length; row++)
            for(int col = 0; col<cells[row].length; col++)
                drawCell(row, col);

        drawWorker();
        
        for(int i=0; i<11; i++){
            UI.drawString(String.valueOf(i), 30, 55+(i*25));
        }
        for(int i=0; i<20; i++){
            UI.drawString(String.valueOf(i), 45+(i*25), 55+(25*11));
        }
    }

    /**
     * Draw the cell at a given position
     */
    private void drawCell(Position pos) {
        drawCell(pos.row, pos.col);
    }

    /**
     * Draw the cell at a given row,col
     */
    private void drawCell(int row, int col) {
        double left = LEFT_MARGIN+(CELL_SIZE* col);
        double top = TOP_MARGIN+(CELL_SIZE* row);
        cells[row][col].draw(left, top, CELL_SIZE);
    }

    /**
     * Draw the worker at its current position.
     */
    private void drawWorker() {
        double left = LEFT_MARGIN+(CELL_SIZE* workerPos.col);
        double top = TOP_MARGIN+(CELL_SIZE* workerPos.row);
        UI.drawImage("worker-"+workerDir+".gif",
            left, top, CELL_SIZE,CELL_SIZE);
    }

    /**
     * Load a grid of cells (and Worker position) for the current level from a file
     */
    public void doLoad() {
        Path path = Path.of("warehouse" + level + ".txt");

        if (! Files.exists(path)) {
            UI.printMessage("Run out of levels!");
            level--;
        }
        else {
            List<String> lines = new ArrayList<String>();
            try {
                Scanner sc = new Scanner(path);
                while (sc.hasNext()){
                    lines.add(sc.nextLine());
                }
                sc.close();
            } catch(IOException e) {UI.println("File error: " + e);}

            int rows = lines.size();
            cells = new Cell[rows][];

            for(int row = 0; row < rows; row++) {
                String line = lines.get(row);
                int cols = line.length();
                cells[row]= new Cell[cols];
                for(int col = 0; col < cols; col++) {
                    char ch = line.charAt(col);
                    if (ch=='w'){
                        cells[row][col] = new Cell("empty");
                        workerPos = new Position(row,col);
                    }
                    else if (ch=='.') cells[row][col] = new Cell("empty");
                    else if (ch=='#') cells[row][col] = new Cell("wall");
                    else if (ch=='s') cells[row][col] = new Cell("shelf");
                    else if (ch=='b') cells[row][col] = new Cell("box");
                    else {
                        throw new RuntimeException("Invalid char at "+row+","+col+"="+ch);
                    }
                }
            }
            drawWarehouse();
            UI.printMessage("Level "+level+": Push the boxes to their target positions. Use buttons or put mouse over warehouse and use keys (arrows, wasd, ijkl, u)");
        }
    }

    /**
     * Add the buttons and set the key listener.
     */
    public void setupGUI(){
        UI.setMouseListener(this::doMouse);
        UI.addButton("New Level", () -> {level++; doLoad();});
        UI.addButton("Restart",   this::doLoad);
        UI.addButton("Undo",   this::undo);
        UI.addButton("Redo",   this::redo);
        UI.addButton("left",      () -> {moveOrPush("left");});
        UI.addButton("up",        () -> {moveOrPush("up");});
        UI.addButton("down",      () -> {moveOrPush("down");});
        UI.addButton("right",     () -> {moveOrPush("right");});
        UI.addButton("Quit",      UI::quit);

        UI.setKeyListener(this::doKey);
        UI.setDivider(0.0);
    }

    /** 
     * Respond to key actions
     */
    public void doKey(String key) {
        key = key.toLowerCase();
        if (key.equals("i")|| key.equals("w") ||key.equals("up")) {
            moveOrPush("up");
        }
        else if (key.equals("k")|| key.equals("s") ||key.equals("down")) {
            moveOrPush("down");
        }
        else if (key.equals("j")|| key.equals("a") ||key.equals("left")) {
            moveOrPush("left");
        }
        else if (key.equals("l")|| key.equals("d") ||key.equals("right")) {
            moveOrPush("right");
        }
    }

    public static void main(String[] args) {
        Sokoban skb = new Sokoban();
        skb.setupGUI();
    }
}
