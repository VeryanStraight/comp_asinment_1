// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.
        
/* Code for COMP103 - 2021T2, Assignment 1
* Name:
* Username:
* ID:
*/

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
* DeShredder allows a user to sort fragments of a shredded document ("shreds") into strips, and
* then sort the strips into the original document.
* The program shows
*   - a list of all the shreds along the top of the window, 
*   - the working strip (which the user is constructing) just below it.
*   - the list of completed strips below the working strip.
* The "rotate" button moves the first shred on the list to the end of the list to let the
*  user see the shreds that have disappeared over the edge of the window.
* The "shuffle" button reorders the shreds in the list randomly
* The user can use the mouse to drag shreds between the list at the top and the working strip,
*  and move shreds around in the working strip to get them in order.
* When the user has the working strip complete, they can move
*  the working strip down into the list of completed strips, and reorder the completed strips
*
*/
public class DeShredder {

    // Fields to store the lists of Shreds and strips.  These should never be null.
    private List<Shred> allShreds = new ArrayList<Shred>();    //  List of all shreds
    private List<Shred> workingStrip = new ArrayList<Shred>(); // Current strip of shreds
    private List<List<Shred>> completedStrips = new ArrayList<List<Shred>>();

    // Constants for the display and the mouse
    public static final double LEFT = 20;       // left side of the display
    public static final double TOP_ALL = 20;    // top of list of all shreds 
    public static final double GAP = 5;         // gap between strips
    public static final int SIZE = (int)Shred.SIZE; // size of the shreds

    public static final double TOP_WORKING = TOP_ALL+SIZE+GAP;
    public static final double TOP_STRIPS = TOP_WORKING+(SIZE+GAP);

    //Fields for recording where the mouse was pressed  (which list/strip and position in list)
    // note, the position may be past the end of the list!
    private List<Shred> fromStrip;   // The strip (List of Shreds) that the user pressed on
    private int fromPosition = -1;   // index of shred in the strip
    
    /**
     * Initialises the UI window, and sets up the buttons. 
     */
    public void setupGUI() {
        UI.addButton("Load library",   this::loadLibrary);
        UI.addButton("Rotate",         this::rotateList);
        UI.addButton("Shuffle",        this::shuffleList);
        UI.addButton("Complete Strip", this::completeStrip);
        UI.addButton("Save Immage",           this::saveAsOneImmage);
        UI.addButton("Quit",           UI::quit);

        UI.setMouseListener(this::doMouse);
        UI.setWindowSize(1000,800);
        UI.setDivider(0);
    }
    
    /**
     * Asks user for a library of shreds, loads it, and redisplays.
     * Uses UIFileChooser to let user select library
     * and finds out how many images are in the library
     * Calls load(...) to construct the List of all the Shreds
     */
    public void loadLibrary(){
        Path filePath = Path.of(UIFileChooser.open("Choose first shred in directory"));
        Path directory = filePath.getParent(); //subPath(0, filePath.getNameCount()-1);
        int count=1;
        while(Files.exists(directory.resolve(count+".png"))){ count++; }
        //loop stops when count.png doesn't exist
        count = count-1;
        load(directory, count);   // YOU HAVE TO COMPLETE THE load METHOD
        display();
    }
    
    /**
     * Empties out all the current lists (the list of all shreds,
     *  the working strip, and the completed strips).
     * Loads the library of shreds into the allShreds list.
     * Parameters are the directory containing the shred images and the number of shreds.
     * Each new Shred needs the directory and the number/id of the shred.
     */
    public void load(Path dir, int count) {
        //clears the lists
        allShreds.clear();
        workingStrip.clear();
        completedStrips.clear();
        
        //Loads the library of shreds into the allShreds list.
        for(int i=0; i<count; i++){
            allShreds.add(new Shred(dir, i+1));
        }
    }
    
