uniform mat4 u_MVPMatrix;		 // A constant representing the combined model/view/projection matrix.
uniform mat4 u_MVMatrix;		 // A constant representing the combined model/view matrix.

attribute vec4 a_Position;		 // Per-vertex position information we will pass in.
attribute vec3 a_Normal;		 // Per-vertex normal information we will pass in.

attribute vec2 a_TexCoordinate0; // Per-vertex texture coordinate information we will pass in.
uniform float texArrayRowsAndOffset[16];

attribute vec2 blendMap;

varying vec3 v_Position;		 // This will be passed into the fragment shader.
varying vec3 v_Normal;			 // This will be passed into the fragment shader.
varying vec2 v_TexCoordinates[8];    // This will be passed into the fragment shader.

// The entry point for our vertex shader.  
void main()                                                 	
{                                                         
	// Transform the vertex into eye space. 	
	v_Position = vec3(u_MVMatrix * a_Position);            		
	
	// Pass through the texture coordinate.
	vec2 pass_texCoord = (a_TexCoordinate0 / texArrayRowsAndOffset[1]) + texArrayRowsAndOffset[2];

	v_TexCoordinates[0] = pass_texCoord;

	// Transform the normal's orientation into eye space.
    v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0));

	// gl_Position is a special variable used to store the final position.
	// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
	gl_Position = u_MVPMatrix * a_Position;                       		  
}                                                          