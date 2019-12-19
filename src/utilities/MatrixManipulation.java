package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Constants;
import config.Constants.DM;
import entity.Movie;

public class MatrixManipulation{

	public static double[] convlutionalFilter(List<double[]> ds, double[][] filter){
		List<double[]> dsDuplicate= new ArrayList<>(ds);
		int columnCount= dsDuplicate.get(0).length;
		while (dsDuplicate.size()> 1){ // Until we got only one
			for (int i= 0; i< dsDuplicate.size()- 1; i++){ // repeat for all rows
				double[] mainRow= dsDuplicate.get(i);
				double[] secRow= dsDuplicate.get(i+ 1);
				double[] newRow= new double[mainRow.length];

				for (int j= 0; j< columnCount; j++){ // for all columns
					double total= 0.0;
					// TODO: Make this dynamic
					if (j!= 0)
						total+= filter[0][0]* mainRow[j- 1];
					if (j!= 0)
						total+= filter[1][0]* secRow[j- 1];

					total+= filter[0][1]* mainRow[j];
					total+= filter[1][1]* secRow[j];

					if (j+ 1!= columnCount)
						total+= filter[0][2]* mainRow[j+ 1];
					if (j+ 1!= columnCount)
						total+= filter[1][2]* secRow[j+ 1];

					total/= 6;

					newRow[j]= total;
					// dsDuplicate.get(i)[j]= total;
				}
				dsDuplicate.remove(i);
				dsDuplicate.add(i, newRow);
			}
			dsDuplicate.remove(dsDuplicate.size()- 1);
		}

		return dsDuplicate.get(0);
	}

	public static double[] convlutionalFilter(double[] d, double[][] filter){
		double[] newVector= d;

		for (int j= 0; j< d.length; j++){ // for all columns
			double total= 0.0;
			// TODO: Make this dynamic
			if (j!= 0)
				total+= filter[0][0]* d[j- 1];

			total+= filter[0][1]* d[j];

			if (j+ 1!= d.length)
				total+= filter[0][2]* d[j+ 1];

			total/= 6;

			newVector[j]= total;
		}

		return newVector;
	}

	// Returns the average of the given arrays
	public static double[] average(List<double[]> ds){
		int arrayLength= ds.get(0).length;
		double[] output= new double[arrayLength];

		for (double[] d : ds){
			for (int i= 0; i< arrayLength; i++){
				output[i]+= d[i];
			}
		}

		for (int i= 0; i< arrayLength; i++){
			output[i]/= ds.size();
		}

		return output;
	}

	public static Map<Integer, Double> averageSparse(List<double[]> ds){
		int arrayLength= ds.get(0).length;
		double[] output= new double[arrayLength];

		for (double[] d : ds){
			for (int i= 0; i< arrayLength; i++){
				output[i]+= d[i];
			}
		}

		for (int i= 0; i< arrayLength; i++){
			output[i]/= ds.size();
		}

		Map<Integer, Double> map= new HashMap<>();
		for (int i= 0; i< output.length; i++)
			if (output[i]!= 0.0)
				map.put(i, output[i]);

		return map;
	}

	// Returns the sum of the given arrays
	public static double[] sum(List<double[]> ds){
		int arrayLength= ds.get(0).length;
		double[] output= new double[arrayLength];

		for (double[] d : ds){
			for (int i= 0; i< arrayLength; i++){
				output[i]+= d[i];
			}
		}

		return output;
	}

	// Returns the sum of the given movies
	public static double[] sumMovieValues(List<Movie> movies){
		int arrayLength= movies.get(0).getValues().length;
		double[] output= new double[arrayLength];

		for (Movie movie : movies){
			double[] d= movie.getValues();
			int rating= movie.getRating();
			for (int i= 0; i< arrayLength; i++){
				output[i]+= d[i]* rating;
			}
		}

		return output;
	}

	// Turns sparseVector double[]
	public static List<double[]> sparseVectorListToVectorList(List<Map<Integer, Double>> sparseVectors,
			int lastPosition){
		List<double[]> vectors= new ArrayList<>();

		for (Map<Integer, Double> sparseVector : sparseVectors){
			double[] vector= new double[lastPosition- 1];
			for (int i= 0; i< vector.length; i++)
				if (sparseVector.containsKey(i))
					vector[i]= sparseVector.get(i);
			vectors.add(vector);
		}

		return vectors;
	}

	public static double[] sparseVectorToVector(Map<Integer, Double> sparseVector, int lastPosition){
		double[] vector= new double[lastPosition- 1];
		for (int i= 0; i< vector.length; i++)
			if (sparseVector.containsKey(i))
				vector[i]= sparseVector.get(i);

		return vector;
	}

