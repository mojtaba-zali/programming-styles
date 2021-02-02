package aspects;

/**
 * This utility class should be used by Aspects' code as reference to color the UI elements.
 * If you call it from the main method or from any of your non-aspects classes, you are doing something wrong.
 * 
 * @author gambi
 *
 */
public class ColorPalette {

    // RESET Char
    public static final String RESET = "\u001B[0m"; // ANSI_RESET 
        
    // Boat
    public static final String BOAT_BACKGROUND = "\u001B[41m"; // ANSI_RED_BACKGROUND  
    public static final String BOAT_FOREGROUND = "\u001B[37m"; // ANSI_WHITE 
    
    // Banner
    public static final String BANNER_BACKGROUND = "\u001B[40m"; // ANSI_BLACK_BACKGROUND
    public static final String BANNER_FOREGROUND = "\u001B[33m"; // ANSI_YELLOW 
    
    // Sea and Board
    public static final String BOARD_BACKGROUND = "\u001B[46m"; // ANSI_CYAN_BACKGROUND
    public static final String BOARD_FOREGROUND = "\u001B[34m"; // ANSI_BLUE 
    
    // PAWS
    public static final String FISHER = "\u001B[33m"; // ANSI_YELLOW 
    public static final String FISH = "\u001B[37m"; // ANSI_WHITE 
    
}
