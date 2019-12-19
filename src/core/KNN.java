package core;

import java.util.ArrayList;
import java.util.List;

import org.neuroph.nnet.learning.kmeans.KVector;
import org.neuroph.nnet.learning.knn.KNearestNeighbour;

import entity.Genre;
import entity.Movie;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class KNN{

	public static void main(String[] args) {
		DBConnection.init();
	    
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		DBConnection.retrieveMovieValues(movies);
		
		
		KNearestNeighbour knn= new KNearestNeighbour();
		List<KVector> dataSet= new ArrayList<>();
		for(Movie movie: movies)	dataSet.add(new KVector(movie.getValues()));
		
		knn.setDataSet(dataSet);
		
		Movie movie= Movie.getMovieByName(movies, "San andreas", false);
		KVector[] resu= knn.getKNearestNeighbours(new KVector(movie.getValues()), 29);
		
		System.out.println("Size is "+ resu.length);
		
		for(KVector vector: resu) {
			System.out.println(vector.getIntensity());
			System.out.println(vector.getCluster());
			System.out.println(vector.getDistance());
//			System.out.println(MatrixManipulation.getClosestMovie(vector.getValues(), movies).getName());
//			System.out.println("My distance is "+ MatrixManipulation.getVectorDistance(movie.getValues(), vector.getValues()));
		}
		
		
		DBConnection.end();
	}
}
