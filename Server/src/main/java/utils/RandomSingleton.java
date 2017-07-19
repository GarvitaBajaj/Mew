package utils;

import java.util.Random;

public class RandomSingleton extends Random{

private static final long serialVersionUID = 3439913064434358610L;
	
	private static RandomSingleton random = null;
	
	protected RandomSingleton() {	
	}
	
	public static RandomSingleton getInstance() {
	      if(random == null) {
	         random = new RandomSingleton();
	         long seed = System.currentTimeMillis();
	         random.setSeed(seed);
	      }
	      return random;
	}
}
