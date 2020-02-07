package comp557.a2;

import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.stereo.StereoDeviceRenderer.Eye;

import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.parameters.IntParameter;
import mintools.parameters.Parameter;
import mintools.parameters.ParameterListener;
import mintools.swing.VerticalFlowPanel;
import mintools.viewer.FlatMatrix4d;

import java.lang.Math.*;

/**
 * Depth of field (DOF) Dolly camera parameters
 * 
 * Note that many of these parameters have limits that help avoid bad settings
 * but not everything will necessarily work perfectly in some settings.  For 
 * instance, nothing is preventing you from setting your near plane past your 
 * far plane!  There will be other bad settings in this assignment too.
 * 
 * @author kry
 */
public class DOFCamera {

	DoubleParameter near = new DoubleParameter( "near plane distance m", 0.25, 0.25, 20 );
	DoubleParameter far  = new DoubleParameter( "far plane distance m", 80, 10, 200 );
	DoubleParameter focusDesired = new DoubleParameter( "focus plane distance m", 15, 0.4, 200 );
	DoubleParameter fstop = new DoubleParameter( "f-stop", 1.2, 1.2, 22 );
	DoubleParameter focalLength = new DoubleParameter( "focal length mm", 35, 18, 500 );     
	DoubleParameter fovy = new DoubleParameter( "fov y degrees", 34, 5, 90 );
	DoubleParameter sensorHeight = new DoubleParameter( "sensor height mm", 24, 10, 100 ); // limits?
	DoubleParameter dolly = new DoubleParameter( "dolly m", 15, 0.5, 30 );
	
	/** Enables follow focus when you change the dolly parameter */
	BooleanParameter dollyFocus = new BooleanParameter( "enable dolly focus", false );
	
	/** Enables follow zoom when you change the dolly parameter */
	BooleanParameter dollyZoom = new BooleanParameter( "enable dolly zoom", false );
	
	/** This exponential filter parameter provides a bit of an animation effect to some parameters */
	DoubleParameter filterRate = new DoubleParameter( "filter rate", 0.5, 0, 1 );

	/** Focus point in world */
	Point3d focusPoint = new Point3d( 0, 0, 0 );
	
	/** desired look at point in world, which drives the actual temporally filtered position */
	Point3d lookAtDesired = new Point3d( 0, 0.5, 0 );
	/** desired eye position in world, which drives the actual temporally filtered position */
	Point3d eyeDesired = new Point3d( -5, 0.75, 10 );

	/** temporally filtered focus parameter */
	double focusDistance = 15; 
	/** temporally filtered look at position */
	Point3d lookAt = new Point3d( 0, 1, 0 );
	/** temporally filtered eye position */
	Point3d eye = new Point3d( 0, 0, 10 );
	
	/** samples to average to create depth of field blur */
	IntParameter samples = new IntParameter( "samples", 8, 1, 20 );    

	BooleanParameter drawWithBlur = new BooleanParameter("draw with DOF blur", false );
	
    /** Helper class for creating a number of Poisson disk random samples in a region */
    private final FastPoissonDisk fpd = new FastPoissonDisk();
    
    // focus plane height
    private Point3d tl, tr, bl, br;
    
    /**
     * Gentle temporal filtering of the look at point, eye position, and focus distance
     * TODO OBJECTIVE 8: Note you will want to use the filtered values for dolly focus and dolly zoom 
     */
    public void updateFilteredQuantities() {
    	double alpha = filterRate.getValue();
    	final Vector3d v = new Vector3d();
    	
    	focusDistance = alpha * focusDistance + (1-alpha) * focusDesired.getValue();
    	    	
    	v.scale( 1 - alpha, lookAtDesired );
    	lookAt.scale( alpha );
    	lookAt.add( v );
    	    	
    	v.scale( 1 - alpha, eyeDesired );
    	eye.scale( alpha );
    	eye.add( v );    
    	
		if ( dollyFocus.getValue() ) {
	    	// TODO OBJECTIVE 8: Set the focusDistance based on the dolly
			final Vector3d viewDir = new Vector3d();
			viewDir.sub( lookAt, eye );
			viewDir.normalize();
			final Vector3d focusDir = new Vector3d();
			focusDir.sub( focusPoint, eye );
			double distance = focusDir.length();
			focusDir.normalize();
			double dot = focusDir.x * viewDir.x + focusDir.y * viewDir.y + focusDir.z * viewDir.z;
			
			focusDistance = distance * dot;
		}
		
		if ( dollyZoom.getValue() ) {
			// TODO OBJECTIVE 8: Set the focal length based on the dolly
			// Hint: store the size (e.g., height) of the focus plane rectangle
			// and make sure it stays constant with respect to the other parameters
			double ratio = sensorHeight.getValue() / 2 / tl.y;
			focalLength.setValue(focusDistance * ratio);
		}
    }
	
