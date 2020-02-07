package comp557.a4;

import java.util.Stack;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

public class BSDF {
	
	IntersectResult intersection;
	Scene scene;
	Integrator integrator;
	
	int check = 0;	//reflection and refraction check, if we hit such material we set it 1 and 2 respectively
	
	public double samplingWeight = 0.5;
	
	public BSDF(IntersectResult result, Scene s, Integrator i) {
		intersection = result;
		scene = s;
		integrator = i;
	}
	
	public Vector3d eval() {
		Vector3d color = new Vector3d();
		if (intersection.material == null) return color;
		switch (intersection.material.type) {
		case DIFFUSE:
			color = evalDiffuse(intersection);
			break;
		case SPECULAR:
			color = evalSpecular(intersection);
			break;
		case MIRROR:
			color = evalMirror(intersection, scene, integrator);
			break;
		case FRESNEL:
			color = evalFresnel(intersection);
			break;
		default:
			break;
		}
		
		return color;
	}
	
	public Vector3d sample(Sampler sampler, Vector2d pdf) {
		Vector3d color = new Vector3d();
		
		switch (intersection.material.type) {
		case DIFFUSE:
			color = sampleDiffuse(intersection, sampler, pdf);
			break;
		case SPECULAR:
			color = sampleSpecular(intersection, sampler, pdf);
			break;
		default:
			break;
		}
		
		return color;
	}
	
	//check if there is reflection or refraction happening, if so we need to update the intersection to the latest diffuse point
	public void checkReflectionAndRefraction(IntersectResult i) {
		switch (intersection.material.type) {
		case MIRROR:
			evalMirror(i, scene, integrator);
			check = 1;
			break;
		case FRESNEL:
			evalFresnel(i);
			check = 2;
			break;
		default:
			break;
		}
	}
	
	public Vector3d updateReflectionAndRefraction(Vector3d fr, IntersectResult i) {
		Vector3d c = new Vector3d(fr);
		
		switch (check) {
		case 1:
			if (i.reflection != null) {
				c.set(i.reflection.albedo);
				c.scale(1 - i.reflection.reflectance);
				fr.scale(i.reflection.reflectance);
				c.add(fr);
				c.clamp(0.0, 1);
			}
			break;
		case 2:
			if (i.refraction != null) {
				Vector3d diffuse = new Vector3d(fr);
				diffuse.scale(1.f - (float)i.refraction.fresnel_out);
				diffuse.scale(1.f / (float)i.refraction.cosTheta_out);
				
				diffuse.scale(1.f - (float)i.refraction.fresnel_in);
				diffuse.scale(1.f / (float)i.refraction.cosTheta_in);
				
				c.add(diffuse);
			}
			break;
		default:
			break;
		}
		check = 0;
		return c;
	}
	
	public Vector3d evalDiffuse(IntersectResult i) {
		Vector3d color = new Vector3d();
		
		if (i.wi.dot(i.n) > 0 && i.wo.dot(i.n) > 0) {
			Color4f diffuse = new Color4f(intersection.material.diffuse);
			//diffuse.scale((float) Util.INV_PI);
			Vector3d d = new Vector3d(diffuse.x, diffuse.y, diffuse.z);
			d.scale((float)Math.max(0, intersection.n.dot(intersection.wi)));

			color.add(d);
		}
		return color;
	}
	
	public Vector3d sampleDiffuse(IntersectResult i, Sampler sampler, Vector2d pdf) {
		Vector3d color = new Vector3d();
		
		double[] sample = sampler.next2D();
		Vector3d wi = integrator.squareToCosineHemisphere(sample);
		Frame frame = new Frame(i.n);
		i.wi = frame.toWorld(wi);
		Color4f diffuse = new Color4f(intersection.material.diffuse);
		diffuse.scale((float) Util.INV_PI);
		diffuse.scale((float) Math.max(0.0, Math.abs(wi.z)));
		color = Util.color4fToVector3d(diffuse);
		pdf.x = diffusePDF(wi.z);
		return color;
	}
	
	private double diffusePDF(double cosTheta) {
		return integrator.squareToCosineHemispherePdf(cosTheta);
	}
	
