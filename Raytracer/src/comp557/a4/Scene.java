package comp557.a4;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.naming.spi.DirStateFactory.Result;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import mintools.viewer.SceneGraphNode;

/**
 * Simple scene loader based on XML file format.
 */
public class Scene {
    
    /** List of surfaces in the scene */
    public List<Intersectable> surfaceList = new ArrayList<Intersectable>();
	
	/** All scene lights */
	public Map<String,Light> lights = new HashMap<String,Light>();

    /** Contains information about how to render the scene */
    public Render render;
    
    /** The ambient light colour */
    public Color3f ambient = new Color3f();

    /** 
     * Default constructor.
     */
    public Scene() {
    	this.render = new Render();
    }
    
    /**
     * renders the scene
     */
    public void render(boolean showPanel) {
 
        Camera cam = render.camera; 
        int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        render.init(w, h, showPanel);
        
        Sampler sampler = new Sampler(260728557);
        
        //calculate view matrix inverse
        Matrix4d inverseViewMatrix = LookAt(cam.from, cam.to, cam.up);
        inverseViewMatrix.invert();
        
        //depth of field
        double blurSamples = 3;
        double focusDistance = 8;
        double sampleRadius = 0.1;
        
	    Integrator integrator = new Integrator(sampler);
        int spp = render.samples;
        //multi-threading
        IntStream heightRange = IntStream.range(0, h);
        heightRange.forEach(j -> {
        	//if (render.isDone()) return;
        	//System.out.println(j);
        	//for ( int j = 0; j < h && !render.isDone(); j++ ) {
        		 	for ( int i = 0; i < w && !render.isDone(); i++ ) {
                	 	Color4f c = new Color4f(ambient.x, ambient.y, ambient.z, 1);
                	 																//Color4f c = new Color4f();
                	 	if (spp > 1) {	//super sampling
                	 		for (int k = 0; k<spp; k++) {
                	 			Ray ray = new Ray();
                	 			IntersectResult intersection = new IntersectResult();
                	 			double[] offset = sampler.next2D();
                	 			//System.out.println(offset[0] + " " + offset[1]);
					  		
                	 			generateRay(i, j, offset, cam, ray, inverseViewMatrix, -1);
					  		
                	 			intersect(ray, intersection);
							
//					 			if (j == h - 30 && i == w/2 + 50) {
//									System.out.println("c" + intersection.n.toString());
//					 			}
					 			if (render.pathTracing) {
									integrator.maxDepth = render.depth;
									integrator.rrDepth = render.rr_depth;
									integrator.rrProb = render.rr_prob;
									c.add(integrator.render(ray, intersection, this, true));
								}
								else {
									c.add(integrator.render(ray, intersection, this));
								}
					  		}
                		 	c.scale(1.f / (float)spp);
                	 	}
                	 	else {
						    // TODO: Objective 1: generate a ray (use the generateRay method)
							// calculate the pixel point
							Ray ray = new Ray();
							IntersectResult intersection = new IntersectResult();
							double[] offset = new double[] {0.5, 0.5};
							generateRay(i, j, offset, cam, ray, inverseViewMatrix, -1);
							
							if (render.depthOfField) {
								for (int s = 0; s < blurSamples; ++s) {
									double[] blurOffset = sampler.next2D();
									generateRay(i, j, blurOffset, cam, ray, inverseViewMatrix, focusDistance);
									
									// TODO: Objective 2: test for intersection with scene surfaces
									intersect(ray, intersection);
									
								    // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
									if (render.pathTracing) {
										integrator.maxDepth = render.depth;
										integrator.rrDepth = render.rr_depth;
										integrator.rrProb = render.rr_prob;
										c.add(integrator.render(ray, intersection, this, true));
									}
									else {
										c.add(integrator.render(ray, intersection, this));
									}
								}
								c.scale((float) (1.0 / blurSamples));
							}
							else {
							    // TODO: Objective 2: test for intersection with scene surfaces
								intersect(ray, intersection);
								
							    // TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
								if (render.pathTracing) {
									integrator.maxDepth = render.depth;
									integrator.rrDepth = render.rr_depth;
									integrator.rrProb = render.rr_prob;
									c.add(integrator.render(ray, intersection, this, true));
								}
								else {
									c.add(integrator.render(ray, intersection, this));
								}
							}
						}
					
                	 	c.clamp(0, 1);
					
						int r = (int)(255*c.x);																
                      	int g = (int)(255*c.y);
                      	int b = (int)(255*c.z);
                      	int a = 255;
                      	int argb = (a<<24 | r<<16 | g<<8 | b);    
                      	
                      	// update the render image
                      	render.setPixel(i, j, argb);
        		 	}
        //}
        });
        
        // save the final render image
        render.save();
        
        // wait for render viewer to close
        render.waitDone();
        
    }

