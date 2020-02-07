package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import javax.vecmath.*;

public class Wing extends GraphNode {
	
	Vector3d _color;
	Vector3d _position;
	Vector3d _scale;

	public Wing(String name) {
		super(name);
		
		_scale = new Vector3d(1, 1, 1);
		_position = new Vector3d(0, 0, 0);
		_color = new Vector3d(1, 1, 1);
	}

	public Wing(String name, Vector3d pos, Vector3d scale, Vector3d color) {
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
			
		//wing at the back
		gl.glPushMatrix();
		gl.glTranslated(0, 0, -0.4);
		gl.glScaled(_scale.x, _scale.y, _scale.z);
		gl.glRotated(-30, 0, 1, 0);
		
		gl.glColor3d(_color.x, _color.y, _color.z);	
		drawWing(gl);
		gl.glPopMatrix();
		
		//wing in the front
		gl.glPushMatrix();
		gl.glScaled(_scale.x, _scale.y, _scale.z);
		gl.glTranslated(0, 0, 0.45);
		
		gl.glColor3d(_color.x, _color.y, _color.z);	
		drawWing(gl);
		gl.glPopMatrix();
		
		super.display(drawable);
		gl.glPopMatrix();
	}

	private void drawWing(GL2 gl) {
		gl.glPushMatrix();
		gl.glTranslated(-2, 0, 0);
		gl.glScaled(4, 0.24, 0.3);
		glut.glutSolidCube(1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(-2.25, 0, 0.28);
		gl.glScaled(3.5, 0.23, 0.3);
		glut.glutSolidCube(1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(-2.5, 0, 0.58);
		gl.glScaled(3, 0.22, 0.3);
		glut.glutSolidCube(1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(-3.25, 0, 0.34);
		gl.glScaled(2.5, 0.22, 1);
		glut.glutSolidCube(1);
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		gl.glTranslated(-4.5, -0.11, 0.34);
		draw_cylinder(gl, 0.5, 0.22);
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
