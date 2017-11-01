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
    Viewer3 app = new Viewer3( "View a Scene", 800, 800, 30, args[0] );
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

  // Unit speed of camera
  private double speed = 0;


  // construct basic application with given title, pixel width and height
  // of drawing area, and frames per second
  public Viewer3( String appTitle, int pw, int ph, int fps, String fileName )
  {
    super( appTitle, pw, ph, (long) ((1.0/fps)*1000000000) );

    // initial viewing setup
    eye = new Triple( 50, -1, 10 );
    azimuth = 90;  
    altitude = 0;
    distance = 2;    

    sceneTris = new TriList();
    try {
      // Build ground and buildings
      ground(50, 50, 100, 100); 
      building(2, 2, 4, 4, 25);
      building(50, 50, 3, 3, 30);
      building(4, 6, 3, 8, 50);
      building(70, 70, 10, 3, 45);
      building(20, 80, 10, 20, 35);
      building(90, 85, 5, 10, 22);
      building(35, 40, 4, 6, 18);
      building(10, 28, 4, 3, 35);
      building(10, 90, 5, 5, 25);
      building(36, 90, 3, 5, 16);
      building(40, 5, 2, 2, 10);
      building(45, 75, 3, 3, 20);
      building(55, 60, 4, 5, 25);
      building(25, 60, 3, 5, 15);
      building(38, 35, 4, 3, 24);
      building(60, 30, 2, 2, 18);
      building(90, 5, 3, 5, 38);
      building(80, 40, 4, 2, 22);
      building(75, 60, 4, 4, 20);
      building(80, 50, 2, 2, 16);
      building(65, 20, 5, 6, 30);
      building(25, 25, 4, 4, 16);          

    }
    catch(Exception ex)
    {
      System.out.println("Buildings could not be loaded");
    }
    // get model triangle data from file
/*   // Uncomment below code to get model triangles from a file instead of the hardcoded building method
    Scanner input;
    try {

      input = new Scanner( new File( fileName ) );
      
      // read number of triangles
      int num = input.nextInt();  input.nextLine();

      sceneTris = new TriList();
      // building(50, 50, 10, 20, 40);
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
*/
System.out.println("Finished constructor up to not including updateView");
    updateView();

  }// constructor

  // Creates a building based on given coordinates and size information
  protected void building(double xCoord, double yCoord, double len, double width, double height)
  {
    double x1 = (xCoord - (width/2));	// Low x
    double x2 = (xCoord + (width/2));	// High x
    double y1 = (yCoord - (len/2));	// Low y
    double y2 = (yCoord + (len/2));	// High y

    Vertex vertex1, vertex2, vertex3;

    Triple pos1, pos2, pos3, pos4, pos5, pos6, pos7, pos8, col1, col2, col3, col4;
    Vertex vert1, vert2, vert3;

    pos1 = new Triple(x1, y1, 0);
    pos2 = new Triple(x2, y1, 0);
    pos3 = new Triple(x2, y1, height);
    pos4 = new Triple(x1, y1, height);
    pos5 = new Triple(x1, y2, 0);
    pos6 = new Triple(x2, y2, 0);
    pos7 = new Triple(x2, y2, height);
    pos8 = new Triple(x1, y2, height);

    col1 = new Triple(1, 0, 0);
    col2 = new Triple(0, 1, 0);
    col3 = new Triple(0, 0, 1);
    col4 = new Triple(1, 0, 1);
    
    // Front Triangles
    Triangle tri1 = new Triangle(new Vertex(pos1, col1), new Vertex(pos2, col1), new Vertex(pos3, col1));
    Triangle tri2 = new Triangle(new Vertex(pos3, col1), new Vertex(pos4, col1), new Vertex(pos1, col1));
    sceneTris.add(tri1);
    sceneTris.add(tri2);
    
    // Left Triangles
    tri1 = new Triangle(new Vertex(pos1, col2), new Vertex(pos5, col2), new Vertex(pos8, col2));
    tri2 = new Triangle(new Vertex(pos8, col2), new Vertex(pos4, col2), new Vertex(pos1, col2));
    sceneTris.add(tri1);
    sceneTris.add(tri2);

    // Right Triangles
    tri1 = new Triangle(new Vertex(pos2, col3), new Vertex(pos6, col3), new Vertex(pos7, col3));
    tri2 = new Triangle(new Vertex(pos7, col3), new Vertex(pos3, col3), new Vertex(pos2, col3));
    sceneTris.add(tri1);
    sceneTris.add(tri2);

    // Back Triangles
    tri1 = new Triangle(new Vertex(pos5, col4), new Vertex(pos6, col4), new Vertex(pos7, col4));
    tri2 = new Triangle(new Vertex(pos7, col4), new Vertex(pos8, col4), new Vertex(pos5, col4));
    sceneTris.add(tri1);
    sceneTris.add(tri2);

    // Top Triangles
    tri1 = new Triangle(new Vertex(pos4, col1), new Vertex(pos3, col2), new Vertex(pos7, col3));
    tri2 = new Triangle(new Vertex(pos7, col3), new Vertex(pos8, col2), new Vertex(pos4, col1));
    sceneTris.add(tri1);
    sceneTris.add(tri2); 
  }
 
  // Creates a ground based on given coordinate and size information
  protected void ground(double xCoord, double yCoord, double len, double width)
  {
    double x1 = (xCoord - (width/2));
    double x2 = (xCoord + (width/2));
    double y1 = (yCoord - (len/2));
    double y2 = (yCoord + (len/2));

    Triple pos1, pos2, pos3, pos4, col1;

    pos1 = new Triple(x1, y1, 0);
    pos2 = new Triple(x2, y1, 0);
    pos3 = new Triple(x2, y2, 0);
    pos4 = new Triple(x1, y2, 0);
    col1 = new Triple(0.5, 0.5, 0.5);

    Triangle tri1 = new Triangle(new Vertex(pos1, col1), new Vertex(pos2, col1), new Vertex(pos3, col1));
    Triangle tri2 = new Triangle(new Vertex(pos3, col1), new Vertex(pos4, col1), new Vertex(pos1, col1));
    sceneTris.add(tri1);
    sceneTris.add(tri2);
     
  }

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
        else if( code == GLFW.GLFW_KEY_UP && mods == 0) {// UP increases speed
          speed--;
        }
        else if( code == GLFW.GLFW_KEY_DOWN && mods == 0) {// DOWN decreases speed
          speed++;
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

    // Updates speed of camera
    if(speed != 0)
    {
      // Changes position of eye based on unit of speed
      // Change coefficient of speed to change how fast the camera moves per speed unit
      eye = eye.plus(eMinusA.mult(speed*0.01));
      updateViewAndSend();
    }
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