	public static Map<Integer, Double> vectorToSparseVector(double[] vector){
		Map<Integer, Double> map= new HashMap<>();

		for (int i= 0; i< vector.length; i++)
			if (vector[i]!= 0)
				map.put(i, vector[i]);

		return map;
	}

	// Makes all the values in the vector negative
	public static double[] negativify(double[] vector){

		for (int i= 0; i< vector.length; i++){
			if (vector[i]> 0)
				vector[i]= -1* vector[i];
		}

		return vector;
	}

	public static Map<Integer, Double> negativify(Map<Integer, Double> sparseVector){

		for (Map.Entry<Integer, Double> entry : sparseVector.entrySet()){
			if (entry.getValue()> 0)
				entry.setValue(entry.getValue()* -1);
		}

		return sparseVector;
	}

	// Makes all the values in the vector positive
	public static double[] positivify(double[] vector){

		for (int i= 0; i< vector.length; i++){
			if (vector[i]< 0)
				vector[i]= -1* vector[i];
		}

		return vector;
	}

	// Returns the movie with the closest vector to the given vector
	public static Movie[] getClosestMovie(double[] vector, List<Movie> movies, int k, DM distanceMeasure){

		Movie[] closestMovies= new Movie[k];
		double[] distances= new double[k];
		for (int i= 0; i< distances.length; i++)
			distances[i]= Double.MAX_VALUE;

		for (Movie movie : movies){
			if (movie.getRating()!= 99)
				continue;
			double movieDistance= getVectorDistance(vector, movie.getValues(), distanceMeasure);
			int n= 0;
			for (int i= 1; i< distances.length; i++)
				if (distances[i]> distances[n])
					n= i;

			if (movieDistance< distances[n]){
				distances[n]= movieDistance;
				closestMovies[n]= movie;
			}
		}

		for (int i= 0; i< distances.length- 1; i++){
			for (int j= i+ 1; j< distances.length; j++){
				if (distances[i]> distances[j]){
					double tmpDistance= distances[i];
					distances[i]= distances[j];
					distances[j]= tmpDistance;

					Movie tmpClosestMovie= closestMovies[i];
					closestMovies[i]= closestMovies[j];
					closestMovies[j]= tmpClosestMovie;
				}
			}
		}

		System.out.println(Arrays.toString(closestMovies));
		System.out.println(Arrays.toString(distances));

		return closestMovies;
	}

	// Returns the movie with the closest vector to the given vector and clusters
	public static Movie[] getClosestMovieCluster(double[] vector, List<Movie> movies, int k,
			Map<Integer, Integer> clusterScore, DM distanceMeasure, List<Movie> likedMovies, boolean USBased){

		Movie[] closestMovies= new Movie[k];
		double[] distances= new double[k];
		for (int i= 0; i< distances.length; i++)
			distances[i]= Double.MAX_VALUE;

		Movie[] closestMoviesCluster= new Movie[k];
		double[] distancesCluster= new double[k];
		for (int i= 0; i< distancesCluster.length; i++)
			distancesCluster[i]= Double.MAX_VALUE;

		for (Movie movie : movies){
			if (movie.getRating()!= 99|| likedMovies.contains(movie)|| (USBased&& !movie.getLanguage().equals("US")))
				continue;
			double movieDistance= getVectorDistance(vector, movie.getValues(), distanceMeasure);

			// Finding farthest distance in normal
			int n= 0;
			for (int i= 1; i< distances.length; i++)
				if (distances[i]> distances[n])
					n= i;

			if (movieDistance< distances[n]){
				distances[n]= movieDistance;
				closestMovies[n]= movie;
			}

			// Finding farthest distance in clusterBased
			double newDistance= movieDistance;
			if (clusterScore.containsKey(movie.getCluster()))
				newDistance/= clusterScore.get(movie.getCluster());

			n= 0;
			for (int i= 1; i< distancesCluster.length; i++)
				if (distancesCluster[i]> distancesCluster[n])
					n= i;

			if (newDistance< distancesCluster[n]){
				distancesCluster[n]= newDistance;
				closestMoviesCluster[n]= movie;
			}
		}

		for (int i= 0; i< distances.length- 1; i++){
			for (int j= i+ 1; j< distances.length; j++){
				if (distances[i]> distances[j]){
					double tmpDistance= distances[i];
					distances[i]= distances[j];
					distances[j]= tmpDistance;

					Movie tmpClosestMovie= closestMovies[i];
					closestMovies[i]= closestMovies[j];
					closestMovies[j]= tmpClosestMovie;
				}
			}
		}
		for (int i= 0; i< distancesCluster.length- 1; i++){
			for (int j= i+ 1; j< distancesCluster.length; j++){
				if (distancesCluster[i]> distancesCluster[j]){
					double tmpDistance= distancesCluster[i];
					distancesCluster[i]= distancesCluster[j];
					distancesCluster[j]= tmpDistance;

					Movie tmpClosestMovie= closestMoviesCluster[i];
					closestMoviesCluster[i]= closestMoviesCluster[j];
					closestMoviesCluster[j]= tmpClosestMovie;
				}
			}
		}

//		System.out.println(Arrays.toString(closestMovies));
//		System.out.println(Arrays.toString(distances));
//		System.out.println(Arrays.toString(closestMoviesCluster));
//		System.out.println(Arrays.toString(distancesCluster));
		// for(Movie movie: closestMoviesCluster) System.out.println(movie);

		return closestMovies;
	}

