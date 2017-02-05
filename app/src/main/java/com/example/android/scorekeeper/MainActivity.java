package com.example.android.scorekeeper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.scorekeeper.R.style.card;

public class MainActivity extends AppCompatActivity {

    /**
     * This is the Java code for a football (soccer) match score keeper application.
     * Because the two rival teams can't score at the same time or score and get a yellow/red card,
     * we will consider the game to be something like a turn-based game, but in which a team can
     * have an undefined number of consecutive turns. For example, team A can score, get a red card,
     * get a yellow card, score again, score again etc, while team B doesn't score or get any card.
     * This fact is important because it will help to undo and redo our moves.
     */

    //The variables for player 1:
    int p1Score = 0; // goals team 1 scored
    int p1YellowCards = 0; // yellow card team 1 has
    int p1RedCards = 0; // red card team 1 has

    //The variables for player 2:
    int p2Score = 0; // goals team 2 scored
    int p2YellowCards = 0; // yellow card team 2 has
    int p2RedCards = 0; // red card team 2 has

    /**
     * "level" shows how many events happened until now and it will help us to undo/redo moves.
     * Like in "Hansel and Gretel" story, we will leave a trail of bread crumbs
     * (which is the variable named timeLine) so we can go back at any moment.
     * In other words, "level" is the time where Hansel and Gratel are right now (present).
     * When we will decrease the variable named "level" we will go back in time. (undo)
     * When we will increase the variable named "level" we will go in the future. (redo)
     */
    int level = 0;

    /**
     * timeLine is an array of integer values (that's why we typed "int[]" and not just "int"
     * An array is a container object that holds a fixed number of values of a single type.
     * Based on the value we find on the level "K" of this array, we will know what happened
     * that turn in the match (who scored / who got a red card / etc)
     * In other words, timeLine is a "log" (like in a shooter game, Counter-Strike for example,
     * where kills appear in upper right corner, or phone call history etc).

     * Here's a legend:
     * -3 -> player 1 got a red card.
     * -2 -> player 1 got a yellow card
     * -1 -> player 1 scored
     * 0 -> nothing
     * 1 -> player 2 scored
     * 2 -> player 2 got a yellow card
     * 3 -> player 2 got a red card
     * Basically negative values are for player 1, and positive ones for player 2.
     */
    int[] timeLine = new int[1000];

    /**
     * These variables are used to save values when the device is rotated.
     * More here: https://developer.android.com/guide/components/activities/activity-lifecycle.html
     */
    private static final String STATE_P1SCORE = "P1SCORE";
    private static final String STATE_P1YELLOWCARDS = "P1YELLOWCARDS";
    private static final String STATE_P1REDCARDS = "P1REDCARDS";
    private static final String STATE_P2SCORE = "P2SCORE";
    private static final String STATE_P2YELLOWCARDS = "P2YELLOWCARDS";
    private static final String STATE_P2REDCARDS = "P2REDCARDS";
    private static final String STATE_LEVEL = "LEVEL";
    private static final String STATE_TIMELINE = "TIMELINE";

    // Declaring these TextViews and "finding" them in onCreate method to optimize the application
    private TextView scoreTextView;
    private TextView yellowCardsTextView1;
    private TextView yellowCardsTextView2;
    private TextView redCardsTextView1;
    private TextView redCardsTextView2;

    /*
    // These strings were used to show some messages.
    private String SCORE_MESSAGE = "Score updated!";
    private String YELLOW_CARD_MESSAGE = "A yellow card was given to a player!";
    private String RED_CARD_MESSAGE = "A red card was given to a player!";
    private String RESTART_MESSAGE = "The game was restarted!";
    private String UNDO_MESSAGE = "The last action was undoed!";
    private String REDO_MESSAGE = "The last undoed action was redoed!";
    private String ERROR_MESSAGE = "Can't do that!";
    */

