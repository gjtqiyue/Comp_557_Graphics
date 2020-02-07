package comp557.a4;

import java.util.HashMap;

import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import com.jogamp.graph.geom.Vertex;

public class Mesh extends Intersectable {
	
	/** Static map storing all meshes by name */
	public static Map<String,Mesh> meshMap = new HashMap<String,Mesh>();
	
	/**  Name for this mesh, to allow re-use of a polygon soup across Mesh objects */
	public String name = "";
	
	/**
	 * The polygon soup.
	 */
	public PolygonSoup soup;

	public Mesh() {
		super();
		this.soup = null;
	}			
		
	@Override
	public void intersect(Ray ray, IntersectResult result) {
		
		// TODO: Objective 7: ray triangle intersection for meshes
		
		for (Mesh m : meshMap.values()) {
			//iterate through faces
			for (int i=0; i<m.soup.faceList.size(); i++) {
				IntersectResult tmpResult = new IntersectResult();
				intersectFace(ray, tmpResult, m.soup.faceList.get(i));
				if (tmpResult.t > 1e-9 && tmpResult.t < result.t) {
					//System.out.println(tmpResult.t);
					result.t = tmpResult.t;
					result.p = tmpResult.p;
					result.material = tmpResult.material;
					result.n = tmpResult.n;
				}
			}
		}
		
		if (Double.isInfinite(result.t)) {
			return;
		}
		
		return;
	}
	
	private void intersectFace(Ray ray, IntersectResult result, int[] face) {
		 Point3d v0 = soup.vertexList.get(face[0]).p;
		 Point3d v1 = soup.vertexList.get(face[1]).p;
		 Point3d v2 = soup.vertexList.get(face[2]).p;
		 
		 Vector3d v0v1 = new Vector3d(v1);
		 v0v1.sub(v0);
		 Vector3d v1v2 = new Vector3d(v2);
		 v1v2.sub(v1);
		 Vector3d v2v0 = new Vector3d(v0);
		 v2v0.sub(v2);
		 Vector3d v0v2 = new Vector3d(v2);
		 v0v2.sub(v0);
		 
		 Vector3d n = new Vector3d();
		 n.cross(v0v1, v0v2);
		 n.normalize();
		 
		 Vector3d h = new Vector3d();
		 h.cross(ray.viewDirection, v0v2);
		 double a = v0v1.dot(h);
		 
		 if (a > -0.00001 && a < 0.00001) {
			 return;
		 }
		 
		 double f = 1/a;
		 Vector3d s = new Vector3d(ray.eyePoint);
		 s.sub(v0);
		 
		 double u = f * s.dot(h);
		 
		 if (u < 0 || u > 1) {
			 return;
		 }
		 
		 Vector3d q = new Vector3d();
		 q.cross(s, v0v1);
		 
		 double v = f * q.dot(ray.viewDirection);
		 
		 if (v < 0 || u + v > 1) {
			 return;
		 }
		 
		 double t = f * q.dot(v0v2);
		 
		 if (t > 0.00001) {
			 result.t = t;
			 ray.getPoint(t, result.p);
			 result.material = material;
			 result.n = n;
		 }
//		 Vector3d n = new Vector3d();
//		 n.cross(v0v1, v0v2);
//		 n.normalize();
//		 
//		 if (n.dot(ray.viewDirection) >= 0) {
//			 return;	//ray parallel to plane or same direction to plane
//		 }
//		 
////		 Vector3d v = new Vector3d(ray.eyePoint);
////		 v.sub(v0);
////		 double d = v.dot(n);
//		 
//		 Vector3d o = new Vector3d(ray.eyePoint);
//		 double t = - (o.dot(n)) / ray.viewDirection.dot(n);
//		 
//		 if (t < 1e-9) {
//			 return;	//negative t value
//		 }
//		 //System.out.println(t);
//		 Point3d phit = new Point3d();
//		 ray.getPoint(t, phit);
//		 
//		 
//		 //test if p is inside the triangle
//		 Vector3d pv0 = new Vector3d(phit);
//		 pv0.sub(v0);
//		 Vector3d pv1 = new Vector3d(phit);
//		 pv1.sub(v1);
//		 Vector3d pv2 = new Vector3d(phit);
//		 pv2.sub(v2);
//		 
//		 Vector3d cross0 = new Vector3d();
//		 cross0.cross(v0v1, pv0);
//		 Vector3d cross1 = new Vector3d();
//		 cross1.cross(v1v2, pv1);
//		 Vector3d cross2 = new Vector3d();
//		 cross2.cross(v2v0, pv2);
//		 
//		 if (cross0.dot(n) > 0 && cross1.dot(n) > 0 && cross2.dot(n) > 0) {
//			 //inside the triangle
//			 result.t = t;
//			 result.p = phit;
//			 result.material = material;
//			 result.n = new Vector3d(0, 1, 0);	//face normal
//			 //System.out.println(n.x*phit.x+n.y*phit.y+n.z*phit.z);
//		 }
	}

}