	// Returns the movie with the closest vector to the given vector and clusters
	// and adds movie score
	public static Movie[] getClosestMovieClusterRatingEdition(double[] vector, List<Movie> movies, int k,
			Map<Integer, Integer> clusterScore, DM distanceMeasure, List<Movie> likedMovies, boolean USBased){

		Movie[] closestMovies= new Movie[k];
		double[] distances= new double[k];
		for (int i= 0; i< distances.length; i++)
			distances[i]= Double.MAX_VALUE;

		Movie[] closestMoviesCluster= new Movie[k];
		double[] distancesCluster= new double[k];
		for (int i= 0; i< distancesCluster.length; i++)
			distancesCluster[i]= Double.MAX_VALUE;

		for (Movie movie : movies){
			if (movie.getRating()!= 99|| likedMovies.contains(movie)|| (USBased&& !movie.getLanguage().equals("US")))
				continue;
			double movieDistance= getVectorDistance(vector, movie.getValues(), distanceMeasure)
					* Math.sqrt((10/ (double) movie.getAverageRating()));

			// Finding farthest distance in normal
			int n= 0;
			for (int i= 1; i< distances.length; i++)
				if (distances[i]> distances[n])
					n= i;

			if (movieDistance< distances[n]){
				distances[n]= movieDistance;
				closestMovies[n]= movie;
			}

			// Finding farthest distance in clusterBased
			double newDistance= movieDistance;
			if (clusterScore.containsKey(movie.getCluster()))
				newDistance/= clusterScore.get(movie.getCluster());

			n= 0;
			for (int i= 1; i< distancesCluster.length; i++)
				if (distancesCluster[i]> distancesCluster[n])
					n= i;

			if (newDistance< distancesCluster[n]){
				distancesCluster[n]= newDistance;
				closestMoviesCluster[n]= movie;
			}
		}

		for (int i= 0; i< distances.length- 1; i++){
			for (int j= i+ 1; j< distances.length; j++){
				if (distances[i]> distances[j]){
					double tmpDistance= distances[i];
					distances[i]= distances[j];
					distances[j]= tmpDistance;

					Movie tmpClosestMovie= closestMovies[i];
					closestMovies[i]= closestMovies[j];
					closestMovies[j]= tmpClosestMovie;
				}
			}
		}
		for (int i= 0; i< distancesCluster.length- 1; i++){
			for (int j= i+ 1; j< distancesCluster.length; j++){
				if (distancesCluster[i]> distancesCluster[j]){
					double tmpDistance= distancesCluster[i];
					distancesCluster[i]= distancesCluster[j];
					distancesCluster[j]= tmpDistance;

					Movie tmpClosestMovie= closestMoviesCluster[i];
					closestMoviesCluster[i]= closestMoviesCluster[j];
					closestMoviesCluster[j]= tmpClosestMovie;
				}
			}
		}

//		System.out.println(Arrays.toString(closestMovies));
//		System.out.println(Arrays.toString(distances));
//		System.out.println(Arrays.toString(closestMoviesCluster));
//		System.out.println(Arrays.toString(distancesCluster));
		// for(Movie movie: closestMoviesCluster) System.out.println(movie);

		return closestMovies;
	}

	// Returns the movie with the closest vector to the given vector
	public static Movie[] getClosestMovieSpareToVector(double[] vector, List<Movie> movies, int k, DM distanceMeasure){

		Movie[] closestMovies= new Movie[k];
		double[] distances= new double[k];
		for (int i= 0; i< distances.length; i++)
			distances[i]= Double.MAX_VALUE;

		for (Movie movie : movies){
			if (movie.getRating()!= 99)
				continue;
			double movieDistance= getVectorDistance(vector,
					sparseVectorToVector(movie.sparseVector, movie.lastPosition), distanceMeasure);
			int n= 0;
			for (int i= 1; i< distances.length; i++)
				if (distances[i]> distances[n])
					n= i;

			if (movieDistance< distances[n]){
				distances[n]= movieDistance;
				closestMovies[n]= movie;
			}
		}

		for (int i= 0; i< distances.length- 1; i++){
			for (int j= i+ 1; j< distances.length; j++){
				if (distances[i]> distances[j]){
					double tmpDistance= distances[i];
					distances[i]= distances[j];
					distances[j]= tmpDistance;

					Movie tmpClosestMovie= closestMovies[i];
					closestMovies[i]= closestMovies[j];
					closestMovies[j]= tmpClosestMovie;
				}
			}
		}

		// System.out.println(Arrays.toString(closestMovies));
		// System.out.println(Arrays.toString(distances));

		return closestMovies;
	}

