package utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Constants;
import entity.Movie;

public class IO{

	//	Saves the Movie Values
	public static void saveMV(List<Movie> movies, String location) {
		try {
			BufferedWriter myWriter= new BufferedWriter(new FileWriter(location));
			
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
	
	//	Loads the Movie Value for content based
	public static void loadMV(List<Movie> movies, String location){
		List<Movie> movieDuplidate= new ArrayList<Movie>(movies);
		try {
			BufferedReader myReader= new BufferedReader(new java.io.FileReader(location));
			String myRead;
			while((myRead= myReader.readLine())!= null) {
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
			Constants.vectorSize= movies.get(0).lastPosition;
			myReader.close();
		}catch (Exception e) {
			System.out.println("Error "+ e);
			e.printStackTrace();
		}
	}
	
	public static void loadSVDMV(List<Movie> movies, String location){
		try {
			BufferedReader myReader= new BufferedReader(new FileReader(location));
			int n= 0;
			String myRead;
			while((myRead= myReader.readLine())!= null) {
				String[] valuesStr= myRead.split(",");
				double[] values= new double[valuesStr.length];
				
				for(int i= 0; i< valuesStr.length; i++)	values[i]= Double.parseDouble(valuesStr[i]);
				
				movies.get(n).setValues(values);
				movies.get(n).lastPosition= 301;
				n++;
			}
			Constants.vectorSize= 501;
			myReader.close();
		}catch (Exception e) {
			System.out.println("Error "+ e);
			e.printStackTrace();
		}
	}
	
	public static List<Map<Integer, Double>> loadUV(String location){
		List<Map<Integer, Double>> list= new ArrayList<>();
		
		try {
			BufferedReader myReader= new BufferedReader(new FileReader(location));
			int n= 0;
			String myRead;
			while((myRead= myReader.readLine())!= null) {
				if(myRead.trim().length()== 0)	continue;
				String[] ratings= myRead.split(",");
				Map<Integer, Double> map= new HashMap<>();
				
				for(String str: ratings) {
					double rating= Double.parseDouble(str.split(":")[1])- 2.5;
					if(rating<= 0)	rating-= 0.5;
					map.put(Integer.parseInt(str.split(":")[0]), rating);
				}
				list.add(map);
			}
			
			myReader.close();
		}catch (Exception e) {
			System.out.println("Error "+ e);
			e.printStackTrace();
		}

		return list;
	}
	
	public static void updateCluster(List<Movie> movies) {
		try {
			BufferedReader myReader= new BufferedReader(new FileReader(Constants.movie_cluster));
			int n= 0;
			String myRead;
			while((myRead= myReader.readLine())!= null) {
				movies.get(n++).setCluster(Integer.parseInt(myRead));
			}
			
			myReader.close();
		}catch (Exception e) {
			System.out.println("Error "+ e);
			e.printStackTrace();
		}
	}
}


