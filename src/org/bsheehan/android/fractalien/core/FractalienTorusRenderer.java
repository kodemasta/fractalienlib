package org.bsheehan.android.fractalien.core;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

import org.bsheehan.android.fractalien.core.utils.Grid;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

public class FractalienTorusRenderer implements GLSurfaceView.Renderer {
    private Grid mGrid;
    private float mAngle;

    public void onDrawFrame(GL10 gl) {
		// set to true by fractal producing thread
		if (this.fractalProduced){
			// Update OpenGL texture memory.
			generate2DTextureMap(gl);
			// now that texture has been mapped the fractal buffer can be updated
			// by the producer thread. Set this to false will allow the producer thread
			// to go and create the next texture.
			this.fractalProduced = false;
		}

        //checkGLError(gl);
        // Current context doesn't support cube maps.
        // Indicate this by drawing a red background.
        gl.glClearColor(0,0,0,0);
        
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();

        GLU.gluLookAt(gl, 0, 0, -9, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        gl.glRotatef(mAngle,        0, 1, 0);
        gl.glRotatef(mAngle*0.25f,  1, 0, 0);

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);


        //checkGLError(gl);
        mGrid.draw(gl);

        //checkGLError(gl);

        mAngle += 0.2f;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //checkGLError(gl);
        gl.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 12);
        //checkGLError(gl);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //checkGLError(gl);
        // This test needs to be done each time a context is created,
        // because different contexts may support different extensions.

        mGrid = generateTorusGrid(gl, 60, 60, 3.0f, 1.0f);

