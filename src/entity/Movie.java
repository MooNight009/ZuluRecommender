package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.similarity.JaroWinklerDistance;

public class Movie{
	
	private int mlMovieId= 0;

	private String name;
	private String tconst;
	private String mlId= "0";
	private List<Genre> genres= new ArrayList<>();
	private List<Actor> actors= new ArrayList<>();	//	This isn't being used
	private List<Writer> writers= new ArrayList<>();	//	This isn't being used
	private List<Director> directors= new ArrayList<>();	//	This isn't being used
	
	private int cluster;
	
	public Map<Genre, Double> movieGenreScores= new HashMap<>();
	public Map<String, Double> movieCastAndCrewScores= new HashMap<>();
	
	private double[] values= null;
	
	public List<Boolean> CAndCValues= new ArrayList<>();
	
	public Map<Integer, Double> sparseVector= new HashMap<>();
	public int lastPosition;

	private int rating= 99; // Rating the user has set
	private int numberOfVotes;
	private int averageRating;
	private int yearOfRelease;
	private int length;
	private String language= null;
	private boolean wasMovieRecommended;
	
	//	Calculated values
	private double recommendationRating;	
	
	private double actorRecommendationRating;
	private int actorRatingPosition;
	private double genreRecommendationRating;
	private int genreRatingPosition;
	private double genreWriterRecommendationRating;
	private int genreWriterRatingPosition;
	private double genreDirectorRecommendationRating;
	private int genreDirectorRatingPosition;

	public Movie(String name, String tconst){
		this.name= name;
		this.tconst= tconst;
	}

	public List<Genre> getGenres(){
		return genres;
	}

