import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import config.Constants;
import config.Constants.DM;
import config.Constants.GenreEnum;
import core.Cluster;
import core.CollabRecommender;
import core.DBConnection;
import core.Engine;
import core.NeuralNet;
import entity.Actor;
import entity.Director;
import entity.Genre;
import entity.Movie;
import entity.Writer;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class Main{
	public static String userId= "2";
	//	Jimmy= 1
	//	Rock= 2
	
	public static void main(String[] args){
		
//		firstTimeJsonCreation();
//		updateRating();
	    
//		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
//		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
//		IO.loadSVDMV(movies, Constants.movie_values_SVD);
//		IO.updateCluster(movies);
//		
		askUserForInput();

//		nnaskUserForInput();

		DBConnection.end();
	}

	private static void nnaskUserForInput() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.retrieveMovieValues(movies);	//	Updates movies with the values
		DBConnection.updateUserRating(genres, movies, userId);	//	Gets session information based on userId in db
		
		List<double[]> watchedMovieScores1= new ArrayList<>();
		for(Movie movie1: movies)	if(movie1.getRating()!= 99)	watchedMovieScores1.add(movie1.getValues());
		
//		System.out.println(MatrixManipulation.getClosestMovie(NeuralNet.prediction(MatrixManipulation.average(watchedMovieScores1)), movies));
		
		
		Scanner in= new Scanner(System.in);
		String input;
		System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
		while(!(input= in.nextLine()).equals("end")) {
			String[] inputArray;
			int rating;
			
			try {
				inputArray= input.split("_");
				rating= Integer.parseInt(inputArray[1]);
			}
			catch (Exception e) {
				System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
				continue;
			}
			
			
			Movie movie= Movie.getMovieByName(movies, inputArray[0], true);
			movie.setRating(rating); 	//	User watched and liked it

			DBConnection.insertIntoUserMovieRatings(userId, movie.getTconst(), rating, false);
			

			List<double[]> watchedMovieScores= new ArrayList<>();
			for(Movie movie1: movies)	if(movie1.getRating()!= 99)	watchedMovieScores.add(movie1.getValues());
			
//			System.out.println(MatrixManipulation.getClosestMovie(NeuralNet.prediction(MatrixManipulation.average(watchedMovieScores)), movies));


			System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
		}
		
		in.close();
	}
	
	public static void askUserForInput() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		System.out.println("Loaded movies");
		List<Map<Integer, Double>> UV= IO.loadUV(Constants.user_values);
		System.out.println("Loaded user ratings");
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		System.out.println("Loaded movie values");
		IO.updateCluster(movies);
		System.out.println("Loaded movie cluster");
		DBConnection.init();
		
		DBConnection.updateUserRating(genres, movies, userId);	//	Gets session information based on userId in db
		
		List<Movie> likedMovies= new ArrayList<>();
		for(Movie movie: movies)	if(movie.getRating()!= 99)	likedMovies.add(movie);	

		printTopMovies(movies, likedMovies, UV);
		
		Scanner in= new Scanner(System.in);
		String input;
		System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
		while(!(input= in.nextLine()).equals("end")) {
			String[] inputArray;
			int rating;
			
			try {
				inputArray= input.split("_");
				rating= Integer.parseInt(inputArray[1]);
			}
			catch (Exception e) {
				System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
				continue;
			}
			
			
			Movie movie= Movie.getMovieByName(movies, inputArray[0], true);
			System.out.println("Movie genres: "+ movie.getGenres());
			
			
			
			movie.setRating(rating); 	//	User watched and liked it
			likedMovies.add(movie);
			
			
			DBConnection.insertIntoUserMovieRatings(userId, movie.getTconst(), rating, false);
			
			printTopMovies(movies, likedMovies, UV);

			System.out.println("Please write down a movie name followed by '_' and rating [-1 !like, 0 watch later, 1 for like]");
		}
		
		in.close();
	}
	
	//	First run to save values and whatnot into json
	private static void firstTimeJsonCreation() {
		List<Genre> genres= Genre.getGenreList();	//	Create Genre list
		List<Movie> movies= DBConnection.getMovies(genres);	//	Create movie list
//		List<Movie> movies= JsonConverter.readMovies(genres);
		Genre.fillConnection(movies, genres);	//	Update 
		DBConnection.updateRating(movies);
		JsonConverter.saveGenres(genres);
		JsonConverter.saveMovies(movies);
	}
	//	Update number of Votes and average rating
	private static void updateRating() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.updateRating(movies);
		JsonConverter.saveMovies(movies);
	}
	
	private static void printTopMovies(List<Movie> movies, List<Movie> likedMovies, List<Map<Integer, Double>> UV) {
		if (likedMovies.size()== 0) return;
		Map<Integer, Integer> clusterScore= Cluster.getClusterScore(likedMovies);
		MatrixManipulation.getClosestMovieCluster(MatrixManipulation.sumMovieValues(likedMovies), movies, 10, clusterScore, DM.DM_EuclideanDistance, likedMovies, false);
		System.out.println();
		MatrixManipulation.getClosestMovieClusterRatingEdition(MatrixManipulation.sumMovieValues(likedMovies), movies, 10, clusterScore, DM.DM_EuclideanDistance, likedMovies, false);
		System.out.println();
		
		CollabRecommender.getRecommendationsCluster(movies, UV, CollabRecommender.getUserRating(movies, likedMovies), clusterScore, MatrixManipulation.sumMovieValues(likedMovies), 3, likedMovies, false);
	}
}
