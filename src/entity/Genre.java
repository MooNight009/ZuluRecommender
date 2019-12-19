package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import config.Constants.GenreEnum;

public class Genre{

	private GenreEnum genre; // Genre name

	private int movieCount; // Number of movies with this genre
	private int sessionCount; // ^ for this session

	private Map<Genre, Double> genreConnection= new HashMap<>();
	private Map<Genre, Integer> sessionGenreConnection= new HashMap<>(); // This is currently not being used

	private double sessionScore= 0.0;

	public Genre(GenreEnum genre, int movieCount){
		this.genre= genre;
		this.movieCount= 0;
	}

	public double getSessionScore(){
		return sessionScore;
	}

	public void setSessionScore(double sessionScore){
		this.sessionScore= sessionScore;
	}

	public int getSessionCount(){
		return sessionCount;
	}

	public void setSessionCount(int sessionCount){
		this.sessionCount= sessionCount;
	}

	public Map<Genre, Double> getGenreConnection(){
		return genreConnection;
	}

	public void setGenreConnection(Map<Genre, Double> genreConnection){
		this.genreConnection= genreConnection;
	}

	public GenreEnum getGenre(){
		return genre;
	}

	public void setGenre(GenreEnum genre){
		this.genre= genre;
	}

	public int getMovieCount(){
		return movieCount;
	}

	public void setMovieCount(int movieCount){
		this.movieCount= movieCount;
	}

	public Map<Genre, Integer> getSessionGenreConnection(){
		return sessionGenreConnection;
	}

	public void setSessionGenreConnection(Map<Genre, Integer> sessionGenreConnection){
		this.sessionGenreConnection= sessionGenreConnection;
	}

	public void addConnection(Genre genre){
		this.genreConnection.put(genre, 0.0);
	}

	public void addSessionConnection(Genre genre){
		this.sessionGenreConnection.put(genre, 0);
	}
	
	public String toString() {
		String str= this.getGenre().toString();
		
		return str;
	}

	public String getJson(){
		String json= "{";

		json+= "\"genre\": \""+ this.genre+ "\",";
		json+= "\"movieCount\": \""+ this.movieCount+ "\",";
		json+= "\"connections\": [";
		for (Map.Entry<Genre, Double> entry : this.genreConnection.entrySet()){
			json+= "{";
			json+= "\"genre\": \""+ entry.getKey().getGenre()+ "\",";
			json+= "\"value\": \""+ entry.getValue()+ "\"";
			json+= "}";
		}

		json+= "]";

		json+= "} ,";
		return json;
	}

	//	Returns Genre class with the requeste GenreEnum
	public static Genre getGenreBasedOnEnum(GenreEnum genreEnum, List<Genre> genres) {
		for(Genre genre: genres)	if(genre.getGenre().equals(genreEnum))	return genre;
		return null;
	}

	// Updates the score
	public static void updateScores(List<Genre> genres){
		for (Genre genre : genres)
			genre.setSessionScore(0.0);

		// Update the score here
		for (Genre genre : genres)
			Genre.recursiveScoreUpdate(genres, genre, genre.getSessionCount());
	}

	private static void recursiveScoreUpdate(List<Genre> genres, Genre g, double score){
		g.setSessionScore(score+ g.getSessionScore());
		// TODO: Update this with the original later
		for (Map.Entry<Genre, Double> entry : g.getGenreConnection().entrySet()){
			if (Math.pow(entry.getValue(), 2)* score> 0.005){
				recursiveScoreUpdate(genres, entry.getKey(), score* Math.pow(entry.getValue(), 2));
			}
		}
	}

	// Fills up genreConnection
	public static void fillConnection(List<Movie> movies, List<Genre> genres){
		//	Sets the count for each genre
		for (Movie movie : movies){ // Loop through movies
			for (Genre movieGenre : movie.getGenres()){ // Loop through movie's genres
				movieGenre.setMovieCount(movieGenre.getMovieCount()+ 1);
				for (Genre movieGenre2 : movie.getGenres()){ // Loops through movie's genres to get the one not current
					if (!movieGenre2.equals(movieGenre)){
						for (Map.Entry<Genre, Double> entry : movieGenre.getGenreConnection().entrySet()){
							if (entry.getKey().equals(movieGenre2)){
								entry.setValue(entry.getValue()+ 1);
							}
						}
					}
				}
			}
		}

		for (Genre genre : genres){
			// genre.setMovieCount(genre.getGenreConnection().get(genre).intValue());
			genre.getGenreConnection().remove(genre);
			genre.getSessionGenreConnection().remove(genre);
			for (Map.Entry<Genre, Double> entry : genre.getGenreConnection().entrySet()){
				if (entry.getValue()!= 0.0)
					entry.setValue(entry.getValue()/ genre.getMovieCount());
			}
		}
	}

	// This method creates list of genres based on genreEnum
	public static List<Genre> getGenreList(){
		List<Genre> genres= new ArrayList<>();

		for (GenreEnum genreEnum : GenreEnum.values()){
			Genre genre= new Genre(genreEnum, genreEnum.totalCount);
			Map<GenreEnum, Double> map= new HashMap<>();
			for (GenreEnum genreEnum2 : GenreEnum.values()){
				map.put(genreEnum2, 0.0);
			}

			genres.add(genre);
		}

		// Create the connection list
		for (Genre genre : genres){
			for (Genre genre2 : genres){
				genre.addConnection(genre2);
				genre.addSessionConnection(genre2);
			}
		}

		return genres;
	}
}