    /**
     * Rotate the list of all shreds by one step to the left
     * and redisplay;
     * Should not have an error if the list is empty
     * (Called by the "Rotate" button)
     */
    public void rotateList(){
        if(!allShreds.isEmpty()){
            //puts the first shred to the end and removes it from it's inital postion.
            Shred firstShred  = allShreds.get(0);
            allShreds.remove(0);
            allShreds.add(firstShred);
            
            display();
        }
    }
    
    /**
     * find if the anny of the strips in allShreds is simmlar to the last in the working strip
     * -finds the pixles of either side 
     * -compaires to the pixles of the oppsit side of the shreds in allShreds
     * -if they a
     */
    public void findSimmlarShred(){
        if(!workingStrip.isEmpty()){
            int size = workingStrip.size();
            Shred shred = workingStrip.get(size-1);
            
            
            Color[] left = new Color[SIZE];
            Color[] right = new Color[SIZE];
            
            left = findLeftPixels(shred);
            right = findRightPixels(shred);
            
            for(Shred nextSherd: allShreds){
                 Color[] nextLeft = new Color[SIZE];
                 Color[] nextRight = new Color[SIZE];
                 
                 nextLeft = findLeftPixels(nextSherd);
                 nextRight = findRightPixels(nextSherd);
                 
                 if(isSimmlar(left, nextRight) || isSimmlar(right, nextLeft)){
                     nextSherd.changeHighlight(); //highlight if matches
                 }else if(nextSherd.isHighlight()){
                     nextSherd.changeHighlight(); //if it does match but is highlight chand it back
                 }   
            }
        }else{
            for(Shred sherd: allShreds){
                if(sherd.isHighlight()){
                     sherd.changeHighlight(); 
                 }   
            }
        }
    }
    
    public boolean isSimmlar(Color[] list1, Color[] list2){
        List<Color> colours1 = new ArrayList<Color>();
        List<Color> colours2 = new ArrayList<Color>();
        
        for(int i=0; i<SIZE; i++){
            if(!colours1.contains(list1[i])){colours1.add(list1[i]);}
            if(!colours2.contains(list2[i])){colours2.add(list2[i]);}
        }
        
        if(colours1.size() == 1 || colours2.size() == 1){
            return false;
        }
        
        int count = 0;
        
        for(int i=0; i<SIZE; i++){//go throught the pixels
            Color color = list1[i];
            
            if(i>1 && i<SIZE-2){ //if not next to either end
                for(int j=-2; j<=2; j++){ //go through the pixles  in the other list within 2 places of the current pixel.
                    if(color.equals(list2[i+j])){//if they have the same colour increse count and jump out f the loop
                        count++;
                        break;
                    }
                }
            }
            
            if(i<=1 ){ //if is at start
                for(int j=-i; j<=2; j++){ 
                    if(color.equals(list2[i+j])){//if they have the same colour increse count and jump out f the loop
                        count++;
                        break;
                    }
                }
            }
            
            if(i>=SIZE-2 ){ //if is at end
                for(int j=-2; j<=(SIZE-1-i); j++){ 
                    if(color.equals(list2[i+j])){//if they have the same colour increse count and jump out f the loop
                        count++;
                        break;
                    }
                }
            }
        }

        double raito = count/SIZE;
        
        if(raito>=0.8){
            return true;
        }
        
        return false;
    }
    
    /*public boolean isSimmlar(Color[] list1, Color[] list2){
        int numMatching =0;
        List<Color> colours1 = new ArrayList<Color>();
        List<Color> colours2 = new ArrayList<Color>();
        
        for(int i=0; i<SIZE; i++){
            if(!colours1.contains(list1[i])){colours1.add(list1[i]);}
            if(!colours2.contains(list2[i])){colours2.add(list2[i]);}
        }
        
        if(colours1.size() == 1 || colours2.size() == 1){
            return false;
        }
        
        if(colours1.size() > colours2.size()){
            for(Color colour :colours2){
                if(colours1.contains(colour)){
                    numMatching++;
                }
            }
            if(numMatching == colours2.size()){return true;}
        }else{
            for(Color colour :colours1){
                if(colours2.contains(colour)){
                    numMatching++;
                }
            }
            if(numMatching == colours1.size()){return true;}
        } 
        
        return false;
    }*/
    
