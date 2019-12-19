package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import config.Constants;
import config.Constants.GenreEnum;
import entity.Genre;
import entity.Movie;

public class JsonConverter{

	public static void saveGenres(List<Genre> genres) {
		try{
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(Constants.genre_json));
			
			myWriter.write("[");
			myWriter.newLine();
			for(Genre genre: genres) {
				String json= genre.getJson();
				myWriter.write(json);
				myWriter.newLine();
			}
			myWriter.write("]");
			
			myWriter.close();
		} catch (IOException e){
			// TODO Auto-generated catch block
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static List<Genre> readGenres(){
		List<Genre> genres= new ArrayList<>();
		
		Object obj;
		try{
			obj= new JSONParser().parse(new FileReader(new File(Constants.genre_json)));
			JSONArray jo= (JSONArray) obj;
			
			
			
			for(int i= 0; i< jo.size(); i++) {
				JSONObject genreObject= (JSONObject) jo.get(i);
				
				GenreEnum mainGenreEnum= GenreEnum.valueOf(genreObject.get("genre").toString());
				int movieCount= Integer.parseInt(genreObject.get("movieCount").toString());
				
				Genre genre= null;
				for(Genre g: genres) {
					if(g.getGenre().equals(mainGenreEnum)) {
						g.setMovieCount(movieCount);
						genre= g;
					}
				}
				if(genre== null){
					genre= new Genre(mainGenreEnum, movieCount);
					genres.add(genre);
				}
				
				JSONArray connectionJson= (JSONArray) genreObject.get("connections");
				for(int j= 0; j< connectionJson.size(); j++) {
					JSONObject connectionGenreObject= (JSONObject) connectionJson.get(j);
					GenreEnum connectionGenreEnum= GenreEnum.valueOf(connectionGenreObject.get("genre").toString());
					double connectionValue= Double.parseDouble(connectionGenreObject.get("value").toString());
					
					Genre genre2= null;
					
					for(Genre g: genres) {
						if(g.getGenre().equals(connectionGenreEnum)) {
							genre2= g;
						}
					}
					
					if(genre2== null) {
						genre2= new Genre(connectionGenreEnum, 0);
						genres.add(genre2);
					}
					
					genre.getGenreConnection().put(genre2, connectionValue);
				}
			}
			
		} catch (IOException| ParseException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return genres;
	}

	public static void saveMovies(List<Movie> movies) {
		try{
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(Constants.movie_json));
			
			myWriter.write("[");
			myWriter.newLine();
			for(Movie movie: movies) {
				String json= movie.getJson();
				myWriter.write(json);
				myWriter.newLine();
			}
			myWriter.write("]");
			
			myWriter.close();
		} catch (IOException e){
			// TODO Auto-generated catch block
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static List<Movie> readMovies(List<Genre> genres){
		List<Movie> movies= new ArrayList<>();
		
		Object obj;
		try{
			obj= new JSONParser().parse(new FileReader(new File(Constants.movie_json)));
			JSONArray jo= (JSONArray) obj;
			
			
			
			for(int i= 0; i< jo.size(); i++) {
				JSONObject movieObject= (JSONObject) jo.get(i);
				
//				GenreEnum mainGenreEnum= GenreEnum.valueOf(genreObject.get("genre").toString());
				String tconst= movieObject.get("tconst").toString();
				String name= movieObject.get("name").toString();
				int yearOfRelease= Integer.parseInt(movieObject.get("yearOfRelease").toString());
				int length= Integer.parseInt(movieObject.get("length").toString());
				int averageRating= Integer.parseInt(movieObject.get("averageRating").toString());
				int numberOfVotes= Integer.parseInt(movieObject.get("numberOfVotes").toString());
				String language= movieObject.get("language").toString();
				String mlId= movieObject.get("mlId").toString();


				Movie movie= new Movie(name, tconst);
				movie.setAverageRating(averageRating);
				movie.setNumberOfVotes(numberOfVotes);
				movie.setMlId(mlId);
				movie.setYearOfRelease(yearOfRelease);
				movie.setLength(length);
				movie.setLanguage(language);
				
				
				JSONArray connectionJson= (JSONArray) movieObject.get("genres");
				for(int j= 0; j< connectionJson.size(); j++) {
					movie.addGenre(Genre.getGenreBasedOnEnum(GenreEnum.valueOf(connectionJson.get(j).toString()), genres));
				}
				movies.add(movie);
			}
			
		} catch (IOException| ParseException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return movies;
	}
}
