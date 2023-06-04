package org.example;
public class Skill {
    public int type;
    public int level;

    public Skill(int type, int level){
        this.type = type;
        this.level = level;
    }

    public String ToString(){
        return "[" + type + ", " + level + "]";
    }
}
