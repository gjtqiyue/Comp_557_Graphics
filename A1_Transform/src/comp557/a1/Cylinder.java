package comp557.a1;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

public class Cylinder extends GraphNode {

	Vector3d _color;
	Vector3d _position;
	Vector3d _scale;

	public Cylinder(String name) {
		super(name);
		
		_scale = new Vector3d(1, 1, 1);
		_position = new Vector3d(0, 0, 0);
		_color = new Vector3d(1, 1, 1);
	}

	public Cylinder(String name, Vector3d pos, Vector3d scale, Vector3d color) {
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
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		GLU glu = new GLU();
		
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
		gl.glRotated(90, 1, 0, 0);
		gl.glScaled(_scale.x, _scale.y, _scale.z);
		gl.glColor3d(_color.x, _color.y, _color.z);				
		
		draw_cylinder(gl, 0.5, 1);
		
		super.display(drawable);
		gl.glPopMatrix();
	}
	
	void draw_cylinder(GL2 gl, double radius, double height) {
		double x              = 0.0f;
		double z              = 0.0f;
		double angle          = 0.0f;
		double angle_stepsize = 0.1f;

		/** Draw the tube */
		gl.glColor3d(_color.x, _color.y, _color.z);
		gl.glBegin(gl.GL_QUAD_STRIP);
		angle = 0.0f;
		while( angle < 2*Math.PI ) {
		    x = radius * Math.cos(angle);
		    z = radius * Math.sin(angle);
		    Vector3d n = new Vector3d(x, 0.0, z);
		    n.normalize();
		    gl.glNormal3d(n.x, n.y, n.z);
		    gl.glVertex3d(x, 0 , z);
		    gl.glVertex3d(x, height , z);
		    angle = angle + angle_stepsize;
		}
		gl.glVertex3d(radius, 0.0, 0.0);
		gl.glVertex3d(radius, height, 0.0);
		
		gl.glEnd();
		
		/** Draw the circle on top of cylinder */
		gl.glColor3d(_color.x, _color.y, _color.z);
		gl.glBegin(gl.GL_POLYGON);
		angle = 0.0;
		gl.glNormal3d(0.0, 1.0, 0.0);
		while( angle < 2*Math.PI ) {
			x = radius * Math.cos(angle);
			z = radius * Math.sin(angle);
			gl.glVertex3d(x, height , z);
			angle = angle + angle_stepsize;
	 	}
		gl.glVertex3d(radius, height, 0.0);
	 	gl.glEnd();
	 	
	 	/** Draw the circle on top of cylinder */
		gl.glColor3d(_color.x, _color.y, _color.z);
		gl.glBegin(gl.GL_POLYGON);
		angle = 0.0;
		gl.glNormal3d(0.0, -1.0, 0.0);
		while( angle < 2*Math.PI ) {
			x = radius * Math.cos(angle);
			z = radius * Math.sin(angle);
			gl.glVertex3d(x, 0 , z);
			angle = angle + angle_stepsize;
	 	}
		gl.glVertex3d(radius, 0.0, 0.0);
	 	gl.glEnd();
	}
	

}