    /** Used to let dependent parameters update one another */
    boolean ignoreParameterChangeCallback = false;
     
    JPanel getControls() {
    	VerticalFlowPanel vfp = new VerticalFlowPanel();
    	vfp.add( near.getSliderControls(false) );
    	vfp.add( far.getSliderControls(false) );
    	vfp.add( focusDesired.getSliderControls(false) );
    	vfp.add( focalLength.getSliderControls(false) );
    	vfp.add( fovy.getSliderControls(false) );
    	vfp.add( fstop.getSliderControls(false) );
    	vfp.add( sensorHeight.getSliderControls(false) );
    	vfp.add( dolly.getSliderControls(false) );    	
    	vfp.add( dollyFocus.getControls() );
    	vfp.add( dollyZoom.getControls() );    	
    	vfp.add( filterRate.getSliderControls( false ) );
    	vfp.add( samples.getSliderControls() );
    	vfp.add( drawWithBlur.getControls() );
    	
    	dolly.addParameterListener( new ParameterListener<Double>() {			
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				// update the target eye position given the new dolly parameters
				double v = dolly.getValue();
				final Vector3d viewDir = new Vector3d();
				viewDir.sub( lookAtDesired, eyeDesired );
				viewDir.normalize();
				viewDir.scale( v );
				eyeDesired.sub( lookAtDesired, viewDir );
			}
		});
    	focalLength.addParameterListener( new ParameterListener<Double>() {
    		@Override
    		public void parameterChanged(Parameter<Double> parameter) {
    			if ( ignoreParameterChangeCallback ) return;
    			// TODO OBJECTIVE 4: compute fovy, field of view in the y direction
    			double length = parameter.getValue();
    			double arctan = sensorHeight.getValue() / 2 / length;
    			double fov = 2 * Math.toDegrees(Math.atan(arctan));
    			//System.out.println("arctan: " + arctan + " fov: " + fov + "\n");
    			
    			double value = fov; // change this!

    			ignoreParameterChangeCallback = true;
    			fovy.setValue(value);
    			ignoreParameterChangeCallback = false;
    		}
		});
    	fovy.addParameterListener( new ParameterListener<Double>() {
    		@Override
    		public void parameterChanged(Parameter<Double> parameter) {
    			if ( ignoreParameterChangeCallback ) return;
    			// TODO OBJECTIVE 4: compute focal length given the field of view in the y direction
    			double fov_half = parameter.getValue() / 2;
    			double tan = Math.tan(Math.toRadians(fov_half));
    			double length = sensorHeight.getValue() / 2 / tan;
    			
    			double value = length; // change this!
    			
    			ignoreParameterChangeCallback = true;
    			focalLength.setValue( value );
    			ignoreParameterChangeCallback = false;
    		}
		});
    	
