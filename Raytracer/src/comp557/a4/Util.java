package comp557.a4;

import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3d;

public class Util {
	
	public static final double PI = 3.14159265358979323846;
	public static final double INV_PI = 0.31830988618379067154;
	public static final double INV_TWOPI = 0.15915494309189533577;
	public static final double INV_FOURPI = 0.07957747154594766788;
	
	public static double clamp(double x, double min, double max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	public static double clamp(double x, int min, int max) {
		if (x < min) return min;
		if (x > max) return max;
		return x;
	}
	
	public static Color4f componentMultiplication4f( Tuple4f w, Tuple4f v ) {
		Color4f res = new Color4f(w.x*v.x, w.y*v.y, w.z*v.z, w.w*v.w);
		return res;
	}
	
	public static Vector3d componentMultiplication3d( Tuple3d w, Tuple3d v ) {
		Vector3d res = new Vector3d(w.x*v.x, w.y*v.y, w.z*v.z);
		return res;
	}
	
	public static Vector3d reflect( Vector3d i, Vector3d n) {
		Vector3d r = new Vector3d(n);
		r.scale(2 * i.dot(n));
		r.sub(i);
		r.normalize();
		return r;
	}
	
	public static Vector3d refract( Vector3d i, Vector3d n, double ni, double nt) {
		double eta = ni / nt;
		double cosThetaI = n.dot(i);
		double sinThetaI2 = Math.max(0.0, 1 - cosThetaI * cosThetaI);
		double sinThetaT2 = eta * eta * sinThetaI2;
		
		if (sinThetaT2 >= 1)
			return null;
		
		//wt = eta * -wi + (eta * cosThetaI - cosThetaI) * n
		// wt_perpendicular = sinThetaT
		double cosThetaT = Math.sqrt(1 - sinThetaT2);
		
		Vector3d wt = new Vector3d(i);
		wt.negate();
		wt.scale(eta);
		Vector3d temp = new Vector3d(n);
		temp.scale(eta * cosThetaI - cosThetaT);
		wt.add(temp);
		wt.normalize();
		return wt;
	}
	
	public static Color4f vector3dToColor4f(Vector3d v) {
		return new Color4f((float)v.x, (float)v.y, (float)v.z, 1);
	}
	
	public static Vector3d color4fToVector3d(Color4f c) {
		return new Vector3d((double)c.x, (double)c.y, (double)c.z);
	}
	
	public static double dotProduct(Tuple3d w, Tuple3d v) {
			return w.x*v.x+w.y*v.y+w.z*v.z;
	}
	
	public static double intersectSphere(Ray ray, Point3d center, double radius) {
		Vector3d dirToSphere = new Vector3d();
    	dirToSphere.add(center);
    	dirToSphere.sub(ray.eyePoint);
    	if (dirToSphere.dot(ray.viewDirection) < 0) 
    		return -1;
    	
    	
    	double rSquare = radius * radius;
    	Vector3d d = new Vector3d(ray.viewDirection);
    	Point3d p = new Point3d(ray.eyePoint);
    	double distToPc = dirToSphere.dot(ray.viewDirection);
    	d.scale(distToPc);
    	Point3d pc = new Point3d(ray.eyePoint);
    	pc.add(d);
    	Vector3d temp = new Vector3d(pc);
    	temp.sub(center);
    	
//    	if (center.equals(new Point3d(1, 1, 1))) {
//    		System.out.println("hello");
//    	}
    	if (temp.lengthSquared() > rSquare)
    		return -1;

    	double distInBetween = Math.sqrt(rSquare - temp.lengthSquared());
    	double t1 = distToPc - distInBetween;
    	double t2 = distToPc + distInBetween;

    	//output the one that is closer and find the intersection point
    	double t = Math.min(t1, t2);
    	if (t < 0) t = Math.max(t1, t2);
    	//check if this intersected point is the closest point
    	return t;
	}
}
