package org.bsheehan.android.fractalien.core;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import org.bsheehan.android.fractalien.core.function.IteratedFunctionFactory.FractalType;
import org.bsheehan.android.fractalien.core.utils.ESShader;
import org.bsheehan.android.fractalien.core.utils.ESShapes;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.SystemClock;
import android.util.Log;

/**
 * @author bsheehan@baymoon.com
 * @date April 12, 2011
 * 
 * @name FractalienGlRenderer
 * 
 * @description Extends Android GLSurfaceViewRender for customizing the OpenGl scene
 * displayed by the Activity. 
 */
public class FractalienCubeGlRendererNoShader implements GLSurfaceView.Renderer {

	/**
	 * Constructor
	 * @param context
	 */
	public FractalienCubeGlRendererNoShader(Context context) {


		// create the cube vertices for rendering
		this.cubePrimative.genCube(1.0f);
	}

	/**
	 * This method is called to change update the texture state with the latest fractal 
	 * bitmap.
	 * @return textureMap ID updated
	 */
	private int updateTexture() {

		// glTexImage2D accepts a level integer parameter for mip mapping. 
		// Currently we are not using hierarchical texturing so set this to zero.
		final int level = 0;

		// set the texture ID to bind future texture attribute operations to.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIds[0]);

		Log.i("bob", "texture bound" + this.fractal.getWidth() + " " + this.fractal.getHeight());

		// this call assigns texture pixels to the OpenGl texture memory
		// TODO: investigate glTexSubImage2D for performance boost.
		GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, level, GLES20.GL_RGBA,
				this.fractal.getWidth(), this.fractal.getHeight(), 0, GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE, this.fractal.getBufferColors());