        launchThreads();
        //checkGLError(gl);
    }
    
	private void launchThreads() {
		this.fractal = FractalFactory.createRandomFractal();
		this.killThreads = false;

		// go forth and create fractal bitmaps
		this.produceFractalThread = new Thread(new Runnable() {

			private int dim = FractalienTorusRenderer.this.kMaxDim;

			@Override
			public void run() {
				while (!FractalienTorusRenderer.this.killThreads) {
					if (!FractalienTorusRenderer.this.fractalProduced) {
						synchronized (FractalienTorusRenderer.this.lock) {
							// increment fractal sampling resolution
							this.dim <<= 1;

							// If we have exceeded the max fractal sample resolution, then reset to a new fractal
							// and lowest resolution to start process of iteratively rendering
							// successively higher resolution fractal textures. This way the user will see the fractal
							// start off as low resolution textures and progressively display at higher resolutions. 
							// This avoids a lag time in user experience if we only displayed highest resolution. 
							// When we achieve the highest res, then sleep for a few seconds to enjoy the artwork !
							if (this.dim > FractalienTorusRenderer.this.kMaxDim) {
								FractalFactory.makeItCool(FractalienTorusRenderer.this.fractal, false);
								this.dim = 1;
								FractalienTorusRenderer.this.fractal.setDims(this.dim, this.dim);
							}

							// Generate a fractal by updating the pixel buffer with assigned RGB values.
							// Once complete the OpenGl rendering thread can access this buffer for texture map binding.
							FractalFactory.generateFullFractal(FractalienTorusRenderer.this.fractal, this.dim);
							FractalienTorusRenderer.this.fractalProduced = true;
							Log.d("bob", "produced");

							// Notify waiting threads that we have a new bitmap
							FractalienTorusRenderer.this.lock.notifyAll();

							// Lets only sit around a bit so we can continue creating
							// higher res bitmaps of the currently fractal region
							int sleep = 200;

							// Now that we finally have a full res fractal, lets kick back and check it 
							// out for a few seconds, before continuing our journey.
							if (this.dim == FractalienTorusRenderer.this.kMaxDim)
								sleep = 10000;
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

		// THIS THREAD IS FOR DEMONSTRATION PURPOSES. A CONSUMER THREAD TO MATCH THE PRODUCER THREAD.
		// Patiently wait for notifications that new fractal bitmaps have been created
		// Not currently used for this version. But may come in handy.
//		this.consumeFractalThread = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				while (!FractalienTorusRenderer.this.killThreads) {
//					if (FractalienTorusRenderer.this.fractalProduced) {
//						synchronized (FractalienTorusRenderer.this.lock) {
//							try {
//								Log.d("bob", "consumed");
//
//								// wait for notification from producer thread that a new pixel buffer is ready.
//								FractalienTorusRenderer.this.lock.wait();
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
//		this.consumeFractalThread.start(); 	
	}


    private void generate2DTextureMap(GL10 gl) {
		//IFractal fractal = FractalFactory.createFractal();
		//FractalFactory.makeItCool(fractal);
		//FractalFactory.generateFullFractal(fractal, 128);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
   		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		gl.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

   		// now we render into the mip map level of the last bound texture id
    	gl.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
				fractal.getWidth(), fractal.getHeight(), 0, GLES20.GL_RGBA,
				GLES20.GL_UNSIGNED_BYTE, fractal.getBufferColors());
    	
		//GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
		
		Log.i("bob", "texture bound" + fractal.getWidth() + " " +  fractal.getHeight());

		gl.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		gl.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		// the texture wraps over at the edges (repeat)
		gl.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_REPEAT);
		gl.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_REPEAT);

//		// TODO turn this on for actual device
//		FloatBuffer largest = FloatBuffer.allocate(1);
//		GLES20
//				.glGetFloatv(GLES11Ext.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT,
//						largest);
//		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
//				GLES11Ext.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest.get());

	}


    private Grid generateTorusGrid(GL gl, int uSteps, int vSteps, float majorRadius, float minorRadius) {
        Grid grid = new Grid(uSteps + 1, vSteps + 1);
        for (int j = 0; j <= vSteps; j++) {
            double angleV = Math.PI * 2 * j / vSteps;
            float cosV = (float) Math.cos(angleV);
            float sinV = (float) Math.sin(angleV);
            for (int i = 0; i <= uSteps; i++) {
                double angleU = Math.PI * 2 * i / uSteps;
                float cosU = (float) Math.cos(angleU);
                float sinU = (float) Math.sin(angleU);
                float d = majorRadius+minorRadius*cosU;
                float x = d*cosV;
                float y = d*(-sinV);
                float z = minorRadius * sinU;

                float nx = cosV * cosU;
                float ny = -sinV * cosU;
                float nz = sinU;

                float length = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
                nx /= length;
                ny /= length;
                nz /= length;

                grid.set(i, j, x, y, z, nx, ny, nz,2*i/(float)uSteps, 5*j/(float)vSteps);
            }
        }
        grid.createBufferObjects(gl);
        return grid;
    }

    private boolean checkIfContextSupportsCubeMap(GL10 gl) {
        return checkIfContextSupportsExtension(gl, "GL_OES_texture_cube_map");
    }

    /**
     * This is not the fastest way to check for an extension, but fine if
     * we are only checking for a few extensions each time a context is created.
     * @param gl
     * @param extension
     * @return true if the extension is present in the current context.
     */
    private boolean checkIfContextSupportsExtension(GL10 gl, String extension) {
        String extensions = " " + gl.glGetString(GL10.GL_EXTENSIONS) + " ";
        // The extensions string is padded with spaces between extensions, but not
        // necessarily at the beginning or end. For simplicity, add spaces at the
        // beginning and end of the extensions string and the extension string.
        // This means we can avoid special-case checks for the first or last
        // extension, as well as avoid special-case checks when an extension name
        // is the same as the first part of another extension name.
        return extensions.indexOf(" " + extension + " ") >= 0;
    }
    
	// The value of these variables will never be cached thread-locally: 
	// all reads and writes will go straight to "main memory"; 
	volatile private IFractal fractal;
	volatile protected boolean fractalProduced = false;

	// lock used for synchronizing the fractal bitmap produce/consume threads
	private final Object lock = new Object();
	protected boolean killThreads;
	
	private Thread produceFractalThread;
	private Thread consumeFractalThread;
	
	private final int kMaxDim = 256;

}