    	return vfp.getPanel();
    }
    
    /**
     * Calls glFrustum with appropriate parameters.
     * @param drawable
     * @param i which sample point to use to create a shifted frustum
     */
    public void setupProjection( GLAutoDrawable drawable, int i ) {
    	GL2 gl = drawable.getGL().getGL2();
    	
    	// TODO OBJECTIVE 1: Compute parameters to call glFrustum
    	double aspectRatio = (double)drawable.getSurfaceWidth() / (double)drawable.getSurfaceHeight();
    	double nearHalfHeight = near.getValue() * (sensorHeight.getValue() / 2 / focalLength.getValue());
    	double nearHalfWidth = nearHalfHeight * aspectRatio;
    	//System.out.println("h: " + nearHalfHeight + " w: " + nearHalfWidth + "\n");
    	
    	if (i == 0) {
    		//gl.glFrustum(0, nearHalfWidth*2, 0, nearHalfHeight*2, near.getValue(), far.getValue());
    		gl.glFrustum(-nearHalfWidth, nearHalfWidth, -nearHalfHeight, nearHalfHeight, near.getValue(), far.getValue());
    	}
    	else {
	    	// TODO OBJECTIVE 7: revisit this function for shifted perspective projection, if necessary
	    	final Point2d p = new Point2d();
	        double s = getEffectivePupilRadius();
		    fpd.get( p, i, samples.getValue() );
		    double ox = s * p.x; // eye offset from center + effective aperture displacement 
	        double oy = s * p.y;  
	       
	        double ratio = sensorHeight.getValue() / focalLength.getValue();
	    	double height = focusDistance * ratio / 2;
	    	double width = height * aspectRatio;
	        // originally it's 1 - near / focus, but in order to transform back to new camera space we need to subtract the shifted distance
	        // resulted formula is ox * (1 - near / focus - 1) = ox * ( - near / focus );
	        //double shifted_d_ratio = 1 - near.getValue() / Math.abs(tl.z);
	        
	        gl.glFrustum(	(-width - ox) * near.getValue() / focusDistance, 
	        				(width - ox) * near.getValue() / focusDistance, 
	        				(-height - oy) * near.getValue() / focusDistance, 
	        				(height - oy) * near.getValue() / focusDistance, 
	        				near.getValue(), 
	        				far.getValue()
	        );
    	}
    	
    	
    }
    
    
    /**
     * Creates a viewing transformation.
     * Note that you may or may not need to know which aperture sample is being drawn
     * @param drawable
     * @param i identifies which aperture sample
     */
    public void setupViewingTransformation( GLAutoDrawable drawable, int i ) {
    	GL2 gl = drawable.getGL().getGL2();
    	GLU glu = new GLU();
    	// TODO OBJECTIVE 1: Set up the viewing transformation
//    	gl.glMatrixMode(GL2.GL_MODELVIEW);
//    	gl.glLoadIdentity();
    	if (i == 0) {
    		glu.gluLookAt(eye.x, eye.y, eye.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
    	}
    	else {
	    	// TODO OBJECTIVE 7: revisit this function for shifted perspective projection, if necessary
	    	final Point2d p = new Point2d();
	        double s = getEffectivePupilRadius();
		    fpd.get( p, i, samples.getValue() );
		    double ox = s * p.x; // eye offset from center + effective aperture displacement 
	        double oy = s * p.y;
	        Point3d shift = new Point3d(ox, oy, 0);
	        
	        final FlatMatrix4d V = new FlatMatrix4d();
	        final FlatMatrix4d Vinv = new FlatMatrix4d();
	        
	        gl.glPushMatrix();
	        gl.glLoadIdentity();
	        glu.gluLookAt(eye.x, eye.y, eye.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
	        gl.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, V.asArray(), 0 );
			V.reconstitute();
			Vinv.getBackingMatrix().invert( V.getBackingMatrix() );
			gl.glPopMatrix();
			
			Point3d newEye = new Point3d(0, 0, 0);
			Vinv.getBackingMatrix().transform(shift, newEye);
			
			//glu.gluLookAt(newEye.x, newEye.y, newEye.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
	        glu.gluLookAt(newEye.x, newEye.y, newEye.z, lookAt.x + newEye.x - eye.x, lookAt.y + newEye.y - eye.y, lookAt.z + newEye.z - eye.z, 0, 1, 0);
    	}

    }
    
    /**
     * Draw the focus plane rectangle that exactly fits inside the frustum.
     * This is the rectangle that must stay fixed for all shifted perspective
     * frustums you create for a depth of field blur.
     * (expects to be drawing in coordinates of the camera view frame) 
     * @param drawable
     */
    public void drawFocusPlane( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();

    	// TODO OBJECTIVE 6: Draw the focus plane rectangle
    	double ratio = sensorHeight.getValue() / focalLength.getValue();
    	double height = focusDistance * ratio;
    	double aspectRatio = drawable.getSurfaceWidth() / drawable.getSurfaceHeight();
    	double width = height * aspectRatio;		// in mm unit
    	tl = new Point3d(-width/2, height/2, -focusDistance);
    	tr = new Point3d(width/2, height/2, -focusDistance);
    	bl = new Point3d(-width/2, -height/2, -focusDistance);
    	br = new Point3d(width/2, -height/2, -focusDistance);
    	
    	gl.glColor3f(0,0,1); 
    	gl.glPushMatrix();
    	gl.glDisable( GL2.GL_LIGHTING );
	    gl.glBegin( GL2.GL_LINE_LOOP );
	    // use gl.glVertex3d calls to specify the 4 corners of the rectangle
	    gl.glVertex3d(tl.x, tl.y, tl.z);
	    gl.glVertex3d(tr.x, tr.y, tr.z);
	    gl.glVertex3d(br.x, br.y, br.z);
	    gl.glVertex3d(bl.x, bl.y, bl.z);
	    gl.glEnd();
	    gl.glPopMatrix();
    }

    /** 
     * Draws the sensor plane 
     * (expects to be drawing in coordinates of the camera view frame) 
     */
    public void drawSensorPlane( GLAutoDrawable drawable ) {
	   
    	// TODO OBJECTIVE 2: Draw the sensor plane rectangle
    	double aspectRatio = drawable.getSurfaceWidth() / drawable.getSurfaceHeight();
    	double sensorWidth = sensorHeight.getValue() * aspectRatio;		// in mm unit
    	Vector3d tl = new Vector3d(-sensorWidth/2/1000, sensorHeight.getValue()/2/1000, -focalLength.getValue()/1000);
    	Vector3d tr = new Vector3d(sensorWidth/2/1000, sensorHeight.getValue()/2/1000, -focalLength.getValue()/1000);
    	Vector3d bl = new Vector3d(-sensorWidth/2/1000, -sensorHeight.getValue()/2/1000, -focalLength.getValue()/1000);
    	Vector3d br = new Vector3d(sensorWidth/2/1000, -sensorHeight.getValue()/2/1000, -focalLength.getValue()/1000);
    	
	    GL2 gl = drawable.getGL().getGL2();
	    gl.glColor3f(0,1,0); 
    	gl.glPushMatrix();
    	gl.glDisable( GL2.GL_LIGHTING );
	    gl.glBegin( GL2.GL_LINE_LOOP );
	    // use gl.glVertex3d calls to specify the 4 corners of the rectangle
	    gl.glVertex3d(tl.x, tl.y, tl.z);
	    gl.glVertex3d(tr.x, tr.y, tr.z);
	    gl.glVertex3d(br.x, br.y, br.z);
	    gl.glVertex3d(bl.x, bl.y, bl.z);
	    gl.glEnd();
	    gl.glPopMatrix();
    }

    /**
     * Draws samples on the aperture, i.e., the centers of projection 
     * for shifted perspective projections necessary to generate a 
     * depth of field blur.
     * (expects to be drawing in coordinates of the camera view frame) 
     * @param drawable
     */
    public void drawAperture( GLAutoDrawable drawable ) {
    	GL2 gl = drawable.getGL().getGL2();
    	gl.glDisable( GL2.GL_LIGHTING );
    	gl.glPointSize(3f);
    	gl.glBegin( GL.GL_POINTS );
    	final Point2d p = new Point2d();
        double s = getEffectivePupilRadius();
    	for ( int i = 0; i < samples.getValue(); i++ ) {
	    	fpd.get( p, i, samples.getValue() );
	        double ox = s * p.x; // eye offset from center + effective aperture displacement 
	        double oy = s * p.y;
	        gl.glVertex3d( ox, oy, 0 );
    	}
        gl.glEnd();
    }
    
    /** 
     * Computes the radius of displaced sample points to emulate a given f-stop 
     * for the current focal length and focus distance settings.
     * @return
     */
    private double getEffectivePupilRadius() {
	    double fl = focalLength.getValue() / 100;	//1000 before
	    double fd = -focusDistance; 
	    double f = 1/(1/fd+1/fl);
	    double r = f / fstop.getValue() / 2; // divide by 2 to get radius of effective aperture 
		return r; 
    }
    
}
