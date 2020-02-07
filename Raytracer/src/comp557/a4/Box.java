package comp557.a4;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A simple box class. A box is defined by it's lower (@see min) and upper (@see max) corner. 
 */
public class Box extends Intersectable {

	public Point3d max;
	public Point3d min;
	
    /**
     * Default constructor. Creates a 2x2x2 box centered at (0,0,0)
     */
    public Box() {
    	super();
    	this.max = new Point3d( 1, 1, 1 );
    	this.min = new Point3d( -1, -1, -1 );
    }	

	@Override
	public void intersect(Ray ray, IntersectResult result) {
		// TODO: Objective 6: intersection of Ray with axis aligned box
		Point3d center = new Point3d((max.x + min.x) / 2, (max.y + min.y) / 2, (max.z + min.z) / 2);
		intersectSlab(ray, result, center, min.x, min.y, min.z, max.x, max.y, max.z);
	}	
	
	private void intersectSlab(Ray r, IntersectResult result, Point3d c, double xmin, double ymin, double zmin, double xmax, double ymax, double zmax) {
		Point3d p = r.eyePoint;
		Vector3d d = r.viewDirection;
		double t_xmin = (xmin - p.x) / d.x;
		double t_xmax = (xmax - p.x) / d.x;
		double t_ymin = (ymin - p.y) / d.y;
		double t_ymax = (ymax - p.y) / d.y;
		double t_zmin = (zmin - p.z) / d.z;
		double t_zmax = (zmax - p.z) / d.z; 
		
		double t_xlow = Math.min(t_xmin, t_xmax);
		double t_xhigh = Math.max(t_xmin, t_xmax);
		double t_ylow = Math.min(t_ymin, t_ymax);
		double t_yhigh = Math.max(t_ymin, t_ymax);
		double t_zlow = Math.min(t_zmin, t_zmax);
		double t_zhigh = Math.max(t_zmin, t_zmax);
		
		double t_min = Math.max(Math.max(t_xlow, t_ylow), t_zlow);
		double t_max = Math.min(Math.min(t_xhigh, t_yhigh), t_zhigh);
		
		if (t_max < t_min) {
			return;
		}
		
		if (t_min < 1e-9) {
			return;
		}
		
		result.t = Math.abs(t_min);
		r.getPoint(t_min, result.p);
		Vector3d n = new Vector3d(result.p);
		if (n.x == n.y || n.x == n.z || n.y == n.x) {
//			n.sub(c);
//			n.normalize();
		}
		else if (Math.abs(n.x - xmin) < 1e-5) {
			n.set(-1, 0, 0);
		}
		else if (Math.abs(n.x - xmax) < 1e-5) {
			n.set(1, 0, 0);
		}
		else if (Math.abs(n.y - ymin) < 1e-5) {
			n.set(0, -1, 0);
		}
		else if (Math.abs(n.y - ymax) < 1e-5) {
			n.set(0, 1, 0);
		}
		else if (Math.abs(n.z - zmin) < 1e-5) {
			n.set(0, 0, -1);
		}
		else if (Math.abs(n.z - zmax) < 1e-5) {
			n.set(0, 0, 1);
		}
		else {
			n.set(1, 1, 1);
		}
		result.n = n;//new Vector3d(1, 0, 0);
		result.material = material;
		return;
	}

}
