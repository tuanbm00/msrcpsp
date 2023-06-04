package org.example;

import java.util.List;

public class HeuristicPoint {
    public int conflict; // skill
    public int condition; // time
    public int score;

    public List<Integer> timeSolver;
    public HeuristicPoint(){
        conflict = 0;
        condition = 0;
        score = 0;
    }
}
