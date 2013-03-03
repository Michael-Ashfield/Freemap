package freemap.hikar;

import android.opengl.GLES20;
import java.nio.Buffer;
import android.util.Log;

// Controls the interface between CPU and GPU, i.e. all the interfacing with shaders
// and associating buffer data with shader variables.

public class GPUInterface {

    int vertexShader=-1, fragmentShader=-1, shaderProgram=-1;
    
    public GPUInterface(String vertexShaderCode, String fragmentShaderCode)
    {
        if((vertexShader = addVertexShader(vertexShaderCode)) >= 0)
            if((fragmentShader = addFragmentShader(fragmentShaderCode)) >= 0)
                shaderProgram = makeProgram(vertexShader, fragmentShader);
    }
    
    public int addVertexShader(String shaderCode)
    {
        return getShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }
    
    public int addFragmentShader(String shaderCode)
    {
        return  getShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }
    
    public static int getShader(int shaderType, String shaderCode)
    {
        int shader = GLES20.glCreateShader(shaderType);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader,GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0]==0)
        {
            Log.e("hikar", "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return -1;
        }
        return shader;
    }
    
    public static int makeProgram(int vertexShader, int fragmentShader)
    {
       int shaderProgram=GLES20.glCreateProgram();
       GLES20.glAttachShader(shaderProgram, vertexShader);
       GLES20.glAttachShader(shaderProgram, fragmentShader);
       GLES20.glLinkProgram(shaderProgram);
       int[] linkStatus = new int[1];
       GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
       if(linkStatus[0]==0)
       {
           Log.e("hikar", "Error linking shader program: " + GLES20.glGetProgramInfoLog(shaderProgram));
           GLES20.glDeleteProgram(shaderProgram);
           return -1;
       }
       GLES20.glUseProgram(shaderProgram);
       return shaderProgram;
    }
    
    // Draw buffered data:
    // we select the buffer, get the shader var ref, tell opengl the format of the data
    // and then draw the data
    public void drawBufferedData(Buffer vertices, Buffer indices, int stride, String attrVar)
    {
        if(isValid())
        {
            int attrVarRef= getShaderVarRef(attrVar);
            vertices.position(0);
            indices.position(0);
            GLES20.glEnableVertexAttribArray(attrVarRef);
            GLES20.glVertexAttribPointer(attrVarRef, 3, GLES20.GL_FLOAT, false, stride, vertices);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.limit(), GLES20.GL_UNSIGNED_SHORT, indices);
        }
    }
    
    public int getShaderVarRef(String shaderVar)
    {
        int refAttrVar = isValid() ?  GLES20.glGetAttribLocation(shaderProgram, shaderVar) : -1;
        return refAttrVar;
    }
    
    
    // Do something with the modelview and perspective matrices
    
    public void sendMatrix(float[] mtx, String shaderMtxVar)
    {
        if(isValid())
        {
            int refMtxVar = GLES20.glGetUniformLocation(shaderProgram, shaderMtxVar);
            GLES20.glUniformMatrix4fv(refMtxVar, 1, false, mtx, 0); // 1 = one matrix http://www.khronos.org/opengles/sdk/docs/man/xhtml/glUniform.xml
        }
    }
    
    public void setColour(String shaderVar, float[] colour)
    {
        if(isValid())
        {
            int refShaderVar = GLES20.glGetUniformLocation(shaderProgram, shaderVar);
            GLES20.glUniform4fv (refShaderVar, 1, colour, 0); // 1 = one uniform variable http://www.khronos.org/opengles/sdk/docs/man/xhtml/glUniform.xml
        }
    }

    public boolean isValid()
    {
        return shaderProgram >= 0;
    }
}