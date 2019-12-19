package core;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import config.Constants.DM;
import entity.Movie;
import utilities.MatrixManipulation;

public class EuclideanDistanceTester{
	public static void testOneForOneSparse(Map<Map<Integer, Double>, Map<Integer, Double>> data, List<Movie> movies){
		System.out.println(Instant.now()+ " Data size "+ data.size());
		long time= System.nanoTime();
		int correctGuesses= 0;

		for (Map.Entry<Map<Integer, Double>, Map<Integer, Double>> entry : data.entrySet()){
			Movie[] closestMovies= MatrixManipulation.getClosestMovieSparseVector(entry.getKey(), movies, 10,
					DM.DM_EuclideanDistance);

			for (Movie movie : closestMovies){
				if (MatrixManipulation.getVectorDistanceSparseVector(entry.getValue(), movie.sparseVector,
						DM.DM_EuclideanDistance)== 0.0){
					// System.out.println("Found movie");
					correctGuesses++;
					break;
				}
			}
		}

		System.out.println(Instant.now()+ " Correct guesses "+ correctGuesses);
		System.out.println("It took "+ (System.nanoTime()- time));
	}

	public static void testOneForOne(Map<double[], double[]> data, List<Movie> movies){
		int correctGuesses= 0;

		for (Map.Entry<double[], double[]> entry : data.entrySet()){
			Movie[] closestMovies= MatrixManipulation.getClosestMovie(entry.getKey(), movies, 10,
					DM.DM_EuclideanDistance);

			for (Movie movie : closestMovies){
				try{
					if (MatrixManipulation.getVectorDistance(entry.getValue(), movie.getValues(),
							DM.DM_EuclideanDistance)== 0.0){
						// System.out.println("Found movie");
						correctGuesses++;
						break;
					}
				} catch (Exception e){
					e.printStackTrace();
					// System.out.println(Arrays.toString(entry.getKey()));
					// System.out.println(Arrays.toString(closestMovies));
					// System.out.println(correctGuesses);
					System.out.println("This one failed with guesses "+ correctGuesses);
					return;
					// System.out.println(movie.getValues());
				}
			}
		}

		System.out.println(Instant.now()+ " Correct guesses "+ correctGuesses);
	}

	public static double testDistance(List<double[]> input, List<double[]> output, List<Movie> movies){
		double distance= 0;
		for (int i= 0; i< input.size(); i++){
//			if (i== 2){
//				System.out.println(Arrays.toString(input.get(i)));
//				System.out.println(Arrays.toString(output.get(i)));
//			}
			distance+= MatrixManipulation.getVectorDistance(input.get(i), output.get(i), DM.DM_EuclideanDistance);
		}

//		System.out.println(Instant.now()+ " Correct guesses "+ distance);
		return distance;
	}

	static int correctGuesses= 0;

	public static void testOneForOneParallel(Map<Map<Integer, Double>, Map<Integer, Double>> data, List<Movie> movies){
		System.out.println(Instant.now()+ " Data size "+ data.size());
		long time= System.nanoTime();
		ParallelTasks tasks= new ParallelTasks();
		List<Map<Integer, Double>> key= new ArrayList<>(data.keySet());
		List<Map<Integer, Double>> value= new ArrayList<>(data.values());
		data= null;
		int cores= Runtime.getRuntime().availableProcessors();

		for (int i= 0; i< cores; i++){
			final int core= i;
			Runnable task= new Runnable(){
				@Override
				public void run(){
					for (int j= core; j< key.size(); j+= core+ 1){
						Movie[] closestMovies= MatrixManipulation.getClosestMovieSparseVector(key.get(j), movies, 10,
								DM.DM_EuclideanDistance);

						for (Movie movie : closestMovies){
							if (MatrixManipulation.getVectorDistanceSparseVector(value.get(j), movie.sparseVector,
									DM.DM_EuclideanDistance)== 0.0){
								// System.out.println("Found movie");
								correctGuesses++;
								break;
							}
						}
					}
				}
			};

			tasks.add(task);
		}

		try{
			System.out.println(Instant.now()+ " Started task");
			tasks.go();
		} catch (InterruptedException e){
			e.printStackTrace();
		}

		System.out.println(Instant.now()+ " Correct guesses "+ correctGuesses);
		System.out.println("It took "+ (System.nanoTime()- time));
	}

}
