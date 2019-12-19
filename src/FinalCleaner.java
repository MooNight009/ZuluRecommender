import java.util.ArrayList;
import java.util.List;

import config.Constants;
import core.DBConnection;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;

//	This method removed the ones with low score as well as majority of the duplicates
public class FinalCleaner{
	public static void main(String[] args){
//		removeLowScoreAndDuplicate();
//		updateIMDBMoviesWithNewMovies();
//		updateJSONForYearAndLength();
		updateJSONForLanguage();
	}
	
	public static void updateJSONForYearAndLength() {
		DBConnection.init();
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.updateWithYearAndLength(movies);
		
		JsonConverter.saveMovies(movies);
	}
	
	public static void updateIMDBMoviesWithNewMovies() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.init();
		
		List<Movie> DBMovies= DBConnection.getMovies(genres);
		List<Movie> toBeRemoved= new ArrayList<>();
		System.out.println(DBMovies.size());
		
		loop:
		for(int i= 0; i< DBMovies.size(); i++) {
			if(i% 1000== 0)	System.out.println(i+ "-"+ DBMovies.size()+ "-"+ toBeRemoved.size()+ "-"+ movies.size());
			for(Movie movie: movies) {
				if(DBMovies.get(i).getTconst().equals(movie.getTconst())) {
					DBMovies.remove(i);
					i--;
					movies.remove(movie);
					continue loop;
				}
			}
		}
		
		System.out.println(DBMovies.size());
		System.out.println(movies.size());

		DBConnection.removeMovies(DBMovies);
		
		DBConnection.end();
	}
	
	public static void removeLowScoreAndDuplicate() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
//		IO.loadSVDMV(movies, Constants.movie_values_TFIDF_SVD);
		IO.loadMV(movies, Constants.movie_values);
		
		int lowScore= 0;
		int nameDuplicate= 0;
		for(int i= 0; i< movies.size(); i++) {
			Movie firstMovie= movies.get(i);
//			if(firstMovie.getNumberOfVotes()< 1000&& firstMovie.getMlId().equals("0")) {
			if(firstMovie.getNumberOfVotes()< 1000) {
				movies.remove(i);
				i--;
				lowScore++;
			}
		}
		
		System.out.println("Size after deleting "+ movies.size());
		
		loop:
		for(int i= 0; i< movies.size(); i++) {
			if(i%10000== 0)	System.out.println(lowScore+ "-"+ nameDuplicate+ "-"+ i);
			Movie firstMovie= movies.get(i);
			for(int j= i+ 1; j< movies.size(); j++) {
//				if(firstMovie.getName().equals(movies.get(j).getName())&& firstMovie.getMlId().equals("0") && movies.get(j).getMlId().equals("0")) {
				if(firstMovie.getName().equals(movies.get(j).getName())) {
					if(movies.get(j).getNumberOfVotes()< firstMovie.getNumberOfVotes()) {
						movies.remove(j);
						j--;
						nameDuplicate++;
					}
					else if(firstMovie.getNumberOfVotes()< movies.get(j).getNumberOfVotes()) {
						movies.remove(i);
						i--;
						nameDuplicate++;
						continue loop;
					}
					else	System.out.println("Wtf");
				}
			}
		}
		
		System.out.println("Low score count is "+ lowScore);
		System.out.println("Duplicate names is "+ nameDuplicate);
		System.out.println("Total is "+ movies.size());
		System.out.println(movies.size());
		JsonConverter.saveMovies(movies);
		IO.saveMV(movies, Constants.movie_values);
	}

	public static void updateJSONForLanguage() {
		DBConnection.init();
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.updateWithLanguage(movies);
		
		JsonConverter.saveMovies(movies);
	}
}
