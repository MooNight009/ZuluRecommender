package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sun.media.sound.SoftTuning;

import config.Constants.GenreEnum;

public class BadMethodForInsertingToDB{
	static Connection conn;
	static Statement stmt = null;
	static Statement stmt2= null;
	static List<String> list= new ArrayList<>();

	public static void main(String[] args) {
		System.out.println("STarted cleaning");
		createCastAndCrewValueTable();
		
		//	Need to run this code after cleaning to get the actors
//		try {
//			Class.forName("com.mysql.jdbc.Driver");
//			conn= DriverManager.getConnection("jdbc:mysql://localhost/moviedataset?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root", "");
//			
//			System.out.println("Connected to conn");
//			
//			stmt = conn.createStatement();
//			stmt2 = conn.createStatement();
////	        ResultSet rs = stmt.executeQuery("SELECT titlecrew2.tconst, writers\r\n" + 
////	        		"FROM titlecrew2\r\n" + 
////	        		"INNER JOIN movies\r\n" + 
////	        		"ON titlecrew2.tconst= movies.tconst");
////	        System.out.println(rs.getFetchSize());
////	        int n= 0;
////	        
////			while(rs.next()) {
////				if(n++%10000== 0)	System.out.println(n);
////				String tconst= rs.getString("tconst");
////				String[] nconstList= rs.getString("writers").split(",");
////				String position= "writer";
////				
////				for(String nconst: nconstList) {
////					stmt2.executeUpdate("INSERT INTO cast_and_crew (tconst, nconst, position)\r\n" + 
////							"VALUES('"+ tconst+ "', '"+ nconst.trim()+ "', '"+ position+ "')");
////				}
////			}
//			
//			
//			System.out.println("Started phase 2");
//			list= new ArrayList<>();
//			ResultSet rs = stmt.executeQuery("SELECT titleprincipals2.tconst, titleprincipals2.nconst, titleprincipals2.category\r\n" + 
//					"FROM titleprincipals2 \r\n" + 
//					"INNER JOIN movies\r\n" + 
//					"ON titleprincipals2.tconst= movies.tconst\r\n" + 
//					"WHERE titleprincipals2.category= 'actor'");
//	        int n= 0;
//	        System.out.println(rs.getFetchSize());
//	        System.out.println("Started the process");
//			while(rs.next()) {
//				if(n++%10000== 0)	System.out.println(n);
//				String tconst= rs.getString("tconst");
//				String nconst= rs.getString("nconst");
//				String position= "actor";
//				
//				stmt2.executeUpdate("INSERT INTO cast_and_crew (tconst, nconst, position)\r\n" + 
//						"VALUES('"+ tconst+ "', '"+ nconst.trim()+ "', '"+ position+ "')");
//			}
//			
//			
//
//			stmt.close();
//			stmt2.close();
//			conn.close();
//		} catch (ClassNotFoundException | SQLException e) {
//			e.printStackTrace();
//			stmt= null;
//			conn= null;
//			stmt2= null;
//		}
	}
	
	private static void doGenre() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn= DriverManager.getConnection("jdbc:mysql://localhost/moviedataset?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root", "");
			
			System.out.println("Connected to conn");
			
			stmt = conn.createStatement();
			stmt2 = conn.createStatement();
	        ResultSet rs = stmt.executeQuery("SELECT titlebasic2.tconst, genres\r\n" + 
	        		"FROM titlebasic2\r\n" + 
	        		"INNER JOIN movies\r\n" + 
	        		"ON titlebasic2.tconst= movies.tconst");
	        System.out.println(rs.getFetchSize());
	        int n= 0;
	        
			while(rs.next()) {
				if(n++%10000== 0)	System.out.println(n);
				String tconst= rs.getString("tconst");
				String[] genreList= rs.getString("genres").split(",");
				
				for(String genre: genreList) {
					stmt2.executeUpdate("INSERT INTO movie_genre (tconst, genre)\r\n" + 
							"VALUES('"+ tconst+ "', '"+ genre.trim()+ "')");
				}
			}
			

			stmt.close();
			stmt2.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			stmt= null;
			conn= null;
			stmt2= null;
		}
	}
	
	private static void cleanTP2() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn= DriverManager.getConnection("jdbc:mysql://localhost/moviedataset?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root", "");
			
			System.out.println("Connected to conn");
			
			stmt = conn.createStatement();
	        
			for(int i= 0; i< 10000; i++) {
				if(i%10== 0)	System.out.println("Cleanned "+ i);
				stmt.executeUpdate("\r\n" + 
						"		DELETE FROM titleprincipals2\r\n" + 
						"		WHERE tconst NOT IN (SELECT m.tconst FROM movies m)\r\n" + 
						"	    LIMIT 500");
				stmt.executeUpdate("DELETE FROM `movies`\r\n" + 
						"WHERE tconst NOT IN (SELECT m.tconst FROM movie_genre m)\r\n" + 
						"LIMIT 500");
			}
			

			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			stmt= null;
			conn= null;
		}
	}
	
	private static void createMovieWeightsTable() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn= DriverManager.getConnection("jdbc:mysql://localhost/moviedataset?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root", "");
			
			System.out.println("Connected to conn");
			
			stmt = conn.createStatement();
	        
			for(GenreEnum genreEnum: GenreEnum.values()) {
				
				stmt.executeUpdate("ALTER TABLE movie_weights\r\n" + 
						"ADD COLUMN g_"+ genreEnum.toString().toLowerCase()+ " VARCHAR(255)");
			}
			

			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			stmt= null;
			conn= null;
		}
	}
	
	private static void createCastAndCrewValueTable() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn= DriverManager.getConnection("jdbc:mysql://localhost/moviedataset?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC","root", "");
			
			System.out.println("Connected to conn");
			
			stmt = conn.createStatement();
	        
			for(GenreEnum genreEnum: GenreEnum.values()) {
				
				stmt.executeUpdate("ALTER TABLE cast_and_crew_values\r\n" + 
						"ADD COLUMN g_"+ genreEnum.toString().toLowerCase()+ " VARCHAR(255)");
			}
			

			stmt.close();
			conn.close();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			stmt= null;
			conn= null;
		}
	}
}
