/*  
   view a scene whose
   data comes from a data file
   using interactive view setup
   creation
   and doing view transformations
   in the GPU using uniform variables
*/

import java.io.File;
import java.util.Scanner;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.*;

import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFW;

public class Viewer3 extends Basic
{
  public static void main(String[] args)
  {
    if( args.length != 1 ) {
      System.out.println("Usage:  j Viewer3 world1");
      System.exit(1);
    }
    Viewer3 app = new Viewer3( "View a Scene", 500, 500, 30, args[0] );
    app.start();
  }// main

  // instance variables 

  private Shader v1, f1;
  private int hp1;

  private int vao;  // handle to the vertex array object

  private TriList sceneTris;

  // viewing setup parameters
  private Triple eye;
  private double azimuth, altitude, distance;

  // CPU variables to feed the uniform variables
  private Triple e, eMinusA, bMinusA, cMinusA;
  private FloatBuffer eBuff, eaBuff, baBuff, caBuff;

  // locations of the uniform variables:
  private int eLoc, eaLoc, baLoc, caLoc;

  // construct basic application with given title, pixel width and height
  // of drawing area, and frames per second
  public Viewer3( String appTitle, int pw, int ph, int fps, String fileName )
  {
    super( appTitle, pw, ph, (long) ((1.0/fps)*1000000000) );

    // initial viewing setup
    eye = new Triple( 50, 0, 0 );
    azimuth = 90;  
    altitude = 0;
    distance = 2;    

    // get model triangle data from file
    Scanner input;
    try {
      input = new Scanner( new File( fileName ) );
      
      // read number of triangles
      int num = input.nextInt();  input.nextLine();

      sceneTris = new TriList();

      for( int k=0; k<num; k++ ) {
        Triangle tri = new Triangle( input );
        // System.out.println("got triangle " + k + ": " + tri );
        sceneTris.add( tri );
      }
    }
    catch( Exception exc ) {
      System.out.println("problem loading data file");
      exc.printStackTrace();
      System.exit(1);
    }

System.out.println("Finished constructor up to not including updateView");
    updateView();

  }// constructor

  protected void init()
  {
    String vertexShaderCode =
"#version 330 core\n"+
"layout (location = 0 ) in vec3 vertexPosition;\n"+
"layout (location = 1 ) in vec3 vertexColor;\n"+
"uniform vec3 e;\n"+
"uniform vec3 eMinusA;\n"+
"uniform vec3 bMinusA;\n"+
"uniform vec3 cMinusA;\n"+
"out vec3 color;\n"+
"void main(void)\n"+
"{\n"+
"  color = vertexColor;\n"+
"  vec3 p = vertexPosition;\n" +
"  vec3 pMinusE = p-e;\n" +
"  float lambda = - (dot(eMinusA,eMinusA) / dot(eMinusA,pMinusE) );\n" +
"  float beta = lambda * ( dot(bMinusA,pMinusE) / dot(bMinusA,bMinusA) );\n" +
"  float gamma = lambda * ( dot(cMinusA,pMinusE) / dot(cMinusA,cMinusA) );\n" +
"  gl_Position = vec4(beta,gamma,2*lambda-1,1.0);\n"+
"}\n";

    System.out.println("Vertex shader:\n" + vertexShaderCode + "\n\n" );

    v1 = new Shader( "vertex", vertexShaderCode );

    String fragmentShaderCode =
"#version 330 core\n"+
"in vec3 color;\n"+
"layout (location = 0 ) out vec4 fragColor;\n"+
"void main(void)\n"+
"{\n"+
"  fragColor = vec4(color, 1.0 );\n"+
"}\n";

    System.out.println("Fragment shader:\n" + fragmentShaderCode + "\n\n" );

    f1 = new Shader( "fragment", fragmentShaderCode );

    hp1 = GL20.glCreateProgram();
         Util.error("after create program");
         System.out.println("program handle is " + hp1 );

    GL20.glAttachShader( hp1, v1.getHandle() );
         Util.error("after attach vertex shader to program");

    GL20.glAttachShader( hp1, f1.getHandle() );
         Util.error("after attach fragment shader to program");

    GL20.glLinkProgram( hp1 );
         Util.error("after link program" );

    GL20.glUseProgram( hp1 );
         Util.error("after use program");

    // find locations of the uniforms
    eLoc = GL20.glGetUniformLocation( hp1, "e" );
          Util.error("get loc of e");
    eaLoc = GL20.glGetUniformLocation( hp1, "eMinusA" );
    baLoc = GL20.glGetUniformLocation( hp1, "bMinusA" );
    caLoc = GL20.glGetUniformLocation( hp1, "cMinusA" );
System.out.println("Found locations: " + eLoc + " " + eaLoc + " " + baLoc + " " + caLoc );

    sendUniformData();

    // set up depth testing so that closer to 1 is in front,
    // closer to -1 is behind
    GL11.glEnable( GL11.GL_DEPTH_TEST );
    GL11.glClearDepth( -1.0f );
    GL11.glDepthFunc( GL11.GL_GREATER );

    GL11.glClearColor( 1.0f, 1.0f, 1.0f, 0.0f );

    // set up GPU with sceneTris once and for all:
    // (can do because no triangle ever changes)
    sceneTris.prepare();

  }// init

