    package pt.tecnico.Common;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
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
    // private final String user = "postgres";
    // private final String password = "1234";

    private final String addUserStatement ="INSERT INTO users (username, passwd, salt) VALUES(?,?,?);"; // usar no register
    private final String addFileStatement ="INSERT INTO files (filename,filehash) VALUES(?,?);"; // usar no writefile
    private final String addUserFileStatement ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm, file_owner ) VALUES(?,?,?,?,?);";

    private final String updateFileStatement = "UPDATE files SET filehash = ? WHERE f_id = ?";

    private final String giveReadPerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm, file_owner) VALUES(?,?,?,?,?);";
    private final String giveWritePerms ="INSERT INTO user_files (user_id, file_id, read_perm, write_perm, file_owner) VALUES(?,?,?,?,?);";
    private final String removeReadPerms ="UPDATE user_files SET read_perm = false WHERE user_id = 'smth';";
    private final String removeWritePerms ="UPDATE user_files SET write_perm = false WHERE user_id = 'smth';";

    private final String checkReadPerms ="SELECT read_perm FROM user_files WHERE user_id = ? AND file_id = ? ";
    private final String checkWritePerms ="SELECT write_perm FROM user_files WHERE user_id = ? AND file_id = ?";

    private final String deleteFileUserDB ="DELETE FROM user_files where user_id = ? AND file_id = ?";
    private final String deleteFileDB ="DELETE FROM files where f_id = ?";
    private final String getUserIDbyName ="SELECT u_id FROM users WHERE username = ?";
    private final String getUserNameByID ="SELECT username FROM users WHERE u_id = ?";
    private final String getFileIDbyName ="SELECT f_id FROM files WHERE filename = ?";
    private final String getFilenamebyID ="SELECT filename FROM files WHERE f_id = ?";
    private final String checkIsOwnerByID ="SELECT file_owner FROM user_files WHERE user_id = ? AND file_id = ? ";
    private final String getFileList ="SELECT file_id FROM user_files WHERE user_id = ? AND read_perm = ? ";
    private final String getUserPWbyName ="SELECT passwd FROM users WHERE username = ? ";
    private final String getUserSaltbyName ="SELECT salt FROM users WHERE username = ? ";
    private final String fileExists ="SELECT f_id FROM files WHERE filename = ? ";
    private final String getFileHashbyName ="SELECT filehash FROM files WHERE filename = ?";

    private final String getAllUsersWithAccessToFile = "SELECT user_id FROM user_files WHERE file_id = ?";

    public void addUserDatabase (Connection con, String name, String passwdKey){
        
        try{
            PreparedStatement ps = con.prepareStatement(addUserStatement);
            
            ps.setString(1, name);
            
            byte[] salt = CryptographyImpl.GenerateSalt();
            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            
            byte[] hashedPassword = CryptographyImpl.GenerateSaltedSHA3Digest(passwdKey.getBytes(), salt);
            String encodedPass = Base64.getEncoder().encodeToString(hashedPassword);
            
            ps.setString(2, encodedPass);
            ps.setString(3, encodedSalt);
            ps.executeUpdate();
            
            con.commit();
            System.out.println("User successfully added to database");

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

            String encodedBytes = Base64.getEncoder().encodeToString(filehash);
            ps.setString(2, encodedBytes);
            ps.executeUpdate();
            con.commit();          
            System.out.println("File successfully added to database");
         

        } catch (SQLException e){
            System.out.println("Failed to add file to database");
            e.printStackTrace();
            try{
                 con.rollback();
            } catch (SQLException ignore){              
            }
        }
    }  

    public void updateFileDatabase(Connection con, int fileID, byte[] filehash) {
        try{
            PreparedStatement ps = con.prepareStatement(updateFileStatement);

            String encodedBytes = Base64.getEncoder().encodeToString(filehash);
            ps.setString(1, encodedBytes);
            ps.setInt(2, fileID);
            ps.executeUpdate();
            con.commit();          
            System.out.println("File updated with new hash");
         

        } catch (SQLException e){
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
            System.out.println("User-File association successfully added to database");
         

        } catch (SQLException e){
            System.out.println("Failed to add User-File association to database");
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
        erro.add(0, "ERROR");
        try{
            PreparedStatement ps = con.prepareStatement(getFileList);
            ps.setInt(1, userID);
            ps.setBoolean(2, true);
            ResultSet rs = ps.executeQuery();
            int i = 0;

            while(rs.next()){
                result.add(i,rs.getInt(1)); // get int dá a column
                i++;
            }
            rs.close();
            con.commit();    
            
            for (int j =0; j< result.size(); j++){
                resultado.add(getFileNamebyFileID(con, result.get(j)));
            }  
            return resultado;

        } catch (SQLException e){
            System.out.println("Failed to fetch file list from the database");
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
            System.out.println("It is [" + result + "] that the user has read perms on the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("Failed to check user's read permissions");
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
            System.out.println("It is [" + result + "] that the user has write perms on the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("Failed to check user's write permissions");
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
            System.out.println("It is [" + result + "] that the user is the owner of the file");      
           
            return result;

        } catch (SQLException e){
            System.out.println("Failed to check if the user is the owner of the file");
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
            System.out.println("It is [" + result + "] that the file already exists");      
           
            if (oi != 0){
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e){
            System.out.println("Failed to check if the file already exists");
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
            System.out.println("File has been deleted sucessfully");

        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error deleting the file from the database");
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
            System.out.println("File has been deleted sucessfully");

        } catch (SQLException e){
            e.printStackTrace();
            System.out.println("Error deleting the file from the database");
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
                result = rs.getInt(1);
            }
            rs.close();
            con.commit();    
            System.out.println("UserID has been found");      
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching he userID from the database");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    }  
   
    public String getUsernamebyID (Connection con, int userID){
        int erro = 404;
        String result = "";
        try{
            PreparedStatement ps = con.prepareStatement(getUserNameByID);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getString(1);
            }
            rs.close();
            con.commit();    
            System.out.println("Username has been found!");      
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching the username from the database");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return null;
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
                result = rs.getInt(1);
            }
            rs.close();
            con.commit();    
            System.out.println("FileID has been found");      
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching the fileID from the database");
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
            System.out.println("Error fetching the file name from the database");
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
                result = rs.getString(1);
            }
            rs.close();
            con.commit();        
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching the user's password from the database");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    }  

    public String getUserSaltbyUsername (Connection con, String userName){
        String erro = "username does not exist";
        String result= null;
        try{
            PreparedStatement ps = con.prepareStatement(getUserSaltbyName);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getString(1);
            }
            rs.close();
            con.commit();        
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching the user's salt from the database");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return erro;
        }
    } 

    public byte[] getFileHashbyFilename (Connection con, String fileName){
        String erro = "404";
        String result= null;
        try{
            PreparedStatement ps = con.prepareStatement(getFileHashbyName);
            ps.setString(1, fileName);
            ResultSet rs = ps.executeQuery();

            while(rs.next()){
                result = rs.getString(1);
            }

            byte[] decodedHash = Base64.getDecoder().decode(result);

            rs.close();
            con.commit();    
            return decodedHash;

        } catch (SQLException e){
            System.out.println("Error fetching the file's hash from the database");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return null;
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

    public List<Integer> getUsersWithAccessToFile (Connection con, int file_id){
        
        List<String> erro = new ArrayList<String>();
        List<Integer> result = new ArrayList<Integer>();
        List<String> resultado = new ArrayList<String>();
        erro.add(0, "ERROR");
        try{
            PreparedStatement ps = con.prepareStatement(getAllUsersWithAccessToFile);
            ps.setInt(1, file_id);

            ResultSet rs = ps.executeQuery();
            int i = 0;

            while(rs.next()){
                result.add(i,rs.getInt(1)); // get int dá a column
                i++;
            }
            rs.close();
            con.commit();    
            
            return result;

        } catch (SQLException e){
            System.out.println("Error fetching users with access to the file");
            e.printStackTrace();
            try{
                 con.rollback();
        
            } catch (SQLException ignore){              
            }
        return null;
        }
        
    }

    /**
     * Connect to the PostgreSQL database-
     *
     * @return a Connection object
     */
    public Connection connect(String username, String password) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(false);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return conn;
    }


}