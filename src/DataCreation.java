import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import core.DBConnection;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.Writer;
import utilities.JsonConverter;

public class DataCreation{

	public static void main(String[] args){
		DBConnection.init();
//		updateMovieWeightTableGenreEdition();
		updateMLIdMovies();
		
		DBConnection.end();
	}
	
	private static void updateMLIdMovies() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		List<Movie> duplicateOriginal= new ArrayList<Movie>(movies);
		Map<String, String> mlData= DBConnection.getMLMoviesList();	//	First is id, second is name
		int s= 0;
		
		System.out.println(mlData.size());
		
		for(Map.Entry<String, String> entry: mlData.entrySet()) {
			if(s++% 10000== 0) {
				System.out.println(Instant.now()+ "-"+ s);
			}
			Movie movie= Movie.getMovieByName(movies, entry.getValue(), 0.7);
			if(movie== null) {
				System.out.println("Failed "+ entry.getValue()+ "-"+ entry.getKey());
				continue;
			}
			movie.setMlId(entry.getKey());
			movies.remove(movie);
		}
		
		int n= 0;
		for(Movie movie: duplicateOriginal) if(!movie.getMlId().equals("0"))	n++;
		System.out.println("Found movies "+ n);
		
//		DBConnection.updateMLIDInMovies(duplicateOriginal);
		
//		JsonConverter.saveMovies(duplicateOriginal);
	}
	
	private static void defaulJsonCreation() {
		List<Genre> genres= Genre.getGenreList();	//	Create Genre list
		List<Movie> movies= DBConnection.getMovies(genres);	//	Create movie list
//		List<Movie> movies= JsonConverter.readMovies(genres);
		Genre.fillConnection(movies, genres);	//	Update 
		DBConnection.updateRating(movies);
		JsonConverter.saveGenres(genres);
		JsonConverter.saveMovies(movies);
	}
	
	private static void updateMovieWeightTableGenreEdition() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		
		//	Initit the movie genre map
		for(Movie movie: movies) {
			for(Genre genre: genres)	movie.movieGenreScores.put(genre, 0.0);
		}
		
		//	Calculate the values for the genre
		for(Movie movie: movies) {
			for(Genre movieGenre: movie.getGenres())	recursiveGenreScoreUpdate(genres, movieGenre, 1, movie);
		}
		
		System.out.println("Now we can update the table");
		updateMovieWeightTableActorEdition(genres, movies);
		
		
	}
	
	private static void updateMovieWeightTableActorEdition(List<Genre> genres, List<Movie> movies) {
		System.out.println(Instant.now());
		
//		List<Actor> actors= DBConnection.getActors(movies);
//		updateValueActor(actors, genres);
////		DBConnection.updateActorValues(actors);
//		System.out.println(Instant.now()+ " Finished getting actors");
//		for(Movie movie: movies) {
//			List<Genre> movieGenres= movie.getGenres();
//			List<Double> actorScore= new ArrayList<>();
//			
//			for(Actor actor: movie.getActors()) {
//				double score= 0.0;
//				for(Genre genre: movieGenres)	score+= actor.movieGenreScores.get(genre);
//				actorScore.add(score/ movieGenres.size());
//			}
//			
//			Collections.sort(actorScore);
//			Collections.reverse(actorScore);
//			
//			for(int i= 0; i< 3; i++) {
//				if(i>= actorScore.size())	movie.movieCastAndCrewScores.put("a_"+ i, 0.0);
//				else	movie.movieCastAndCrewScores.put("a_"+ i, actorScore.get(i));
//			}
//			DBConnection.updateMovieWeight(movie, true);
//		}
//		
//		actors= null;
//		
//		List<Director> directors= DBConnection.getDirector(movies);
//		updateValueDirector(directors, genres);
////		DBConnection.updateDirectorValues(directors);
//		System.out.println(Instant.now()+ " Finished getting directors");for(Movie movie: movies) {
//			List<Genre> movieGenres= movie.getGenres();
//			List<Double> directorScore= new ArrayList<>();
//			
//			for(Director director: movie.getDirectors()) {
//				double score= 0.0;
//				for(Genre genre: movieGenres)	score+= director.movieGenreScores.get(genre);
//				directorScore.add(score/ movieGenres.size());
//			}
//			
//			Collections.sort(directorScore);
//			Collections.reverse(directorScore);
//			
//			
//			
//			for(int i= 0; i< 3; i++) {
//				if(i>= directorScore.size())	movie.movieCastAndCrewScores.put("d_"+ i, 0.0);
//				else	movie.movieCastAndCrewScores.put("d_"+ i, directorScore.get(i));
//			}
//			
//			DBConnection.updateMovieWeight(movie, false);
//		}
//		
//		directors= null;
//		
		List<Writer> writers= DBConnection.getWriters(movies);
		updateValueWriter(writers, genres);
		DBConnection.updateWriterValues(writers);
		System.out.println(Instant.now()+ " Finished getting writer");
		
		//	Now we update movie score for actor, writer and director
		
		for(Movie movie: movies) {
			List<Genre> movieGenres= movie.getGenres();
			List<Double> writerScore= new ArrayList<>();
			
			for(Writer writer: movie.getWriters()) {
				double score= 0.0;
				for(Genre genre: movieGenres)	score+= writer.movieGenreScores.get(genre);
				writerScore.add(score/ movieGenres.size());
			}
			
			Collections.sort(writerScore);
			Collections.reverse(writerScore);
			
			for(int i= 0; i< 3; i++) {
				if(i>= writerScore.size())	movie.movieCastAndCrewScores.put("w_"+ i, 0.0);
				else	movie.movieCastAndCrewScores.put("w_"+ i, writerScore.get(i));
			}
			
			DBConnection.updateMovieWeight(movie, false);
		}
	}
	
	private static void updateValueActor(List<Actor> actors, List<Genre> genres) {
		//	Set the map values
		for(Actor actor: actors)	for(Genre genre: genres)	actor.movieGenreScores.put(genre, 0.0);
		
		//	Now we update the values
		for(Actor actor: actors) {
			for(Movie movie: actor.getMovies()) {
				Map<Genre, Double> movieGenreScores= movie.movieGenreScores;
				for(Map.Entry<Genre, Double> entry: actor.movieGenreScores.entrySet()) {
					entry.setValue(entry.getValue()+ movieGenreScores.get(entry.getKey()));
				}
			}
			for(Map.Entry<Genre, Double> entry: actor.movieGenreScores.entrySet()) {
				entry.setValue(entry.getValue()/ actor.getMovies().size());
			}
			
		}
	}
	
	private static void updateValueWriter(List<Writer> writers, List<Genre> genres) {
		//	Set the map values
		for(Writer writer: writers)	for(Genre genre: genres)	writer.movieGenreScores.put(genre, 0.0);
		
		//	Now we update the values
		for(Writer writer: writers) {
			for(Movie movie: writer.getMovies()) {
				Map<Genre, Double> movieGenreScores= movie.movieGenreScores;
				for(Map.Entry<Genre, Double> entry: writer.movieGenreScores.entrySet()) {
					entry.setValue(entry.getValue()+ movieGenreScores.get(entry.getKey()));
				}
			}
			for(Map.Entry<Genre, Double> entry: writer.movieGenreScores.entrySet()) {
				entry.setValue(entry.getValue()/ writer.getMovies().size());
			}
		}
	}
	
	private static void updateValueDirector(List<Director> directors, List<Genre> genres) {
		//	Set the map values
		for(Director director: directors)	for(Genre genre: genres)	director.movieGenreScores.put(genre, 0.0);
		
		//	Now we update the values
		for(Director director: directors) {
			for(Movie movie: director.getMovies()) {
				Map<Genre, Double> movieGenreScores= movie.movieGenreScores;
				for(Map.Entry<Genre, Double> entry: director.movieGenreScores.entrySet()) {
					entry.setValue(entry.getValue()+ movieGenreScores.get(entry.getKey()));
				}
			}
			for(Map.Entry<Genre, Double> entry: director.movieGenreScores.entrySet()) {
				entry.setValue(entry.getValue()/ director.getMovies().size());
			}
		}
	}
	
	//	Recursively updates the movieGenreScore for each movie
	public static void recursiveGenreScoreUpdate(List<Genre> genres, Genre g, double score, Movie movie){
		double value= movie.movieGenreScores.get(g);
		movie.movieGenreScores.remove(g);
		movie.movieGenreScores.put(g,  value+ score);
		
		// TODO: Update this with the original later
		for (Map.Entry<Genre, Double> entry : g.getGenreConnection().entrySet()){
			if (Math.pow(entry.getValue(), 2)* score> 0.005){
				recursiveGenreScoreUpdate(genres, entry.getKey(), score* Math.pow(entry.getValue(), 2), movie);
			}
		}
	}
}
