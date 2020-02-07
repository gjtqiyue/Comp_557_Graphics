package comp557.a4;

import javax.vecmath.Vector3d;

public class Reflection {
	
	public double reflectance;
	
	public Vector3d albedo;
	
	public Reflection(Vector3d a, double r) {
		reflectance = r;
		albedo = a;
	}
	
	public Reflection() {
		
	}
}
