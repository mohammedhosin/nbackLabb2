package se.kth.anderslm.ttt.model;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.Queue;

public class TicLogic {

    /*
     Game logic part
     */
    public static final int SIZE = 3;
    private int nrOfRounds,correctAnswers,currentRound;

    private ArrayList<Integer> nLatestStimuli;

    public void reset() {
        nLatestStimuli.clear();
        nrOfRounds=20;
        correctAnswers=0;
        currentRound=0;
    }

    public void addToLatestStimuliArray(int index){
        if (nLatestStimuli.size()==3){
            nLatestStimuli.remove(0);
        }
        nLatestStimuli.add(index);
    }


    public boolean isNstepsBackMatched(){
        if (nLatestStimuli.size()>1) {
            if (nLatestStimuli.get(nLatestStimuli.size() - 1) == nLatestStimuli.get(0))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    /*
    Singleton part
     */
    public static TicLogic getInstance(int stepsBack) {
        if (ticLogic == null) {
            ticLogic = new TicLogic(stepsBack);
        }
        return ticLogic;
    }

    private static TicLogic ticLogic = null;

    private TicLogic(int stepsBack) { // NB! Must be private - Singleton implementation
        nLatestStimuli = new ArrayList<>(stepsBack);
        reset();
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public int getNrOfRounds() {
        return nrOfRounds;
    }

    public void incrementCurrentRound(){
        currentRound++;
    }

    public int getCurrentRound() {
        return currentRound;
    }

}
