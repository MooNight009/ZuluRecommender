import java.io.BufferedReader;
import java.io.FileReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DBConnection;
import entity.Genre;
import entity.Movie;
import utilities.JsonConverter;

public class DataCreationUpdated{

	public static void main(String[] args){
		DBConnection.init();
//		updateMovieWeightTableGenreEdition();
		updateMLIdMovies();
//		getMLData();
		DBConnection.end();
	}
	
	private static void updateMLIdMovies() {
		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		List<Movie> duplicateOriginal= new ArrayList<Movie>(movies);
		System.out.println(movies.size());
		for(int i= 0; i< movies.size(); i++) {
			if(!movies.get(i).getMlId().equals("0")) {
				movies.remove(i);
				i--;
			}
		}
		System.out.println(movies.size());
		
		Map<String, String> mlData= getMLData();
		int s= 0;
		
		System.out.println(mlData.size());
		
		for(Map.Entry<String, String> entry: mlData.entrySet()) {
			if(s++% 10000== 0) {
				System.out.println(Instant.now()+ "-"+ s);
			}
			Movie movie= Movie.getMovieByName(movies, entry.getValue(), 0.7);
			if(movie== null) {
//				System.out.println("Failed "+ entry.getValue()+ "-"+ entry.getKey());
				continue;
			}
			movie.setMlId(entry.getKey());
			movies.remove(movie);
		}
		
		int n= 0;
		for(Movie movie: duplicateOriginal) if(!movie.getMlId().equals("0"))	n++;
		System.out.println("Found movies "+ n);
		
//		DBConnection.updateMLIDInMovies(duplicateOriginal);
		
		JsonConverter.saveMovies(duplicateOriginal);
	}
	

	private static Map<String, String> getMLData(){
		Map<String, String> mlData= new HashMap<>();
		
		try{
			BufferedReader myReader= new BufferedReader(new FileReader("D:\\FYP\\ml-20m\\movies.csv"));
			
			String myRead= myReader.readLine();
			while((myRead= myReader.readLine())!= null) {
				String[] data= myRead.split(",");
				if(Integer.parseInt(data[0])< 131262) {
//					System.out.println("passed "+ data[0]);
					continue;
				}
				mlData.put(data[0], data[1].split("\\(")[0]);
			}
			
			myReader.close();
		}catch (Exception e) {
			System.out.println(e);
		}
		
		System.out.println(mlData.size());
		return mlData;
	}
}