	public Vector3d evalSpecular(IntersectResult i) {
		Vector3d color = new Vector3d();
		
		if (i.wi.dot(i.n) > 0 && i.wo.dot(i.n) > 0) {
			Color4f diffuse = new Color4f(i.material.diffuse);
			//diffuse.scale((float) Util.INV_PI);
			//diffuse.scale((float) Util.INV_PI);
			Vector3d d = new Vector3d(diffuse.x, diffuse.y, diffuse.z);
	
			Color4f specular = intersection.material.specular;
			float exp = intersection.material.shinyness;
			//specular.scale((float) (Util.INV_TWOPI * (exp+2)));
			
			//calculate reflection
			Vector3d r = new Vector3d(i.wo);
			r.add(i.wi);
			r.normalize();
		
			Vector3d s = new Vector3d(specular.x, specular.y, specular.z);
			s.scale((float)Math.max(0, Math.pow(intersection.n.dot(r), exp)));
			
			color.add(d);
			color.add(s);
			color.scale((float)Math.max(0, i.n.dot(i.wi)));
		}
		return color;
	}
	
	
	public Vector3d sampleSpecular(IntersectResult i, Sampler sampler, Vector2d pdf) {
		Vector3d color = new Vector3d();
		int exp = (int) intersection.material.shinyness;
		if (sampler.next() < samplingWeight) {
			Frame reflectedDirFrame = new Frame(i.n);
			
			Vector3d wi_local = integrator.squareToPhongLobe(sampler.next2D(), exp);
			wi_local.normalize();
			i.wi = reflectedDirFrame.toWorld(wi_local);
			
			pdf.x = phongPdf(i, exp);
		}
		else {
			Vector3d wi_local = integrator.squareToCosineHemisphere(sampler.next2D());
			Frame frame = new Frame(i.n);
			Vector3d wi = frame.toWorld(wi_local);
			wi.normalize();
			i.wi.set(wi);
			
			pdf.x = phongPdf(i, exp);
			
		}
		color = evalSpecular(i);
		return color;
	}
	
	private double phongPdf(IntersectResult i, int exp) {	
		double cosTheta = i.n.dot(i.wi);
		double pdf_diffuse = integrator.squareToCosineHemispherePdf(cosTheta);
		double pdf_specular = integrator.squareToPhongLobePdf(cosTheta, exp);
		
		double pdf = pdf_diffuse * (1 - samplingWeight) + pdf_specular * samplingWeight;
		return pdf;
	}
	
	public Vector3d evalGloosy(IntersectResult i, Scene scene, Integrator integrator) {
		Vector3d color = new Vector3d();
		
		
		return color;
	}

	public Vector3d evalMirror(IntersectResult i, Scene scene, Integrator integrator) {
		Vector3d color = new Vector3d();
		
		if (i.wo.dot(i.n) > 0) {
			float reflectance = i.material.reflectance;
			
			if (reflectance < 0)
				reflectance = 1;

			//perfect reflection, no absorbing energy
			i.wi.set(Util.reflect(i.wo, i.n));
			
			Color4f diffuse = new Color4f(i.material.diffuse);
			Vector3d d = new Vector3d(diffuse.x, diffuse.y, diffuse.z);
			d.scale(1.f / Math.abs((float)i.wi.dot(i.n)));
			d.clamp(0.0, 1.0);
			i.reflection = new Reflection(d, reflectance);

			//ray trace to the nearest hit point
			Ray ray = new Ray(i.p, i.wi);
			IntersectResult mirrorHit = new IntersectResult();
			scene.intersect(ray, mirrorHit);
			
			if (!Double.isInfinite(mirrorHit.t)) {
				//update wo and wi to the new hit point
				i.wo.set(mirrorHit.wo);
				i.n.set(mirrorHit.n);
				i.p.set(mirrorHit.p);
				i.material = mirrorHit.material;
				i.t = mirrorHit.t;
				integrator.sampleLightDir(intersection);
				
				BSDF bsdf = new BSDF(i, scene, integrator);
				Vector3d fr = new Vector3d();
				fr = bsdf.eval();
				fr.scale(1.f / Math.abs((float)i.wi.dot(i.n)));
				//the color is accumulated based on reflectance attribute
				//fr = (diffuse * (1 - reflectance) + incoming light * reflectance) * cosTheta
				fr.scale(reflectance);
//				d.scale(1 - reflectance);
//				fr.add(d);
				
				color = fr;
			}
			else {
				color.set(0, 0, 0);
			}
		}
		
		return color;
	}
	
	public Vector3d sampleMirror(IntersectResult i, Scene scene, Integrator integrator) {
		Vector3d color = new Vector3d();
		
		if (i.wo.dot(i.n) > 0) {
			float reflectance = i.material.reflectance;
			
			if (reflectance < 0)
				reflectance = 1;
			
			Color4f diffuse = new Color4f(i.material.diffuse);
			Vector3d d = new Vector3d(diffuse.x, diffuse.y, diffuse.z);
			//d.scale(1.f / Math.abs((float)i.wi.dot(i.n)));
			
			//perfect reflection, no absorbing energy
			i.wi.set(Util.reflect(i.wo, i.n));

			//ray trace to the nearest hit point
			Ray ray = new Ray(i.p, i.wi);
			IntersectResult mirrorHit = new IntersectResult();
			scene.intersect(ray, mirrorHit);
			
			if (!Double.isInfinite(mirrorHit.t)) {
				//update wo and wi to the new hit point
				i.wo.set(mirrorHit.wo);
				i.n.set(mirrorHit.n);
				i.p.set(mirrorHit.p);
				i.material = mirrorHit.material;
				i.t = mirrorHit.t;
				integrator.sampleLightDir(intersection);
				
				BSDF bsdf = new BSDF(i, scene, integrator);
				Vector3d fr = new Vector3d();
				Vector2d pdf = new Vector2d();
				fr = bsdf.sample(integrator.sampler, pdf);
				fr.scale(1.f / Math.abs((float)i.wi.dot(i.n)));
				//the color is accumulated based on reflectance attribute
				//fr = (diffuse * (1 - reflectance) + incoming light * reflectance) * cosTheta
				fr.scale(reflectance);
				d.scale(1 - reflectance);
				fr.add(d);
				
				color = d;
			}
			else {
				color.set(0, 0, 0);
			}
		}
		
		return color;
	}

