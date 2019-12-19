package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Writer {

	private String nconst;
	private List<Movie> movies= new ArrayList<>();

	private int movieCount= 0;

	public Map<Genre, Double> movieGenreScores= new HashMap<>();

	public Writer(String nconst){
		this.nconst= nconst;
	}

	public String getNconst(){
		return nconst;
	}

	public void setNconst(String nconst){
		this.nconst= nconst;
	}

	public List<Movie> getMovies(){
		return movies;
	}

	public void setMovies(List<Movie> movies){
		this.movies= movies;
	}

	public void addMovie(Movie movie){
		this.movies.add(movie);
	}

	public int getMovieCount(){
		return movieCount;
	}

	public void setMovieCount(int movieCount){
		this.movieCount= movieCount;
	}

}
