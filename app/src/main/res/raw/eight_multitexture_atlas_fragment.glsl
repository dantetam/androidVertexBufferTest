precision mediump float;       	// Set the default precision to medium. We don't need as high of a 
								// precision in the fragment shader.
uniform vec3 u_LightPos;       	// The position of the light in eye space.
uniform vec3 u_CameraPos;       // The position of the camera in eye space

//uniform sampler2D backgroundTexture;
uniform sampler2D texture0;    // The input texture.
uniform sampler2D texture1;
uniform sampler2D texture2;
uniform sampler2D texture3;
uniform sampler2D texture4;
uniform sampler2D texture5;
uniform sampler2D texture6;
uniform sampler2D texture7;
uniform sampler2D blendMap;

varying vec3 v_Position;		// Interpolated position for this fragment.
varying vec3 v_Normal;         	// Interpolated normal for this fragment.
varying vec2 v_TexCoordinate;   // Interpolated texture coordinate per fragment.
//varying vec4 v_Color;
  
// The entry point for our fragment shader.
void main()                    		
{
    vec4 totalColor;

    vec4 blendMapColor = texture2D(blendMap, v_TexCoordinate0);
    vec2 tiledCoords = v_TexCoordinate;
    float v = blendMapColor.r;
    if (v <= 1.0/8.0) {
        totalColor = texture(texture0, tiledCoords);
    }
    else if (v <= 2.0/8.0) {
        totalColor = texture(texture1, tiledCoords);
    }
    else if (v <= 3.0/8.0) {
        totalColor = texture(texture2, tiledCoords);
    }
    else if (v <= 4.0/8.0) {
        totalColor = texture(texture3, tiledCoords);
    }
    else if (v <= 5.0/8.0) {
        totalColor = texture(texture4, tiledCoords);
    }
    else if (v <= 6.0/8.0) {
        totalColor = texture(texture5, tiledCoords);
    }
    else if (v <= 7.0/8.0) {
        totalColor = texture(texture6, tiledCoords);
    }
    else {
        totalColor = texture(texture7, tiledCoords);
    }

    vec4 texel = totalColor;
    //texel.a = 1.0;

    //texel = vec4(1.0, 0.0, 0.0, 1.0);

    //vec4 texel = texture2D(u_Texture, v_TexCoordinate);
    if (texel.a < 0.5)
        discard;
	// Will be used for attenuation.
    float distance = length(u_LightPos - v_Position);                  
	
	// Get a lighting direction vector from the light to the vertex.
    vec3 lightVector = normalize(u_LightPos - v_Position);

	// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
	// pointing in the same direction then it will get max illumination.
    float diffuse = max(dot(v_Normal, lightVector), 0.3);

	vec3 normalDirection = normalize(v_Normal);
    vec3 viewDirection = normalize(u_CameraPos - v_Position);

	// Add attenuation. 
    //diffuse = diffuse * (1.0 / distance);
    
    // Add ambient lighting
    diffuse = diffuse + 0.3;

	// Multiply the color by the diffuse illumination level and texture value to get final output color.
    //gl_FragColor = texture2D(u_Texture, v_TexCoordinate);

    //gl_FragColor = vec4(diffuse, diffuse, diffuse, 1.0);
    //gl_FragColor = mix(diffuse, gray, 0.5);

    gl_FragColor = (diffuse * texel);

    //vec4 colored = (diffuse * texture2D(u_Texture, v_TexCoordinate));
    //float newOpacity = min(1.0, colored.w / abs(dot(viewDirection, normalDirection)));
    //gl_FragColor = vec4(vec3(colored), newOpacity);
}

