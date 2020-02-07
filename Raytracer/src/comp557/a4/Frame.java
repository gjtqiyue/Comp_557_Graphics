package comp557.a4;

import javax.vecmath.Vector3d;

public class Frame {
	
	Vector3d n = new Vector3d();
	
	Vector3d s = new Vector3d();
	
	Vector3d t = new Vector3d();
	
	public Frame(Vector3d normal) {
		n.set(normal);
		coordinateSystem(n, s, t);
	}
	
	private void coordinateSystem(Vector3d a, Vector3d b, Vector3d c) {
		if (Math.abs(a.x) > Math.abs(a.y)) {
			double invLen = 1 / Math.sqrt(a.x * a.x + a.z * a.z);
			c.set(a.z * invLen, 0, -a.x * invLen);
		}
		else {
			double invLen = 1 / Math.sqrt(a.y * a.y + a.z * a.z);
			c.set(0, a.z * invLen, -a.y * invLen);
		}
		b.cross(c, a);
	}
	
	public Vector3d toLocal(Vector3d v) {
		return new Vector3d(v.dot(s), v.dot(t), v.dot(n));
	}
	
	public Vector3d toWorld(Vector3d v) {
		//s * v.x + t * v.y + n * v.z;
		Vector3d res = new Vector3d();
		Vector3d v1 = new Vector3d(s);
		v1.scale(v.x);
		Vector3d v2 = new Vector3d(t);
		v2.scale(v.y);
		Vector3d v3 = new Vector3d(n);
		v3.scale(v.z);
		res.add(v1);
		res.add(v2);
		res.add(v3);
		return res;
	}
}
