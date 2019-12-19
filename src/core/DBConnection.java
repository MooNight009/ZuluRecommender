package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Constants.GenreEnum;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.TTUser;
import entity.Writer;

public class DBConnection{

	static Connection conn;
	static Statement stmt= null;

	// UPDATED V1
	// Connects to database
	public static void init(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn= DriverManager.getConnection(
					"jdbc:mysql://localhost/moviedataset?sendStringParametersAsUnicode=false&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
					"root", "");
			
//			conn= DriverManager.getConnection(
//					"jdbc:mysql://localhost/moviedataset?sendStringParametersAsUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC",
//					"root", "");
			

			System.out.println("Connected to conn");

			// stmt = conn.createStatement();
			// ResultSet rs = stmt.executeQuery("SELECT titleType, genres FROM
			// `title_basics` WHERE titleType= 'movie'");
			// while(rs.next()) {
			// System.out.println(rs.getString("genres"));
			// }

		} catch (ClassNotFoundException| SQLException e){
			e.printStackTrace();
			stmt= null;
			conn= null;
		}
	}

	// UPDATED V1
	// Finishes connection to database
	public static void end(){
		try{
			stmt.close();
			conn.close();
			System.out.println("Closed conn");

		} catch (SQLException e){
			e.printStackTrace();
			stmt= null;
			conn= null;
		}
	}

	// UPDATED V1
	// Returns list of movie names and their tconst
	public static ArrayList<String[]> getNames(){
		try{
			ArrayList<String[]> list= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, name FROM `movies`");
			while (rs.next()){
				String[] tmp= new String[] { rs.getString("tconst"), rs.getString("name")};
				list.add(tmp);
			}
			return list;
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// UPDATED V1
	// Return Map containing Genre and count of each
	public static Map<GenreEnum, Integer> getGenreCount(){
		Map<GenreEnum, Integer> map= null;

		try{
			map= new HashMap<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT * FROM `movie_genre`");
			while (rs.next()){
				GenreEnum genre= GenreEnum.valueOf(rs.getString("genre").replace("-", "_").toUpperCase());
				if (map.containsKey(genre)){
					Integer score= map.get(genre)+ 1;
					map.remove(genre);
					map.put(genre, score);
				} else
					map.put(genre, 1);
			}
			return map;
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return map;
	}

	// UPDATED V1
	// Returns List of movies containing tconst name and genres
	public static List<Movie> getMovies(List<Genre> genres){
		System.out.println("Started retrieving movie names");
		List<Movie> movies= null;

		try{
			movies= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, name, number_of_votes, average_rating FROM `movies`");
			int n= 0;
			while (rs.next()){
				if(n++% 1000== 0)	System.out.println(java.time.LocalDate.now()+ "-"+ n);
				String tconst= rs.getString("tconst");
				String name= rs.getString("name");
				int averageRating= Integer.parseInt(rs.getString("number_of_votes"));
				int numberOfVotes= Integer.parseInt(rs.getString("average_rating"));
				
				Movie movie= new Movie(name, tconst);
				movies.add(movie);
				movie.setAverageRating(averageRating);
				movie.setNumberOfVotes(numberOfVotes);
				
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
//		return setMovieGenres(movies, genres);
		return movies;
	}
	
	// UPDATED Final
	// Returns List of movies containing tconst name and genres
	public static void removeMovies(List<Movie> movies){
		System.out.println("Started deleting movie names");

		try{
			stmt= conn.createStatement();
			int n= 0;
			String tconsts= "";
			for(int i= 0; i< movies.size(); i++) {
				if(n++%1000== 0)	System.out.println("Deleted "+ n);
				tconsts+= "'"+ movies.get(i).getTconst()+ "',";
				
				if(i%100== 0) {
					tconsts= tconsts.substring(0, tconsts.length()- 1);
					stmt.executeUpdate("Delete FROM `movies` WHERE movies.tconst IN ("+ tconsts+ ")");
					tconsts= "";
				}
			}
			tconsts= tconsts.substring(0, tconsts.length()- 1);
			stmt.executeUpdate("Delete FROM `movies` WHERE tconst IN ("+ tconsts+ ")");
			
			
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	UPDATED V1
	//	Updates the given list with Genres
	public static List<Movie> setMovieGenres(List<Movie> movies, List<Genre> genres){
		System.out.println("Started setting movie genre");
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT * FROM movie_genre");
			int n= 0;
			while (rs.next()){
				if(n++% 1000== 0)	System.out.println(java.time.LocalTime.now()+ "-"+ n);
//				if(n% 50000== 0)	break;
				String tconst= rs.getString("tconst");
				String genre= rs.getString("genre");
				
				for(Movie movie: movies) {
					if(movie.getTconst().equals(tconst)) {
						movie.addGenre(Genre.getGenreBasedOnEnum(GenreEnum.valueOf(genre.replace("-", "_").toUpperCase()), genres));
						break;
					}
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return movies;
	}
	
	//	UPDATED V1
	//	Updates genre based on user_movie_rating
	public static void updateUserRating(List<Genre> genres, List<Movie> movies, String userId) {
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT * FROM user_movie_ratings WHERE user_id= '"+ userId+ "'");
			while (rs.next()){
				String tconst= rs.getString("tconst");
				Integer rating= Integer.parseInt(rs.getString("rating"));
				boolean isRecommended= rs.getString("is_recommended").equals("1");
				
				for(Movie movie: movies) {
					if(movie.getTconst().equals(tconst)) {
						movie.setRating(rating);
						movie.setWasMovieRecommended(isRecommended);
//						for(Genre movieGenre: movie.getGenres()) {
//							movieGenre.setSessionCount(movieGenre.getSessionCount()+ rating);
//						}
					}
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		Genre.updateScores(genres);
	}
	
	//	UPDATED V1
	//	Inserts new row in user_movie_ratings
	public static void insertIntoUserMovieRatings(String userId, String tconst, int rating, boolean isRecommended) {
		try{
			stmt= conn.createStatement();
			stmt.executeUpdate("INSERT INTO user_movie_ratings(user_id, tconst, rating, is_recommended) VALUES("+ userId+ ", '"+ tconst+ "' , "+ rating+ ", "+ isRecommended+ ")");
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}

	
	//	UPDATED FINAL
	//	Updates movies list with Year of Release and Length
	public static void updateWithYearAndLength(List<Movie> movies) {
		
		try {
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, year_of_release, length FROM `movies`");
			int n= 0;
			while (rs.next()){
//				if(n++%1000== 0)	System.out.println(n);
				
				String tconst= rs.getString("tconst");
				
				for(Movie movie: movies) {
					if(movie.getTconst().equals(tconst)) {
						movie.setYearOfRelease(Integer.parseInt(rs.getString("year_of_release")));
						movie.setLength(Integer.parseInt(rs.getString("length")));
						break;
					}
				}
			}
		}catch (Exception e) {
			System.out.println("Error in updating year and length ");
			e.printStackTrace();
		}
	}
	
	//	UPDATED FINAL
	//	UPDATES movies with Language
	public static void updateWithLanguage(List<Movie> movies) {
		
		try {
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, language FROM `movies`");
			int n= 0;
			while (rs.next()){
//				if(n++%1000== 0)	System.out.println(n);
				
				String tconst= rs.getString("tconst");
				
				for(Movie movie: movies) {
					if(movie.getTconst().equals(tconst)) {
						movie.setLanguage(rs.getString("language"));
						break;
					}
				}
			}
		}catch (Exception e) {
			System.out.println("Error in updating year and length ");
			e.printStackTrace();
		}
	}
	
	//	UPDATED V1
	//	Returns list of movies with writer and updates Movie list
	public static List<Writer> getWriters(List<Movie> movies){
		List<Writer> writers= null;
		
		try{
			writers= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, nconst FROM `cast_and_crew` where position= 'writer'");
			int n= 0;
			while (rs.next()){
				if(n++% 100000== 0)	System.out.println(Instant.now()+ "-"+ n+ "-"+ writers.size());
				String tconst= rs.getString("tconst");
				String nconst= rs.getString("nconst");
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				if(movie== null) continue;
				
				boolean isAvailable= false;
				for(Writer writer: writers) {
					if(writer.getNconst().equals(nconst)) {
						writer.addMovie(movie);
						isAvailable= true;
						movie.addWriter(writer);
						break;
					}
				}
				if(!isAvailable) {
					Writer writer= new Writer(nconst);
					writer.addMovie(movie);
					writers.add(writer);
					movie.addWriter(writer);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return writers;
	}
	
	//	UPDATED V1
	//	Returns list of movies with director and updates Movie list
	public static List<Director> getDirector(List<Movie> movies){
		List<Director> directors= null;
		
		try{
			directors= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, nconst FROM `cast_and_crew` where position= 'director'");
			int n= 0;
			while (rs.next()){
				if(n++% 10000== 0)	System.out.println(Instant.now()+ "-"+ n+ "-"+ directors.size());
				String tconst= rs.getString("tconst");
				String nconst= rs.getString("nconst");
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				
				boolean isAvailable= false;
				for(Director director: directors) {
					if(director.getNconst().equals(nconst)) {
						director.addMovie(movie);
						movie.addDirector(director);
						isAvailable= true;
						break;
					}
				}
				if(!isAvailable) {
					Director director= new Director(nconst);
					director.addMovie(movie);
					directors.add(director);
					movie.addDirector(director);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return directors;
	}
	
	//	UPDATED V1
	//	Returns list of movies with actor and updates Movie list
	public static List<Actor> getActors(List<Movie> movies){
		List<Actor> actors= null;
		
		try{
			actors= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, nconst FROM `cast_and_crew` where position= 'actor' ORDER BY nconst");
			int n= 0;
			Actor lastActor= null;
			String lastNconst= "";
			while (rs.next()){
				if(n++% 100000== 0)	System.out.println(Instant.now()+ "-"+ n+ "-"+ actors.size());
				String tconst= rs.getString("tconst");
				String nconst= rs.getString("nconst");
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				
//				boolean isAvailable= false;
				if(lastNconst.equals(nconst)) {
					lastActor.addMovie(movie);
					movie.addActor(lastActor);
				}
				else{
					Actor actor= new Actor(nconst);
					actor.addMovie(movie);
					actors.add(actor);
					movie.addActor(actor);
					lastActor= actor;
					lastNconst= nconst;
				}
				
				
//				for(Actor actor: actors) {
//					if(actor.getNconst().equals(nconst)) {
//						actor.addMovie(movie);
//						movie.addActor(actor);
//						isAvailable= true;
//						break;
//					}
//				}
//				if(!isAvailable) {
//					Actor actor= new Actor(nconst);
//					actor.addMovie(movie);
//					actors.add(actor);
//					movie.addActor(actor);
//				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return actors;
	}
	
	
	
	//	UPDATED V1
	//	Returns list of movies with writer and updates Movie list
	//	One movie
	public static List<Writer> getWriters(Movie movie, List<Writer> writers){
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT nconst FROM `cast_and_crew` where position= 'writer'"+ " AND tconst= '"+ movie.getTconst()+ "'");
			while (rs.next()){
				String nconst= rs.getString("nconst");
				
				
				boolean isAvailable= false;
				for(Writer writer: writers) {
					if(writer.getNconst().equals(nconst)) {
						writer.setMovieCount(writer.getMovieCount()+ movie.getRating());
						writer.addMovie(movie);
						isAvailable= true;
						break;
					}
				}
				if(!isAvailable) {
					Writer writer= new Writer(nconst);
					writer.setMovieCount(writer.getMovieCount()+ movie.getRating());
					writer.addMovie(movie);
					writers.add(writer);
					movie.addWriter(writer);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return writers;
	}
	
	//	UPDATED V1
	//	Returns list of movies with director and updates Movie list
	//	One movie
	public static List<Director> getDirectors(Movie movie, List<Director> directors){
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, nconst FROM `cast_and_crew` where position= 'director'"+ " AND tconst= '"+ movie.getTconst()+ "'");
			while (rs.next()){
				String nconst= rs.getString("nconst");
				
				
				boolean isAvailable= false;
				for(Director director: directors) {
					if(director.getNconst().equals(nconst)) {
						director.setMovieCount(director.getMovieCount()+ movie.getRating());
						director.addMovie(movie);
						isAvailable= true;
						break;
					}
				}
				if(!isAvailable) {
					Director director= new Director(nconst);
					director.setMovieCount(director.getMovieCount()+ movie.getRating());
					director.addMovie(movie);
					directors.add(director);
					movie.addDirector(director);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return directors;
	}
	
	//	UPDATED V1
	//	Returns list of movies with actor and updates Movie list
	//	One movie
	//	TODO: Updating to using cnc
	public static List<Actor> getActors(Movie movie, List<Actor> actors){
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst, nconst FROM `titleprincipals2` where tconst= '"+ movie.getTconst()+ "'");
			while (rs.next()){
				String nconst= rs.getString("nconst");
				
				
				boolean isAvailable= false;
				for(Actor actor: actors) {
					if(actor.getNconst().equals(nconst)) {
						actor.setMovieCount(actor.getMovieCount()+ movie.getRating());
						actor.addMovie(movie);
						isAvailable= true;
						break;
					}
				}
				if(!isAvailable) {
					Actor actor= new Actor(nconst);
					actor.setMovieCount(actor.getMovieCount()+ movie.getRating());
					actor.addMovie(movie);
					actors.add(actor);
					movie.addActor(actor);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return actors;
	}

	
	
	//	UPDATED V1
	//	Updates writer with movies
	public static void getWriterMovies(List<Movie> movies, Writer writer){
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst FROM `cast_and_crew` where position= 'writer'"+ " AND nconst= '"+ writer.getNconst()+ "'");
			while (rs.next()){
				String tconst= rs.getString("tconst");
				if(writer.getNconst().equals("nm0166256")|| writer.getNconst().equals("nm0615780"))	System.out.println("This "+ tconst);
				
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				if(movie== null)	continue;
				if(writer.getNconst().equals("nm0166256")|| writer.getNconst().equals("nm0615780"))	System.out.println("That "+ movie.getName()+ "-"+ writer.getMovies().size());
				if(!writer.getMovies().contains(movie))	writer.addMovie(movie);
				if(writer.getNconst().equals("nm0166256")|| writer.getNconst().equals("nm0615780"))	System.out.println("and "+ writer.getMovies().size());
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	UPDATED V1
	//	Returns list of movies with director and updates Movie list
	//	One movie
	public static void getDirectorMovies(List<Movie> movies, Director director){
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst FROM `cast_and_crew` where position= 'director'"+ " AND nconst= '"+ director.getNconst()+ "'");
			while (rs.next()){
				String tconst= rs.getString("tconst");
				
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				if(movie== null)	continue;
				if(!director.getMovies().contains(movie))	director.addMovie(movie);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
	}
	
	//	UPDATED V1
	//	Returns list of movies with actor and updates Movie list
	//	One movie
	public static void getActorMovies(List<Movie> movies, Actor actor){
		if(actor.getNconst().equals("nm0425005"))	System.out.println();
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT tconst FROM `titleprincipals2` where  nconst= '"+ actor.getNconst()+ "'");
			while (rs.next()){
				String tconst= rs.getString("tconst");
				
				
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				if(movie== null)	continue;
				if(!actor.getMovies().contains(movie))	actor.addMovie(movie);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
	}
	
	//	UPDATED V1
	//	Return movie with the requested idfrom ML_Movies
	public static Movie updateMovieIdML(List<Movie> movies, int id) {
		Movie movie= null;
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT movieId, title FROM `ml_movies` WHERE movieId= '"+ id+ "'");
			while (rs.next()){
				String movieName= rs.getString("title").split("\\(")[0].trim().toLowerCase();
				int movieId= Integer.parseInt(rs.getString("movieId"));
				
				
				movie= Movie.getMovieByName(movies, movieName, 0.7);
				if(movie== null) {
					System.out.println("Couldn't find "+ movieName);
					return null;
				}
				movie.setMlMovieId(movieId);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		return movie;
	}
	
	//	UPDATED V1
	//	Returns list of users in ml_ratings
	public static List<TTUser> getMLUserbase() {
		List<TTUser> ttUsers= new ArrayList<>();
		
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT movieId, userId, rating FROM `ml_ratings` ORDER BY userId");
			Map<Integer, Double> movieIds= new HashMap<>();
			int lastUserId= 0;
			while (rs.next()){
				int movieId= Integer.parseInt(rs.getString("movieId"));
				int userId= Integer.parseInt(rs.getString("userId"));
				double rating= Double.parseDouble(rs.getString("rating"));
				
				if(userId!= lastUserId) {
					ttUsers.add(new TTUser(movieIds.size(), movieIds));
					movieIds= new HashMap<>();
					lastUserId= userId;
				}
				movieIds.put(movieId, rating);
			}
			ttUsers.add(new TTUser(movieIds.size(), movieIds));
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		
		return ttUsers;
	}
	
	//	UPDATED V2
	//	Returns map of movieID and title
	public static Map<String, String> getMLMoviesList(){
		Map<String, String> map= new HashMap<>();
		try{
			stmt= conn.createStatement();
			
			ResultSet rs= stmt.executeQuery("SELECT movieId, title FROM `ml_movies`");
			while (rs.next()){
				String id= rs.getString("movieId");
				String title= rs.getString("title");
				title= title.split("\\(")[0].trim().toLowerCase();
				
				map.put(id, title);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
		
		return map;
	}
	
	//	UPDATED V2
	//	Updates ml_id in movies
	public static void updateMLIDInMovies(List<Movie> movies) {
		try{
			stmt= conn.createStatement();
			
			for(Movie movie: movies) {
				if(movie.getMlId().equals("0"))	continue;
				String command= "UPDATE movies SET ml_id= '"+ movie.getMlId()+ "' WHERE tconst= '"+ movie.getTconst()+ "'";
				
				stmt.executeUpdate(command);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	UPDATED V2
	//	Updates movie with values
	public static void retrieveMovieValues(List<Movie> movies) {
		try{
			stmt= conn.createStatement();
			for(Movie movie: movies) {
				ResultSet rs= stmt.executeQuery("SELECT * FROM `movie_weights` WHERE tconst= '"+ movie.getTconst()+ "'");
				while (rs.next()){
					double[] values= new double[37];
				    for (int i = 2; i < 39; i++) {
				    	String value= rs.getString(i);
				        if(values== null|| value.equals("NULL")|| value.equals("NaN"))	values[i- 2]= 0.0;
				        else values[i- 2]= Double.parseDouble(value);
				    }
					movie.setValues(values);
				}
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	Updates the list of movies with average and number of votes
	public static void updateRating(List<Movie> movies) {
		try{
			stmt= conn.createStatement();
			
			ResultSet rs= stmt.executeQuery("SELECT number_of_votes, average_rating, tconst FROM `movies`");
			int n= 0;
			while (rs.next()){
				if(n++% 1000== 0)	System.out.println(n);
				String tconst= rs.getString("tconst");
				int averageRating= 0;
				int numberOfVotes= 0;
				
				if(rs.getString("number_of_votes")!= null)averageRating= Integer.parseInt(rs.getString("number_of_votes"));
				if(rs.getString("average_rating")!= null)	numberOfVotes= Integer.parseInt(rs.getString("average_rating"));
				Movie movie= Movie.getMovieByTconst(tconst, movies);
				movie.setAverageRating(averageRating);
				movie.setNumberOfVotes(numberOfVotes);
			}
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	UPDATED V2
	//	Updates the movie weights table with 
	public static void updateMovieWeight(Movie movie, boolean genre) {
		try{
			stmt= conn.createStatement();
			
			String command= "UPDATE movie_weights SET\n";
//			if(genre)	for(Map.Entry<Genre, Double> entry: movie.movieGenreScores.entrySet())	command+= ", g_"+ entry.getKey().toString().toLowerCase();
//			for(Map.Entry<String, Double> entry: movie.movieCastAndCrewScores.entrySet())	command+= ", "+ entry.getKey();
//			command+= ") VALUES (";
//			if(genre)	for(Map.Entry<Genre, Double> entry: movie.movieGenreScores.entrySet())	command+= ", '"+ entry.getValue()+ "'";
//			for(Map.Entry<String, Double> entry: movie.movieCastAndCrewScores.entrySet())	command+= ", '"+ entry.getValue()+ "'";
//			command+= ")";
			
			if(genre)	for(Map.Entry<Genre, Double> entry: movie.movieGenreScores.entrySet()) command+= "g_"+ entry.getKey().toString().toLowerCase()+ "= '"+ entry.getValue()+ "',\n";
//			for(Map.Entry<String, Double> entry: movie.movieCastAndCrewScores.entrySet()) command+= entry.getKey().toString().toLowerCase()+ "= '"+ entry.getValue()+ "' ,\n";
			command= command.substring(0, command.length()- 2);
			command+= "\nWHERE tconst= '"+ movie.getTconst()+ "'";
//			System.out.println(command);
			while(command.contains("(, "))	command= command.replace("(, ", "(");
			stmt.executeUpdate(command);
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	//	UPDATED V2
	//	Updates the cast and crew values table
	public static void updateActorValues(List<Actor> actors) {
		try{
			stmt= conn.createStatement();
			
			for(Actor actor: actors) {
				String command= "INSERT INTO cast_and_crew_values(nconst, movie_count, position";
				for(Map.Entry<Genre, Double> entry: actor.movieGenreScores.entrySet())	command+= ", g_"+ entry.getKey().toString().toLowerCase();
				command+= ") VALUES ('"+ actor.getNconst()+ "', "+ actor.getMovies().size()+ ", 'actor'";
				for(Map.Entry<Genre, Double> entry: actor.movieGenreScores.entrySet())	command+= ", '"+ entry.getValue()+ "'";
				command+= ")";
				stmt.executeUpdate(command);
			}
//			stmt.executeUpdate("INSERT INTO user_movie_ratings(user_id, tconst, rating, is_recommended) VALUES("+ userId+ ", '"+ tconst+ "' , "+ rating+ ", "+ isRecommended+ ")");
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	public static void updateDirectorValues(List<Director> directors) {
		try{
			stmt= conn.createStatement();
			
			for(Director director: directors) {
				String command= "INSERT INTO cast_and_crew_values(nconst, movie_count, position";
				for(Map.Entry<Genre, Double> entry: director.movieGenreScores.entrySet())	command+= ", g_"+ entry.getKey().toString().toLowerCase();
				command+= ") VALUES ('"+ director.getNconst()+ "', "+ director.getMovies().size()+ ", 'director'";
				for(Map.Entry<Genre, Double> entry: director.movieGenreScores.entrySet())	command+= ", '"+ entry.getValue()+ "'";
				command+= ")";
				stmt.executeUpdate(command);
			}
//			stmt.executeUpdate("INSERT INTO user_movie_ratings(user_id, tconst, rating, is_recommended) VALUES("+ userId+ ", '"+ tconst+ "' , "+ rating+ ", "+ isRecommended+ ")");
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	public static void updateWriterValues(List<Writer> writers) {
		try{
			stmt= conn.createStatement();
			
			for(Writer writer: writers) {
				String command= "INSERT INTO cast_and_crew_values(nconst, movie_count, position";
				for(Map.Entry<Genre, Double> entry: writer.movieGenreScores.entrySet())	command+= ", g_"+ entry.getKey().toString().toLowerCase();
				command+= ") VALUES ('"+ writer.getNconst()+ "', "+ writer.getMovies().size()+ ", 'writer'";
				for(Map.Entry<Genre, Double> entry: writer.movieGenreScores.entrySet())	command+= ", '"+ entry.getValue()+ "'";
				command+= ")";
				stmt.executeUpdate(command);
			}
//			stmt.executeUpdate("INSERT INTO user_movie_ratings(user_id, tconst, rating, is_recommended) VALUES("+ userId+ ", '"+ tconst+ "' , "+ rating+ ", "+ isRecommended+ ")");
			
		} catch (SQLException e){
			e.printStackTrace();
		}
	}
	
	public static ArrayList<String> getDirectors(String tconst){
		try{
			ArrayList<String> list= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT directors FROM `titleCrew2` WHERE tconst= '"+ tconst+ "'");
			while (rs.next()){
				list.add(rs.getString("directors"));
			}
			return list;
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String> getWriters(String tconst){
		try{
			ArrayList<String> list= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT writers FROM `titleCrew2` WHERE tconst= '"+ tconst+ "'");
			while (rs.next()){
				list.add(rs.getString("writers"));
			}
			return list;
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<String[]> getCrew(String tconst){
		try{
			ArrayList<String[]> list= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt
					.executeQuery("SELECT nconst, category, job FROM `titleprincipals2` WHERE tconst= '"+ tconst+ "'");
			while (rs.next()){
				list.add(new String[] { rs.getString("nconst"), rs.getString("category"), rs.getString("job")});
			}
			return list;
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String[] getRatings(String tconst){
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt
					.executeQuery("SELECT averageRating, numVotes FROM `titleratings2` WHERE tconst= '"+ tconst+ "'");
			while (rs.next()){
				return new String[] { rs.getString("averageRating"), rs.getString("numVotes")};
			}
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getCrewName(String nconst){
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT primaryName FROM `namebasics2` WHERE nconst= '"+ nconst+ "'");
			while (rs.next()){
				return rs.getString("primaryName");
			}
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static String getGenre(String tconst){
		try{
			stmt= conn.createStatement();
			ResultSet rs= stmt.executeQuery("SELECT genres FROM `titlebasic2` WHERE tconst= '"+ tconst+ "'");
			while (rs.next()){
				return rs.getString("genres");
			}
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static void getNames2(){
		try{
//			ArrayList<String> list= new ArrayList<>();
			stmt= conn.createStatement();
			ResultSet rs= stmt
					.executeQuery("SELECT tconst, primaryTitle, genres FROM `titlebasic2` WHERE titleType= 'movie'");
			while (rs.next()){
				System.out.println(rs.getString("tconst")+ "\t"+ rs.getString("primaryTitle")+ "\t"
						+ rs.getString("genres")+ "\t"+ rs.getString("genres"));
			}
		} catch (SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
