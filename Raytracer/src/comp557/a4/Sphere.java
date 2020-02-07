package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

/**
 * A simple sphere class.
 */
public class Sphere extends Intersectable {
    
	/** Radius of the sphere. */
	public double radius = 1;
    
	/** Location of the sphere center. */
	public Point3d center = new Point3d( 0, 0, 0 );
    
    /**
     * Default constructor
     */
    public Sphere() {
    	super();
    }
    
    /**
     * Creates a sphere with the request radius and center. 
     * 
     * @param radius
     * @param center
     * @param material
     */
    public Sphere( double radius, Point3d center, Material material ) {
    	super();
    	this.radius = radius;
    	this.center = center;
    	this.material = material;
    }
    
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    	
        // TODO: Objective 2: intersection of ray with sphere
    	
    	//check if sphere is behind the ray
    	Vector3d dirToSphere = new Vector3d();
    	dirToSphere.add(center);
    	dirToSphere.sub(ray.eyePoint);
    	if (dirToSphere.dot(ray.viewDirection) < 0) 
    		return;
    	
    	
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
    		return;

    	double distInBetween = Math.sqrt(rSquare - temp.lengthSquared());
    	double t1 = distToPc - distInBetween;
    	double t2 = distToPc + distInBetween;

    	//output the one that is closer and find the intersection point
    	double t = Math.min(t1, t2);
    	if (t < 0) t = Math.max(t1, t2);
    	//check if this intersected point is the closest point
	    if (result.p != null && t < result.t) {
	    	result.t = t;
	    	//find the point
	    	ray.getPoint(result.t, result.p);
	    	//calculate normal, assume uniform sphere
	    	Vector3d n = new Vector3d();
	    	n.add(result.p);
	    	n.sub(center);
	    	n.normalize();
	    	result.n = n;
	    	//assign material
	    	result.material = material;
    	}
    	
    }
    
    private double dot( Tuple3d w, Tuple3d v ) {
    	return w.x*v.x+w.y*v.y+w.z*v.z;
    }
    
}
