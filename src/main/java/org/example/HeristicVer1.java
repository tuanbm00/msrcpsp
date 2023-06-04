package org.example;

import java.io.IOException;
import java.util.*;

public class HeristicVer1 {
    public long timeStart;
    public long timeEnd;
    public List<List<Integer>> solver = new ArrayList<>();
    private final FileReaderTAP fr;
    Random rd = new Random();
    public  HeristicVer1 (String filename) throws IOException {
        fr = new FileReaderTAP(filename);
    }

    public void CreateSolve(){
        solver.clear();
        for(int i = 0;i < fr.numResource;i++){
            List<Integer> solverR = new ArrayList<>();
            solver.add(solverR);
        }
        Random rd = new Random();
        for(int i = 0;i < fr.numTask;i++){
            solver.get(rd.nextInt(fr.numResource)).add(i);
        }
    }

    public void Solve(int loop){
        timeStart = System.currentTimeMillis();
        int limitLoop = 0;
        HeuristicPoint best = Evaluate(solver);
        for(int i = 0;i < loop;i++){
            limitLoop++;
            int task = SelectTask(solver);
            List<List<List<Integer>>> listBestSolver = new ArrayList<>();
            List<List<Integer>> copy = new ArrayList<>();
            for (List<Integer> integers : solver) {
                copy.add(new ArrayList<>(integers));
            }

            HeuristicPoint bestPoint = Evaluate(solver);
            //System.out.println("Best condition first: " + bestPoint.condition + ", Best score: " + bestPoint.score);

            for (List<Integer> integers : copy) {
                if (integers.contains(task)) {
                    integers.remove((Integer) task);
                    break;
                }
            }
            for(int j = 0;j < copy.size();j++){
                for(int k = 0; k < copy.get(j).size() + 1;k++){
                    List<List<Integer>> trial = new ArrayList<>();
                    for (List<Integer> integers : copy) {
                        trial.add(new ArrayList<>(integers));
                    }
                    trial.get(j).add(k, task);
                    HeuristicPoint point = Evaluate(trial);
                    if(point.conflict < bestPoint.conflict){
                        bestPoint.conflict = point.conflict;
                        bestPoint.condition = point.condition;
                        bestPoint.score = point.score;
                        listBestSolver.clear();
                        listBestSolver.add(trial);
                    }
                    else if(point.conflict == bestPoint.conflict){
                        if(point.condition < bestPoint.condition){
                            bestPoint.conflict = point.conflict;
                            bestPoint.condition = point.condition;
                            bestPoint.score = point.score;
                            listBestSolver.clear();
                            listBestSolver.add(trial);
                        }
                        else if (point.condition == bestPoint.condition){
                            if(point.condition == 0){
                                if(point.score < bestPoint.score) {
                                    bestPoint.score = point.score;
                                    listBestSolver.clear();
                                    listBestSolver.add(trial);
                                }
                                else if(point.score == bestPoint.score){
                                    listBestSolver.add(trial);
                                }
                            }
                            else{
                                listBestSolver.add(trial);
                            }
                        }
                    }
                }
            }
            if(listBestSolver.size() > 0){
                solver = listBestSolver.get(rd.nextInt(listBestSolver.size()));
                //System.out.println(solver);
                if(best.conflict > bestPoint.conflict || (best.conflict == bestPoint.conflict && best.condition < bestPoint.condition)
                || (best.conflict == bestPoint.conflict && best.condition == bestPoint.condition && best.score > bestPoint.score)){
                    best.conflict = bestPoint.conflict;
                    best.condition = bestPoint.condition;
                    best.score = bestPoint.score;
                    limitLoop = 0;
                    System.out.println("Best conflict: " + bestPoint.conflict + ", Best condition: " + bestPoint.condition + ", Best score: " + bestPoint.score + ", task: " + task + ", loop: " + i);
                }
            }
//            if(limitLoop > 2000){
//                System.out.println("Break at loop: " + i);
//                break;
//            }
        }
        timeEnd = System.currentTimeMillis();
    }

