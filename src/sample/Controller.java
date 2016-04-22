package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

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
    Button submitButton;

    private ObservableList<Course> observableCourses = FXCollections.observableArrayList();
    private ObservableList<Integer> observableGroups = FXCollections.observableArrayList();
    private ObservableList<Integer> observableRecitations = FXCollections.observableArrayList();
    @FXML
    ListView problemView;

    ObservableList<String> problems = FXCollections.observableArrayList();

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
                        getGroups(observableCourses.get((Integer)new_value));
                    }
                });

        recitationBox.getSelectionModel().selectedIndexProperty()
                .addListener(new ChangeListener<Number>() {
                    public void changed(ObservableValue ov, Number value, Number new_value) {
                        String cid = ((Course)courseBox.getSelectionModel().getSelectedItem()).id;
                        int recid = (Integer)new_value;
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
            ArrayList<Problem> list = new ArrayList<Problem>();
            String url = "jdbc:postgresql://localhost:5432/lab2";

            // get the postgresql database connection
            conn = DriverManager.getConnection(url,"postgres", "password");
            PreparedStatement st = conn.prepareStatement("SELECT DISTINCT problems.problemid, problems.masterproblemid FROM problems WHERE recitationid = ?");
            st.setInt(1, Integer.parseInt(id));
            ResultSet rs = st.executeQuery();
            while ( rs.next() )
            {
                list.add(new Problem(rs.getString("problemid"), rs.getString("masterproblemid")));
                System.out.println(rs.getString("problemid"+" "+"masterproblemid"));
            }
            rs.close();
            st.close();

            problems.add(rs.getString("problemid"));
            problemView.setItems(problems);


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
