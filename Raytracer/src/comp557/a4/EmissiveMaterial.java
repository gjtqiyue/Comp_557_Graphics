package comp557.a4;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Tuple3d;

public class EmissiveMaterial extends Material {
	
	public Color4f color = new Color4f();
	
	public double power;
    
    public EmissiveMaterial() {

    }
}
