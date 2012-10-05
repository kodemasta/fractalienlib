package org.bsheehan.android.fractalien.core;

import org.bsheehan.android.fractalien.core.function.IteratedFunctionFactory;
import org.bsheehan.android.fractalien.core.function.IteratedFunctionFactory.FractalType;

import android.util.Log;

/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name FractalFactory
 * @description This creates and updates fractal instances for use in texture mapping. 
 */
public class FractalFactory {

	/**
	 * Create an instance of a fractal generator class
	 * @return
	 */
	static public IFractal createRandomFractal() {
		final IFractal fractal = new Fractal(IteratedFunctionFactory.createRandomFunction());		
		return fractal;
	}
	
	static public IFractal createFractal(FractalType type) {
		final IFractal fractal = new Fractal(IteratedFunctionFactory.createIteratedFunction(type));		
		return fractal;
	}

	/**
	 * For a given fractal, try various zoom in and region crops to
	 * locate a visually stunning section of the iterated complex system.
	 * 
	 * @param fractal - input fractal to zoom in on
	 * @return
	 */
	static public void makeItCool(IFractal fractal, boolean centered) {
		final int w = 32;
		final int h = 32;
		fractal.setDims(w, h);
		fractal.generate();
		do {
			do {
				fractal.getFractalFunction().setRandomRegion(centered);
			} while (!fractal.isInterestingAtAll(w, h));
			fractal.generate();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while (!fractal.isCoolEnough());

		// now that we have a cool region, lets assign a cool colormap
		fractal.setRandomColorSet();
	}

	/**
	 * Once a cool fractal region has been located using makeItCool, a full color mapped
	 * fractal can be generated using this call.
	 * 
	 * @param fractal to generate
	 * @param dim - desired screen size.
	 */
	static public void generateFullFractal(IFractal fractal, int dim) {

		// update the resolution and generate
		fractal.setDims(dim, dim);
		fractal.generate();
		fractal.assignColors();

		Log.i("bob", "fractal generated");
	}
}