		// set the texture interpolation to higher quality
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		// lets not repeat the texture. just one fractal bitmap per cube side
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);

		// This fixes a strange artifact of texture mapping where the texture appears to warp 
		// when rotated in the projection matrix for the scene. 
		final FloatBuffer largest = FloatBuffer.allocate(1);
		GLES20.glGetFloatv(GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
				largest);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest.get());

		return this.textureIds[0];
	}

	// CAlled when surface is created at startup.
	// Initialize the shader and program object
	//
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
	    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    gl.glEnable(GL10.GL_TEXTURE_2D);
	      // Define the lighting
	      float lightAmbient[] = new float[] { 0.2f, 0.2f, 0.2f, 1 };
	      float lightDiffuse[] = new float[] { 1, 1, 1, 1 };
	      float[] lightPos = new float[] { 1, 1, 1, 1 };
	      gl.glEnable(GL10.GL_LIGHTING);
	      gl.glEnable(GL10.GL_LIGHT0);
	      gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0);
	      gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0);
	      gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);

		// Generate texture IDs for texture mapping
		GLES20.glGenTextures(1, this.textureIds, 0);

		// Starting rotation angle for the cube
		this.currentAngle = 0.0f;

		gl.glCullFace(GLES20.GL_BACK);
		gl.glEnable(GLES20.GL_CULL_FACE);
	    gl.glDepthFunc(GLES20.GL_LEQUAL);
	    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		// Fire off the producer/consumer threads that update the fractal RGB buffer, and set a flag
		// for the rendering thread to update the texture map.
		launchThreads();
	}

	/** 
	 * Two threads are launched. One that generates fractal bitmaps, and one that checks to
	 * see if they are created. 
	 * 
	 *	In order to avoid user wait time to see a texture, we show low resolution textures
	 * 	increasing the resolution each step until a full res texture is created. Until we get a
	 *  full resolution texture, we stick with the same fractal region in the complex plane.
	 *  
	 *  Each time a particular resolution is generated, we notify the consuming thread that a textxure
	 *  is available. This thread sets a flag in the OpenGl rendering thread that allows the texture memory
	 *  to map to the newly produced fractal bitmap.
	 */
	private void launchThreads() {
		this.fractal = FractalFactory.createFractal(FractalType.JULIASET);
		this.killThreads = false;

		// go forth and create fractal bitmaps
		this.produceFractalThread = new Thread(new Runnable() {

			private int dim = FractalienCubeGlRendererNoShader.this.kMaxDim;

			@Override
			public void run() {
				while (!FractalienCubeGlRendererNoShader.this.killThreads) {
					if (!FractalienCubeGlRendererNoShader.this.fractalProduced) {
						synchronized (FractalienCubeGlRendererNoShader.this.lock) {
							// increment fractal sampling resolution
							this.dim <<= 1;

							// If we have exceeded the max fractal sample resolution, then reset to a new fractal
							// and lowest resolution to start process of iteratively rendering
							// successively higher resolution fractal textures. This way the user will see the fractal
							// start off as low resolution textures and progressively display at higher resolutions. 
							// This avoids a lag time in user experience if we only displayed highest resolution. 
							// When we achieve the highest res, then sleep for a few seconds to enjoy the artwork !
							if (this.dim > FractalienCubeGlRendererNoShader.this.kMaxDim) {
								FractalFactory.makeItCool(FractalienCubeGlRendererNoShader.this.fractal, true);
								this.dim = 1;
								FractalienCubeGlRendererNoShader.this.fractal.setDims(this.dim, this.dim);
							}

							// Generate a fractal by updating the pixel buffer with assigned RGB values.
							// Once complete the OpenGl rendering thread can access this buffer for texture map binding.
							FractalFactory.generateFullFractal(FractalienCubeGlRendererNoShader.this.fractal, this.dim);
							FractalienCubeGlRendererNoShader.this.fractalProduced = true;
							Log.d("bob", "produced");

							// Notify waiting threads that we have a new bitmap
							FractalienCubeGlRendererNoShader.this.lock.notifyAll();

							// Lets only sit around a bit so we can continue creating
							// higher res bitmaps of the currently fractal region
							int sleep = 500;

							// Now that we finally have a full res fractal, lets kick back and check it 
							// out for a few seconds, before continuing our journey.
							if (this.dim > FractalienCubeGlRendererNoShader.this.kMaxDim)
								sleep = 5000;
							try {
								Thread.sleep(sleep);
							} catch (final InterruptedException e) {
								// if we interrupt the sleeping thread an exception will get us here.
								return;
							}
						}
					}
				}
			}
		});

//		// THIS THREAD IS FOR DEMONSTRATION PURPOSES. A CONSUMER THREAD TO MATCH THE PRODUCER THREAD.
//		// Patiently wait for notifications that new fractal bitmaps have been created
//		// Not currently used for this version. But may come in handy.
//		this.consumeFractalThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (!FractalienCubeGlRendererNoShader.this.killThreads) {
//					if (FractalienCubeGlRendererNoShader.this.fractalProduced) {
//						synchronized (FractalienCubeGlRendererNoShader.this.lock) {
//							try {
//								Log.d("bob", "consumed");
//
//								// wait for notification from producer thread that a new pixel buffer is ready.
//								FractalienCubeGlRendererNoShader.this.lock.wait();
//							} catch (final InterruptedException e) {
//								// if we interrupt the waiting thread an exception will get us here.
//								return;
//							}
//						}
//					}
//				}
//			}
//		});

		// start the threads
		this.produceFractalThread.start();
		//NOT USEFUL AT THIS TIME. FOR DEMO PURPOSES ONLY
		//this.consumeFractalThread.start(); 	
	}


	// OpenGl event driven call to render the surface
	public void onDrawFrame(GL10 gl) {

		// set to true by fractal producing thread
		if (this.fractalProduced){
			// Update OpenGL texture memory.
			updateTexture();
			// now that texture has been mapped the fractal buffer can be updated
			// by the producer thread. Set this to false will allow the producer thread
			// to go and create the next texture.
			this.fractalProduced = false;
		}

		if (this.lastTime == 0)
			this.lastTime = SystemClock.uptimeMillis();
		final long curTime = SystemClock.uptimeMillis();
		final long elapsedTime = curTime - this.lastTime;
		final float deltaTime = elapsedTime / 10000.0f;
		this.lastTime = curTime;

		// Compute a rotation angle based on time to rotate the cube
		// This will keep the rotation the same on fast and slow platforms
		this.currentAngle += (deltaTime * 40.0f);
		if (this.currentAngle >= 360.0f)
			this.currentAngle -= 360.0f;


		// Clear the color buffer
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


	      // Position model so we can see it
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glRotatef(this.currentAngle, 1, 0, 0);
		gl.glTranslatef(0, 0, -3.0f);
	    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, this.cubePrimative.getVertices());
	   // gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer);
		// Bind the texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIds[0]);
	    gl.glDrawElements(GL10.GL_TRIANGLES, 
	    		this.cubePrimative.getNumIndices(), 
	    		GL10.GL_UNSIGNED_SHORT, 
	    		this.cubePrimative.getIndices());
	 
		//drawSingleShape(0, 0, -2, this.textureIds[0]);

	}

	/**
	 * This will draw a single primitive into the 3D scene
	 * @param i
	 * @param j
	 * @param k
	 * @param texIndex
	 */
	private void drawSingleShape(int i, int j, int k, int texIndex) {
		//update(i * 1.5f, j * 1.5f, k);
		// Draw the cube
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 
				this.cubePrimative.getNumIndices(),
				GLES20.GL_UNSIGNED_SHORT, 
				this.cubePrimative.getIndices());
	}

	//
	// Handle surface changes
	//
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		this.width = w;
		this.height = h;

		// Compute the window aspect ratio
		this.aspect = (float)this.width / (float) this.height;
		// Generate a perspective matrix with a 60 degree FOV
