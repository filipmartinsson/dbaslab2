package sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Controller {

    @FXML
    Button idButton;
    @FXML
    TextField idField;
    @FXML
    ChoiceBox courseBox;
    @FXML
    ChoiceBox groupBox;
    @FXML
    ChoiceBox recitationBox;
    @FXML
    ListView<Problem> problemView;
    @FXML
    Button submitButton;

    private ObservableList<Course> observableCourses = FXCollections.observableArrayList();
    private ObservableList<Integer> observableGroups = FXCollections.observableArrayList();
    private ObservableList<Integer> observableRecitations = FXCollections.observableArrayList();
    private ObservableList<Problem> problems = FXCollections.observableArrayList();
    private ArrayList<Problem> solvedProblems = new ArrayList<Problem>();
    private ArrayList<Integer> alreadySolvedProblemUIds = new ArrayList<>();

    private int studentid;
    private int recitationid;
    private int groupid;
    private boolean shouldTrackBeWritten;

    @FXML
    public void initialize() {


        problemView.setCellFactory(CheckBoxListCell.forListView(new Callback<Problem, ObservableValue<Boolean>>() {
            @Override
            public ObservableValue<Boolean> call(Problem item) {
                BooleanProperty observable = new SimpleBooleanProperty();
                observable.addListener((obs, wasSelected, isNowSelected) -> problemChecked(item, wasSelected, isNowSelected)
                        //System.out.println("Check box for "+item.toString()+" changed from "+wasSelected+" to "+isNowSelected)
                );
                return observable ;
            }
        }));



        Connection conn = null;
        courseBox.setDisable(true);
        courseBox.setItems(observableCourses);

        problemView.setDisable(true);

        groupBox.setDisable(true);
        groupBox.setItems(observableGroups);

        recitationBox.setDisable(true);
        recitationBox.setItems(observableRecitations);

        groupBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        groupid = (Integer) new_value+1;
                        groupBox.setDisable(true);
                        problemView.setDisable(false);
                        solvedProblems.clear();
                    }
                });

        recitationBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        problems.clear();
                        problemView.setDisable(true);

                    }
                });
        courseBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        if (observableCourses.size() > 0){
                            getRecitations(observableCourses.get((Integer)new_value));

                            problemView.setDisable(true);

                        }
                    }
                });

        recitationBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        if(observableRecitations.size() > 0){
                            String cid = ((Course)courseBox.getSelectionModel().getSelectedItem()).id;
                            int recid = observableRecitations.get((Integer)new_value);
                            System.out.println(recid);
                            try{

                                getGroups((Course)courseBox.getSelectionModel().getSelectedItem(), recid, studentid);
                                courseBox.setDisable(false);
                                getProblems(recid, cid);


                            }
                            catch (Exception e){
                                e.printStackTrace();
                                System.exit(2);
                            }
                        }


                    }
                });

        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("SUBMIT, BITCH");
                try{
                    Connection conn = null;
                    String url = "jdbc:postgresql://localhost:5432/lab2";

                    // FINDS THE ALREADY SOLVED PROBLEMS FOR THE STUDENT AND PUTS THEM IN ALREADYSOLVEDPROBLEMIDS ARRAYLIST
                    conn = DriverManager.getConnection(url,"postgres", "password");
                    PreparedStatement st = conn.prepareStatement("SELECT problemid FROM solved WHERE studentid = ?");
                    st.setInt(1, studentid);
                    ResultSet rs = st.executeQuery();
                    while(rs.next()){
                        alreadySolvedProblemUIds.add(rs.getInt("problemid"));
                    }

                    if(shouldTrackBeWritten) {
                        // get the postgresql database connection
                        PreparedStatement st1 = conn.prepareStatement("INSERT INTO tracks VALUES(? , ?, ?)");

                        st1.setInt(1, studentid);
                        st1.setInt(2, recitationid);
                        st1.setInt(3, groupid);

                        st1.executeUpdate();
                        System.out.println(st1.getMetaData());
                        st1.close();
                    }

                    //Loop with problems over SolvedProblems
                    for (int i = 0; i < solvedProblems.size(); i++) {
                        int probid = solvedProblems.get(i).getProblemId();
                        int unid = solvedProblems.get(i).getUId();

                        try{
                            if(!alreadySolvedProblemUIds.contains(unid)){ //If the problem is not already solved. Otherwise we will have database primary key not unique error

                                conn = DriverManager.getConnection(url,"postgres", "password");

                                PreparedStatement st2 = conn.prepareStatement("INSERT INTO solved VALUES(?, ?)");
                                st2.setInt(1, unid); //unid or probid???
                                st2.setInt(2, studentid);

                                st2.executeUpdate();
                                //ignore results...
                                st2.close();
                            }

                        }
                        catch (Exception e){
                            e.printStackTrace();
                            System.exit(2);
                        }
                    }

                }
                catch (Exception e){
                    e.printStackTrace();
                    System.exit(2);
                }





            }
        });



        try
        {
            // the postgresql driver string
            Class.forName("org.postgresql.Driver");

            // the postgresql url
            String url = "jdbc:postgresql://localhost:5432/lab2";

            // get the postgresql database connection
            conn = DriverManager.getConnection(url,"postgres", "password");

            System.out.println("success");

            idButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    studentid=Integer.parseInt(idField.getText());
                    getUser(idField.getText());

                }
            });


        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.exit(2);
        }
    }

    public void problemChecked(Problem p, boolean wasSelected, boolean isSelected){
        if(isSelected){
            solvedProblems.add(p);
        }
        else if(wasSelected && !isSelected){
            solvedProblems.remove(p);
        }
        System.out.println(solvedProblems.size());
    }

    public void getUser(String id){
        try{
            observableGroups.clear();
            observableRecitations.clear();
            problems.clear();

            observableCourses.clear();
            groupBox.setDisable(true);
            recitationBox.setDisable(true);
            courseBox.setDisable(true);
            Connection conn = null;
            String url = "jdbc:postgresql://localhost:5432/lab2";

            // get the postgresql database connection
            conn = DriverManager.getConnection(url,"postgres", "password");
            PreparedStatement st = conn.prepareStatement("SELECT DISTINCT courses.name, courses.courseid FROM students, courses, takes WHERE takes.studentid = ? AND courses.courseid = takes.courseid");
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            courseBox.setDisable(false);
            while ( rs.next() )
            {
                observableCourses.add(new Course(rs.getString("courseid"),rs.getString("name")));
                System.out.println(rs.getString("name"));
            }
            rs.close();
            st.close();

        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }

    }

    public void getProblems(int rid, String cid){


        try{
            Connection conn = null;
            ArrayList<Problem> rawList = new ArrayList<Problem>();
            String url = "jdbc:postgresql://localhost:5432/lab2";
            conn = DriverManager.getConnection(url,"postgres", "password");

            PreparedStatement st = conn.prepareStatement("SELECT DISTINCT id,problems.problemid, problems.masterproblemid FROM problems WHERE recitationid = ? AND courseid = ?");
            st.setInt(1, rid);
            st.setString(2, cid);
            ResultSet rs = st.executeQuery();
            while ( rs.next() )
            {
                int mid;
                try{
                    mid = rs.getInt("masterproblemid");
                }catch (Exception e){
                    mid=-1;
                }
                System.out.println(String.valueOf(rs.getInt("problemid")));
                rawList.add(new Problem(rs.getInt("id"),rs.getInt("problemid"), mid, cid, rid));

            }
            rs.close();
            st.close();


            Collections.sort(rawList);

            for (int i = 0; i < rawList.size(); i++) {
                int pid = rawList.get(i).getProblemId();
                int uid = rawList.get(i).getUId();
                String courseId = rawList.get(i).getCourseId();

                System.out.println(pid);
                problems.add(rawList.get(i));
                for (int i2 = 0; i2 < rawList.size(); i2++) {
                    if(pid==rawList.get(i2).getMasterProblem()){
                        problems.add(rawList.get(i2));
                    }
                }
            }

            problemView.setItems(problems);

            problemView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            problemView.setOnMouseClicked(new EventHandler<Event>() {

                @Override
                public void handle(Event event) {
                    ObservableList<Problem> selectedItems =  problemView.getSelectionModel().getSelectedItems();

                    for(Problem s : selectedItems){
                        System.out.println("selected item " + s.getUId());
                    }

                }

            });

        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }

    }

    public void getGroups(Course c, int recid, int userID){
        try{
            observableGroups.clear();
            int uRecid = -1;
            Connection conn = null;
            String url = "jdbc:postgresql://localhost:5432/lab2";
            conn = DriverManager.getConnection(url,"postgres", "password");

            //GET UNIQUE RECITATION ID FROM SELECTION
            PreparedStatement st = conn.prepareStatement("SELECT id FROM recitations WHERE courseid = ? AND recitationid = ?");
            st.setString(1, c.id);
            st.setInt(2, recid);
            ResultSet rs = st.executeQuery();
            while(rs.next()){
                uRecid = rs.getInt("id");
                recitationid = uRecid;
                System.out.println("RECIDUNI: " + uRecid);
            }

            //GET ALL GROUPS FOR THAT REC
            st = conn.prepareStatement("SELECT courses.groups FROM courses WHERE courses.courseid = ?");
            st.setString(1, c.id);
            rs = st.executeQuery();


            while ( rs.next() )
            {
                int max = Integer.parseInt(rs.getString("groups"));
                for (int i = 1; i <= max;i++){
                    observableGroups.add(i);
                }
            }

            //SEE IF USER ALREADY HAS A GROUP FOR THAT RECITATION
            st = conn.prepareStatement("SELECT groupid FROM tracks WHERE studentid = ? AND recitationid = ?");
            st.setInt(1, userID);
            st.setInt(2, uRecid);
            rs = st.executeQuery();
            if ( rs.next() )
            {
                int groupid = rs.getInt("groupid");
                System.out.println("GROUPID: " + groupid);
                groupBox.getSelectionModel().select(groupid-1);
                groupBox.setDisable(true);
                shouldTrackBeWritten = false;
            }
            else {
                System.out.println("NO GROUP FOR THIS REC AND USER");
                groupBox.setDisable(false);
                shouldTrackBeWritten = true;
            }
            rs.close();
            st.close();

        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }

    }


    public void getRecitations(Course c){
        observableGroups.clear();
        observableRecitations.clear();
        problems.clear();
        try{
//            observableGroups.removeAll();
//            observableRecitations.removeAll();
            Connection conn = null;
            String url = "jdbc:postgresql://localhost:5432/lab2";
            conn = DriverManager.getConnection(url,"postgres", "password");

            PreparedStatement st = conn.prepareStatement("SELECT recitationid FROM recitations WHERE courseid = ?");
            st.setString(1, c.id);
            ResultSet rs = st.executeQuery();
            recitationBox.setDisable(false);
            while ( rs.next() )
            {
                int id = rs.getInt("recitationid");
                System.out.println(id);
                observableRecitations.add(id);
            }


            rs.close();
            st.close();



        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }

    }

}
