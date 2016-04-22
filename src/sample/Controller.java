package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;
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
    ListView problemView;
    @FXML
    Button submitButton;

    private ObservableList<Course> observableCourses = FXCollections.observableArrayList();
    private ObservableList<Integer> observableGroups = FXCollections.observableArrayList();
    private ObservableList<Integer> observableRecitations = FXCollections.observableArrayList();
    private ObservableList<String> problems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        Connection conn = null;
        courseBox.setDisable(true);
        courseBox.setItems(observableCourses);

        groupBox.setDisable(true);
        groupBox.setItems(observableGroups);

        recitationBox.setDisable(true);
        recitationBox.setItems(observableRecitations);

        courseBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        if (observableCourses.size() > 0){
                            getGroups(observableCourses.get((Integer)new_value));
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
                                Connection conn = null;
                                String url = "jdbc:postgresql://localhost:5432/lab2";

                                // get the postgresql database connection
                                conn = DriverManager.getConnection(url,"postgres", "password");
                                PreparedStatement st = conn.prepareStatement("SELECT DISTINCT id FROM recitations WHERE courseid = ? AND recitationid = ?");
                                st.setString(1, cid);
                                st.setInt(2, recid);
                                ResultSet rs = st.executeQuery();
                                courseBox.setDisable(false);
                                while ( rs.next() )
                                {
                                    System.out.println("CHANGE REC");
                                    getProblems(rs.getString("id"));
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
                });

        submitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("SUBMIT, BITCH");

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

    public void getUser(String id){
        try{
            observableGroups.clear();
            observableRecitations.clear();
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

    public void getProblems(String id){

        try{
            Connection conn = null;
            ArrayList<Problem> rawList = new ArrayList<Problem>();
            String url = "jdbc:postgresql://localhost:5432/lab2";

            // get the postgresql database connection
            conn = DriverManager.getConnection(url,"postgres", "password");
            PreparedStatement st = conn.prepareStatement("SELECT DISTINCT problems.problemid, problems.masterproblemid FROM problems WHERE recitationid = ?");
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            while ( rs.next() )
            {
                int mid;
                try{
                    mid = rs.getInt("masterproblemid");
                }catch (Exception e){
                    mid=0;
                }
                System.out.println(String.valueOf(rs.getInt("problemid")));
                rawList.add(new Problem(rs.getInt("problemid"), mid));
            }
            rs.close();
            st.close();

            for (int i = 0; i < rawList.size(); i++) {
                int pid = rawList.get(i).getProblem();
                System.out.println(pid);
                problems.add(String.valueOf(pid));
                for (int i2 = 0; i2 < rawList.size(); i2++) {
                    if(pid==rawList.get(i2).getMasterProblem()){
                        problems.add("      " + String.valueOf(rawList.get(i2).getProblem()));
                    }
                }
            }
            problemView.setItems(problems);

            problemView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

            problemView.setOnMouseClicked(new EventHandler<Event>() {

                @Override
                public void handle(Event event) {
                    ObservableList<String> selectedItems =  problemView.getSelectionModel().getSelectedItems();

                    for(String s : selectedItems){
                        System.out.println("selected item " + s);
                    }

                }

            });

        }
        catch (Exception e){
            e.printStackTrace();
            System.exit(2);
        }

    }



    public void getGroups(Course c){
        observableGroups.clear();
        observableRecitations.clear();
        try{
//            observableGroups.removeAll();
//            observableRecitations.removeAll();
            Connection conn = null;
            String url = "jdbc:postgresql://localhost:5432/lab2";

            // get the postgresql database connection
            conn = DriverManager.getConnection(url,"postgres", "password");
            PreparedStatement st = conn.prepareStatement("SELECT courses.groups FROM courses WHERE courses.courseid = ?");
            st.setString(1, c.id);
            ResultSet rs = st.executeQuery();
            groupBox.setDisable(false);

            while ( rs.next() )
            {
                int max = Integer.parseInt(rs.getString("groups"));
                for (int i = 1; i <= max;i++){
                    observableGroups.add(i);
                }
            }

            st = conn.prepareStatement("SELECT recitationid FROM recitations WHERE courseid = ?");
            st.setString(1, c.id);
            rs = st.executeQuery();
            recitationBox.setDisable(false);
            if (!rs.next()){

            }
            while ( rs.next() )
            {
                int id = Integer.parseInt(rs.getString("recitationid"));
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
