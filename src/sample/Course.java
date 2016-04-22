package sample;

/**
 * Created by admin on 2016-04-22.
 */
public class Course {

    public String name;
    public String id;

    public Course(String id, String name){
        this.id = id;
        this.name = name;
    }
    public String toString(){
        return this.name;
    }
}

