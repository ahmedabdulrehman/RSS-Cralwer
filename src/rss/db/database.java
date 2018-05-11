package rss.db;

// Java classes
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.Iterator;

//Syndication classes
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndEntry;

//HTML parsing classes
import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.extractors.ArticleExtractor;
import org.jsoup.Jsoup;

//Project classes
import rss.http.httpConnection;

public class database {
    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://localhost";

    // Database credentials
    private static final String USER = "";
    private static final String PASS = "";

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(JDBC_DRIVER);
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Database connection established.");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Cannot connect to database server!");
            System.exit(1);
        }
        return connection;
    }
    public void configureDatabase () {
        Connection connection = getConnection();
        Statement dbStatement = null;
        try {
            dbStatement = connection.createStatement();
            String query =  "CREATE TABLE IF NOT EXISTS Resources "
                            + "(id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE, "
                            + "Channel VARCHAR(128), "
                            + "Title TEXT, "
                            + "Description TEXT, "
                            + "Detailed_Description TEXT, "
                            + "Uri VARCHAR(512), "
                            + "Author VARCHAR(256), "
                            + "Pub_Date TIMESTAMP, "
                            + "PRIMARY KEY (id))";
            dbStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS publicDataCampaigns;");
            dbStatement.executeUpdate("USE publicDataCampaigns;");
            dbStatement.executeUpdate(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }finally{
            try{
                if(dbStatement != null)
                    connection.close();
                    System.out.println("Database connection successfully closed.");
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
    }

     public void updateTable(String channelTitle, SyndFeed feed) throws SQLException {
         Connection connection = getConnection();
         String sqlQuery = "INSERT INTO Resources(Channel, Title, Description, Uri, Author, Pub_Date) VALUES (?,?,?,?,?,?)";
         connection.setAutoCommit(false);
         PreparedStatement ps = null;
         Statement statement = null;
         ResultSet resultSet = null;
         try {
             ps = connection.prepareStatement(sqlQuery);
             statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             statement.executeUpdate("use publicDataCampaigns");
             for (Iterator<?> entryIter = feed.getEntries().iterator(); entryIter.hasNext();) {
                 SyndEntry syndEntry = (SyndEntry) entryIter.next();
                 int count = 0;
                 resultSet = statement.executeQuery("SELECT Title FROM Resources");
                 while (resultSet.next()) {
                     if (syndEntry.getTitle() != null) {
                         if (syndEntry.getTitle().contentEquals(resultSet.getString("Title"))) {
                             count++;
                         }
                     }
                 }
                 if (count < 1) {
                     ps.setString(1, channelTitle);
                     if (syndEntry.getTitle() == null) {
                         ps.setString(2, "");
                     } else {
                         ps.setString(2, Jsoup.parse(syndEntry.getTitle()).text());
                     }
                     if (syndEntry.getDescription() == null) {
                         ps.setString(3, "");
                     } else {
                         ps.setString(3, Jsoup.parse(syndEntry.getDescription().getValue()).text());
                     }
                     if (syndEntry.getLink() == null) {
                         ps.setNull(4, Types.VARCHAR);
                     } else {
                         ps.setString(4, syndEntry.getLink());
                     }
                     if (syndEntry.getAuthor() == null) {
                         ps.setNull(5, Types.VARCHAR);
                     } else {
                         ps.setString(5, syndEntry.getAuthor());
                     }
                     if (syndEntry.getPublishedDate() == null) {
                         ps.setNull(6, Types.DATE);
                     } else {
                         java.util.Date utilDate = syndEntry.getPublishedDate();
                         java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
                         ps.setTimestamp(6, sqlDate);
                     }
                     ps.executeUpdate();
                     connection.commit();
                     System.out.println("Entry successfully added.");
                 } else {
                     System.out.println("Entry with Title.... " +syndEntry.getTitle()+ " .... already exists.");
                 }
             }
             System.out.println("All the entries have been saved successfully.");
         } catch (SQLException e) {
             System.err.print("sql exception while performing prepared statement....." + e);
             try {
                 System.err.print("Transaction is being rolled back");
                 connection.rollback();
             } catch (SQLException excep) {
                 System.err.print("rollback exception....." + excep);
             }
         } finally {
             if (ps != null) { ps.close(); }
             connection.setAutoCommit(true);
             try{
                 connection.close();
                 System.out.println("Database connection successfully closed.");
             }catch(SQLException se){
                 se.printStackTrace();
             }
         }
     }

     public void truncate() throws SQLException {
        Connection connection = getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("USE publicDataCampaigns");
            statement.executeUpdate("TRUNCATE TABLE Resources;");
            System.out.println("Old table truncated.");
        } catch (SQLException e) {
            System.err.print(e);
        }
        if (statement != null) {
            connection.close();
            System.out.println("Database connection successfully closed.");
        }
     }

     public void dropTable() throws SQLException {
         Connection connection = getConnection();
         Statement statement = null;
         try {
             statement = connection.createStatement();
             statement.executeUpdate("DROP DATABASE IF EXISTS publicDataCampaigns;");
             System.out.println("Old database dropped.");
         } catch (SQLException e) {
             System.err.print(e);
         }
         if (statement != null) {
             connection.close();
             System.out.println("Database connection successfully closed.");
         }
     }

    public void addDetailedDescription() throws SQLException {
        Connection connection = getConnection();
        httpConnection http = null;
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        URL url;
        String sqlQuery = "UPDATE Resources SET Detailed_Description = ? WHERE Uri = ?";
        connection.setAutoCommit(false);
        try {
            statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statement.executeUpdate("use publicDataCampaigns");
            ps = connection.prepareStatement(sqlQuery);
            ArticleExtractor article = ArticleExtractor.getInstance();
            System.setProperty("http.agent", "Chrome");
            resultSet = statement.executeQuery("SELECT Uri FROM Resources");
            while (resultSet.next()) {
                String dbUrl = resultSet.getString("Uri");
                if (dbUrl != null ) {
                    http = new httpConnection();
                    int code = 600;

                    if (dbUrl.matches("^(http|https)://.*$")) {
                        url = new URL(dbUrl);
                        code = http.httpConn(url);

                        if (code >= 300 && code < 400) {
                            url = http.httpToHttps(url);
                            code = http.httpConn(url);
                        }

                        if (code < 400) {
                            ps.setString(1, article.getText(url));
                            ps.setString(2, resultSet.getString("Uri"));
                            ps.executeUpdate();
                            connection.commit();
                            System.out.println("Entry successfully added with link.... " + url);
                        } else {
                            System.out.println("Entry not added with link..... " + url);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.print(e);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (BoilerpipeProcessingException e) {
            e.printStackTrace();
        }
        if (ps != null) { ps.close(); }
        connection.setAutoCommit(true);
        if (statement != null) {
            connection.close();
            System.out.println("Database connection successfully closed.");
        }
    }
}