    /**
     * Saving the instance state because the main activity will destroy when screen
     * orientation is changed.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_P1SCORE, p1Score);
        savedInstanceState.putInt(STATE_P1YELLOWCARDS, p1YellowCards);
        savedInstanceState.putInt(STATE_P1REDCARDS, p1RedCards);
        savedInstanceState.putInt(STATE_P2SCORE, p2Score);
        savedInstanceState.putInt(STATE_P2YELLOWCARDS, p2YellowCards);
        savedInstanceState.putInt(STATE_P2REDCARDS, p2RedCards);
        savedInstanceState.putInt(STATE_LEVEL, level);
        savedInstanceState.putIntArray(STATE_TIMELINE, timeLine);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Restoring values back to normal.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //Each variable is restored
        p1Score = savedInstanceState.getInt(STATE_P1SCORE);
        p1YellowCards = savedInstanceState.getInt(STATE_P1YELLOWCARDS);
        p1RedCards = savedInstanceState.getInt(STATE_P1REDCARDS);
        p2Score = savedInstanceState.getInt(STATE_P2SCORE);
        p2YellowCards = savedInstanceState.getInt(STATE_P2YELLOWCARDS);
        p2RedCards = savedInstanceState.getInt(STATE_P2REDCARDS);
        level = savedInstanceState.getInt(STATE_LEVEL);
        timeLine = savedInstanceState.getIntArray(STATE_TIMELINE);

        //Updating the TextViews.
        displayScore();
        displayYellowCards();
        displayRedCards();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreTextView = (TextView) findViewById(R.id.scoreText);
        yellowCardsTextView1 = (TextView) findViewById(R.id.yellowCardsTextView1);
        yellowCardsTextView2 = (TextView) findViewById(R.id.yellowCardsTextView2);
        redCardsTextView1 = (TextView) findViewById(R.id.redCardsTextView1);
        redCardsTextView2 = (TextView) findViewById(R.id.redCardsTextView2);
    }

    //Increasing first / second team's score and displaying it.
    public void increaseScore(View view) {
        level++; // one player scored, so the level needs to be increased (a turn is finished)
        /**
         * Because I wanted to write less code, I decided to add first player's buttons "-1" tag
         * and second player's button "1" tag.
         * By doing so, I created only one method for both players, and judging by the tag I'll
         * decide which value to increase.
         * We transform an object's content to a string view.getTag().toString(), which is
         * converted into an int.
         */
        int i = Integer.parseInt(view.getTag().toString());
        if (i == -1) {
            p1Score++;
        } else {
            p2Score++;
        }
        /**
         * Remember that I mentioned in the beginning that negative values are for player 1?
         * Well, the tag for first player's button is also "-1".
         * So, we can just multiply the value with the variable i (which is -1 or 1)
         * If you're more comfortable, here is an equivalent code:
         if (i < 0) {
            timeLine[level] = - 1;
         } else {
            timeLine[level] = 1;
         }
         */
        timeLine[level] = 1 * i;