//		this.perspective.matrixLoadIdentity();
//
//		if (this.aspect > 1.0f)
//			this.perspective.perspective(60.0f, this.aspect, 1.0f, 10.0f);
//		else
//			this.perspective.perspective(80.0f, this.aspect, 1.0f, 10.0f);

	    // Define the view frustum
	    gl.glViewport(0, 0, width, height);
	    gl.glMatrixMode(GL10.GL_PROJECTION);
	    float ratio = (float) width / height;
	    GLU.gluPerspective(gl, 45.0f, ratio, 1, 100f);


	}

	/**
	 * Update the Model View matrix with animated transformations (e.g. rotations)
	 * @param x
	 * @param y
	 * @param z
	 */
	private void update(float x, float y, float z) {

	
		// Generate a model view matrix to rotate/translate the cube
		//this.modelview.matrixLoadIdentity();

		// Translate away from the viewer
		//this.modelview.translate(x, y, z);

		// Rotate the cube
		//this.modelview.rotate(this.currentAngle, 1.0f, 0.0f, 0.0f);
		//this.modelview.rotate(this.currentAngle*.25f, 0.0f, 1.0f, 0.0f);

		// Compute the final MVP by multiplying the
		// modevleiw and perspective matrices together
		//this.mMVPMatrix.matrixMultiply(this.modelview.get(), this.perspective.get());
	}

	// For debug. Report OpenGl Error
	static void checkGLError(GL gl) {
		final int error = GLES20.glGetError();
		if (error != GLES20.GL_NO_ERROR) {
			throw new RuntimeException("GLError 0x" + Integer.toHexString(error));
		}
	}

	public void onDestroy() {
		// kill off threads
		this.killThreads = true;
		// delete shaders and program from GPU state.
		ESShader.cleanup();
	}
	// Attribute locations
	//private int positionLoc;
	//private int texCoordLoc;

	// Additional member variables
	private int width;
	private int height;

	// Vertex data
	private final ESShapes cubePrimative = new ESShapes();

	// Rotation angle
	private float currentAngle;

	// MVP matrix
	//private final ESTransform mMVPMatrix = new ESTransform();

	private long lastTime = 0;

	private Thread produceFractalThread;
	private Thread consumeFractalThread;

	//private final ESTransform perspective = new ESTransform();
	//private final ESTransform modelview = new ESTransform();

	// Texture object handle
	private final int[] textureIds = new int[1];

	// this all important value specifies the final fractal bitmap resolution
	// that will generated. It is a factor of 2 to work with OpenGl texture mapping
	// functions. TODO: This should be adjusted dynamically for device screen res.
	private final int kMaxDim = 64;

	private float aspect;

	// The value of these variables will never be cached thread-locally: 
	// all reads and writes will go straight to "main memory"; 
	volatile private IFractal fractal;
	volatile protected boolean fractalProduced = false;

	// lock used for synchronizing the fractal bitmap produce/consume threads
	private final Object lock = new Object();
	protected boolean killThreads;


}
