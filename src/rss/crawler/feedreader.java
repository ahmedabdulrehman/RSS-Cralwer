package rss.crawler;

import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner; // to take user input

// Syndication libraries from Rome
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

//import project classes
import rss.db.database;
import rss.input.uriObject;
import rss.input.uriReader;

public class feedreader {

    public static void main(String[] args) {
        boolean ok = false;
        String choice = "random";
        Scanner reader = new Scanner(System.in);
        System.out.println(
                "Press 1 to drop database\n" +
                        "Press 2 to truncate table\n" +
                        "Press 3 to update Detailed Description\n" +
                        "press any key to update existing table"
        );
        choice = reader.next();
        reader.close();
        uriReader parser= new uriReader();
        ArrayList<uriObject> urlList = parser.xmlFileParser();

        database db = new database();
        try {
            switch (choice) {
                case "1" : db.dropTable();
                case "2" : db.truncate();
                case "3" : db.addDetailedDescription();
                default : break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if ( !choice.equals("3")) {
            for (uriObject object : urlList) {
                System.out.print(object.getChannel() + " ");
                try {
                    URL feedUrl = new URL(object.getLink());
                    HttpURLConnection httpcon = (HttpURLConnection) feedUrl.openConnection();
                    httpcon.addRequestProperty("User-Agent", "Mozilla/4.76");

                    SyndFeedInput input = new SyndFeedInput();
                    SyndFeed feed = input.build(new XmlReader(httpcon));

                    db.configureDatabase();
                    db.updateTable(object.getChannel(), feed);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println("ERROR: " + ex.getMessage());
                }
            }
        }
    }
}
