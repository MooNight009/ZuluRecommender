import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import config.Constants;
import config.Constants.DM;
import entity.Genre;
import entity.Movie;
import utilities.IO;
import utilities.JsonConverter;
import utilities.MatrixManipulation;

public class TestEnv{

	public static void main(String[] args){
//		List<Map<Integer, Double>> UV= IO.loadUV(Constants.user_values);
		
		int n= 0;
		int s= 0;
		

		List<Genre> genres= JsonConverter.readGenres();	//	Reads the genres from json
		List<Movie> movies= JsonConverter.readMovies(genres);	//	Reads the movies from json
		for(int i= 0; i< movies.size(); i++)	if(movies.get(i).getName().length()> n) {
			n= movies.get(i).getName().length();
			s= i;
		}
		
		System.out.println(n);
		System.out.println(movies.get(s).getName());
	}
	
}
