package core;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import entity.Movie;

public class Crawler{

	//	Return an array with Desc and Poster src
	public static String[] getDescAndPoster(Movie movie) {
		String[] res= new String[2];
		
		try {
			Document doc= Jsoup.connect("https://www.imdb.com/title/"+ movie.getTconst()+ "/").get();
			
			Elements imgs= doc.select("img");
			for(Element img: imgs) {
				if(img.attr("title").contains("Poster")) {
					res[1]= img.absUrl("src");
					break;
				}
			}
			
			Elements summary= doc.getElementsByClass("summary_text");
			res[0]= summary.text();
			if(res[0].contains("... See full summary »"))	res[0]= res[0].replace("... See full summary »", "...");
			
		}catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
		
		
		return res;
	}
}
