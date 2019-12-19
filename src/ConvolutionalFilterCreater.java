import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.sound.midi.Soundbank;

import config.Constants;
import config.Constants.DM;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class ConvolutionalFilterCreater{
	public static void main(String[] args){
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
//		IO.loadMV(movies, Constants.movie_values);
		IO.loadSVDMV(movies, Constants.movie_values_SVD);
		
		List<double[][]> filters= new ConvolutionalFilterCreater(3).getList();
		
		
		Movie m1, m2, m3, m4;
		
		m1= Movie.getMovieByName(movies, "Thor: Rangnarok", true);
//		m1.setValues(MatrixManipulation.sparseVectorToVector(m1.sparseVector, m1.lastPosition));
		m2= Movie.getMovieByName(movies, "Avengers: Infinity War", true);
//		m2.setValues(MatrixManipulation.sparseVectorToVector(m2.sparseVector, m2.lastPosition));
		m3= Movie.getMovieByName(movies, "Iron Man 2", true);
//		m3.setValues(MatrixManipulation.sparseVectorToVector(m3.sparseVector, m3.lastPosition));
		m4= Movie.getMovieByName(movies, "Iron Man 3", true);
//		m4.setValues(MatrixManipulation.sparseVectorToVector(m4.sparseVector, m4.lastPosition));
		
		List<double[]> input1= new ArrayList<>();
		input1.add(m1.getValues());
		input1.add(m2.getValues());
		input1.add(m3.getValues());
		input1.add(m4.getValues());
		
		List<double[]> input2= new ArrayList<>();
		input2.add(m3.getValues());
		input2.add(m4.getValues());
		
		System.out.println(Arrays.toString(m1.getValues()));
		System.out.println(Arrays.toString(m2.getValues()));
		int round= 0;
		double[][] bestFilter= null;
		double bestDistance= Double.MAX_VALUE;
		System.out.println(filters.size());
		
		Random r= new Random();
//		for(int i=0; i< 2; i++) {
		while(filters.size()> 0) {
			int n= r.nextInt(filters.size());
			double[][] filter= filters.get(n);
			
			
			
			
			
			double distance= 0.0;
//			double[] output2= MatrixManipulation.convlutionalFilter(input2, filter);
//			distance+= MatrixManipulation.getVectorDistance(output2, input1.get(0), DM.DM_EuclideanDistance);
//			distance+= MatrixManipulation.getVectorDistance(output2, input2.get(1), DM.DM_EuclideanDistance);
//			
			
			double[] output1= MatrixManipulation.convlutionalFilter(input1, filter);
			distance+= MatrixManipulation.getVectorDistance(output1, input1.get(0), DM.DM_EuclideanDistance);
			distance+= MatrixManipulation.getVectorDistance(output1, input1.get(1), DM.DM_EuclideanDistance);
			distance+= MatrixManipulation.getVectorDistance(output1, input1.get(2), DM.DM_EuclideanDistance);
			distance+= MatrixManipulation.getVectorDistance(output1, input1.get(3), DM.DM_EuclideanDistance);
			
			
			
			
			if(distance< bestDistance) {
				bestDistance= distance;
				bestFilter= filter;
			}
			if(distance== bestDistance&& distance== 0) {
				System.out.println("We got two of the same "+ distance);
//				System.out.println(Arrays.toString(output1));
//				System.out.println(Arrays.toString(input1.get(0)));
			}
			
			if(round++%1000== 0) {
				System.out.println("Distance "+ bestDistance+ " for the following filter");
				System.out.println(Arrays.toString(bestFilter[0])+ "-"+ Arrays.toString(bestFilter[1]));
			}
			filters.remove(n);
			
			if(filters.size()== 0) {
				Movie[] closestMovies= MatrixManipulation.getClosestMovie(output1, movies, 10, DM.DM_EuclideanDistance);
				System.out.println(Arrays.toString(closestMovies));
				
//				Movie[] closestMovies1= MatrixManipulation.getClosestMovie(output2, movies, 10, DM.DM_EuclideanDistance);
//				System.out.println(Arrays.toString(closestMovies1));
			}
		}
		
		

	}

	private List<double[][]> list= new ArrayList<>();
	private int size;

	public ConvolutionalFilterCreater(int size){
		this.size= size;
		Object[] chars= { 0.0, 1.0, 2.0, 3.0};
		PermuteCallback callback= new PermuteCallback(){

			@Override
			public void handle(Object[] snapshot){
				double[][] array= new double[2][size];
				for (int i= 0; i< snapshot.length; i++){
					int n= 0;
					int t= i;
					while (t- size>= 0){
						n++;
						t-= size;
					}
					array[n][t]= (double)snapshot[i];
					// System.out.print(snapshot[i]);
				}
				list.add(array);
			}
		};

		permute(chars, 2* size, callback);
//		for (double[][] array : list){
//			for (double[] row : array)
//				System.out.print(Arrays.toString(row)+ ",");
//			System.out.println();
//		}
//		System.out.println(list.size());
	}

	public List<double[][]> getList(){
		return list;
	}

	public void setList(List<double[][]> list){
		this.list= list;
	}

	public int getSize(){
		return size;
	}

	public void setSize(int size){
		this.size= size;
	}

	private void permute(Object[] a, int k, PermuteCallback callback){
		int n= a.length;

		int[] indexes= new int[k];
		int total= (int) Math.pow(n, k);

		Object[] snapshot= new Object[k];
		while (total--> 0){
			for (int i= 0; i< k; i++){
				snapshot[i]= a[indexes[i]];
			}
			callback.handle(snapshot);

			for (int i= 0; i< k; i++){
				if (indexes[i]>= n- 1){
					indexes[i]= 0;
				} else{
					indexes[i]++;
					break;
				}
			}
		}
	}

	public static interface PermuteCallback{
		public void handle(Object[] snapshot);
	};
}