    /*public boolean containsSimmlarColor(Color color, List<Color> colours){
        int size = colours.size();
        
        int colorNum = color.getRed() + color.getGreen() + color.getBlue();
        for(int i=0; i<size; i++){
            Color colorCompare = colours.get(i);
            int colorNumCompare = colorCompare.getRed() + colorCompare.getGreen() + colorCompare.getBlue();
            
            if(Math.abs(colorNum-colorNumCompare) < 500){
                 return true;
            }
        }
        return false;
    }*/
    
    /**
     * finds all the pixles on the left side of a shred
     * 
     */
    public Color[] findLeftPixels(Shred shred){
        Color[][] ShredPixels = loadImage(shred.getFileName());
        Color[] left = new Color[SIZE];
        
        for(int i=0; i<SIZE; i++){
            left[i] = ShredPixels[i][0];
        }

        return left;
    }
    
    /**
     * finds all the pixles on the right side of a shred
     * 
     */
    public Color[] findRightPixels(Shred shred){
        Color[][] ShredPixels = loadImage(shred.getFileName());
        int size = ShredPixels.length;
        Color[] right = new Color[size];
        
        for(int i=0; i<size; i++){
            right[i] = ShredPixels[i][0];
        }

        return right;
    }
    
    /**
     * finds the dimensiond of the final image
     * puts the shreds in to one 2d array
     * saves the image
     * 
     */
    public void saveAsOneImmage(){
        //width and height of final immage
        int height = completedStrips.size() * SIZE ; 
        int length = lengthOFImmage();
        
        //2D arrays for the immage the the curent peice being added 
        Color[][] immage = new Color[height][length];
        Color[][] immagePeice;
        
        //varibles to keep track of how much of the image is alredy filled in 
        int imageRow = 0;
        int imageCol = 0;
       
        for(List<Shred> strip :completedStrips){
            //adds padding if nessary and records where it was added on the left or right
            int lenghtOFStrip = strip.size()*SIZE;
            int padding = length-lenghtOFStrip;
            String padSide = "";
            if(padding>0){padSide = pad(length, lenghtOFStrip, immage, imageRow);}
            
            for(Shred shred :strip){
                //puts the next shred in a 2d array
                String fileName = shred.getFileName();
                immagePeice = loadImage(fileName);
                
                
                if(padSide.equalsIgnoreCase("left")){//if there is no passing or it is to the right
                    //goes throught the immage and add the pixels of the immagePeice off set by what has alredy been filled in
                    for(int i=0; i<(SIZE); i++){
                        for(int j=0; j<(SIZE); j++){
                            immage[i+imageRow][j+imageCol+padding] = immagePeice[i][j];
                        }
                    }
                }else{//if there is padding on the left
                    //goes throught the immage and add the pixels of the immagePeice off set by what has alredy been filled in
                    for(int i=0; i<SIZE; i++){
                        for(int j=0; j<SIZE; j++){
                            immage[i+imageRow][j+imageCol] = immagePeice[i][j];
                        }
                    }
                }
                imageCol += SIZE;
            }
            imageRow += SIZE;
            imageCol = 0;
        }
        
        //saves the finished image to a file
        String fileName = UIFileChooser.save();
        saveImage(immage, fileName);
    }

   
    /**
     * finds the length of the longest strip
     * 
     */
    public int lengthOFImmage(){
        int lenghtmax=0;
        
        //goes through the list and finds the longest one
        for(List<Shred> strip : completedStrips){
            int lenght = strip.size() * SIZE;
            if(lenght>lenghtmax){
                lenghtmax = lenght;
            }
        }
        
        return lenghtmax;
    }
    
