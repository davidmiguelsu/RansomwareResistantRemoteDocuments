    package pt.tecnico.Common;

import java.sql.Statement;
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
    
    //Database URL
    private final String url = "jdbc:postgresql://localhost:5432/Ransom";
    
    
    //Credentials
    private final String user = "postgres";
    private final String password = "1234";

    private final String addUserStatement ="INSERT INTO users (username, passwd) VALUES(?,?);"; // usar no register
    private final String addFileStatement ="INSERT INTO files (filename) VALUES(?);"; // usar no writefile
    private final String addUserFileStatement ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";

    private final String giveReadPerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";
    private final String giveWritePerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";
    private final String removeReadPerms ="UPDATE user_files SET read_perm = false WHERE user_id = 'smth';";
    private final String removeWritePerms ="UPDATE user_files SET write_perm = false WHERE user_id = 'smth';";

    private final String checkReadPerms ="SELECT read_perm FROM user_files WHERE user_id = 'smth';";
    private final String checkWritePerms ="SELECT write_perm FROM user_files WHERE user_id = 'smth';";

    private final String getUserIDbyName ="SELECT u_id FROM users WHERE username = ?";
    private final String getFileIDbyName ="SELECT f_id FROM files WHERE filename = ?";



    public void addUserDatabase (Connection con, String name, String passwdKey){
        
        try{
            PreparedStatement ps = con.prepareStatement(addUserStatement);
            
            ps.setString(1, name);
            ps.setString(2, passwdKey);
            ps.executeUpdate();
            
            con.commit();
            System.out.println("User added !!");

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
            System.out.println("File added !!");
         

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS");
            e.printStackTrace();
            try{
                 con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }  

    public void addUserFileDatabase (Connection con, int userID, int fileID){

        try{
            PreparedStatement ps = con.prepareStatement(addUserFileStatement);
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ps.setBoolean(3, true);
            ps.setBoolean(4, true);
            ps.executeUpdate();
            con.commit();          
            System.out.println("user_files added !!");
         

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS");
            e.printStackTrace();
            try{
                 con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }  
    


    public int getUserIDbyUsername (Connection con, String userName){
        int erro = 404;
        int result= 0;
        try{
            PreparedStatement ps = con.prepareStatement(getUserIDbyName);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                System.out.println("Column 1 returned");
                result = rs.getInt(1);
                System.out.println(result);
            }
            rs.close();
            con.commit();    
            System.out.println("userID has been found!");      
            return result;

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    }  

    public int getFileIDbyFileName (Connection con, String fileName){
        int erro = 404;
        int result= 0;
        try{
            PreparedStatement ps = con.prepareStatement(getFileIDbyName);
            ps.setString(1, fileName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                System.out.println("Column 1 returned");
                result = rs.getInt(1);
                System.out.println(result);
            }
            rs.close();
            con.commit();    
            System.out.println("fileID has been found!");      
            return result;

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    }  



    // // give another user read perms to the file
    // public void giveReadPermission (Connection con, String userName, String fileName){
        
    //     try{
    //         PreparedStatement ps = con.prepareStatement(giveReadPerms);
            
    //         ps.setString(1, userName);
    //         ps.setString(2,fileName);
    //         ps.setBoolean(3, true);
    //         ps.setBoolean(4, false);
    //         ps.executeUpdate();
    //         con.commit();

    //     } catch (SQLException e){
    //         e.printStackTrace();
    //         try{
    //             con.rollback();
    //         } catch (SQLException ignore){              
    //         }
    //     }
    // }    
    
    // // give another user read/write perms to the file
    // public void giveWritePermission (Connection con, String userName, String fileName){
        
    //     try{
    //         PreparedStatement ps = con.prepareStatement(giveWritePerms);
            
    //         ps.setString(1, userName);
    //         ps.setString(2,fileName);
    //         ps.setBoolean(3, false);
    //         ps.setBoolean(4, true);
    //         ps.executeUpdate();
            
    //         con.commit();

    //     } catch (SQLException e){
    //         e.printStackTrace();
    //         try{
    //             con.rollback();
    //         } catch (SQLException ignore){              
    //         }
    //     }
    // }


    /**
     * Connect to the PostgreSQL database-
     *
     * @return a Connection object
     */
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            System.out.println("AutoCommit has been set to false");
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