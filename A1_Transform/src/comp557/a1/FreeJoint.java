package comp557.a1;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;

import mintools.parameters.DoubleParameter;

public class FreeJoint extends GraphNode {

	DoubleParameter tx;
	DoubleParameter ty;
	DoubleParameter tz;
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
		
	public FreeJoint( String name ) {
		super(name);
		dofs.add( tx = new DoubleParameter( name+" tx", 0, -5, 5 ) );		
		dofs.add( ty = new DoubleParameter( name+" ty", 0, -5, 5 ) );
		dofs.add( tz = new DoubleParameter( name+" tz", 0, -5, 5 ) );
		dofs.add( rx = new DoubleParameter( name+" rx", 0, -360, 360 ) );		
		dofs.add( ry = new DoubleParameter( name+" ry", 0, -360, 360 ) );
		dofs.add( rz = new DoubleParameter( name+" rz", 0, -360, 360 ) );
	}
	
	@Override
	public void display( GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		// TODO: implement the rest of this method​‌​​​‌‌​​​‌‌​​​‌​​‌‌‌​​‌	
        int width = drawable.getSurfaceWidth();
        int height = drawable.getSurfaceHeight();     
		GLU glu = new GLU();
		//gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
//        gl.glMatrixMode(GL2.GL_PROJECTION);
//        gl.glLoadIdentity();
//        // Calculate The Aspect Ratio Of The Window
//        float aspectRatio = (float) width / (float) height;
//        glu.gluPerspective(45f, aspectRatio, 0.1f, 100);
        
		gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        //camera location
        glu.gluLookAt(0, 20, 20, 0, 0, 0, 0, 1, 0);
        
        //rotate along x degree
        gl.glRotated(rx.getFloatValue(), 1, 0, 0);
        //rotate along y degree
        gl.glRotated(ry.getFloatValue(), 0, 1, 0);
        //rotate along z degree
        gl.glRotated(rz.getFloatValue(), 0, 0, 1);
        //translate
        gl.glTranslated(tx.getFloatValue(), ty.getFloatValue(), tz.getFloatValue());
        //gl.glScaled(t,1,1);
        gl.glPushMatrix();
        
        //glut.glutSolidSphere(0.1f, 20, 20);
        
        super.display(drawable);
        
        gl.glPopMatrix();
	}

	
}