	public void addGenre(Genre genre){
		this.genres.add(genre);
	}

	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name= name;
	}

	public String getTconst(){
		return tconst;
	}

	public void setTconst(String tconst){
		this.tconst= tconst;
	}

	public void addWriter(Writer writer){
		this.writers.add(writer);
	}

	public void addDirector(Director director){
		this.directors.add(director);
	}

	public void addActor(Actor actor){
		this.actors.add(actor);
	}

	public int getRating(){
		return rating;
	}

	public void setRating(int rating){
		this.rating= rating;
	}

	public List<Actor> getActors(){
		return actors;
	}

	public List<Writer> getWriters(){
		return writers;
	}

	public List<Director> getDirectors(){
		return directors;
	}

	public double getRecommendationRating(){
		return recommendationRating;
	}

	public void setRecommendationRating(double recommendationRating){
		this.recommendationRating= recommendationRating;
	}
	
	public boolean isWasMovieRecommended(){
		return wasMovieRecommended;
	}

	public void setWasMovieRecommended(boolean wasMovieRecommended){
		this.wasMovieRecommended= wasMovieRecommended;
	}

	public int getAverageRating(){
		return averageRating;
	}

	public void setAverageRating(int averageRating){
		this.averageRating= averageRating;
	}

	public int getNumberOfVotes(){
		return numberOfVotes;
	}

	public void setNumberOfVotes(int numberOfVotes){
		this.numberOfVotes= numberOfVotes;
	}

	public int getActorRatingPosition(){
		return actorRatingPosition;
	}

	public void setActorRatingPosition(int actorRatingPosition){
		this.actorRatingPosition= actorRatingPosition;
	}

	public int getGenreRatingPosition(){
		return genreRatingPosition;
	}

	public void setGenreRatingPosition(int genreRatingPosition){
		this.genreRatingPosition= genreRatingPosition;
	}

	public int getGenreWriterRatingPosition(){
		return genreWriterRatingPosition;
	}

	public void setGenreWriterRatingPosition(int genreWriterRatingPosition){
		this.genreWriterRatingPosition= genreWriterRatingPosition;
	}

	public int getGenreDirectorRatingPosition(){
		return genreDirectorRatingPosition;
	}

	public void setGenreDirectorRatingPosition(int genreDirectorRatingPosition){
		this.genreDirectorRatingPosition= genreDirectorRatingPosition;
	}

	public double getGenreWriterRecommendationRating(){
		return genreWriterRecommendationRating;
	}

	public void setGenreWriterRecommendationRating(double genreWriterRecommendationRating){
		this.genreWriterRecommendationRating= genreWriterRecommendationRating;
	}

	public double getGenreDirectorRecommendationRating(){
		return genreDirectorRecommendationRating;
	}

	public void setGenreDirectorRecommendationRating(double genreDirectorRecommendationRating){
		this.genreDirectorRecommendationRating= genreDirectorRecommendationRating;
	}

	public double getActorRecommendationRating(){
		return actorRecommendationRating;
	}

	public void setActorRecommendationRating(double actorRecommendationRating){
		this.actorRecommendationRating= actorRecommendationRating;
	}

	public double getGenreRecommendationRating(){
		return genreRecommendationRating;
	}

	public void setGenreRecommendationRating(double genreRecommendationRating){
		this.genreRecommendationRating= genreRecommendationRating;
	}

	public int getMlMovieId(){
		return mlMovieId;
	}

	public void setMlMovieId(int mlMovieId){
		this.mlMovieId= mlMovieId;
	}
	
	public String toString() {
		return this.name;
	}

	public String getMlId(){
		return mlId;
	}

	public void setMlId(String mlId){
		this.mlId= mlId;
	}

	public double[] getValues(){
		return values;
	}

	public void setValues(double[] values){
		this.values= values;
	}

	public void setGenres(List<Genre> genres){
		this.genres= genres;
	}

	public void setActors(List<Actor> actors){
		this.actors= actors;
	}

	public void setDirectors(List<Director> directors){
		this.directors= directors;
	}
	

	public void setWriters(List<Writer> writers){
		this.writers= writers;
	}

	public int getYearOfRelease(){
		return yearOfRelease;
	}

	public void setYearOfRelease(int yearOfRelease){
		this.yearOfRelease= yearOfRelease;
	}

	public int getLength(){
		return length;
	}

	public void setLength(int length){
		this.length= length;
	}

	public int getCluster(){
		return cluster;
	}

	public void setCluster(int cluster){
		this.cluster= cluster;
	}

	public String getLanguage(){
		return language;
	}

	public void setLanguage(String language){
		this.language= language;
	}

	public String getJson(){
		String json= "{";

		json+= "\"name\": \""+ this.name.replaceAll("\"", "")+ "\",";
		json+= "\"tconst\": \""+ this.tconst+ "\",";
		json+= "\"mlId\": \""+ this.mlId+ "\",";
		json+= "\"averageRating\": \""+ this.averageRating+ "\",";
		json+= "\"numberOfVotes\": \""+ this.numberOfVotes+ "\",";
		json+= "\"length\": \""+ this.length+ "\",";
		json+= "\"yearOfRelease\": \""+ this.yearOfRelease+ "\",";
		json+= "\"language\": \""+ this.language+ "\",";
		json+= "\"genres\": [";
		for (Genre genre : this.genres){
			json+= "\"";
			json+= genre.getGenre();
			json+= "\",";
		}

		json+= "]";

		json+= "} ,";
		return json;
	}

	// Returns movie with the given tconst
	public static Movie getMovieByTconst(String tconst, List<Movie> movies){
		for (Movie movie : movies)
			if (movie.getTconst().equals(tconst))
				return movie;
		return null;
	}

	// Returns the movie closest to the given name
	public static Movie getMovieByName(List<Movie> movies, String name, boolean printResult){
		name= name.toLowerCase();
		JaroWinklerDistance jwd= new JaroWinklerDistance();
//		LevenshteinDistance lsd= new LevenshteinDistance();
		Movie selectedMovie= null;
		double distance= 0;
		for (Movie movie : movies){
			String movieName= movie.getName().toLowerCase();
			if(selectedMovie!= null&& Double.compare(jwd.apply(name, movieName), distance)== 0) {
				if(movie.getNumberOfVotes()> selectedMovie.getNumberOfVotes()) {
					selectedMovie= movie;
					distance= jwd.apply(name, movieName);
				}
			}
			else if (Double.compare(jwd.apply(name, movieName), distance)> 0){
				selectedMovie= movie;
				distance= jwd.apply(name, movieName);
			}
		}
		if(printResult)	System.out.println("Found the movie: "+ selectedMovie.getName());
		if(printResult)	System.out.println("Confidence of matching:"+ distance);

		return selectedMovie;
	}
	
	// Returns the movie closest to the given name
	public static Movie getMovieByName(List<Movie> movies, String name, double threshhold){
		JaroWinklerDistance jwd= new JaroWinklerDistance();
		Movie selectedMovie= null;
		double distance= 0;
		
		for (Movie movie : movies){
			String movieName= movie.getName().toLowerCase();
			double movieJWDDistance= jwd.apply(name, movieName);
			
			if(movieJWDDistance< threshhold)	continue;
			int comparisonResult= Double.compare(movieJWDDistance, distance);
			if(selectedMovie!= null&& comparisonResult== 0) {
				if(movie.getNumberOfVotes()> selectedMovie.getNumberOfVotes()) {
					selectedMovie= movie;
					distance= movieJWDDistance;
				}
			}
			else if (comparisonResult> 0){
				selectedMovie= movie;
				distance= jwd.apply(name, movieName);
			}
		}
		
		return selectedMovie;
	}
	
	//	Returns movie with the given movieId
	public static Movie getMovieById(int id, List<Movie> movies) {
		for(Movie movie: movies)
			if(movie.getMlId().equals(id+ ""))
				return movie;
		return null;
	}
	
	//	Returns movie index with the given movieId
	public static int getMovieIndexById(int id, List<Movie> movies) {
		for(int i= 0; i< movies.size(); i++)
			if(movies.get(i).getMlId().equals(id+ ""))
				return i;
		return -1;
	}
}
