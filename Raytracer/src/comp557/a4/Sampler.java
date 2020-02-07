package comp557.a4;

import java.util.Random;

public class Sampler {
	
	Random rng;
	
	public Sampler(long seed) {
		rng = new Random(seed);
	}
	
	public double next() {
		return rng.nextDouble();
	}
	
	public double[] next2D() {
		return new double[] {rng.nextDouble(), rng.nextDouble()};
	}
	
	public int nextInt(int min, int max) {
		return min + rng.nextInt(max);
	}
}