    /**
     * pads the required space with white in the 2d array
     * 
     */
    public String pad(int lengthRequired, int length, Color[][] immage, int row){
        int padding = lengthRequired-length;   
        String padSide = UI.askString("pad the left or right of row "+((row/SIZE) +1)+ ": "); //find what side the user wants the padding
    
        for(int i=row; i<row+SIZE; i++){
            if(padSide.equalsIgnoreCase("left")){
                //adds padding to the start of the row
                for(int j=0; j<(padding); j++){
                    immage[i][j]= Color.white;
                }
            }else{
                //adds padding to the end of the row
                for(int j=length; j<lengthRequired; j++){
                    immage[i][j] = Color.white;
                }
            }
        }
        return padSide;
    }
    
    
    
    /**
     * Shuffle the list of all shreds into a random order
     * and redisplay;
     */
    public void shuffleList(){
        if(!allShreds.isEmpty()){
            Collections.shuffle(allShreds);
            display();
        }
    }

    /**
     * Move the current working strip to the end of the list of completed strips.
     * (Called by the "Complete Strip" button)
     */
    public void completeStrip(){
        if(!workingStrip.isEmpty()){
            List<Shred> finishedStrip = new ArrayList<>(workingStrip);
            completedStrips.add(finishedStrip);
            workingStrip.clear();
            
            display();
        }
    }
    
    /**
     * Simple Mouse actions to move shreds and strips
     *  User can
     *  - move a Shred from allShreds to a position in the working strip
     *  - move a Shred from the working strip back into allShreds
     *  - move a Shred around within the working strip.
     *  - move a completed Strip around within the list of completed strips
     *  - move a completed Strip back to become the working strip
     *    (but only if the working strip is currently empty)
     * Moving a shred to a position past the end of a List should put it at the end.
     * You should create additional methods to do the different actions - do not attempt
     *  to put all the code inside the doMouse method - you will lose style points for this.
     * Attempting an invalid action should have no effect.
     * Note: doMouse uses getStrip and getColumn, which are written for you (at the end).
     * You should not change them.
     */
    public void doMouse(String action, double x, double y){
        if (action.equals("pressed")){
            fromStrip = getStrip(y);      // the List of shreds to move from (possibly null)
            fromPosition = getColumn(x);  // the index of the shred to move (may be off the end)
        }
        if (action.equals("released")){
            List<Shred> toStrip = getStrip(y); // the List of shreds to move to (possibly null)
            int toPosition = getColumn(x);     // the index to move the shred to (may be off the end)
            // perform the correct action, depending on the from/to strips/positions
            if(!validAction(toStrip, toPosition)){
                return;
            }
            
            if((fromStrip == allShreds || fromStrip == workingStrip)
                && (toStrip == allShreds || toStrip == workingStrip)){
                moveShred(toStrip, toPosition);
                findSimmlarShred(); 
            }else{
                moveStrip(toStrip);
                findSimmlarShred();
            }
                       
            display();
        }
    }
            
    // Additional methods to perform the different actions, called by doMouse
    public void moveStrip(List<Shred> toStrip){
        if(toStrip == workingStrip){
            if(toStrip.isEmpty()){
                //move to working strip
                workingStrip = fromStrip;
                completedStrips.remove(fromStrip);
            }
            
        }else{
            //swiches strips
            int toIndex = completedStrips.indexOf(toStrip);
            int fromIndex = completedStrips.indexOf(fromStrip);
            
            completedStrips.set(toIndex, fromStrip);
            completedStrips.set(fromIndex, toStrip);
        }
    }
 
    public void moveShred(List<Shred> toStrip, int toPosition){
        Shred shred = fromStrip.get(fromPosition);
        fromStrip.remove(fromPosition);
        toStrip.add(toPosition, shred);
    }
    
