    package pt.tecnico.Common;

import java.beans.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author postgresqltutorial.com
 */
public class DatabaseImpl{

    private final String url = "jdbc:postgresql://localhost:5432/Ransom";
    private final String user = "postgres";
    private final String password = "1234";

    private final String addUserStatement ="INSERT INTO users (username, passwd) VALUES(?,?);"; // usar no register
    private final String addFileStatement ="INSERT INTO files ( file_name) VALUES(?);"; // usar no writefile
    private final String giveReadPerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";
    private final String giveWritePerms ="wwww";
    private final String removeReadPerms ="UPDATE user_files SET read_perm = false WHERE user_id = 'smth';";
    private final String removeWritePerms ="UPDATE user_files SET write_perm = false WHERE user_id = 'smth';";
    private final String checkReadPerms ="SELECT read_perm FROM user_files WHERE user_id = 'smth';";
    private final String checkWritePerms ="SELECT write_perm FROM user_files WHERE user_id = 'smth';";
    private final String getUserIDbyName ="SELECT u_id FROM user WHERE username == '  = 'smth';";



    public void addUserDatabase (Connection con, String name, String passwdKey){
        
        try{
            PreparedStatement ps = con.prepareStatement(addUserStatement);
            
            ps.setString(1, name);
            ps.setString(2, passwdKey);
            ps.executeUpdate();
            
            con.commit();

        } catch (SQLException e){
            e.printStackTrace();
            try{
                con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }

    public void addFileDatabase (Connection con, String fileName){
        
        try{
            PreparedStatement ps = con.prepareStatement(addFileStatement);
            
            ps.setString(1, fileName);
            ps.executeUpdate();
            
            con.commit();

        } catch (SQLException e){
            e.printStackTrace();
            try{
                con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }  
    
    public void giveReadPermission (Connection con, String userName, String fileName){
        
        try{
            PreparedStatement ps = con.prepareStatement(giveReadPerms);
            
            ps.setString(1, userName);
            ps.setString(2,fileName);
            ps.setBoolean(3, true);
            ps.setBoolean(4, false);
            ps.executeUpdate();
            
            con.commit();

        } catch (SQLException e){
            e.printStackTrace();
            try{
                con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }    
    
    public void giveWritePermission (Connection con, String userName, String fileName){
        
        try{
            PreparedStatement ps = con.prepareStatement(giveWritePerms);
            
            ps.setString(1, userName);
            ps.setString(2,fileName);
            ps.setBoolean(3, false);
            ps.setBoolean(4, true);
            ps.executeUpdate();
            
            con.commit();

        } catch (SQLException e){
            e.printStackTrace();
            try{
                con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }


    /**
     * Connect to the PostgreSQL database-
     *
     * @return a Connection object
     */
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DatabaseImpl app = new DatabaseImpl();
        app.connect();
    }
}