	public Vector3d evalFresnel(IntersectResult i) {
		Vector3d color = new Vector3d();

		//glass idx of refraction is 1.6
		i.refraction = new Refraction();
		i.refraction.ni = 1;
		i.refraction.nt = 1.6;
		double ni = i.refraction.ni;
		double nt = i.refraction.nt;

		IntersectResult out = new IntersectResult();
		double cosTheta = i.wo.dot(i.n);
		double fresnel_in = FresnelReflectance.FrDielectric(cosTheta, ni, nt);

//		//reflection part
//		i.wi = Util.reflect(i.wo, i.n);
//		
//		//ray trace to the nearest hit point
//		Ray ray = new Ray(i.p, i.wi);
//		IntersectResult mirrorHit = new IntersectResult();
//		scene.intersect(ray, mirrorHit);
//		
//		//test if ray hit something, then evaluate the light at the hit point
//		Color4f fr = integrator.render(ray, mirrorHit, scene);
//		fr.scale((float) fresnel_in);
//		fr.scale(1.f / Math.abs((float)i.wi.dot(i.n)));
//		color = Util.color4fToVector3d(fr);
		
		//refraction part
		Vector3d refract = Util.refract(i.wo, i.n, ni, nt);
		if (refract == null) return color;
		
		double cosTheta_wi_in = Math.abs(refract.dot(i.n));
		
		Point3d eye_offset = new Point3d(i.p);
		Vector3d offset = new Vector3d(i.n);
		offset.scale(0.01);
		eye_offset.sub(offset);
		Ray refractRay = new Ray(eye_offset, refract);
		
		scene.intersect(refractRay, out);
		
		if (Double.isInfinite(out.t)) return color;	//after refraction there is no hit
		
		Vector3d inner_n = new Vector3d(out.n);
		inner_n.negate();
		inner_n.normalize();
		double cosTheta_out = out.wo.dot(out.n);
		double fresnel_out = FresnelReflectance.FrDielectric(cosTheta_out, ni, nt);
		
		Vector3d refract_out = Util.refract(out.wo, inner_n, nt, ni);
		if (refract_out == null) return color;
		
		double cosTheta_wi_out = Math.abs(refract.dot(inner_n));
			
		//it will find the last diffuse surface it hit
		refractRay.set(out.p, refract_out);
		IntersectResult res = new IntersectResult();
		scene.intersect(refractRay, res);
		
	
		
		i.refraction.cosTheta_in = cosTheta_wi_in;
		i.refraction.cosTheta_out = cosTheta_wi_out;
		i.refraction.fresnel_in = fresnel_in;
		i.refraction.fresnel_out = fresnel_out;
		
		
		if (!Double.isInfinite(res.t)) {
			//update wo and wi to the new hit point
			i.wo.set(res.wo);
			i.n.set(res.n);
			i.p.set(res.p);
			i.material = res.material;
			integrator.sampleLightDir(intersection);
			
			BSDF bsdf = new BSDF(i, scene, integrator);
			Vector3d diffuse = bsdf.eval();
			
			diffuse.scale(1.f - (float)i.refraction.fresnel_out);
			diffuse.scale(1.f / (float)i.refraction.cosTheta_out);
			
			diffuse.scale(1.f - (float)i.refraction.fresnel_in);
			diffuse.scale(1.f / (float)i.refraction.cosTheta_in);
			
			color.add(diffuse);
		}
		else {
		
			Point3d lightPoint = new Point3d();
			Vector3d lightNormal = new Vector3d();
			double pdf_area = integrator.sampleSphereByArea(integrator.sampler.next2D(), out.p, integrator.getCurLight().from, integrator.getCurLight().radius, out, lightPoint, lightNormal);
			
			i.wo.set(out.wo);
			i.n.set(out.n);
			i.p.set(out.p);
			i.wi.set(out.wi);
			i.material = new Material();
			i.material.type = MaterialType.DIFFUSE;
			i.material.diffuse = new Color4f(1, 1, 1, 1);
//			IntersectResult shadow_intersection = new IntersectResult();
//			Ray shadowRay = new Ray(out.p, out.wi);
//			boolean inShadow = integrator.shadowTest(out, scene, shadowRay, shadow_intersection);
//			if (!inShadow) {
//				return integrator.getCurLight().getEmission();
//				i.material
//			}
			
		}
		
		return color;
	}
	
	
}
