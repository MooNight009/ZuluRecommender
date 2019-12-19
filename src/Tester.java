import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Constants;
import config.Constants.DM;
import core.Cluster;
import core.CollabRecommender;
import core.DBConnection;
import core.EncogNeuralNet;
import core.Engine;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class Tester{
	public static String userId= "2";

	public static void main(String[] args) {
//		Main.askUserForInput();
		DBConnection.init();
	    
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		IO.updateCluster(movies);
		List<Map<Integer, Double>> UV= IO.loadUV(Constants.user_values);
		
//		DBConnection.retrieveMovieValues(movies);

		Movie m1= Movie.getMovieByName(movies, "Ride along", true);
		Movie m2= Movie.getMovieByName(movies, "Skyscraper", true);
		Movie m3= Movie.getMovieByName(movies, "Central Intelligence", true);
		Movie m4= Movie.getMovieByName(movies, "Rampage", true);
		
		List<Movie> likedMovies= new ArrayList<>();
		likedMovies.add(m1);
		likedMovies.add(m2);
		likedMovies.add(m3);
		likedMovies.add(m4);
		
		for(Movie movie: likedMovies)	movie.setRating(1);
		
		//	Method to get map for preferred cluster
		Map<Integer, Integer> clusterScore= Cluster.getClusterScore(likedMovies);
		System.out.println(clusterScore);
		
//		MatrixManipulation.getClosestMovieCluster(MatrixManipulation.sumMovieValues(likedMovies), movies, 3, clusterScore, DM.DM_EuclideanDistance);
//		System.out.println(Arrays.toString(MatrixManipulation.getClosestMovieCluster(MatrixManipulation.sumMovieValues(likedMovies), movies, 3, clusterScore, DM.DM_EuclideanDistance)));
//		MatrixManipulation.getClosestMovieClusterRatingEdition(MatrixManipulation.sumMovieValues(likedMovies), movies, 3, clusterScore, DM.DM_EuclideanDistance);
		System.out.println();
		
		
//		CollabRecommender.getRecommendationsCluster(movies, UV, CollabRecommender.getUserRating(movies, likedMovies), clusterScore, MatrixManipulation.sumMovieValues(likedMovies), 3);
		
		System.out.println(Arrays.toString(Engine.getRecommendation(movies, likedMovies, UV, 6, true)));
//		DBConnection.end();
	}
}