    public boolean validAction(List<Shred> toStrip, int toPosition){
        //checks that the user clicked on a strip
        if(fromStrip == null || toStrip == null){
            return false;
        }
        
        int toStripLength = toStrip.size();
        int fromStripLength = fromStrip.size();
        
        //checks that the to/from postions are not out of bounds.
        if(fromStrip != toStrip){
            if(toStripLength<toPosition || fromStripLength<=fromPosition){
                return false;
            }
        }else{
            if(toStripLength<=toPosition || fromStripLength<=fromPosition){
                return false;
            }
        }
        
        return true;
    }
    

    //=============================================================================
    // Completed for you. Do not change.
    // loadImage and saveImage may be useful for the challenge.

    /**
     * Displays the remaining Shreds, the working strip, and all completed strips
     */
    public void display(){
        UI.clearGraphics();

        // list of all the remaining shreds that haven't been added to a strip
        double x=LEFT;
        for (Shred shred : allShreds){
            shred.drawWithBorder(x, TOP_ALL);
            x+=SIZE;
        }

        //working strip (the one the user is workingly working on)
        x=LEFT;
        for (Shred shred : workingStrip){
            shred.draw(x, TOP_WORKING);
            x+=SIZE;
        }
        UI.setColor(Color.red);
        UI.drawRect(LEFT-1, TOP_WORKING-1, SIZE*workingStrip.size()+2, SIZE+2);
        UI.setColor(Color.black);

        //completed strips
        double y = TOP_STRIPS;
        for (List<Shred> strip : completedStrips){
            x = LEFT;
            for (Shred shred : strip){
                shred.draw(x, y);
                x+=SIZE;
            }
            UI.drawRect(LEFT-1, y-1, SIZE*strip.size()+2, SIZE+2);
            y+=SIZE+GAP;
        }
    }

    /**
     * Returns which column the mouse position is on.
     * This will be the index in the list of the shred that the mouse is on, 
     * (or the index of the shred that the mouse would be on if the list were long enough)
     */
    public int getColumn(double x){
        return (int) ((x-LEFT)/(SIZE));
    }

    /**
     * Returns the strip that the mouse position is on.
     * This may be the list of all remaining shreds, the working strip, or
     *  one of the completed strips.
     * If it is not on any strip, then it returns null.
     */
    public List<Shred> getStrip(double y){
        int row = (int) ((y-TOP_ALL)/(SIZE+GAP));
        if (row<=0){
            return allShreds;
        }
        else if (row==1){
            return workingStrip;
        }
        else if (row-2<completedStrips.size()){
            return completedStrips.get(row-2);
        }
        else {
            return null;
        }
    }

    public static void main(String[] args) {
        DeShredder ds =new DeShredder();
        ds.setupGUI();

    }


    /**
     * Load an image from a file and return as a two-dimensional array of Color.
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public Color[][] loadImage(String imageFileName) {
        if (imageFileName==null || !Files.exists(Path.of(imageFileName))){
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(Files.newInputStream(Path.of(imageFileName)));
            int rows = img.getHeight();
            int cols = img.getWidth();
            Color[][] ans = new Color[rows][cols];
            for (int row = 0; row < rows; row++){
                for (int col = 0; col < cols; col++){                 
                    Color c = new Color(img.getRGB(col, row));
                    ans[row][col] = c;
                }
            }
            return ans;
        } catch(IOException e){UI.println("Reading Image from "+imageFileName+" failed: "+e);}
        return null;
    }

    /**
     * Save a 2D array of Color as an image file
     * From COMP 102 assignment 8&9.
     * Maybe useful for the challenge. Not required for the core or completion.
     */
    public  void saveImage(Color[][] imageArray, String imageFileName) {
        int rows = imageArray.length;
        int cols = imageArray[0].length;
        BufferedImage img = new BufferedImage(cols, rows, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color c =imageArray[row][col];
                img.setRGB(col, row, c.getRGB());
            }
        }
        try {
            if (imageFileName==null) { return;}
            ImageIO.write(img, "png", Files.newOutputStream(Path.of(imageFileName)));
        } catch(IOException e){UI.println("Image reading failed: "+e);}

    }

}