        /**
         * removeLastActions erases variables after our current position in case we precden

         * Example of what would happen WITHOUT this function:
         * 1. A scores, A scores, A scores, B scores.
         {0,  -1,      -1,       -1,        1} (this is how timeLine looks (it has a 0 on first
         position because an array starts with position 0)
         level = 4
         * 2. Undo two times.
         {0,  -1,      -1,       -1,        1} (timeline doesn't change)
         level = 2 (because "undo" was pressed two times)
         * 3. B scores and redo.
         {0,  -1,      -1,       1,        1}
         The last value (1) wasn't deleted when we pressed redo so it remaint here.
         level = 4
         * The result will be: 2-2.

         * Once other button is pressed there is no reason to keep the ruins of the old timeline.
         * Sooo... we just erase it.
         * It's a little bit like in "The Flash" when someone *cough* Barry Allen *cough*
         * screws the timeline (when he goes back in time and does a different move than
         * he previously did, the future changes) :)
         */
        removeLastActions();
        displayScore();
        //Showing a message
        //Toast.makeText(MainActivity.this, SCORE_MESSAGE,  Toast.LENGTH_SHORT).show();
    }

    //Increasing first / second team's number of yellow card and displaying it.
    //This method it's similar with the last one, only but the value is 2 or -2 and we update
    //another TextView and variables.
    public void increaseYellowCards(View view) {
        level++;
        int i = Integer.parseInt(view.getTag().toString());
        if (i == -1) {
            p1YellowCards++;
        } else {
            p2YellowCards++;
        }
        timeLine[level] = 2 * i;
        removeLastActions();
        displayYellowCards();
        //Toast.makeText(MainActivity.this, YELLOW_CARD_MESSAGE,  Toast.LENGTH_SHORT).show();
    }

    //Increasing first / second team's number of red card and displaying it.
    //This method it's similar with the last one, only but the value is 3 or -3.
    public void increaseRedCards(View view) {
        level++;
        int i = Integer.parseInt(view.getTag().toString());
        if (i == -1) {
            p1RedCards++;
        } else {
            p2RedCards++;
        }
        timeLine[level] = 3 * i;
        removeLastActions();
        displayRedCards();
        //Toast.makeText(MainActivity.this, RED_CARD_MESSAGE,  Toast.LENGTH_SHORT).show();
    }

    //Updating the score.
    public void displayScore() {
        scoreTextView.setText(p1Score + " – " + p2Score);
    }

    //Updating the number of yellow cards for both teams.
    public void displayYellowCards() {
        yellowCardsTextView1.setText("" + p1YellowCards);
        yellowCardsTextView2.setText("" + p2YellowCards);
    }

    //Updating the number of red cards for both teams.
    public void displayRedCards() {
        redCardsTextView1.setText("" + p1RedCards);
        redCardsTextView2.setText("" + p2RedCards);
    }

    /**
     * Other local variable (k) is used because we want to keep the "level variable".
     * Level variable need to remain untouched in this case because it will be used next time
     * when we want redo/undo.
     * timeLine[k] will become 0 as long as timeLine[k] is different than 0. Practically, we erase
     * the future
     * More info about "while": https://docs.oracle.com/javase/tutorial/java/nutsandbolts/while.html
     */
    public void removeLastActions() {
        int k = level + 1;
        while (timeLine[k] != 0) {
            timeLine[k] = 0;
        }
    }

    //Reinitializing all values back to 0
    public void restartGame(View view) {
        p1Score = 0;
        p1YellowCards = 0;
        p1RedCards = 0;

        p2Score = 0;
        p2YellowCards = 0;
        p2RedCards = 0;

        displayScore();
        displayYellowCards();
        displayRedCards();

        // Destroying the last timeLine because we don't need it anymore since we restarted the game
        timeLine = new int[1000];

        level = 0;
        //Showing a message
        //Toast.makeText(MainActivity.this, RESTART_MESSAGE, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is where magic happens. We will go back in time! YAAAY!
     * Based on the value of timeLine[level] we decide what variable we should decrease.
     * More info about switch: https://docs.oracle.com/javase/tutorial/java/nutsandbolts/switch.html
     * We check if the level is 0 because we can't go behind "BIG BANG" (you can't have an negative
     * element of an array)
     */
    public void undoLastAction(View view) {
        if (level > 0) {
            switch (timeLine[level]) {
                case -3:
                    p1RedCards--;
                    displayRedCards();
                    break;
                case -2:
                    p1YellowCards--;
                    displayYellowCards();
                    break;
                case -1:
                    p1Score--;
                    displayScore();
                    break;
                case 3:
                    p2RedCards--;
                    displayRedCards();
                    break;
                case 2:
                    p2YellowCards--;
                    displayYellowCards();
                    break;
                case 1:
                    p2Score--;
                    displayScore();
                    break;
            }
            level--;
            //Showing a message
            //Toast.makeText(MainActivity.this, UNDO_MESSAGE,  Toast.LENGTH_SHORT).show();
        } else {
            //Showing a message
            //Toast.makeText(MainActivity.this, ERROR_MESSAGE,  Toast.LENGTH_SHORT).show();
        }
        /**
         * The previous SWITCH is equivalent with this:
         if (timeLine[level] == -3) {
         p1RedCards--;
         displayRedCards();
         } else if(timeLine[level] == -2) {
         p1YellowCards--;
         displayYellowCards();
         } else if(timeLine[level] == -1) {
         p1Score--;
         displayScore();
         } else if(timeLine[level] == 1) {
         p2Score--;
         displayScore();
         } else if(timeLine[level] == 2) {
         p2YellowCards--;
         displayYellowCards();
         } else if(timeLine[level] == 3) {
         p2RedCards--;
         displayRedCards();
         } else if(timeLine[level] == 3) {
         level = 1;
         }
         */
    }

    /**
     * And in this method, very similar with the last one, we go in the future! WOOOW!
     * We check if the next value is 0. If it's 0 it means that nothing happened
     * beginning with that moment.
     */
    public void redoLastAction(View view) {
        if (timeLine[level + 1] != 0) {
            level++;
            switch (timeLine[level]) {
                case -3:
                    p1RedCards++;
                    displayRedCards();
                    break;
                case -2:
                    p1YellowCards++;
                    displayYellowCards();
                    break;
                case -1:
                    p1Score++;
                    displayScore();
                    break;
                case 3:
                    p2RedCards++;
                    displayRedCards();
                    break;
                case 2:
                    p2YellowCards++;
                    displayYellowCards();
                    break;
                case 1:
                    p2Score++;
                    displayScore();
                    break;
            }
            //Showing a message
            //Toast.makeText(MainActivity.this, REDO_MESSAGE,  Toast.LENGTH_SHORT).show();
        } else {
            //Showing a message
            //Toast.makeText(MainActivity.this, ERROR_MESSAGE,  Toast.LENGTH_SHORT).show();
        }
    }
}
