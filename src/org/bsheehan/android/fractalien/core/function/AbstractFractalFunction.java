
package org.bsheehan.android.fractalien.core.function;

import org.bsheehan.android.fractalien.core.utils.Complex;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name AbstractFractalFunction
 * @description Base class for iteration functions on the complex plane. 
 */
public abstract class AbstractFractalFunction implements IIteratedFunction {

	/** This is a limit for the iteration to break at. Successive iterations will
	 * converge or diverge at a particular rate based on the initial location and iterative 
	 * function in the complex plane. The 'velocity' of the iteration escape is the metric used to
	 * map an RGB color for function display **/
	protected float escapeRadius = 4.0f;

	/** This is a maximum limit on number of iterations if escape radius not met. This is a critical
	 * parameter and directly maps to the runtime memory footprint of the application. Larger values
	 * allow for finer grain detail iteration orbits before they are considered escaped.   **/
	protected short maxIterations = 1024;

	/** the rectangular region that the iterative method is defined over **/
	protected float top, bottom, left, right;

	/** complex plane region iterated over **/
	protected RectF fractalRegion;

	/** The complex quadratic constant used during iteration **/
	protected Complex c = new Complex(0, 0);

	/** The complex starting origin used during iteration **/
	protected Complex z0 = new Complex(0, 0);

	public short getMaxIterations() {
		return this.maxIterations;
	}

	public RectF getFractalRegion() {
		return this.fractalRegion;
	}

	/**
	 * For each iteration for the function set the initial values on the complex plane.
	 */
	public void setInitialConditions(Complex z0, Complex c) {
		this.z0 = z0;
		this.c = c;
	}

	/** 
	 * Set the center point of where a particular rectangular region of the function is to be calculated.
	 **/
	public void setCenter(PointF center) {
		this.fractalRegion.offset(center.x - this.fractalRegion.centerX(),
				center.y - this.fractalRegion.centerY());

	}

	/**
	 * Randomly create a region in the complex boundary of interest.
	 */
	public void setRandomRegion(boolean centered) {
		reset();
		boolean validCenter = false;
		final Complex center = new Complex(0,0);

		if (!centered){
			while (!validCenter){
				center.setValues(Math.random() * (this.right-this.left) - this.right, Math.random() * (this.bottom-this.top) - this.bottom);
				validCenter = !isPointInCardioidBulbs(center);
			}
			
			setCenter(new PointF((float)center.getReal(), (float)center.getImaginary()));
			}
		final float zoom = (float) Math.random();
		setZoom(zoom);
	}

	/**
	 * Lifted this optimization off Wikipedia. The central cardioid bulbs in the Mandelbrot set are guaranteed to generate
	 * orbits that converge. This is the least optimal type of iteration that will always hit kMaxIterations.
	 * If we detect a calculation in these bulbs, we can just set the iteration to kMaxIterations and move on.
	 * @param z
	 * @return
	 */
	protected boolean isPointInCardioidBulbs(Complex z) {
		final double r = z.getReal();
		final double i = z.getImaginary();
		final double term1 = r-.25;
		final double term2 = i*i;
		final double q = term1*term1 + term2;
		return q*(q+term1) < .25*term2;
	}

	/**
	 * Set region zoom about the center point of defined boundary region.
	 */
	public void setZoom(float zoom) {
		final float centerX = this.fractalRegion.centerX();
		final float centerY = this.fractalRegion.centerY();
		final float halfWidth = this.fractalRegion.width() * zoom * .5f;
		final float halfheight = this.fractalRegion.height() * zoom * .5f;
		this.fractalRegion.left = centerX - halfWidth;
		this.fractalRegion.right = centerX + halfWidth;
		this.fractalRegion.top = centerY - halfheight;
		this.fractalRegion.bottom = centerY + halfheight;
	}
	
	public void setScale(float screenAspectRatio)
	{
		
		this.fractalRegion.left *= screenAspectRatio;
		this.fractalRegion.right *= screenAspectRatio;
	}

}
