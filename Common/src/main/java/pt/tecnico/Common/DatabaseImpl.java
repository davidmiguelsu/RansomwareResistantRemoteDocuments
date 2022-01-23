    package pt.tecnico.Common;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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
    private final String addFileStatement ="INSERT INTO files (filename,filehash) VALUES(?,?);"; // usar no writefile
    private final String addUserFileStatement ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm, file_owner ) VALUES(?,?,?,?,?);";

    private final String giveReadPerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";
    private final String giveWritePerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm) VALUES(?,?,?,?);";
    private final String removeReadPerms ="UPDATE user_files SET read_perm = false WHERE user_id = 'smth';";
    private final String removeWritePerms ="UPDATE user_files SET write_perm = false WHERE user_id = 'smth';";

    private final String checkReadPerms ="SELECT read_perm FROM user_files WHERE user_id = ? AND file_id = ? ";
    private final String checkWritePerms ="SELECT write_perm FROM user_files WHERE user_id = ? AND file_id = ?";

    private final String deleteFileUserDB ="DELETE FROM user_files where file_id = ? AND user_id = ?";
    private final String deleteFileDB ="DELETE FROM files where f_id = ?";
    private final String getUserIDbyName ="SELECT u_id FROM users WHERE username = ?";
    private final String getFileIDbyName ="SELECT f_id FROM files WHERE filename = ?";
    private final String getFilenamebyID ="SELECT filename FROM files WHERE f_id = ?";
    private final String checkIsOwnerByID ="SELECT file_owner FROM user_files WHERE file_id = ? AND user_id = ? ";
    private final String getFileList ="SELECT file_id FROM user_files WHERE user_id = ? AND read_perm = ? ";
    private final String getUserPWbyName ="SELECT passwd FROM users WHERE username = ? ";
    private final String fileExists ="SELECT f_id FROM files WHERE file_name = ? ";



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

    public void addFileDatabase (Connection con, String fileName, byte [] filehash){
        try{
            PreparedStatement ps = con.prepareStatement(addFileStatement);
            ps.setString(1, fileName);
            ps.setBytes(2, filehash);
            ps.executeUpdate();
            con.commit();          
            System.out.println("File added !! with hash");
         

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS ADDFILEDATABASE");
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
            ps.setBoolean(5, true);
            ps.executeUpdate();
            con.commit();          
            System.out.println("user_files added  !!");
         

        } catch (SQLException e){
            System.out.println("CHEGOU AQUI AMIGOSSSS ADD USERFILE DATABASE");
            e.printStackTrace();
            try{
                 con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }  
    
    public List<String> getListFile (Connection con, int userID ){
        
        List<String> erro = new ArrayList<String>();
        List<Integer> result = new ArrayList<Integer>();
        List<String> resultado = new ArrayList<String>();
        erro.add(0, "DEU MAL VIU");
        try{
            PreparedStatement ps = con.prepareStatement(getFileList);
            ps.setInt(1, userID);
            ps.setBoolean(2, true);
            ResultSet rs = ps.executeQuery();
            int i = 0;

            while(rs.next()){
                result.add(i,rs.getInt(i+1));
                i++;
            }
            rs.close();
            con.commit();    
            
            for (int j =0; j< result.size(); j++){
                resultado.add(getFileNamebyFileID(con, result.get(j)));
            }  
            return resultado;

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

    public boolean doesUserHaveReadPerms (Connection con, int userID, int fileID){
        boolean erro = false;
        boolean result= false;
        try{
            PreparedStatement ps = con.prepareStatement(checkReadPerms);
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getBoolean(1);
            }
            rs.close();
            con.commit();    
            System.out.println("It is " + result + " that the user has read perms on the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("CHEGOU doesUserHaveReadPerms");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    } 

    public boolean doesUserHaveWritePerms (Connection con, int userID, int fileID){
        boolean erro = false;
        boolean result= false;
        try{
            PreparedStatement ps = con.prepareStatement(checkWritePerms);
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getBoolean(1);
            }
            rs.close();
            con.commit();    
            System.out.println("It is " + result + " that the user has write perms on the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("CHEGOU doesUserHaveReadPerms");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    } 

    public boolean checkIsUserOwner (Connection con, int userID, int fileID){
        boolean erro = false;
        boolean result= false;
        try{
            PreparedStatement ps = con.prepareStatement(checkIsOwnerByID);
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getBoolean(1);
            }
            rs.close();
            con.commit();    
            System.out.println("It is " + result + " that the user is the owner of the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("CHEGOU checkIsUserOwner");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    } 
    
    public boolean doesFileExist (Connection con, String fileName){
        boolean erro = false;
        boolean result= false;
        int oi=0;
        try{
            PreparedStatement ps = con.prepareStatement(fileExists);
            ps.setString(1, fileName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                oi = rs.getInt(1);
            }
            rs.close();
            con.commit();    
            System.out.println("It is " + result + " that the file already exists");      
           
            if (oi != 0){
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e){
            System.out.println("CHEGOU checkIsUserOwner");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    } 

    public void deleteFileUserDatabase (Connection con, int userID, int fileID){
        

        try{
            PreparedStatement ps = con.prepareStatement(deleteFileUserDB);            
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ps.executeUpdate();
            
            con.commit();
            System.out.println("File has been deleted sucessfully !!");

        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error deleting file DB !!");
            try{
                con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }

    public void deleteFileDatabase(Connection con, int fileID) {

        try{
            PreparedStatement ps = con.prepareStatement(deleteFileDB);            

            ps.setInt(1, fileID);
            ps.executeUpdate();   
            con.commit();
            System.out.println("File has been deleted sucessfully !!");

        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error deleting file DB !!");
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

    public String getFileNamebyFileID (Connection con, int fileID){
        String erro = "404";
        String result= null;
        try{
            PreparedStatement ps = con.prepareStatement(getFilenamebyID);
            ps.setInt(1, fileID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getString(1);
            }
            rs.close();
            con.commit();    
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

    // for auth purposes
    public String getUserPWbyUsername (Connection con, String userName){
        String erro = "username does not exist";
        String result= null;
        try{
            PreparedStatement ps = con.prepareStatement(getUserPWbyName);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                System.out.println("Column 1 returned");
                result = rs.getString(1);
            }
            rs.close();
            con.commit();        
            return result;

        } catch (SQLException e){
            System.out.println("username not found");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    }  


    // give another user read perms to the file
    public void giveReadPermission (Connection con, int userID, int fileID){
        
        try{
            PreparedStatement ps = con.prepareStatement(giveReadPerms);
            
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ps.setBoolean(3, true);
            ps.setBoolean(4, false);
            ps.setBoolean(5, false);
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
    
    // give another user read/write perms to the file
    public void giveAllPermission (Connection con, int userID, int fileID){
        
        try{
            PreparedStatement ps = con.prepareStatement(giveWritePerms);
            
            ps.setInt(1, userID);
            ps.setInt(2, fileID);
            ps.setBoolean(3, true);
            ps.setBoolean(4, true);
            ps.setBoolean(5, false);
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
            conn.setAutoCommit(false);
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