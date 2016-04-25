package sample;

/**
 * Created by administrator on 22/04/16.
 */
public class Problem implements Comparable<Problem> {
    public int uId;
    public int rid;
    public int problemid;
    public int masterproblemid;
    public String courseId;

    public Problem(int uid, int problemid, int masterproblemid, String courseId, int rid){
        this.uId = uid;
        this.problemid = problemid;
        this.masterproblemid = masterproblemid;
        this.courseId = courseId;
    }
    public int getProblemId(){
        return this.problemid;
    }
    public String getCourseId(){return this.courseId;}
    public int getRecitationId(){
        return this.rid;
    }
    public int getUId(){return this.uId;}
    public int getMasterProblem(){
        return this.masterproblemid;
    }
    public String toString(){
        if(masterproblemid != -1){
            return String.valueOf(problemid);
        }
        else{
            return "        " + String.valueOf(problemid);
        }

    }
    @Override
    public int compareTo(Problem otherproblem) {
        if(problemid < otherproblem.problemid) {
            return -1;
        }else if (uId==otherproblem.uId){
            return 0;
        }
        return 1;
    }
}

//TODO checkbox lista
// unika värden i problem
//compare