  protected void processInputs()
  {
    // process all waiting input events
    while( InputInfo.size() > 0 )
    {
      InputInfo info = InputInfo.get();

      if( info.kind == 'k' && (info.action == GLFW.GLFW_PRESS || 
                               info.action == GLFW.GLFW_REPEAT) )
      {
        int code = info.code;
        int mods = info.mods;  // shift is bit 0, ctrl is bit 1,
                               // option is bit 2

        if( code == GLFW.GLFW_KEY_X && mods == 0 ) {// x
          eye = eye.plus( -1, 0, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_X && mods == 1 ) {// X
          eye = eye.plus( 1, 0, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 0 ) {// y
          eye = eye.plus( 0, -1, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Y && mods == 1 ) {// Y
          eye = eye.plus( 0, 1, 0 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 0 ) {// z
          eye = eye.plus( 0, 0, -1 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_Z && mods == 1 ) {// Z
          eye = eye.plus( 0, 0, 1 );
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_LEFT && mods == 0 ) {
          azimuth += 5;
          if( azimuth >= 360 ) azimuth -= 360;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_RIGHT && mods == 0 ) {
          azimuth -= 5;
          if( azimuth < 0 ) azimuth += 360;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_U && mods == 0 ) {// u
          altitude += 1;
          if( altitude > 89 ) altitude = 89;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_D && mods == 0 ) {// d
          altitude -= 1;
          if( altitude < -89 ) altitude = -89;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_F && mods == 0 ) {// f (farther)
          distance += 0.1;
          updateViewAndSend();
        }
        else if( code == GLFW.GLFW_KEY_N && mods == 0 ) {// n (nearer)
          distance -= 0.1;
          if( distance < 0.1 ) distance = 0.1;
          updateViewAndSend();
        }

      }// input event is a key

      else if ( info.kind == 'm' )
      {// mouse moved
      //  System.out.println( info );
      }

      else if( info.kind == 'b' )
      {// button action
       //  System.out.println( info );
      }

    }// loop to process all input events

  }

  protected void update()
  {
  }

  protected void display()
  {
//    System.out.println( "Step number:  " + getStepNumber() );

    GL11.glClear( GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT );

    sceneTris.draw();

  }

  // using eye, azimuth, altitude, distance
  // generate e, a, b, c
  private void updateView() {

    System.out.println("--------------------------------\n" +
                        "Eye: " + eye + " azi: " + azimuth + " alt: " +
                         altitude + " d: " + distance );

    Triple a, b, c;

    e = eye;  // just notation

    double azi = Math.toRadians(azimuth);
    double alt = Math.toRadians(altitude);
    double cosAzi = Math.cos( azi ), sinAzi = Math.sin( azi );
    double cosAlt = Math.cos( alt ), sinAlt = Math.sin( alt );
    
    // length is distance
    Triple aMinusE = new Triple( cosAlt*distance*cosAzi,
                           cosAlt*distance*sinAzi,
                           distance*sinAlt );
    a = e.plus(aMinusE);
System.out.println("A: " + a );
    eMinusA = e.minus(a);
System.out.println("E-A: " + eMinusA );

    bMinusA = Triple.zAxis.cross( eMinusA );
    bMinusA = bMinusA.normalized(); // make b-a have length 1
    b = a.plus(bMinusA);

    cMinusA = eMinusA.cross( bMinusA );
    cMinusA = cMinusA.normalized();  // make c-a have length 1
    c = a.plus(cMinusA);
    
  }// updateView

  private void sendUniformData() {
System.out.println("about to send data to uniforms" );

    // send viewing info to GPU:
    // (send: E, E-A, B-A, C-A)
    GL20.glUniform3fv( eLoc, e.toBuffer() );
    GL20.glUniform3fv( eaLoc, eMinusA.toBuffer() );
    GL20.glUniform3fv( baLoc, bMinusA.toBuffer() );
    GL20.glUniform3fv( caLoc, cMinusA.toBuffer() );
System.out.println("finished sending data to uniforms");

  }// updateViewAndSend

  private void updateViewAndSend() {
    updateView();
    sendUniformData();
  }

}// Viewer3
