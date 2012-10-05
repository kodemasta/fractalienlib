package org.bsheehan.android.fractalien.core.function;

import org.bsheehan.android.fractalien.core.utils.Complex;

import android.graphics.PointF;
import android.graphics.RectF;

/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name JuliaSetFunction
 * @description A Julia set iterated function generator class
 */
public class JuliaSetFunction extends AbstractFractalFunction {

	/**
	 * Constructor
	 */
	public JuliaSetFunction() {		
		reset();
	}

	public void reset() {
		this.left = -2.0f;
		this.top = -2.0f;
		this.right = 2.0f;
		this.bottom = 2.0f;

		this.fractalRegion = new RectF(this.left, this.top, this.right, this.bottom);
	}

	/**
	 * This iterates over the z variable with fixed c for z = z^2 + c
	 * for definition of various Julia Sets for a given initial Z.
	 */
	@Override
	public short iterate(Complex z) {
		return this.iterateSelf(z, this.c);
	}

	/**
	 * We override here to allow a different range of zoom levels then the
	 * base class allows. We ant to see full Julia Sets from time to time.
	 */
	@Override
	public void setRandomRegion(boolean centered) {
		reset();
		final float cX = (float) Math.random() * (this.right-this.left)/2 - this.right/2;
		final float cY = (float) Math.random() * (this.bottom-this.top)/2 - this.bottom/2;

		setInitialConditions(new Complex(0.0, 0.0), new Complex(cX, cY));
		if (!centered){
			final float centerX = (float) Math.random() * (this.right-this.left) - this.right;
			final float centerY = (float) Math.random() * (this.bottom-this.top) - this.bottom;
			setCenter(new PointF(centerX, centerY));
		}
		final float zoom = (float) Math.random();
		if (zoom < 1.0f && zoom > 0.0f)
			setZoom(zoom);
	}

	/**
	 * This will iterate the quadratic equation z = z^2 + c in the complex
	 * plane. The result will be an orbit that is stable or diverges to the
	 * attractor at infinity. This method returns the number of iterations it
	 * takes for this iterator to escape a specific radius. If it takes more
	 * than a maximum number of iterations, the max number of iterations is
	 * returned.
	 * 
	 * @param z - complex variable z
	 * @param c - complex constant c
	 * @return - number of iterations for magnitude to exceed certain value, kEscapeRadius
	 */
	private short iterateSelf(Complex z, Complex c) {
		// if recursive calls do not create escaping orbit, then return the max
		// iteration allowed.

		final Complex currZ = new Complex(z.getReal(), z.getImaginary());
		for (short i = 0; i < this.maxIterations; ++i){
			currZ.squared();
			currZ.add(c);
			if (currZ.square() > this.escapeRadius)
				return i;
		}
		return (short) (this.maxIterations-1);
	}
}
