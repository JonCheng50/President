
//A play is defined as card(s) that the user wants to play.
public interface Play {

    // Returns whether the play is valid given if it is first, if it's the user's turn, and correct type
    // Also updates quickClear (using checkQuickClear) and skip instance variables
    // References the current TableHand
    boolean isValid(boolean first, boolean turn, boolean type);

    // Checks if play is a Quick Clear and updates quickClear instance variable
    void checkQuickClear();

    boolean isQuickClear();

    boolean isSkip();

    // Returns the type number of a play (1 for single, 2 for double, etc.)
    int getNUM();
}