    public int SelectTask(List<List<Integer>> resolve){
        List<Integer> listTask = new ArrayList<>();
        List<Integer> listTaskSelect = new ArrayList<>();
        List<Integer> listViolation1 = new ArrayList<>();
        List<Integer> listViolation2 = new ArrayList<>();
        List<Integer> listSelectVio = new ArrayList<>();

        for(int i = 0;i < fr.numTask;i++) {
            listViolation1.add(0);
            listViolation2.add(0);
        }

        for(int i = 0;i < fr.numResource;i++){
            for(int task : resolve.get(i)){
                if(fr.KJ[task][i] != 1) {
                    listViolation1.set(task, listViolation1.get(task) + 1);
                }
            }
            for(int j = 0;j < resolve.get(i).size();j++){
                for(int k = j + 1;k < resolve.get(i).size();k++){
                    if(fr.fullRelation[resolve.get(i).get(j)][resolve.get(i).get(k)] == -1) {
                        listViolation2.set(resolve.get(i).get(j), listViolation2.get(resolve.get(i).get(j)) + 1);
                    }
                }
            }
        }

        int maxVio1 = Collections.max(listViolation1);
        for(int i = 0;i < fr.numTask;i++) {
            if (listViolation1.get(i) == maxVio1) {
                listTask.add(i);
                listSelectVio.add(listViolation2.get(i));
            }
        }
        int maxVio2 = Collections.max(listSelectVio);
        for(int i = 0;i < listTask.size();i++){
            if(listSelectVio.get(i) == maxVio2)
                listTaskSelect.add(listTask.get(i));
        }

        if(listTaskSelect.size() == 0)
            return  0;
        return listTaskSelect.get(rd.nextInt(listTaskSelect.size()));
    }

    public HeuristicPoint Evaluate(List<List<Integer>> resolve){
        HeuristicPoint point = new HeuristicPoint();
        for(int i = 0;i < fr.numResource;i++){
            for(int task : resolve.get(i)){
                if(fr.KJ[task][i] != 1) {
                    point.conflict += 1;
                    //System.out.println("resouce " + i + " cant do task " + task);
                }
            }
            for(int j = 0;j < resolve.get(i).size();j++){
                for(int k = j;k < resolve.get(i).size();k++){
                    if(fr.fullRelation[resolve.get(i).get(j)][resolve.get(i).get(k)] == -1) {
                        point.conflict += 1;
                        //System.out.println("task " + (resolve.get(i).get(j)) + " must do after task " + (resolve.get(i).get(k)));
                    }
                }
            }
        }

        if(point.conflict > 0)
            return point;

        List<Integer> timeResult = new ArrayList<>();
        List<Integer> timeResource = new ArrayList<>();
        for(int i = 0;i < fr.numResource;i++) {
            timeResource.add(0);
        }
        for(int i = 0;i < fr.numTask;i++)
            timeResult.add(-1);
        int loop = 0;
        while(timeResult.contains(-1)){
            loop++;
            for(int i = 0;i < fr.numResource;i++){
                for(int j = 0;j < resolve.get(i).size();j++){
                    int task = resolve.get(i).get(j);
                    if(timeResult.get(task) > 0)
                        continue;
                    boolean checkCanStart = true;
                    int timeStart = timeResource.get(i);
                    if(fr.start.get(task) >= 0)
                        if(fr.start.get(task) > timeStart)
                            timeStart = fr.start.get(task);
                    for(int k = 0;k < fr.numTask;k++) {
                        if (fr.fullRelation[task][k] == -1){
                            if (timeResult.get(k) < 0) {
                                checkCanStart = false;
                                break;
                            }
                            else
                                timeStart = Math.max(timeResult.get(k) + fr.duration.get(k), timeStart);
                        }
                    }

                    if(checkCanStart) {
                        loop = 0;
                        //System.out.println("task " + task + "can start" + timeResult);
                        timeResult.set(task, timeStart);
                        timeResource.set(i, timeStart + fr.duration.get(task));
                        if(point.score < timeStart + fr.duration.get(task))
                            point.score = timeStart + fr.duration.get(task);
                    }
                }
            }
            if(loop > 100){
                System.out.println("Error");
                break;
            }
        }
        for(int i = 0;i < fr.numTask;i++){
            if(fr.start.get(i) >= 0)
                if(timeResult.get(i) < fr.start.get(i))
                    point.condition += 1;
            if(fr.end.get(i) > 0)
                if(timeResult.get(i) + fr.duration.get(i) > fr.end.get(i))
                    point.condition += 1;
        }
        point.timeSolver = new ArrayList<>(timeResult);
        return  point;
    }
}
