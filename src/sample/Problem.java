package sample;

/**
 * Created by administrator on 22/04/16.
 */
public class Problem {
    public String problemid;
    public String masterproblemid;

    public Problem(String id, String name){
        this.problemid = problemid;
        this.masterproblemid = masterproblemid;
    }
    public String toStringProblem(){
        return this.problemid;
    }
    public String toStringMasterProblem(){
        return this.masterproblemid;
    }
}
