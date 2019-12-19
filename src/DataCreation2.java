import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import config.Constants;
import core.DBConnection;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.Writer;
import utilities.JsonConverter;

public class DataCreation2{
	
	public static void main(String[] args){
		DBConnection.init();
		createMVGenreFile();
		createMVDirectorFile();
		createMVActorFile();
		createMVWriterFile();
		DBConnection.end();
	}
	
	//	TODO: Create the same but with normalized, meaning 0 or 1 not higher or in between
	//	This creates the default file with genre values
	private static void createMVGenreFile() {
		System.out.println(Instant.now()+ " "+ "Started first part");
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.retrieveMovieValues(movies);
		
		for(Movie movie: movies) {
			double[] values= new double[28];
			double[] previousValues= movie.getValues();
			for(int i= 0; i< 28; i++)	values[i]= previousValues[i];
			movie.setValues(values);
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
//			List<Double> list= new ArrayList<>();
////			list.addAll(Arrays.asList(movie.getValues()));
//			for(double d: movie.getValues())	list.add(d);
			List<Boolean> list= movie.CAndCValues;
			List<Actor> movieActors= movie.getActors();
			for(Actor actor: actors) {
				list.add(movieActors.contains(actor));
//				if(movieActors.contains(actor))	list.add(1.0);
//				else	list.add(0.0);
			}
//			double[] array= new double[list.size()];
//			for(int i= 0; i< list.size(); i++)	array[i]= list.get(i);
//			movie.setValues(array);
			movie.CAndCValues= list;
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
//			List<Double> list= new ArrayList<>();
//			list.addAll(Arrays.asList(movie.getValues()));
//			for(double d: movie.getValues())	list.add(d);
			List<Boolean> list= movie.CAndCValues;
			List<Director> movieDirectors= movie.getDirectors();
			for(Director director: directors) {
				list.add(movieDirectors.contains(director));
//				if(movieDirectors.contains(director))	list.add(1.0);
//				else	list.add(0.0);
			}
//			double[] array= new double[list.size()];
//			for(int i= 0; i< list.size(); i++)	array[i]= list.get(i);
//			movie.setValues(array);
			movie.CAndCValues= list;
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
			List<Boolean> list= movie.CAndCValues;
			List<Writer> movieWriters= movie.getWriters();
			for(Writer writer: writers) {
				list.add(movieWriters.contains(writer));
//				if(movieWriters.contains(writer))	list.add(1.0);
//				else	list.add(0.0);
			}
			movie.CAndCValues= list;
//			double[] array= new double[list.size()];
//			for(int i= 0; i< list.size(); i++)	array[i]= list.get(i);
//			movie.setValues(array);
			movie.setWriters(null);
		}
		
		saveMV(movies);
	}
	
	
	public static void saveMV(List<Movie> movies) {
		try {
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(Constants.movie_values));
			
			for(Movie movie: movies) {
				myWriter.write(movie.getTconst()+ "_");
				for(double d: movie.getValues())	myWriter.write(d+ ",");
				for(Boolean b: movie.CAndCValues) {
					if(b)	myWriter.write("1,");
					else	myWriter.write("0,");
				}
				myWriter.newLine();
			}
			
			myWriter.close();
		} catch (Exception e) {
			
		}
	}
	
	public static void loadMV(List<Movie> movies){
		List<Movie> movieDuplidate= new ArrayList<Movie>(movies);
		try {
			BufferedReader myReader= new BufferedReader(new java.io.FileReader(Constants.movie_values));
			String myRead;
			
			while((myRead= myReader.readLine())!= null) {
				String tconst= myRead.split("_")[0];
				Movie movie= Movie.getMovieByTconst(tconst, movieDuplidate);
				
				String[] valuesString= myRead.split("_")[1].split(",");
				double[] values= new double[28];
				
				for(int i= 0; i< valuesString.length; i++) {
					if(i< 28)	values[i]= Double.parseDouble(valuesString[i]);
					else	movie.CAndCValues.add(valuesString[i].equals("1"));
				}
				
				movie.setValues(values);
				movieDuplidate.remove(movie);
			}
			
			myReader.close();
		}catch (Exception e) {
			
		}
	}
}
