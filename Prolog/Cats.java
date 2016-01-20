public class Cats{
	public static String getCats(String test, int count){
		return "Cats! So many CATS! and " + test+"  "+count;
	}
	public static String getTiger(){
		return "tiger";
	}
	
	public static void main(String[] args){
		System.out.println(Cats.getCats("cats", 125));
	}
}