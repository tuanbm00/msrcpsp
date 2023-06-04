package org.example;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.io.IOException;

public class MIPSolverTAP {
    public  MPVariable[][] c;
    public  MPVariable[] C;
    public  MPVariable[] T;
    public  MPVariable timePS;
    public MPSolver.ResultStatus result;
    public MPObjective objective;
    public long timeStart;
    public long timeEnd;
    private FileReaderTAP fr;
    public  MIPSolverTAP (String filename) throws IOException {
        fr = new FileReaderTAP(filename);
    }

    public  void Solver(float limitTime, Boolean constraintLimitTime){
        timeStart = System.currentTimeMillis();
        Loader.loadNativeLibraries();
        double infinity = java.lang.Double.POSITIVE_INFINITY;

        MPSolver solver = MPSolver.createSolver("SAT");

        c = new MPVariable[fr.numTask][fr.numResource];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numResource;j++){
                c[i][j] = solver.makeNumVar(0,1, "c1(" + i + "," + j + ")");
            }
        }

        C = new MPVariable[fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            C[i] = solver.makeNumVar(0, fr.numResource, "C2(" + i + ")");
        }

        MPVariable[][] t = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                t[i][j] = solver.makeNumVar(0,fr.maxTime, "t1(" + i + "," + j + ")");
            }
        }

        T = new MPVariable[fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            T[i] = solver.makeNumVar(0, fr.maxTime, "T2(" + i + ")");
        }

        MPVariable[][] ordinal = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                ordinal[i][j] = solver.makeNumVar(0,1, "ordinal(" + i + "," + j + ")");
            }
        }

        MPVariable[][] together = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                together[i][j] = solver.makeNumVar(0,fr.numResource, "together(" + i + "," + j + ")");
            }
        }

        MPVariable[][] maxResource = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                maxResource[i][j] = solver.makeNumVar(0,fr.numResource, "maxResource(" + i + "," + j + ")");
            }
        }

        MPVariable[][] minResource = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                minResource[i][j] = solver.makeNumVar(0,fr.numResource, "minResource(" + i + "," + j + ")");
            }
        }

        MPVariable[][] iMax = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                iMax[i][j] = solver.makeNumVar(0,1, "iMax(" + i + "," + j + ")");
            }
        }

        MPVariable[][] iMin = new MPVariable[fr.numTask][fr.numTask];
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numTask;j++){
                iMin[i][j] = solver.makeNumVar(0,1, "iMin(" + i + "," + j + ")");
            }
        }

        timePS = solver.makeNumVar(0, fr.maxTime, "time");
        MPVariable salaryPS = solver.makeNumVar(fr.cMin, fr.cMax, "salary");

        System.out.println("Number of variables = " + solver.numVariables());

        // cong doan chi duoc la tren mot may -> tong = 1
        for(int i = 0;i < fr.numTask;i++){
            MPConstraint ct = solver.makeConstraint(1, 1);
            for(int j = 0;j < fr.numResource;j++){
                ct.setCoefficient(c[i][j], 1);
            }
        }

        // cong doan chi duoc lam tren may co the
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numResource;j++){
                MPConstraint ct = solver.makeConstraint(0, fr.KJ[i][j]);
                ct.setCoefficient(c[i][j], 1);
            }
        }

        // tinh chi phi cua cac may
        MPConstraint ctrSum = solver.makeConstraint(0,0);
        ctrSum.setCoefficient(salaryPS, -1);
        for(int i = 0;i < fr.numTask;i++){
            for(int j = 0;j < fr.numResource;j++){
                ctrSum.setCoefficient(c[i][j], fr.listSalary.get(j) * fr.duration.get(i));
            }
        }

        // tinh C[i] - may thuc hien cong doan i
        for(int i = 0;i < fr.numTask;i++){
            MPConstraint ct = solver.makeConstraint(0, 0);
            ct.setCoefficient(C[i], -1);
            for(int j = 0;j < fr.numResource;j++){
                ct.setCoefficient(c[i][j], j);
            }
        }

        // giới hạn thời gian của máy:
        if(constraintLimitTime) {
            for (int i = 0; i < fr.numTask; i++) {
                if (fr.start.get(i) != -1) {
                    MPConstraint ct = solver.makeConstraint(fr.start.get(i), fr.maxTime);
                    ct.setCoefficient(T[i], 1);
                }
                if (fr.end.get(i) != -1) {
                    MPConstraint ct = solver.makeConstraint(0, fr.end.get(i) - fr.duration.get(i));
                    ct.setCoefficient(T[i], 1);
                }
            }
        }

        // cong doan con thuc hien sau cong doan cha
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                if(fr.relation[i][j] == 1){
                    MPConstraint ct = solver.makeConstraint(-infinity, -fr.duration.get(i));
                    ct.setCoefficient(T[i], 1);
                    ct.setCoefficient(T[j], -1);
                }
                if(fr.relation[i][j] == -1){
                    MPConstraint ct = solver.makeConstraint(fr.duration.get(j), infinity);
                    ct.setCoefficient(T[i], 1);
                    ct.setCoefficient(T[j], -1);
                }
            }
        }

        // hai cong doan thuc hien tren cung 1 may thi cong doan sau phai thuc hien sau khi cong doan truoc ket thuc

        // min(C[i], C[j])
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                MPConstraint ct1 = solver.makeConstraint(0, infinity);
                ct1.setCoefficient(C[i], 1);
                ct1.setCoefficient(minResource[i][j], -1);

                MPConstraint ct2 = solver.makeConstraint(0, infinity);
                ct2.setCoefficient(C[j], 1);
                ct2.setCoefficient(minResource[i][j], -1);

                MPConstraint ct3 = solver.makeConstraint(-fr.M, infinity);
                ct3.setCoefficient(minResource[i][j], 1);
                ct3.setCoefficient(C[i], -1);
                ct3.setCoefficient(iMin[i][j], -fr.M);

                MPConstraint ct4 = solver.makeConstraint(0, infinity);
                ct4.setCoefficient(minResource[i][j], 1);
                ct4.setCoefficient(C[j], -1);
                ct4.setCoefficient(iMin[i][j], fr.M);
            }
        }

        // max(C[i] - C[j], C[j] - C[i])
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                MPConstraint ct1 = solver.makeConstraint(-infinity, 0);
                ct1.setCoefficient(C[i], 1);
                ct1.setCoefficient(maxResource[i][j], -1);

                MPConstraint ct2 = solver.makeConstraint(-infinity, 0);
                ct2.setCoefficient(C[j], 1);
                ct2.setCoefficient(maxResource[i][j], -1);

                MPConstraint ct3 = solver.makeConstraint(-infinity, fr.M);
                ct3.setCoefficient(maxResource[i][j], 1);
                ct3.setCoefficient(C[i], -1);
                ct3.setCoefficient(iMax[i][j], fr.M);

                MPConstraint ct4 = solver.makeConstraint(-infinity, 0);
                ct4.setCoefficient(maxResource[i][j], 1);
                ct4.setCoefficient(C[j], -1);
                ct4.setCoefficient(iMax[i][j], -fr.M);
            }
        }

        // together Max - Min
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                MPConstraint ct = solver.makeConstraint(0, 0);
                ct.setCoefficient(together[i][j], 1);
                ct.setCoefficient(maxResource[i][j], -1);
                ct.setCoefficient(minResource[i][j], 1);
            }
        }

        // xem thu tu may nao thuc hien truoc
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                MPConstraint ct1 = solver.makeConstraint(0, infinity);
                ct1.setCoefficient(T[i], 1);
                ct1.setCoefficient(t[i][j], -1);

                MPConstraint ct2 = solver.makeConstraint(0, infinity);
                ct2.setCoefficient(T[j], 1);
                ct2.setCoefficient(t[i][j], -1);

                MPConstraint ct3 = solver.makeConstraint(-fr.M, infinity);
                ct3.setCoefficient(t[i][j], 1);
                ct3.setCoefficient(T[i], -1);
                ct3.setCoefficient(ordinal[i][j], -fr.M);

                MPConstraint ct4 = solver.makeConstraint(0, infinity);
                ct4.setCoefficient(t[i][j], 1);
                ct4.setCoefficient(T[j], -1);
                ct4.setCoefficient(ordinal[i][j], fr.M);
            }
        }

        //final hai cong doan cung lam 1 may thi cong doan sau bat dau sau khi cong doan truoc hoan thien
        for(int i = 0;i < fr.numTask;i++){
            for(int j = i+1;j < fr.numTask;j++){
                MPConstraint ct1 = solver.makeConstraint(fr.duration.get(j), infinity);
                ct1.setCoefficient(T[i], 1);
                ct1.setCoefficient(T[j], -1);
                ct1.setCoefficient(ordinal[i][j], fr.M);
                ct1.setCoefficient(together[i][j], fr.M);

                MPConstraint ct2 = solver.makeConstraint(fr.duration.get(i) - fr.M, infinity);
                ct2.setCoefficient(T[j], 1);
                ct2.setCoefficient(T[i], -1);
                ct2.setCoefficient(ordinal[i][j], -fr.M);
                ct2.setCoefficient(together[i][j], fr.M);
            }
        }

        for(int i = 0;i <fr.numTask;i++){
            MPConstraint ct = solver.makeConstraint(fr.duration.get(i), infinity);
            ct.setCoefficient(timePS, 1);
            ct.setCoefficient(T[i], -1);
        }

        System.out.println("Number of constraints = " + solver.numConstraints());

        objective = solver.objective();
        objective.setCoefficient(timePS, 1);
        objective.setMinimization();
        solver.setTimeLimit((long) (limitTime * 1000));
        result = solver.solve();
        timeEnd = System.currentTimeMillis();
    }
}