	private Matrix4d LookAt(Point3d eye, Point3d to, Vector3d up) {
		Vector3d toVec = new Vector3d(to);
        toVec.sub(eye);
        toVec.normalize();
        Vector3d left = new Vector3d();
        left.cross(toVec, up);
        left.normalize();
        Vector3d forward = new Vector3d(toVec);
        forward.normalize();
        up.cross(left, forward);
        up.normalize();
//        System.out.println(left.toString());
//        System.out.println(forward.toString());
//        System.out.println(up.toString());
        
        Matrix4d viewMatrix = new Matrix4d();
        viewMatrix.setRow(0, left.x, left.y, left.z, 0);
        viewMatrix.setRow(1, up.x, up.y, up.z, 0);
        viewMatrix.setRow(2, -forward.x, -forward.y, -forward.z, 0);
        viewMatrix.setRow(3, 0, 0, 0, 1);
        Matrix4d eyeMatrix = new Matrix4d();
        eyeMatrix.setRow(0, 1, 0, 0, -eye.x);
        eyeMatrix.setRow(1, 0, 1, 0, -eye.y);
        eyeMatrix.setRow(2, 0, 0, 1, -eye.z);
        eyeMatrix.setRow(3, 0, 0, 0, 1);
        viewMatrix.mul(eyeMatrix);
		return viewMatrix;
	}
    
    /**
     * Generate a ray through pixel (i,j).
     * 
     * @param i The pixel row.
     * @param j The pixel column.
     * @param offset The offset from the center of the pixel, in the range [-0.5,+0.5] for each coordinate. 
     * @param cam The camera.
     * @param ray Contains the generated ray.
     */
	public static void generateRay(final int i, final int j, final double[] offset, final Camera cam, Ray ray, Matrix4d inverseViewMatrix, double focusDistance) {
		
		// TODO: Objective 1: generate rays given the provided parmeters
		int w = cam.imageSize.width;
        int h = cam.imageSize.height;
        
        double aspect_ratio = w / h;
		double scaling = Math.tan((Math.PI / 180 * cam.fovy) / 2);
        
		double x_screen = ((i + offset[0]) / w) * 2 - 1;
		double y_screen = -(((j + offset[1]) / h) * 2 - 1);

		double yPixel = y_screen * scaling;
		double xPixel = x_screen * aspect_ratio * scaling;
		Point3d pixel = new Point3d(xPixel, yPixel, -1);
		//System.out.println(pixel.toString());
		//translate this point to world
		
		inverseViewMatrix.transform(pixel);

		//raycast from camera origin
		Vector3d dir = new Vector3d();
		if (focusDistance > 0) {	//if depthOfField is true, then original ray direction is passed here and focal length is > 0
			Point3d focusPoint = new Point3d();
			ray.getPoint(focusDistance, focusPoint);
			dir.set(focusPoint);
			dir.sub(pixel);
			
			double f = focusDistance - 1;	// 1 is near plane
			double f1 = focusPoint.distance(cam.from);
			double x = ((i + 0.5) / w) * 2 - 1;
			double y = -(((j + 0.5) / h) * 2 - 1);

			y = y * scaling;
			x = x * aspect_ratio * scaling;
			Point3d p = new Point3d(x, y, -1);
			inverseViewMatrix.transform(p);
			
			double offset_x = f1 * (pixel.x - p.x) / f;
			double offset_y = f1 * (pixel.y - p.y) / f;
			Point3d shiftedEye = new Point3d(cam.from.x + offset_x, cam.from.y + offset_y, 0);
			ray.viewDirection = dir;
			ray.eyePoint = shiftedEye;
		}
		else {
			dir.add(pixel);
			dir.sub(cam.from);
			dir.normalize();
			ray.viewDirection = dir;
			ray.eyePoint = cam.from;
		}
	}

	/**
	 * Shoot a shadow ray in the scene and get the result.
	 * 
	 * @param result Intersection result from raytracing. 
	 * @param light The light to check for visibility.
	 * @param root The scene node.
	 * @param shadowResult Contains the result of a shadow ray test.
	 * @param shadowRay Contains the shadow ray used to test for visibility.
	 * 
	 * @return True if a point is in shadow, false otherwise. 
	 */
	public static boolean inShadow(IntersectResult result, SceneNode root, IntersectResult shadowResult, Ray shadowRay) {
		
		// TODO: Objective 5: check for shdows and use it in your lighting computation
		if (root != null) {
			root.intersect(shadowRay, shadowResult);
			if (!Double.isInfinite(shadowResult.t)) {
				return true;
			}
		}

		return false;
	}    
	
	public void intersect(Ray ray, IntersectResult hit) {
		for (Intersectable sur : surfaceList) {
    		sur.intersect(ray, hit);
    	}
	}
	
	public Light lightTest(Ray ray) {
		double hitDist = -1;
		Light hitLight = null;
		for (Light light : lights.values()) {
			if (light.type.equals("sphere")) {
				double t = Util.intersectSphere(ray, light.from, light.radius);
				
				if (hitDist < 0 && t > 0) {
					hitDist = t;
					hitLight = light;
				}
				else if (hitDist > t && t > 0) {
					hitDist = t;
					hitLight = light;
				}
			}
			else {
				hitDist = light.from.distance(ray.eyePoint);
			}
		}
		return hitLight;
	}
}
