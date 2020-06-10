package postgres;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {
	public void addBytee(){
		Connection connection = null;
		try {			
			Class.forName("org.postgresql.Driver");	
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "password");
			if(connection != null){
				System.out.println("CONNECTION ESTABLISHED !");
				Statement st = connection.createStatement();
				PreparedStatement ps = connection.prepareStatement("insert into test_bytea values(?,?)");
				File file = new File("D:\\mm.txt");
				FileInputStream fis = new FileInputStream(file);				
				ps.setInt(1,1);
				ps.setBinaryStream(2, fis, (int)file.length());
				ps.executeUpdate();
				ps.close();
				fis.close();
				ps.execute();				
			}	
			connection.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
public static void main(String[] args) {
	
	
	TestConnection tt = new TestConnection();
	tt.addBytee();
	
	//Connection connection = null;
	/*try {
		
		Class.forName("org.postgresql.Driver");	
		connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "password");
		if(connection != null){
			System.out.println("CONNECTION ESTABLISHED !");
			Statement st = connection.createStatement();
			PreparedStatement ps = connection.prepareStatement("insert into test values(7,?)");
			ps.setString(1, "testsseven");
			ps.execute();
			ResultSet rs = st.executeQuery("select * from test");
			while(rs.next()){
				System.out.println(rs.getInt(1)+" "+rs.getString(2));
			}
		}	
		connection.close();
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
	
}
}
