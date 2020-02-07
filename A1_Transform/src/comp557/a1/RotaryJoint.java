package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import mintools.parameters.DoubleParameter;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;


public class RotaryJoint extends GraphNode {
	
	Vector3d _position;
	Vector3d _axis;
	
	DoubleParameter tx;
	DoubleParameter ty;
	DoubleParameter tz;
	DoubleParameter rotationAngle;
	
	public RotaryJoint(String name, double min, double max) {
		super(name);
		
		dofs.add( rotationAngle = new DoubleParameter( name+" rotation", 0, min, max ));
		_position = new Vector3d(0f, 0f, 0f);
		_axis = new Vector3d(0f, 0f, 0f);
	}
	
	public RotaryJoint(String name, Vector3d position, Vector3d axis, double angle, double min, double max) {
		super(name);
		
		dofs.add( rotationAngle = new DoubleParameter( name+" rotation", angle, min, max ));
		_position = position;
		_axis = axis;
	}
	
	public void setPosition(Tuple3d pos) {
		_position = (Vector3d) pos;
	}
	
	public void setAxis(Tuple3d axis) {
		_axis = (Vector3d) axis;
	}
	
	@Override
	public void display( GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		//translate around the parent
		gl.glTranslated(_position.x, _position.y, _position.z);
		
		//rotate around the axis
		gl.glRotated(rotationAngle.getFloatValue(), _axis.x, _axis.y, _axis.z);
		
		//push the current matrix
		gl.glPushMatrix();
		
		//glut.glutSolidCube(1);
		
		super.display(drawable);
		
		gl.glPopMatrix();
	}

}
