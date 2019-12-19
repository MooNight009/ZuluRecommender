import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.Soundbank;

import config.Constants;
import core.DBConnection;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.Writer;
import utilities.JsonConverter;

public class DataCreation3{
	
	public static void main(String[] args){
		DBConnection.init();
//		firstGenreUpdate();
		createMVGenreFile();
		createMVDirectorFile();
		createMVActorFile();
		createMVWriterFile();
		createYearLengthFile();
		DBConnection.end();
	}
	
	private static void firstGenreUpdate() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		Genre.fillConnection(movies, genres);	//	Update 
		
		for(Movie movie: movies) {
			for(Genre genre: genres)	movie.movieGenreScores.put(genre, 0.0);
		}
		
		//	Calculate the values for the genre
		for(Movie movie: movies) {
			for(Genre movieGenre: movie.getGenres())	DataCreation.recursiveGenreScoreUpdate(genres, movieGenre, 1, movie);
		}
		
		for(Movie movie: movies) {
			
			DBConnection.updateMovieWeight(movie, true);
		}
		
//		for(Genre genre: genres)	System.out.println(genre.getGenreConnection());
		
		
	}
	
	//	TODO: Create the same but with normalized, meaning 0 or 1 not higher or in between
	//	This creates the default file with genre values
	private static void createMVGenreFile() {
		System.out.println(Instant.now()+ " "+ "Started first part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.retrieveMovieValues(movies);
		
		for(Movie movie: movies) {
			double[] previousValues= movie.getValues();
			for(int i= 0; i< 28; i++)	if(previousValues[i]> 0.0)	movie.sparseVector.put(i, previousValues[i]);
			movie.lastPosition= 28;
		}
		
		saveMV(movies);
	}
	
	//	This adds actor values to the list
	private static void createMVActorFile() {
		System.out.println(Instant.now()+ " "+ "Started second part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		loadMV(movies);
		List<Actor> actors= DBConnection.getActors(movies);
		
		for(Movie movie: movies) {
			List<Actor> movieActors= movie.getActors();
			for(Actor actor: actors) {
				if(movieActors.contains(actor))	movie.sparseVector.put(movie.lastPosition, 1.0);
				
				movie.lastPosition+= 1;
			}
			movie.setActors(null);
		}
		
		saveMV(movies);
	}
	
	//	This adds actor values to the list
	private static void createMVDirectorFile() {
		System.out.println(Instant.now()+ " "+ "Started third part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		loadMV(movies);
		List<Director> directors= DBConnection.getDirector(movies);
		
		for(Movie movie: movies) {
			List<Director> movieDirectors= movie.getDirectors();
			for(Director director: directors) {
				if(movieDirectors.contains(director))	movie.sparseVector.put(movie.lastPosition, 1.0);
				
				movie.lastPosition+= 1;
			}
			movie.setDirectors(null);
		}
		
		saveMV(movies);
	}
	
	//	This adds actor values to the list
	private static void createMVWriterFile() {
		System.out.println(Instant.now()+ " "+ "Started fourth part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		loadMV(movies);
		List<Writer> writers= DBConnection.getWriters(movies);
		
		for(Movie movie: movies) {
//			List<Double> list= new ArrayList<>();
////			list.addAll(Arrays.asList(movie.getValues()));
//			for(double d: movie.getValues())	list.add(d);
//			List<Boolean> list= movie.CAndCValues;
			List<Writer> movieWriters= movie.getWriters();
			for(Writer writer: writers) {
				if(movieWriters.contains(writer))	movie.sparseVector.put(movie.lastPosition, 1.0);
				
				movie.lastPosition+= 1;
			}
//			movie.CAndCValues= list;
//			double[] array= new double[list.size()];
//			for(int i= 0; i< list.size(); i++)	array[i]= list.get(i);
//			movie.setValues(array);
			movie.setWriters(null);
		}
		
		saveMV(movies);
	}
	
	//	This adds year and length to the list
	private static void createYearLengthFile() {
		System.out.println(Instant.now()+ " "+ "Started fifth part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		loadMV(movies);
		
		DBConnection.updateWithYearAndLength(movies);
		
		for(Movie movie: movies) {
//			System.out.println(movie.getTconst()+ "-"+ movie.getYearOfRelease()+ "-"+ movie.getLength());
//			System.out.println((movie.getYearOfRelease()- 1911.0)/ 108.0);
//			System.out.println((movie.getLength())/ 566.0);
			movie.sparseVector.put(movie.lastPosition, (movie.getYearOfRelease()- 1911.0)/ 108.0);	//	year
			movie.sparseVector.put(movie.lastPosition+ 1, (movie.getLength())/ 566.0);	//	Length
			
			movie.lastPosition+= 2;
		}
		
		
		saveMV(movies);
	}
	
	public static void saveMV(List<Movie> movies) {
		try {
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(Constants.movie_values));
			
			for(Movie movie: movies) {
				myWriter.write(movie.getTconst()+ "_");
				myWriter.write(movie.lastPosition+ "_");
				for(Map.Entry<Integer, Double> entry: movie.sparseVector.entrySet())	myWriter.write(entry.getKey()+ ":"+ entry.getValue()+ ",");
				myWriter.newLine();
			}
			
			myWriter.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public static void loadMV(List<Movie> movies){
		List<Movie> movieDuplidate= new ArrayList<Movie>(movies);
		try {
			BufferedReader myReader= new BufferedReader(new java.io.FileReader(Constants.movie_values));
			String myRead;
			int n= 0;
			while((myRead= myReader.readLine())!= null) {
				n++;
				String tconst= myRead.split("_")[0];
				Movie movie= Movie.getMovieByTconst(tconst, movieDuplidate);
				movie.lastPosition= Integer.parseInt(myRead.split("_")[1]);
				
				if(myRead.split("_").length== 2) {
					continue;
				}
				String[] valuesString= myRead.split("_")[2].split(",");
				Map<Integer, Double> sparseVector= new HashMap<>();
				for(int i= 0; i< valuesString.length; i++) {
					String[] entry= valuesString[i].split(":");
					sparseVector.put(Integer.parseInt(entry[0]), Double.parseDouble(entry[1]));
				}
				
				movie.sparseVector= sparseVector;
				movieDuplidate.remove(movie);
			}
			Constants.vectorSize= movies.get(0).lastPosition- 1;
			myReader.close();
		}catch (Exception e) {
			System.out.println("Error "+ e);
			e.printStackTrace();
		}
	}
}
