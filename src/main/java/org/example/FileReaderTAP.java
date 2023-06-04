package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileReaderTAP {
    public int numTask, numResource, numSkill;
    public List<Integer> listSalary = new ArrayList<>(); // danh sach chi phi
    public List<List<Skill>> listSkill = new ArrayList<>(); // danh sach cac ki nang cua tung may
    public List<Integer> duration = new ArrayList<>(); // danh sach chi phi thoi gian
    public int [][] relation, fullRelation, relationGA, KJ; // quan he giua cac nhiem vu voi nhau va kha nang thuc nhiem giua may va nhiem vu

    public List<List<Integer>> K = new ArrayList<>();
    public List<Integer> start = new ArrayList<>();
    public List<Integer> end = new ArrayList<>();

    public int maxTime, cMax, cMin, M;
    public FileReaderTAP(String filename) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(fr);
        maxTime = 0;
        M = 999999;
        try {
            String s;
            s = br.readLine();
            numTask = Integer.parseInt(s.substring(13));

            s = br.readLine();
            numResource = Integer.parseInt(s.substring(17));

            s = br.readLine();
            numSkill = Integer.parseInt(s.substring(14));

            br.readLine();
            br.readLine();
            br.readLine();

            for (int i = 0;i < numResource;i++) {
                s = br.readLine();
                String[] arr = s.split("\t");
                List<String> arrString = new ArrayList<>();
                for (String subS : arr) {
                    arrString.addAll(Arrays.asList(subS.split(" ")));
                }
                while(arrString.contains("")){
                    arrString.remove("");
                }
                arrString.remove(0);
                listSalary.add((int)Double.parseDouble(arrString.get(0)));
                arrString.remove(0);
                List<Skill> skillResource = new ArrayList<>();
                int type, level;
                for(int j = 0;j < arrString.size() / 2;j++){
                    type = Integer.parseInt(arrString.get(2*j).substring(1, arrString.get(2*j).length()-1));
                    level = Integer.parseInt(arrString.get(2*j + 1));
                    for(int k = 0;k < level + 1;k++){
                        skillResource.add(new Skill(type, k));
                    }
                }
                listSkill.add(skillResource);
            }

            br.readLine();
            br.readLine();

            relation = new int[numTask][numTask];
            fullRelation = new int[numTask][numTask];
            relationGA = new int[numTask][numTask];
            KJ = new int[numTask][numResource];

            for(int i = 0;i < numTask;i++){
                Arrays.fill(relation[i], 0);
                Arrays.fill(fullRelation[i], 0);
                Arrays.fill(relationGA[i], 0);
                Arrays.fill(KJ[i], 0);
                List<Integer> resourceCanDo = new ArrayList<>();
                K.add(resourceCanDo);
            }

            for(int i = 0;i < numTask;i++){
                s = br.readLine();
                String[] line = s.split("\t");
                List<String> arrayString2 = new ArrayList<>();
                for (String subS : line) {
                    arrayString2.addAll(Arrays.asList(subS.split(" ")));
                }
                while(arrayString2.contains("")){
                    arrayString2.remove("");
                }
                arrayString2.remove(0);
                duration.add(Integer.parseInt(arrayString2.get(0)));
                maxTime += Integer.parseInt(arrayString2.get(0));
                arrayString2.remove(0);
                int skillType = Integer.parseInt(arrayString2.get(0).substring(1, arrayString2.get(0).length() - 1));
                arrayString2.remove(0);
                int skillPro = Integer.parseInt(arrayString2.get(0));
                arrayString2.remove(0);
                start.add(Integer.parseInt(arrayString2.get(0)));
                arrayString2.remove(0);
                end.add(Integer.parseInt(arrayString2.get(0)));
                arrayString2.remove(0);
                for(int j = 0;j < numResource;j++){
//                    var skill = new Skill(skillType, skillPro);
                    if (listSkill.get(j).stream().anyMatch(skill -> (skill.type == skillType && skill.level == skillPro))) {
                        KJ[i][j] = 1;
                        K.get(i).add(j);
                    }
                }

                for (String value : arrayString2) {
                    relationGA[i][Integer.parseInt(value) - 1] = 1;
                    relation[i][Integer.parseInt(value) - 1] = -1;
                    relation[Integer.parseInt(value) - 1][i] = 1;
                    fullRelation[i][Integer.parseInt(value) - 1] = -1;
                    fullRelation[Integer.parseInt(value) - 1][i] = 1;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            br.close();
            fr.close();
            for(int i = 0;i < numTask;i++){
                for(int j = 0;j < numTask;j++){
                    if(fullRelation[i][j] == 1)
                        FillAllRelation(i, j, 1);
                    if(fullRelation[i][j] == -1)
                        FillAllRelation(i, j, -1);
                }
            }
            System.out.println("Input Success!");
        }

        cMin = Collections.min(listSalary) * maxTime;
        cMax = Collections.max(listSalary) * maxTime;
    }

    private void FillAllRelation(int x, int y, int value){
        for(int i = 0;i < numTask;i++){
            if(fullRelation[y][i] == value) {
                fullRelation[x][i] = value;
                FillAllRelation(x, i, value);
                FillAllRelation(y, i, value);
            }
        }
    }
}
