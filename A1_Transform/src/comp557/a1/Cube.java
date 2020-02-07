package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import mintools.parameters.DoubleParameter;
import javax.vecmath.Vector3d;
import javax.vecmath.Color4f;
import javax.vecmath.Tuple3d;

public class Cube extends GraphNode {
	
	Vector3d _color;
	Vector3d _position;
	Vector3d _scale;
	
	public Cube(String name) {
		super(name);
		
		_scale = new Vector3d(1, 1, 1);
		_position = new Vector3d(0, 0, 0);
		_color = new Vector3d(1, 1, 1);
	}

	public Cube(String name, Vector3d pos, Vector3d scale, Vector3d color) {
		super(name);
		
		_scale = scale;
		_position = pos;
		_color = color;
	}
	
	public void setCentre(Tuple3d pos) {
		_position = (Vector3d) pos;
	}
	
	public void setScale(Tuple3d scale) {
		_scale = (Vector3d) scale;
	}
	
	public void setColor(Tuple3d color) {
		_color = (Vector3d) color;
	}

	@Override
	public void display( GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		//gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		//translate
		gl.glTranslated(_position.x, _position.y, _position.z);
		
		//store the matrix of transformation
		gl.glPushMatrix();
		
		//scale
		//if we put scale before push matrix then all the children will be scaled as well
		gl.glScaled(_scale.x, _scale.y, _scale.z);
		gl.glColor3d(_color.x, _color.y, _color.z);		
		
		glut.glutSolidCube(1);
		
		super.display(drawable);
		gl.glPopMatrix();
	}
}
