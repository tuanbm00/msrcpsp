package org.example;

import java.io.IOException;
import java.util.*;

class PointSolution{
    public int fitness;
    public int[] timeTaskDone;

    public PointSolution(int f, int[] time){
        fitness = f;
        timeTaskDone = time;
    }
}

class Individual{
    public int fitness;
    public List<Integer> resolver;

    public Individual(int f, List<Integer> solver){
        fitness = f;
        resolver = solver;
    }
}

public class GeneticAlgorithm {
    public long timeStart;
    public long timeEnd;

    public int bestSorce;

    public int breakLoop;
    private final FileReaderTAP fr;
    private Random rd = new Random();

    private List<List<Integer>> fullRelation = new ArrayList<>();
    public  GeneticAlgorithm (String filename) throws IOException {
        fr = new FileReaderTAP(filename);
        CreateRelation();
    }

    private void CreateRelation(){
        for(int i = 0;i < fr.numTask;i++){
            List<Integer> rltion = new ArrayList<>();
            rltion.add(0);
            rltion.add(0);
            rltion.add(i+1);
            fullRelation.add(rltion);
        }

        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                if(fr.fullRelation[i][j] == 1)
                    fullRelation.get(i).set(0, fullRelation.get(i).get(0) - 1);
                if(fr.fullRelation[i][j] == -1)
                    fullRelation.get(i).set(1, fullRelation.get(i).get(1) + 1);
            }
        }
    }

    public PointSolution CostSolution(List<Integer> solution){
        List<List<Integer>> relation = new ArrayList<>();
        for(int i = 0;i < fr.numTask;i++){
            List<Integer> rl = new ArrayList<>();
            for(int j = 0;j < fr.numTask;j++){
                rl.add(fr.relationGA[i][j]);
            }
            relation.add(rl);
        }
        List<Integer> timeResource = new ArrayList<>();
        int[] listResourceDone = new int[fr.numResource];
        Arrays.fill(listResourceDone, -1);
        List<List<Integer>> listWork = new ArrayList<>();
        for(int i = 0;i < fr.numResource;i++){
            timeResource.add(0);
            List<Integer> work = new ArrayList<>();
            listWork.add(work);
        }
        for(int i = 0;i < fr.numTask;i++){
            listWork.get(solution.get(i)).add(i);
        }
        int[] timeTaskDone = new int[fr.numTask];
        Arrays.fill(timeTaskDone, -1);
        List<List<Integer>> listTimeBefore = new ArrayList<>();
        for(int i = 0;i < fr.numTask;i++){
            List<Integer> timeE = new ArrayList<>();
            for(int j = 0;j < fr.numTask;j++)
                timeE.add(0);
            listTimeBefore.add(timeE);
        }

        int doneTask = 0;
        int salary = 0;
        for(int i = 0;i < fr.numTask;i++){
            salary += fr.listSalary.get(solution.get(i)) * fr.duration.get(i);
        }

        int loop = 0;
        int testLoop = 0;
        while(doneTask < fr.numTask){
            testLoop += 1;
            if(testLoop > 1000){
                System.out.println("Out Loop");
                testLoop = 0;
            }
            for(int i = 0;i < fr.numResource;i++){
                loop += 1;
                Boolean isAdd = false;
                if(listWork.get(i).size() > 0){
                    if(Collections.max(timeResource) != timeResource.get(i))
                        isAdd = true;
                    if(loop > fr.numResource){
                        loop = 0;
                        isAdd = true;
                    }
                    if(isAdd){
                        int taskAdd = -1;
                        int diff = Integer.MAX_VALUE;
                        int timeStart = 0;
                        int timeAfter = 0;
                        int numAfter = 0;
                        for(int j = 0;j < listWork.get(i).size();j++){
                            int task = listWork.get(i).get(j);
                            if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                if (numAfter > fullRelation.get(task).get(0)) {
                                    numAfter = fullRelation.get(task).get(0);
                                    taskAdd = task;
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0){
                                        timeBefore = timeResource.get(i);
                                    }
                                    timeStart = Integer.max(timeBefore, timeResource.get(i));
                                    timeAfter = timeStart;
                                }
                            }
                        }
                        if(numAfter < 0){
                            for(int j = 0;j < listWork.get(i).size();j++){
                                int task = listWork.get(i).get(j);
                                if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0){
                                        timeBefore = timeResource.get(i);
                                    }
                                    if(timeBefore + fr.duration.get(task) > timeAfter)
                                        continue;
                                    int timeDiff = timeAfter - timeBefore - fr.duration.get(task);
                                    if(timeDiff < diff){
                                        diff = timeDiff;
                                        taskAdd = task;
                                        timeStart = Integer.max(timeBefore, timeResource.get(i));
                                    }
                                }
                            }
                        }
                        else{
                            for(int j = 0;j < listWork.get(i).size();j++){
                                int task = listWork.get(i).get(j);
                                if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0 || timeBefore < timeResource.get(i)){
                                        timeBefore = timeResource.get(i);
                                    }
                                    int timeDiff = Math.abs(timeBefore - timeResource.get(i));
                                    if(timeDiff < diff){
                                        diff = timeDiff;
                                        taskAdd = task;
                                        timeStart = Integer.max(timeBefore, timeResource.get(i));
                                    }
                                    else if(timeDiff == diff){
                                        if(fr.duration.get(task) < fr.duration.get(taskAdd)){
                                            taskAdd = task;
                                            timeStart = Integer.max(timeBefore, timeResource.get(i));
                                        }
                                    }
                                }
                            }
                        }
                        if(taskAdd != -1){
                            doneTask += 1;
                            for(int k = 0;k < listWork.get(i).size();k++){
                                if(listWork.get(i).get(k) == taskAdd){
                                    listWork.get(i).remove(k);
                                    break;
                                }
                            }

                            timeResource.set(i, timeStart + fr.duration.get(taskAdd));
                            timeTaskDone[taskAdd] = timeStart + fr.duration.get(taskAdd);
                            for(int m = 0;m < fr.numTask;m++){
                                if(relation.get(m).get(taskAdd) == 1){
                                    listTimeBefore.get(m).set(taskAdd, timeTaskDone[taskAdd]);
                                    relation.get(m).set(taskAdd, 0);
                                }
                            }
                        }
                    }
                }
                else
                    listResourceDone[i] = 0;
            }
        }
        int fitness = Collections.max(timeResource);
        return new PointSolution(fitness, timeTaskDone);
    }

    public PointSolution CostSolutionTime(List<Integer> solution){
        List<List<Integer>> relation = new ArrayList<>();
        for(int i = 0;i < fr.numTask;i++){
            List<Integer> rl = new ArrayList<>();
            for(int j = 0;j < fr.numTask;j++){
                rl.add(fr.relationGA[i][j]);
            }
            relation.add(rl);
        }
        List<Integer> timeResource = new ArrayList<>();
        int[] listResourceDone = new int[fr.numResource];
        Arrays.fill(listResourceDone, -1);
        List<List<Integer>> listWork = new ArrayList<>();
        for(int i = 0;i < fr.numResource;i++){
            timeResource.add(0);
            List<Integer> work = new ArrayList<>();
            listWork.add(work);
        }
        for(int i = 0;i < fr.numTask;i++){
            listWork.get(solution.get(i)).add(i);
        }
        int[] timeTaskDone = new int[fr.numTask];
        Arrays.fill(timeTaskDone, -1);
        List<List<Integer>> listTimeBefore = new ArrayList<>();
        for(int i = 0;i < fr.numTask;i++){
            List<Integer> timeE = new ArrayList<>();
            for(int j = 0;j < fr.numTask;j++)
                timeE.add(0);
            listTimeBefore.add(timeE);
        }

        int doneTask = 0;
        int salary = 0;
        for(int i = 0;i < fr.numTask;i++){
            salary += fr.listSalary.get(solution.get(i)) * fr.duration.get(i);
        }

        int loop = 0;
        int testLoop = 0;
        while(doneTask < fr.numTask){
            testLoop += 1;
            if(testLoop > 1000){
                System.out.println("Out Loop");
                testLoop = 0;
            }
            for(int i = 0;i < fr.numResource;i++){
                loop += 1;
                Boolean isAdd = false;
                if(listWork.get(i).size() > 0){
                    if(Collections.max(timeResource) != timeResource.get(i))
                        isAdd = true;
                    if(loop > fr.numResource){
                        loop = 0;
                        isAdd = true;
                    }
                    if(isAdd){
                        int taskAdd = -1;
                        int diff = Integer.MAX_VALUE;
                        int timeStart = 0;
                        int timeAfter = 0;
                        int numAfter = 0;
                        for(int j = 0;j < listWork.get(i).size();j++){
                            int task = listWork.get(i).get(j);
                            if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                if (numAfter > fullRelation.get(task).get(0)) {
                                    numAfter = fullRelation.get(task).get(0);
                                    taskAdd = task;
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0){
                                        timeBefore = timeResource.get(i);
                                    }
                                    timeStart = Integer.max(timeBefore, timeResource.get(i));
                                    if(fr.start.get(task) > 0){
                                        timeStart = Integer.max(timeStart, fr.start.get(task));
                                    }
                                    if(fr.end.get(task) > 0){
                                        if(timeStart > fr.end.get(task) - fr.duration.get(task))
                                            return new PointSolution(Integer.MAX_VALUE, timeTaskDone);
                                    }
                                    timeAfter = timeStart;
                                }
                            }
                        }
                        if(numAfter < 0){
                            for(int j = 0;j < listWork.get(i).size();j++){
                                int task = listWork.get(i).get(j);
                                if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0){
                                        timeBefore = timeResource.get(i);
                                    }
                                    if(timeBefore + fr.duration.get(task) > timeAfter && fr.end.get(task) < 0)
                                        continue;
                                    int timeDiff = timeAfter - timeBefore - fr.duration.get(task);
                                    if(timeDiff < diff || (fr.end.get(taskAdd) < 0 && fr.end.get(task) > 0) ||
                                            (fr.end.get(task) > 0 && (fr.end.get(taskAdd) - fr.duration.get(taskAdd) > fr.end.get(task) - fr.duration.get(task)))){
                                        diff = timeDiff;
                                        taskAdd = task;
                                        timeStart = Integer.max(timeBefore, timeResource.get(i));
                                        if(fr.start.get(task) > 0){
                                            timeStart = Integer.max(timeStart, fr.start.get(task));
                                        }
                                        if(fr.end.get(task) > 0){
                                            if(timeStart > fr.end.get(task) - fr.duration.get(task))
                                                return new PointSolution(Integer.MAX_VALUE, timeTaskDone);
                                        }
                                    }
                                }
                            }
                        }
                        else{
                            for(int j = 0;j < listWork.get(i).size();j++){
                                int task = listWork.get(i).get(j);
                                if(relation.get(task).stream().mapToInt(Integer::intValue).sum() == 0){
                                    int timeBefore = Collections.max(listTimeBefore.get(task));
                                    if(timeBefore == 0 || timeBefore < timeResource.get(i)){
                                        timeBefore = timeResource.get(i);
                                    }
                                    if(fr.start.get(j) >= 0){
                                        timeBefore = Integer.max(timeBefore, fr.start.get(j));
                                    }
                                    int timeDiff = Math.abs(timeBefore - timeResource.get(i));
                                    Boolean isChange = false;
                                    if(taskAdd != -1){
                                        if(fr.end.get(taskAdd) < 0 && fr.end.get(task) > 0)
                                            isChange = true;
                                        else if(fr.end.get(taskAdd) > 0 && fr.end.get(task) > 0){
                                            if(fr.end.get(taskAdd) - fr.duration.get(taskAdd) > fr.end.get(task) - fr.duration.get(task)){
                                                isChange = true;
                                            }
                                        }
                                    }
                                    if(timeDiff < diff || isChange){
                                        diff = timeDiff;
                                        taskAdd = task;
                                        timeStart = Integer.max(timeBefore, timeResource.get(i));
                                        if(fr.start.get(taskAdd) >= 0){
                                            timeStart = Integer.max(timeStart, fr.start.get(taskAdd));
                                        }
                                        if(fr.end.get(task) > 0){
                                            if(timeStart > fr.end.get(task) - fr.duration.get(task))
                                                return new PointSolution(Integer.MAX_VALUE, timeTaskDone);
                                        }
                                    }
                                    else if(timeDiff == diff){
                                        if(fr.duration.get(task) < fr.duration.get(taskAdd)){
                                            taskAdd = task;
                                            timeStart = Integer.max(timeBefore, timeResource.get(i));
                                            if(fr.start.get(taskAdd) >= 0){
                                                timeStart = Integer.max(timeStart, fr.start.get(taskAdd));
                                            }
                                            if(fr.end.get(task) > 0){
                                                if(timeStart > fr.end.get(task) - fr.duration.get(task))
                                                    return new PointSolution(Integer.MAX_VALUE, timeTaskDone);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(taskAdd != -1){
                            doneTask += 1;
                            listWork.get(i).remove((Integer) taskAdd);
//                            for(int k = 0;k < listWork.get(i).size();k++){
//                                if(listWork.get(i).get(k) == taskAdd){
//                                    listWork.get(i).remove(k);
//                                    break;
//                                }
//                            }

                            timeResource.set(i, timeStart + fr.duration.get(taskAdd));
                            timeTaskDone[taskAdd] = timeStart + fr.duration.get(taskAdd);
                            for(int m = 0;m < fr.numTask;m++){
                                if(relation.get(m).get(taskAdd) == 1){
                                    listTimeBefore.get(m).set(taskAdd, timeTaskDone[taskAdd]);
                                    relation.get(m).set(taskAdd, 0);
                                }
                            }
                        }
                    }
                }
                else
                    listResourceDone[i] = 0;
            }
        }
        int fitness = Collections.max(timeResource);
        return new PointSolution(fitness, timeTaskDone);
    }
    public List<Integer> Crossover(List<Integer> father, List<Integer> mother){
        List<Integer> child = new ArrayList<>();
        int[] timeWork = new int[fr.numResource];
        Arrays.fill(timeWork, 0);
        for(int i = 0;i < fr.numTask;i++){
            if(father.get(i) == mother.get(i))
                child.add(father.get(i));
            else{
                if(timeWork[father.get(i)] < timeWork[mother.get(i)])
                    child.add(father.get(i));
                else if(timeWork[father.get(i)] > timeWork[mother.get(i)])
                    child.add(mother.get(i));
                else{
                    if(rd.nextBoolean())
                        child.add(father.get(i));
                    else
                        child.add(mother.get(i));
                }
            }
            timeWork[child.get(child.size()-1)] += fr.duration.get(i);
        }
        if(rd.nextInt(100) < 10){
            int mutation_task = rd.nextInt(fr.numTask);
            child.set(mutation_task, fr.K.get(mutation_task).get(rd.nextInt(fr.K.get(mutation_task).size())));
        }
        while(father.equals(child) || mother.equals(child)){
            int mutation_task = rd.nextInt(fr.numTask);
            child.set(mutation_task, fr.K.get(mutation_task).get(rd.nextInt(fr.K.get(mutation_task).size())));
        }

        return child;
    }

    public List<Integer> CrossoverLoop(List<Integer> father, List<Integer> mother){
        List<Integer> child = new ArrayList<>();
        for(int i = 0;i < fr.numTask;i++){
            if(rd.nextBoolean())
                child.add(father.get(i));
            else
                child.add(mother.get(i));
        }
        if(rd.nextInt(100) < 10){
            int mutation_task = rd.nextInt(fr.numTask);
            child.set(mutation_task, fr.K.get(mutation_task).get(rd.nextInt(fr.K.get(mutation_task).size())));
        }
        while(father.equals(child) || mother.equals(child)){
            int mutation_task = rd.nextInt(fr.numTask);
            child.set(mutation_task, fr.K.get(mutation_task).get(rd.nextInt(fr.K.get(mutation_task).size())));
        }

        return child;
    }

    public void Solver(int numPopulation, int numChild, int loop){
        timeStart = System.currentTimeMillis();
        List<Individual> populations = new ArrayList<>();
        for(int i = 0;i < numPopulation;i++){
            List<Integer> individual = new ArrayList<>();
            for(int j = 0;j < fr.numTask;j++){
                individual.add(fr.K.get(j).get(rd.nextInt(fr.K.get(j).size())));
            }

            int fitness = CostSolutionTime(individual).fitness;
            Individual child = new Individual(fitness, individual);
            populations.add(child);
        }
        Collections.sort(populations, Comparator.comparingInt(ind -> ind.fitness));
        int bestTime = populations.get(0).fitness;
        System.out.println("Best Time: " + populations.get(0).fitness + " " + populations.get(0).resolver);


        int limitLoop = 0;
        for(int iLoop = 0;iLoop < loop;iLoop++){
            List<Individual> populationChild = new ArrayList<>();
            for(int i = 0;i < numChild;i++){
                int iFather = 0;
                int iMother = 0;
                while (iFather == iMother){
                    iFather = rd.nextInt(numPopulation);
                    iMother = rd.nextInt(numPopulation);
                }
                List<Integer> individual = CrossoverLoop(populations.get(iFather).resolver, populations.get(iMother).resolver);
                int fitness = CostSolutionTime(individual).fitness;
                Individual individualChild = new Individual(fitness, individual);
                Boolean isAdd = true;
                for(int j = 0;j < populationChild.size();j++){
                    if(populationChild.get(j).equals(individualChild)){
                        isAdd = false;
                        break;
                    }
                }
                if(isAdd)
                    populationChild.add(individualChild);
            }

            int readChild = populationChild.size();
            for(int j = 0;j < readChild;j++){
                populations.set(numPopulation - readChild + j, populationChild.get(j));
            }
            Collections.sort(populations, Comparator.comparingInt(ind -> ind.fitness));
            if(bestTime > populations.get(0).fitness){
                bestTime = populations.get(0).fitness;
                limitLoop = 0;
                System.out.println("New Best Time: " + bestTime + " " + populations.get(0).resolver);
            }
            else
                limitLoop += 1;
            if(limitLoop > 2000){
                System.out.println("Break at loop: " + iLoop);
                System.out.println("New Best Time: " + populations.get(0).fitness + " " + populations.get(0).resolver);
                bestSorce = populations.get(0).fitness;
                breakLoop = iLoop;
                break;
            }
        }
        timeEnd = System.currentTimeMillis();
    }
}
