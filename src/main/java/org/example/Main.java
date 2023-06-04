package org.example;

import com.google.ortools.linearsolver.MPSolver;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {

//        listdata = ["10_3_5_3", "10_5_8_5", "10_7_10_7", "15_3_5_3", "15_6_10_6", "15_9_12_9", "100_5_22_15",
//                "100_5_46_15", "100_5_48_9", "100_5_64_9", "100_5_64_15", "100_10_26_15", "100_10_47_9",
//                "100_10_48_15", "100_10_64_9", "100_10_65_15", "100_20_22_15", "100_20_46_15", "100_20_47_9",
//                "100_20_65_9", "100_20_65_15", "200_10_50_9", "200_10_50_15", "200_10_84_9", "200_10_85_15",
//                "200_10_128_15", "200_20_54_15", "200_20_55_9", "200_20_97_9", "200_20_97_15", "200_20_145_15",
//                "200_40_45_9", "200_40_45_15", "200_40_90_9", "200_40_91_15", "200_40_133_15"]
        String[] datas = new String[] {"200_40_45_9"};
//                "100_10_48_15", "100_10_64_9", "100_10_65_15", "100_20_22_15", "100_20_46_15", "100_20_47_9",
//                "100_20_65_9", "100_20_65_15", "200_10_50_9", "200_10_50_15", "200_10_84_9", "200_10_85_15",
//                "200_10_128_15", "200_20_54_15", "200_20_55_9", "200_20_97_9", "200_20_97_15", "200_20_145_15",
//                "200_40_45_9", "200_40_45_15", "200_40_90_9", "200_40_91_15", "200_40_133_15"};
//        for (String file: datas) {
//            String filename = "src/main/data/input/" + file + ".def";
//            MIPSolverTAP solverTAP = new MIPSolverTAP(filename);
//            solverTAP.Solver(1800f, true);
//
//            String fileWrite = "src/main/data/output/" + file + ".def";
//            FileWriter fr = new FileWriter(fileWrite);
//            BufferedWriter br = new BufferedWriter(fr);
//            if(solverTAP.result == MPSolver.ResultStatus.OPTIMAL){
//                System.out.println("Solution:");
//                System.out.println("Objective value = " + solverTAP.objective.value());
//                System.out.println("time = " + solverTAP.timePS.solutionValue());
//            }
//            else {
//                System.out.println("Cant Optimal");
//                System.out.println("Solution:");
//                System.out.println("Objective value = " + solverTAP.objective.value());
//                System.out.println("time = " + solverTAP.timePS.solutionValue());
//            }
//            System.out.println("Time solver MIP: " + (solverTAP.timeEnd - solverTAP.timeStart));
//            br.write("MIP: " + solverTAP.timePS.solutionValue() + " - time solver: " + (solverTAP.timeEnd - solverTAP.timeStart) + " - can optimal: " + (solverTAP.result == MPSolver.ResultStatus.OPTIMAL));
//            br.newLine();
//            HeristicVer1 solver = new HeristicVer1(filename);
//            solver.CreateSolve();
//
//            solver.Solve(10000);
//            System.out.println(solver.solver);
//            HeuristicPoint point = solver.Evaluate(solver.solver);
//            System.out.println(point.timeSolver);
//            System.out.println("Best conflict: " + point.conflict + ", Best condition: " + point.condition + ", Best score: " + point.score);
//            System.out.println("Time solver Heuristic: " + (solver.timeEnd - solver.timeStart));
//            br.write("Heuristic: " + point.score + " - time solver: " + (solver.timeEnd - solver.timeStart));
//            br.newLine();
//
//            GeneticAlgorithm solverGA = new GeneticAlgorithm(filename);
//            solverGA.Solver(70, 30, 10000);
//            System.out.println(solverGA.bestSorce);
//            System.out.println("Time solver GA: " + (solverGA.timeEnd - solverGA.timeStart));
//            br.write("Heuristic: " + solverGA.bestSorce + " - time solver: " + (solverGA.timeEnd - solverGA.timeStart) + " - break loop: " + solverGA.breakLoop);
//            br.newLine();
//
//            br.close();
//            fr.close();
//        }
        for (String file: datas){
            String fileWriteGA = "src/main/data/output/Heuristic/" + file + "_GA.def";
            FileWriter frGA = new FileWriter(fileWriteGA);
            BufferedWriter brGA = new BufferedWriter(frGA);

            String fileWriteGATime = "src/main/data/output/Heuristic/" + file + "_GATime.def";
            FileWriter frGATime = new FileWriter(fileWriteGATime);
            BufferedWriter brGATime = new BufferedWriter(frGATime);

            String fileWriteHeur = "src/main/data/output/Heuristic/" + file + "_Heur.def";
            FileWriter frHeur = new FileWriter(fileWriteHeur);
            BufferedWriter brHeur = new BufferedWriter(frHeur);

            String fileWriteHeurTime = "src/main/data/output/Heuristic/" + file + "_HeurTime.def";
            FileWriter frHeurTime = new FileWriter(fileWriteHeurTime);
            BufferedWriter brHeurTime = new BufferedWriter(frHeurTime);
            for (int i = 0;i < 30;i++) {
                System.out.println("Loop at " + i);
                String filename = "src/main/data/input/" + file + ".def";
                HeristicVer1 solver = new HeristicVer1(filename);
                solver.CreateSolve();

                solver.Solve(10000);
                System.out.println(solver.solver);
                HeuristicPoint point = solver.Evaluate(solver.solver);
                System.out.println(point.timeSolver);
                System.out.println("Best conflict: " + point.conflict + ", Best condition: " + point.condition + ", Best score: " + point.score);
                System.out.println("Time solver Heuristic: " + (solver.timeEnd - solver.timeStart));
                brHeur.write(point.score + "");
                brHeur.newLine();
                brHeurTime.write((solver.timeEnd - solver.timeStart) + "");
                brHeurTime.newLine();

                GeneticAlgorithm solverGA = new GeneticAlgorithm(filename);
                solverGA.Solver(100, 30, 10000);
                System.out.println(solverGA.bestSorce);
                System.out.println("Time solver GA: " + (solverGA.timeEnd - solverGA.timeStart));
                brGA.write(solverGA.bestSorce + "");
                brGA.newLine();
                brGATime.write((solverGA.timeEnd - solverGA.timeStart) + "");
                brGATime.newLine();
            }
            brGA.close();
            frGA.close();
            brGATime.close();
            frGATime.close();
            brHeur.close();
            frHeur.close();
            brHeurTime.close();
            frHeurTime.close();
        }
    }
}