	public static double getVectorDistance(double[] v1, double[] v2, DM distanceMeasure){
		double distance= 0.0;

		switch (distanceMeasure){
		case DM_EuclideanDistance:
			for (int i= 0; i< v1.length; i++){
				distance+= Math.pow(v1[i]- v2[i], 2);
			}
			distance= Math.sqrt(distance);
			break;

		case DM_CosineSimilarity:
			double top= 0;
			double botA= 0;
			double botB= 0;
			
			for (int i= 0; i< v1.length; i++){
				top+= (v1[i]* v2[i]);
			}
			
			for (int i= 0; i< v2.length; i++){
				botA+= (v1[i]* v1[i]);
			}
			botA= Math.sqrt(botA);
			
			for (int i= 0; i< v1.length; i++){
				botB+= (v2[i]* v2[i]);
			}
			botB= Math.sqrt(botB);
			
			distance= top/ (botA* botB);
			
			break;
		}

		return distance;
	}

	// Returns the movie with the closest vector to the given vector
	public static Movie[] getClosestMovieSparseVector(Map<Integer, Double> sparseVector, List<Movie> movies, int k,
			Constants.DM distanceMeasure){

		Movie[] closestMovies= new Movie[k];
		double[] distances= new double[k];
		for (int i= 0; i< distances.length; i++)
			distances[i]= Double.MAX_VALUE;

		for (Movie movie : movies){
			if (movie.getRating()!= 99)
				continue;
			double movieDistance= getVectorDistanceSparseVector(sparseVector, movie.sparseVector, distanceMeasure);

			int n= 0;
			for (int i= 1; i< distances.length; i++)
				if (distances[i]> distances[n])
					n= i;

			if (movieDistance< distances[n]){
				distances[n]= movieDistance;
				closestMovies[n]= movie;
			}
		}

		for (int i= 0; i< distances.length- 1; i++){
			for (int j= i+ 1; j< distances.length; j++){
				if (distances[i]> distances[j]){
					double tmpDistance= distances[i];
					distances[i]= distances[j];
					distances[j]= tmpDistance;

					Movie tmpClosestMovie= closestMovies[i];
					closestMovies[i]= closestMovies[j];
					closestMovies[j]= tmpClosestMovie;
				}
			}
		}

		// System.out.println(Arrays.toString(closestMovies));
		// System.out.println(Arrays.toString(distances));

		return closestMovies;
	}

	public static double getVectorDistanceSparseVector(Map<Integer, Double> sv1, Map<Integer, Double> sv2,
			Constants.DM distanceMeasure){
		double distance= 0.0;

		switch (distanceMeasure){
		case DM_CosineSimilarity:
			double xDOTy= 0.0;
			for (Map.Entry<Integer, Double> entry : sv1.entrySet()){
				if (sv2.containsKey(entry.getKey()))
					xDOTy+= entry.getValue()* sv2.get(entry.getKey());
			}

			double xDOTx= 0.0;
			for (Map.Entry<Integer, Double> entry : sv1.entrySet()){
				xDOTx+= entry.getValue()* entry.getValue();
			}

			double yDOTy= 0.0;
			for (Map.Entry<Integer, Double> entry : sv2.entrySet()){
				yDOTy+= entry.getValue()* entry.getValue();
			}

			distance= xDOTy/ (Math.sqrt(xDOTx)* Math.sqrt(yDOTy));
			break;

		case DM_EuclideanDistance:
			for (Map.Entry<Integer, Double> entry : sv1.entrySet()){
				if (sv2.containsKey(entry.getKey()))
					distance+= Math.pow(entry.getValue()- sv2.get(entry.getKey()), 2);
				else
					distance+= Math.pow(entry.getValue(), 2);
			}

			for (Map.Entry<Integer, Double> entry : sv1.entrySet()){
				if (!sv1.containsKey(entry.getKey()))
					distance+= Math.pow(entry.getValue(), 2);
			}

			distance= Math.sqrt(distance);

			break;
		}

		return distance;
	}
}
