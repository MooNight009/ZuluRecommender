package config;

public final class Constants{

	public static final String main_path= "D:\\FYP",
			json_path= main_path+ "\\JSON",
			genre_json= json_path+ "\\genre.json",
			movie_json= json_path+ "\\movie.json",
			nn_path= main_path+ "\\nn.nnet",
			rnn_path= main_path+ "\\rnn.nnet",
			movie_cluster= main_path+ "\\movie_cluster.mc",
			movie_values= main_path+ "\\movie_values.mv",
			movie_values_TFIDF= main_path+ "\\movie_values_TFIDF.mv",
			movie_values_SVD= main_path+ "\\movie_values_SVD.mv",
			user_values= main_path+ "\\user_values.uv",
			convolutional_filter= main_path+ "\\convolutional_filter.cf";
	
	public static int vectorSize;
	
	public enum GenreEnum{
		FILM_NOIR(765),
		ACTION(43091),
		WAR(8056),
		HISTORY(11775),
		WESTERN(6898),
		DOCUMENTARY(95745),
		SPORT(4881),
		THRILLER(32775),
		NEWS(1787),
		BIOGRAPHY(14610),
		ADULT(7792),
		MYSTERY(12718),
		COMEDY(88877),
		MUSICAL(8920),
		SHORT(21),
		TALK_SHOW(67),
		ADVENTURE(23404),
		HORROR(26271),
		ROMANCE(38054),
		SCI_FI(10450),
		DRAMA(180494),
		GAME_SHOW(9),
		MUSIC(9400),
		CRIME(29332),
		FANTASY(11498),
		ANIMATION(6384),
		FAMILY(14673),
		REALITY_TV(165);
		
		public final int totalCount;
		
		GenreEnum(int totalCount){
			this.totalCount= totalCount;
		}
	}
	
	public enum DM{
		DM_EuclideanDistance,
		DM_CosineSimilarity;
	}
}
