package comp557.a4;

import javax.vecmath.Color4f;
import javax.vecmath.Matrix4d;

public class RayTracingTask implements Runnable {
	
	int i, j, spp;
	Camera cam;
	Matrix4d inverseViewMatrix;
	Scene scene;
	
	public RayTracingTask(int _i, int _j, Camera _cam, Matrix4d inverseView, Scene _scene, int _spp) {
		// TODO Auto-generated constructor stub
		i = _i;
		j = _j;
		cam = _cam;
		inverseViewMatrix = inverseView;
		scene = _scene;
		spp = _spp;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Sampler sampler = new Sampler(260728557);
	    Integrator integrator = new Integrator(sampler);
	    
	    Color4f c = new Color4f(scene.ambient.x, scene.ambient.y, scene.ambient.z, 1);
			//Color4f c = new Color4f();
		if (spp > 1) {	//super sampling
		for (int k = 0; k<spp; k++) {
		Ray ray = new Ray();
		IntersectResult intersection = new IntersectResult();
		double[] offset = sampler.next2D();
		//System.out.println(offset[0] + " " + offset[1]);
		
		Scene.generateRay(i, j, offset, cam, ray, inverseViewMatrix, -1);
		
		scene.intersect(ray, intersection);
		
		//if (j == h - 30 && i == w/2 + 50) {
		//System.out.println("c" + intersection.n.toString());
		//}
		if (scene.render.pathTracing) {
			integrator.maxDepth = scene.render.depth;
			integrator.rrDepth = scene.render.rr_depth;
			integrator.rrProb = scene.render.rr_prob;
			c.add(integrator.render(ray, intersection, scene, true));
		}
		else {
			c.add(integrator.render(ray, intersection, scene));
		}
		}
		c.scale(1.f / spp);
		}
		else {
			// TODO: Objective 1: generate a ray (use the generateRay method)
			// calculate the pixel point
			Ray ray = new Ray();
			IntersectResult intersection = new IntersectResult();
			double[] offset = new double[] {0.5, 0.5};
			Scene.generateRay(i, j, offset, cam, ray, inverseViewMatrix, -1);
			
			// TODO: Objective 2: test for intersection with scene surfaces
			scene.intersect(ray, intersection);
			
			// TODO: Objective 3: compute the shaded result for the intersection point (perhaps requiring shadow rays)
			//      	         		if (j == h/2-100 && i == w/3-90) {
			//      	    					System.out.println("c" + intersection.n.toString());
			//      	    			}
			if (scene.render.pathTracing) {
				integrator.maxDepth = scene.render.depth;
				integrator.rrDepth = scene.render.rr_depth;
				integrator.rrProb = scene.render.rr_prob;
				c.add(integrator.render(ray, intersection, scene, true));
			}
			else {
				c.add(integrator.render(ray, intersection, scene));
			}
		}
		
		c.clamp(0, 1);
		
		int r = (int)(255*c.x);																
		int g = (int)(255*c.y);
		int b = (int)(255*c.z);
		int a = 255;
		int argb = (a<<24 | r<<16 | g<<8 | b);    
		
		// update the render image
		scene.render.setPixel(i, j, argb);
	}

}
