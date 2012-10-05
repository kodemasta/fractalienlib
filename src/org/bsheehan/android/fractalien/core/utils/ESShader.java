package org.bsheehan.android.fractalien.core.utils;

/**
 * NOT WRITTEN BY BSHEEHAN@BAYMOON.COM
 * 
 * Book:      OpenGL(R) ES 2.0 Programming Guide
 * @author:   Aaftab Munshi, Dan Ginsburg, Dave Shreiner
 * ISBN-10:   0321502795
 * ISBN-13:   9780321502797
 * Publisher: Addison-Wesley Professional
 * URLs:      http://safari.informit.com/9780321563835
 * 		      http://www.opengles-book.com
 **/

//ESShader
//
// Utility functions for loading shaders and creating program objects.
//

import android.opengl.GLES20;
import android.util.Log;

public class ESShader 
{
	private static int programId, vertexShaderId, fragmentShaderId;
	//
	///
	/// \brief Load a shader, check for compile errors, print error messages to output log
	/// \param type Type of shader (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER)
	/// \param shaderSrc Shader source string
	/// \return A new shader object on success, 0 on failure
	//
	public static int loadShader ( int type, String shaderSrc )
	{
		int shader;
		final int[] compiled = new int[1];

		// Create the shader object
		shader = GLES20.glCreateShader ( type );

		if ( shader == 0 )
			return 0;

		// Load the shader source
		GLES20.glShaderSource ( shader, shaderSrc );

		// Compile the shader
		GLES20.glCompileShader ( shader );

		// Check the compile status	   
		GLES20.glGetShaderiv ( shader, GLES20.GL_COMPILE_STATUS, compiled, 0);

		if ( compiled[0] == 0 ) 
		{
			Log.e("ESShader", GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			return 0;
		}        
		return shader;
	}

	//
	///
	/// \brief Load a vertex and fragment shader, create a program object, link program.
	///	         Errors output to log.
	/// \param vertShaderSrc Vertex shader source code
	/// \param fragShaderSrc Fragment shader source code
	/// \return A new program object linked with the vertex/fragment shader pair, 0 on failure
	//
	public static int loadProgram ( String vertShaderSrc, String fragShaderSrc )
	{
		//int vertexShader;
		//int fragmentShader;
		//int programObject;
		final int[] linked = new int[1];

		// Load the vertex/fragment shaders
		vertexShaderId= loadShader ( GLES20.GL_VERTEX_SHADER, vertShaderSrc );
		if ( vertexShaderId == 0 )
			return 0;

		fragmentShaderId = loadShader ( GLES20.GL_FRAGMENT_SHADER, fragShaderSrc );
		if ( fragmentShaderId == 0 )
		{
			GLES20.glDeleteShader( vertexShaderId );
			return 0;
		}

		// Create the program object
		programId = GLES20.glCreateProgram ( );

		if ( programId == 0 )
			return 0;

		GLES20.glAttachShader ( programId, vertexShaderId );
		GLES20.glAttachShader ( programId, fragmentShaderId );

		// Link the program
		GLES20.glLinkProgram ( programId );

		// Check the link status
		GLES20.glGetProgramiv ( programId, GLES20.GL_LINK_STATUS, linked, 0);

		if ( linked[0] == 0 ) 
		{
			Log.e("ESShader", "Error linking program:");
			Log.e("ESShader", GLES20.glGetProgramInfoLog ( programId ));
			GLES20.glDeleteProgram ( programId );
			return 0;
		}

		// Free up no longer needed shader resources
		GLES20.glDeleteShader ( vertexShaderId );
		GLES20.glDeleteShader ( fragmentShaderId );

		return programId;
	}

	public static void cleanup() {
		GLES20.glDeleteProgram(programId);
		GLES20.glDeleteShader(vertexShaderId);
		GLES20.glDeleteShader(fragmentShaderId);
	}
}
