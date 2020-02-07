package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class for a plane at y=0.
 * 
 * This surface can have two materials.  If both are defined, a 1x1 tile checker 
 * board pattern should be generated on the plane using the two materials.
 */
public class Plane extends Intersectable {
    
	/** The second material, if non-null is used to produce a checker board pattern. */
	Material material2;
	
	/** The plane normal is the y direction */
	public static final Vector3d n = new Vector3d( 0, 1, 0 );
    
    /**
     * Default constructor
     */
    public Plane() {
    	super();
    }

        
    @Override
    public void intersect( Ray ray, IntersectResult result ) {
    
        // TODO: Objective 4: intersection of ray with plane
    	if (Math.abs(ray.viewDirection.dot(n)) < 1e-8)
    		return;
    	
    	double t = -ray.eyePoint.y / ray.viewDirection.y;
    	//System.out.println(t);
    	if (t > 0 && t < result.t) {
    		result.t = t;
    		Point3d p = new Point3d(ray.eyePoint);
    		Vector3d d = new Vector3d(ray.viewDirection);
    		d.scale(t);
    		p.add(d);
    		result.p = p;
    		
    		result.n = new Vector3d(n);

    		result.material = material;
    		if (material2 != null) {
    			int x = (int)Math.ceil(p.x);
    			int z = (int)Math.ceil(p.z);;
    			if ((x > 0 && z > 0) || (x <= 0 && z <= 0)) {
    				if ((x+z) % 2 == 0) result.material = material;
    				else result.material = material2;
    			}
    			else if ((x > 0 && z <= 0) || (x <= 0 && z > 0)) {
    				if ((x+z) % 2 == 0) result.material = material;
    				else result.material = material2;
    			}
    		}
    	}
    }
    
}
