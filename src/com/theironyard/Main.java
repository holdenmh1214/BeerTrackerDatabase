package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    static void insertBeer(Connection conn, String name, String type) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers(name, type) VALUES (?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }

    static void deleteBeer(Connection conn, int id) throws SQLException{
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM beers WHERE id = " + id);
        stmt.execute();
    }

    static ArrayList<Beer> selectBeer(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beerList = new ArrayList<>();
        int id = 1;
        while(results.next()){
            String name = results.getString("name");
            String type = results.getString("type");
            int idDB = results.getInt("id");
            Beer beer = new Beer(id, name, type, idDB);
            beerList.add(beer);
            id++;
        }
        return beerList;
    }

    static void editBeer(Connection conn, String name, String type, int idDB) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name=?, type=? WHERE id=" + idDB);
        stmt.setString(1, name);
        stmt.setString(2, type);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {

        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (id INT IDENTITY(1,1), name VARCHAR, type VARCHAR)");



        Spark.get(
                "/",
                ((request, response) -> {
                    ArrayList<Beer> beers = selectBeer(conn);
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", beers);
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );
        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    insertBeer(conn, beer.name, beer.type);
                    response.redirect("/");
                    System.out.println();
                    return "";
                })
        );
        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    response.redirect("/");
                    return "";
                })
        );

        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String edit = request.queryParams("beerid");
                    try {
                        String name = request.queryParams("beername");
                        String type = request.queryParams("beertype");
                        int id = Integer.valueOf(edit);
                        //PreparedStatement stmt2 = conn.prepareStatement("UPDATE beers SET name = ?, " +
                          //      "type =? WHERE id =" + id);
                        editBeer(conn, name, type, id);
                       // stmt2.execute();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    response.redirect("/");
                    return "";
                })
        );
    }
}
