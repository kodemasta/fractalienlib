package org.bsheehan.android.fractalien.core;

import java.nio.Buffer;

import org.bsheehan.android.fractalien.core.function.IIteratedFunction;

import android.graphics.Color;


/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name IFractal
 * @description Interface for fractal generator implementors
 */
public interface IFractal {

	/** Set the display resolution **/
	public void setDims(int currDim, int currDim2);

	/** Generate an RGB buffer from the iterated function for this fractal **/
	public boolean generate();

	/** Assign RGB values for the calculated array of iteration values for this fractal **/
	public void assignColors();

	/** Set a color mapping for this fractal. This results in the displayed color palette. **/
	public void setRandomColorSet();

	/** First pass check to see if selected region is of visual interest **/
	public boolean isInterestingAtAll(int w, int h);

	/** Second pass check using histogram to determine if fractal region is of visual interest **/
	public boolean isCoolEnough();

	/**
	 * Return pixel height of generated fractal
	 * @return
	 */
	public int getHeight();

	/** Retrieve the RGB buffer values for use in texture mapping **/
	public Buffer getBufferColors();

	/**
	 * Return pixel width of generated fractal
	 * @return
	 */
	public int getWidth();

	/** Set the iterated fractal method for use when generating fractals **/
	public void setFractalFunction(IIteratedFunction randomFunction);

	/** Get the iterated fractal method for use when generating fractals **/
	public IIteratedFunction getFractalFunction();

	public void setScale(float f);

	public void assignColor(int blue);
}
