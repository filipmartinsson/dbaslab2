package sample;

/**
 * Created by administrator on 22/04/16.
 */
public class Problem {
    public int uId;
    public int problemid;
    public int masterproblemid;
    public String courseId;

    public Problem(int uid, int problemid, int masterproblemid, String courseId){
        this.uId = uid;
        this.problemid = problemid;
        this.masterproblemid = masterproblemid;
        this.courseId = courseId;
    }
    public int getProblemId(){
        return this.problemid;
    }
    public String getCourseId(){return this.courseId;}
    public int getUId(){return this.uId;}
    public int getMasterProblem(){
        return this.masterproblemid;
    }
}
