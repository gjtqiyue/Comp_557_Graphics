package comp557.a4;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class Integrator {
	
	public Ray ray;
	
	public Sampler sampler;
	
	private Light curLight;
	
	public Integrator(Sampler s) {
		sampler = s;
	}
	
	public Color4f render(Ray ray, IntersectResult intersection, Scene scene) {
		Color4f Lr = new Color4f();
		
		if (!Double.isInfinite(intersection.t)) {
    		
    		Lr.add(new Color4f(scene.ambient.x, scene.ambient.y, scene.ambient.z, 1));
    		
    		double emPdf = 1.0 / scene.lights.size();
    		for (Light light : scene.lights.values()) {
    			curLight = light;
				if (curLight.type.equals("point")) {
					
		    		//System.out.println(lightPos.toString());
		    		Vector3d v = new Vector3d();
		    		v.sub(ray.eyePoint, intersection.p);
		    		v.normalize();
		    		intersection.wo.set(v);
		    		sampleLightDir(intersection);
		    		// if diffuse or specular we evaluate the color * light
		    		// point light source is hard to model caustics, only available with area light
	    			BSDF bsdf = new BSDF(intersection, scene, this);	//intersection object might be changed if we hit a mirror surface
        			Vector3d frd = bsdf.eval();
	    			
	        		Ray shadowRay = new Ray(intersection.p, intersection.wi);
	        		IntersectResult shadow_intersection = new IntersectResult();
	        		boolean inShadow = shadowTest(intersection, scene, shadowRay, shadow_intersection, curLight);
	        		
	//	        		if (inShadow && shadow_intersection.material.isTransmissive()) {
	//	        			//if we are in shadow, we need to do another check for transmissive materaial
	//        				intersection.wo = new Vector3d(v);
	//	        			intersection.wi = new Vector3d(l);
	//	        			
	//	        			BSDF bsdf = new BSDF(shadow_intersection, scene, this);
	//	        			Vector3d frd = bsdf.eval();
	//	        			BSDF bsdf_next = new BSDF(shadow_intersection, scene, this);
	//	        			Vector3d li = bsdf_next.eval();
	//	        			
	//	        			Color4f c = Util.vector3dToColor4f(Util.componentMultiplication3d(li, frd));
	//	        			Lr.add(c);
	//	        		}
	        		if (!inShadow) {
	        			Vector3d lightColor = curLight.getEmission();
	        			Vector3d c = Util.componentMultiplication3d(frd, lightColor);
	        			//c.scale((float)Math.max(0, intersection.n.dot(intersection.wi)));
	        			Lr.add(Util.vector3dToColor4f(c));
	        		}
	//	    		else {
	//	    			BSDF bsdf = new BSDF(intersection, scene, this);
	//	    			Vector3d frd = bsdf.eval();
	//	    			Vector4f fr = new Vector4f((float)frd.x, (float)frd.y, (float)frd.z, 1.f);
	//	    			Color4f lightColor = new Color4f(light.color.x * (float)light.power, light.color.y * (float)light.power, light.color.z * (float)light.power, 1.f);
	//	    			Color4f c = Util.componentMultiplication4f(fr, lightColor);
	//	    			//c.scale((float)Math.max(0, intersection.n.dot(intersection.wi)));
	//	    			Lr.add(c);
	//	    		}
				}
				else if (curLight.type.equals("sphere")) {
					BSDF bsdf = new BSDF(intersection, scene, this);
					bsdf.checkReflectionAndRefraction(intersection);	//this value is not 0 if mirror material has a color itself
					
					Point3d lightPoint = new Point3d();
					Vector3d lightNormal = new Vector3d();
					double pdf_area = sampleSphereByArea(sampler.next2D(), intersection.p, curLight.from, curLight.radius, intersection, lightPoint, lightNormal);
					Vector3d lightDir = new Vector3d(intersection.wi);
					
					//the reason why eval again is because wi is only set after sampling a direction based on light area
					//however there is a chance that intersection is changed by mirror evaluation so we have to update and eval the brdf again
					Vector3d fr = bsdf.eval();
					fr.set(bsdf.updateReflectionAndRefraction(fr, intersection));
					
					Ray shadowRay = new Ray(intersection.p, new Vector3d(lightDir));
					IntersectResult shadow_intersection = new IntersectResult();
					//if (scene.bvh->intersect(shadow_ray, v_interaction) && v_interaction.shapeID == em.shapeID) { Li = em.getRadiance() / emPdf; }
					boolean inShadow = shadowTest(intersection, scene, shadowRay, shadow_intersection, curLight);
		        	
					lightDir.negate();
		    		if (!inShadow && lightNormal.dot(lightDir) >= 0) {	//if not blocked
		    			//if not hitting anything else then we definitely can hit a light
		    			Light hitLight = scene.lightTest(shadowRay);
		    			if (hitLight == curLight && pdf_area > 1e-8) {
							Vector3d toLight = new Vector3d(lightPoint);
							toLight.sub(intersection.p);
							double pdf = Util.dotProduct(toLight, toLight) / Math.abs(Util.dotProduct(lightDir, lightNormal)) * pdf_area;
							
							Vector3d li = curLight.getEmission();
							Vector3d direct = Util.componentMultiplication3d(fr, li);
							Lr.add(Util.vector3dToColor4f(direct));
						}
		    		}
				}
    		}
    		//Lr.scale((float) emPdf);
    	}
    	else {
    		Color3f bgcolor = scene.render.bgcolor;
    		Lr = new Color4f(bgcolor.x, bgcolor.y, bgcolor.z, 1);
    	}
		
		Lr.clamp(0, 1);
		return Lr;
	}
	
	/** Explicit path tracing 
	 *  Use Russian roulette as stopping criteria
	 * **/
	int maxDepth = 2;
	int rrDepth = 3;
	double rrProb = 0.9;
	public Color4f render(Ray ray, IntersectResult intersection, Scene scene, boolean pathTracing) {
		Color4f Lr = new Color4f();
		
		Vector3d d = pathTracing(ray, sampler, intersection, 0, scene);
		Lr = Util.vector3dToColor4f(d);
		
		return Lr;
	}
	
	private Vector3d pathTracing(Ray ray, Sampler sampler, IntersectResult intersection, int depth, Scene scene) {
		Ray r = ray;
		Vector3d Li = new Vector3d();
		Vector3d Le = new Vector3d();
		Vector3d directLi = new Vector3d();
		Vector3d indirectLi = new Vector3d();
		Vector3d fr = new Vector3d();
		
		//test if hit light
		Light light = selectEmitter(scene);
		Light light2 = scene.lightTest(ray);
		if (light != null && light2 == light && light.from.distance(ray.eyePoint) < intersection.t) {
			Li = light.getEmission();
			return Li;
		}
		
		
		if (!Double.isInfinite(intersection.t)) {
			BSDF bsdf = new BSDF(intersection, scene, this);
			
			//test if hit light

			if (maxDepth == -1) {
				if (depth > rrDepth) {
					if (sampler.next() > rrProb)
						return Li;
				}
			}
			else {
				if (depth == maxDepth) {
					//test if hit light
					light = scene.lightTest(ray);
					if (light != null) {
						Li = light.getEmission();
					}
					return Li;
				}
			}

			//direct illumination
			double pdf_area = 1e-8;
//			double emPdf;
			
			
			double emPdf = 1.0 / scene.lights.size();
				curLight = light;
				// path tracing should only be working under area light
				if (light.type.equals("point")) {
					return Li;
				}
				
				if (curLight.type.equals("sphere") && curLight.radius > 0) {
					bsdf.checkReflectionAndRefraction(intersection);	//this value is not 0 if mirror material has a color itself
					
					Point3d lightPoint = new Point3d();
					Vector3d lightNormal = new Vector3d();
					pdf_area = sampleSphereByArea(sampler.next2D(), intersection.p, curLight.from, curLight.radius, intersection, lightPoint, lightNormal);
					
					//the reason why eval again is because wi is only set after sampling a direction based on light area
					//however there is a chance that intersection is changed by mirror evaluation so we have to update and eval the brdf again
					fr = bsdf.eval();
					fr.set(bsdf.updateReflectionAndRefraction(fr, intersection));
					
					Vector3d lightDir = new Vector3d(intersection.wi);
					
					Ray shadowRay = new Ray(intersection.p, new Vector3d(lightDir));
					IntersectResult shadow_intersection = new IntersectResult();
					//if (scene.bvh->intersect(shadow_ray, v_interaction) && v_interaction.shapeID == em.shapeID) { Li = em.getRadiance() / emPdf; }
					boolean inShadow = shadowTest(intersection, scene, shadowRay, shadow_intersection, curLight);
		        	
					lightDir.negate();
		    		if (!inShadow && lightNormal.dot(lightDir) >= 0) {	//if not blocked
		    			//if not hitting anything else then we definitely can hit a light
		    			Light hitLight = scene.lightTest(shadowRay);
		    			if (hitLight == curLight && pdf_area > 1e-8) {
							Vector3d toLight = new Vector3d(lightPoint);
							toLight.sub(intersection.p);
							double pdf = Util.dotProduct(toLight, toLight) / Math.abs(Util.dotProduct(lightDir, lightNormal)) * pdf_area;
							
							Vector3d li = light.getEmission();
							directLi = Util.componentMultiplication3d(fr, li);
							directLi.scale(1.0 / (emPdf));
						}
		    		}
				}
				
				//indirect illumination
				Vector2d pdf_bsdf = new Vector2d();
				//bsdf.checkReflectionAndRefraction(intersection);	//this value is not 0 if mirror material has a color itself
				//the reason why eval again is because wi is only set after sampling a direction based on light area
				//however there is a chance that intersection is changed by mirror evaluation so we have to update and eval the brdf again
				fr = bsdf.sample(sampler, pdf_bsdf);
				fr.set(bsdf.updateReflectionAndRefraction(fr, intersection));
				
				Vector3d dir = new Vector3d(intersection.wi);
				IntersectResult sur = new IntersectResult();
				Ray indirectRay = new Ray(intersection.p, dir);
				scene.intersect(indirectRay, sur);
				
				Light hitLight = scene.lightTest(indirectRay);
				
				if (!Double.isInfinite(sur.t) && hitLight == null) {	//hit no light but a object in the scene
					if (pdf_bsdf.x > 1e-8) {
						Vector3d li = pathTracing(indirectRay, sampler, sur, ++depth, scene);
						indirectLi = Util.componentMultiplication3d(fr, li);
					}
				}
				else if (!Double.isInfinite(sur.t) && hitLight != null) {	//hit a light and a object, then we need to check if the object is before the light
					Point3d p = new Point3d(hitLight.from);
					double d = p.distance(intersection.p);
					if ((d > 0 && d > sur.t) || d < 0) {	//check if hit a light, if not, continue
						if (pdf_bsdf.x > 1e-8) {
							Vector3d li = pathTracing(indirectRay, sampler, sur, ++depth, scene);
							indirectLi = Util.componentMultiplication3d(fr, li);
							indirectLi.scale(1.0 / pdf_bsdf.x);
						}
					}
				}
				
				Li.add(Le);
				Li.add(directLi);
				Li.add(indirectLi);
			//Li.scale(emPdf);
		}
		else {
			//it might hit a light if it doesn't hit any scene object
			Light hit = scene.lightTest(r);
			if (hit != null) {
				Li = hit.getEmission();
			}
		}
		
		return Li;
	}
	
	public double sampleSphereByArea(double[] sample, Point3d pShading, Point3d lightCenter, double radius, IntersectResult i, Point3d emPoint, Vector3d emNormal) {
		//Vector3d p_local = squareToCosineHemisphere(sample);
		Vector3d p_local = squareToUniformHemisphere(sample);
		double pdf_y = Util.INV_TWOPI * (1.0 / (radius * radius));
		
		Vector3d light_to_point = new Vector3d(pShading);
		light_to_point.sub(lightCenter);
		light_to_point.normalize();
		Frame em_frame = new Frame(light_to_point);
		Point3d p = new Point3d(lightCenter);
		Vector3d n = em_frame.toWorld(p_local);
		n.normalize();
		emNormal.set(n);
		n.scale(radius);
		p.add(n);
		emPoint.set(p);

		Vector3d wiW = new Vector3d(p);
		wiW.sub(pShading);
		wiW.normalize();
		i.wi = wiW;
		
		return pdf_y;
	}
	
	//return pdf
	private double sampleSphereBySolidAngle(double[] sample, Point3d pShading, Point3d lightCenter, double radius, IntersectResult i, Point3d emPoint, Vector3d emNormal) {
		double dc = lightCenter.distance(pShading);
		Vector3d wc = new Vector3d(lightCenter);
		wc.sub(pShading);
		wc.normalize();
		Frame em_frame = new Frame(wc);
		double sinThetaMax = radius / dc;
		double cosThetaMax = Math.sqrt(1 - sinThetaMax * sinThetaMax);
		double cosTheta = 1 - sample[0] + sample[0] * cosThetaMax;
		double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
		double phi = sample[1] * 2 * Util.PI;
		double ds = dc * sinTheta;
		double dp = dc * cosTheta - Math.sqrt(radius * radius - ds * ds);
		double cosAlpha = (dc * dc + radius * radius - dp * dp) / (2 * dc * radius);
		double sinAlpha = Math.sqrt(Math.max(0, 1 - cosAlpha * cosAlpha));
		
		Vector3d v1 = new Vector3d(em_frame.s);
		v1.negate();
		v1.scale(Math.cos(phi) * sinAlpha);
		Vector3d v2 = new Vector3d(em_frame.t);
		v2.negate();
		v2.scale(Math.sin(phi) * sinAlpha);
		Vector3d v3 = new Vector3d(em_frame.n);
		v3.negate();
		v3.scale(cosAlpha);
		Vector3d n_local = new Vector3d();
		n_local.add(v1);
		n_local.add(v2);
		n_local.add(v3);
		emNormal.set(n_local);	//set the sampled normal
		n_local.scale(radius);
		Vector3d p_world = new Vector3d(lightCenter);
		p_world.add(n_local);
		emPoint.set(p_world);	//set the sampled point
		p_world.sub(pShading);
		p_world.normalize();
		i.wi = p_world;
		return squareToUniformConePdf(cosThetaMax);
	}
		
	private Light selectEmitter(Scene scene) {
		int idx = sampler.nextInt(0, scene.lights.values().size());
		String[] keys = new String[scene.lights.size()];
		return scene.lights.get(scene.lights.keySet().toArray(keys)[idx]);
	}

	public boolean shadowTest(IntersectResult intersection, Scene scene, Ray shadowRay, IntersectResult shadow_intersection, Light curLight) {
		Vector3d threshold = new Vector3d(intersection.n);
		threshold.scale(0.0001);
		shadowRay.eyePoint.add(threshold);
		
		boolean inShadow = false;
		for (Intersectable sur : scene.surfaceList) {
			if ( sur instanceof SceneNode )
				inShadow = Scene.inShadow(intersection, (SceneNode)sur, shadow_intersection, shadowRay);
		}
		//test if we can hit light
		if (inShadow) {
			double distTolight = curLight.from.distance(shadowRay.eyePoint);
			if (shadow_intersection.t < distTolight) {
				inShadow = true;
			}
			else {
				inShadow = false;
			}
		}
		return inShadow;
	}
	
	double[] squareToUniformDiskConcentric(double[] sample) {
		Vector2d v = new Vector2d();
		Vector2d offset = new Vector2d(sample[0] * 2 - 1, sample[1] * 2 - 1);
		if (offset.x == 0 && offset.y == 0) {
			return new double[] {0, 0};
		}
		
		double r, theta;
		if (Math.abs(offset.x) > Math.abs(offset.y)) {
			r = offset.x;
			theta = (offset.y / offset.x) * Util.PI / 4;
		}
		else {
			r = offset.y;
			theta = Util.INV_TWOPI - Util.INV_FOURPI * (offset.x / offset.y);
		}
		v.set(r * Math.cos(theta), r * Math.sin(theta));
		
		return new double[] {v.x, v.y};
	}
	
	Vector3d squareToCosineHemisphere(double[] sample) {
		Vector3d v = new Vector3d();
		double[] p = squareToUniformDiskConcentric(sample);
		double z = Math.sqrt(Math.max(0, 1 - (p[0] * p[0] + p[1] * p[1])));
		v.set(p[0], p[1], z);
		return v;
	}
	
	double squareToCosineHemispherePdf(double cosTheta) {
		return cosTheta * Util.INV_PI;
	}
	
	Vector3d squareToPhongLobe(double[] sample, int exp) {
		Vector3d v = new Vector3d();
		double cosTheta = Math.pow(sample[0], 1 / (exp + 1));
		double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
		double phi = 2 * Util.PI * sample[1];
		v.set(sinTheta * Math.cos(phi), sinTheta * Math.sin(phi), cosTheta);
		return v;
	}
	
	double squareToPhongLobePdf(double cosTheta, int exp) {
		return (exp + 1) / (2 * Util.PI) * Math.pow(cosTheta, exp);
	}
	
	Vector3d squareToUniformCone(double[] sample, double cosThetaMax) {
		Vector3d v = new Vector3d();
		
		double cosTheta = 1 - sample[0] + sample[0] * cosThetaMax;
		double sinTheta = Math.sqrt(1 - cosTheta * cosTheta);
		double phi = Util.PI * 2 * sample[1];
		v.set(sinTheta * Math.cos(phi), sinTheta * Math.sin(phi), cosTheta);
		return v;
	}
	
	double squareToUniformConePdf(double cosThetaMax) {
		double pdf = 1 / (2 * Util.PI * (1 - cosThetaMax));
		return pdf;
	}
	
	Vector3d squareToUniformHemisphere(double[] sample) {
		double z = sample[0];
		double r = Math.sqrt(Math.max(0.f, 1.f - z * z));
		double phi = 2 * Util.PI * sample[1];
		Vector3d v = new Vector3d(r * Math.cos(phi), r * Math.sin(phi), z);
		return v;
	}
	
	public Light getCurLight() {
		return curLight;
	}
	
	public Vector3d sampleLightDir(IntersectResult i) {
		if (curLight.type.equals("point")){
			Vector3d l = new Vector3d();
			Point3d lightPos = curLight.from;
			l.sub(lightPos, i.p);
			l.normalize();
			i.wi.set(l);
			return l;
		}
		return new Vector3d(0, 0, 0);
	}
}
