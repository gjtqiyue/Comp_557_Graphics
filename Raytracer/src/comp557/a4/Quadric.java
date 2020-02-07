package comp557.a4;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;


public class Quadric extends Intersectable {
    
	/**
	 * Radius of the sphere.
	 */
	public Matrix4d Q = new Matrix4d();
	public Matrix3d A = new Matrix3d();
	public Vector3d B = new Vector3d();
	public double C;
	
	/**
	 * The second material, e.g., for front and back?
	 */
	Material material2 = null;
	
	public Quadric() {
	
	}
	
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		Vector4d d = new Vector4d(ray.viewDirection.x, ray.viewDirection.y, ray.viewDirection.z, 0);
		Vector4d p = new Vector4d(ray.eyePoint.x, ray.eyePoint.y, ray.eyePoint.z, 1);
		
		Vector4d Qp = new Vector4d();
		Vector4d Qd = new Vector4d();
		Q.transform(p, Qp);
		Q.transform(d, Qd);
		
		double a = d.dot(Qd);
		double b = (p.dot(Qd) + d.dot(Qp));
		double c = p.dot(Qp);
		
		double delta = b * b - 4 * a * c;
		if (delta < 0) return;
		
		double t1 = (b * -1 + Math.sqrt(delta)) / (2 * a);
		double t2 = (b * -1 - Math.sqrt(delta)) / (2 * a);
		
		double t = -1;
		if (t1 >= 0 && t2 >= 0) {
			t = t1 < t2 ? t1 : t2;
		}
		else if (t1 >= 0) {
			t = t1;
		}
		else if (t2 >= 0) {
			t = t2;
		}
		
		if (t < 0) return;
		
		result.t = t;
		ray.getPoint(t, result.p);
		result.material = material;
		
		Matrix3d AT = new Matrix3d(A);
		AT.transpose();
		AT.add(A);
		AT.transform(result.p, result.n);
		Vector3d Vb = new Vector3d(B);
		Vb.scale(2);
		result.n.sub(Vb);
		result.n.normalize();
	}
	
}
