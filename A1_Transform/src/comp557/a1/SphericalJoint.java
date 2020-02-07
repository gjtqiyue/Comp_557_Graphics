package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import mintools.parameters.DoubleParameter;

import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

public class SphericalJoint extends GraphNode {
	
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
	
	Vector3d _position;
	
	public SphericalJoint(String name) {
		super(name);
		
		_position = new Vector3d(0f, 0f, 0f);
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -90, 90 ) );		
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -45, 45 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -30, 45 ) );
	}

	public SphericalJoint(String name, Vector3d pos, Vector3d rotation) {
		super(name);
		
		_position = pos;
		dofs.add( rx = new DoubleParameter( name+" rx", rotation.x, -90, 90 ) );		
		dofs.add( ry = new DoubleParameter( name+" ry", rotation.y, -45, 45 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", rotation.z, -30, 45 ) );
	}
	
	public void setPosition(Tuple3d pos) {
		_position = (Vector3d) pos;
	}
	
	@Override
	public void display( GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		gl.glTranslated(_position.x, _position.y, _position.z);
		
        //rotate along x degree
        gl.glRotated(rx.getFloatValue(), 1, 0, 0);
        //rotate along y degree
        gl.glRotated(ry.getFloatValue(), 0, 1, 0);
        //rotate along z degree
        gl.glRotated(rz.getFloatValue(), 0, 0, 1); 
		
		gl.glPushMatrix();
		
		//glut.glutSolidCube(1);
		
		super.display(drawable);
		
		gl.glPopMatrix();
	}
}
