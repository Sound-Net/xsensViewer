package layout;

import java.io.File;
import java.util.Comparator;

import com.interactivemesh.jfx.importer.stl.StlMeshImporter;
import comms.SerialUtils;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import main.SensorData;

/**
 * 3D view of the sensor. 
 * @author Jamie Macaulay 
 *
 */
public class SensorPane3D  extends BorderPane {

	private static final String MESH_FILENAME = "src\\resources\\SensorPackageR5.stl";

	private static final double MODEL_SCALE_FACTOR = 10;

	private static final Color lightColor = Color.rgb(244, 255, 250);
	private static final Color jewelColor = Color.rgb(0, 190, 222);


	/**
	 * keep track of mouse positions
	 */
	double mousePosX;
	double mousePosY;
	double mouseOldX;
	double mouseOldY;
	double mouseDeltaX;
	double mouseDeltaY;

	/**
	 * The camera transforms 
	 */
	private Rotate rotateY;
	private Rotate rotateX;
	private Translate translate;
	private Group root3D;

	/**
	 * Group containing axis and a reference 
	 */
	private Group axisGroup;

	/**
	 * Holds the ellipse.
	 */
	private Group sensorGroup;


	/**
	 * Create the magnetic calibration pane. 
	 * @param magneticCalibration - magnetic calibration class to calibrate the pane. 
	 */
	public SensorPane3D() {
		this.setCenter(create3DPane());
	}
	

	public void setOrientationData(SensorData sensormessage) {
		
		if (sensormessage.eularAngles!=null){
			//matrixRotateNode(sensorGroup,Math.toRadians(sensormessage.eularAngles[0]), Math.toRadians(sensormessage.eularAngles[1]), Math.toRadians(sensormessage.eularAngles[2]));
			if (sensormessage.eularAngles[0]!=0 && sensormessage.eularAngles[1]!=0 && sensormessage.eularAngles[2]!=0){
				//get rid of spurious values
				matrixRotateNode(sensorGroup,sensormessage.eularAngles[0], sensormessage.eularAngles[1], sensormessage.eularAngles[2]);
			}
		}
		else if (sensormessage.quaternion!=null){
			double[] angles = SerialUtils.quat2Eul(sensormessage.quaternion);
			if (sensormessage.quaternion[0]!=0 && sensormessage.quaternion[1]!=0 && sensormessage.quaternion[2]!=0 && sensormessage.quaternion[3]!=0){
				matrixRotateNode(sensorGroup,Math.toDegrees(angles[2]), Math.toDegrees(angles[1]), Math.toDegrees(angles[0]));
			}
		}
	}

	
	/**
	 * Rotate by Euler angles 
	 * @param n - the node to rotate; 
	 * @param roll
	 * @param pitch
	 * @param yaw
	 */
//	private void matrixRotateNode(Node n, double roll, double pitch, double yaw){
//	    double A11=Math.cos(roll)*Math.cos(yaw);
//	    double A12=Math.cos(pitch)*Math.sin(roll)+Math.cos(roll)*Math.sin(pitch)*Math.sin(yaw);
//	    double A13=Math.sin(roll)*Math.sin(pitch)-Math.cos(roll)*Math.cos(pitch)*Math.sin(yaw);
//	    double A21=-Math.cos(yaw)*Math.sin(roll);
//	    double A22=Math.cos(roll)*Math.cos(pitch)-Math.sin(roll)*Math.sin(pitch)*Math.sin(yaw);
//	    double A23=Math.cos(roll)*Math.sin(pitch)+Math.cos(pitch)*Math.sin(roll)*Math.sin(yaw);
//	    double A31=Math.sin(yaw);
//	    double A32=-Math.cos(yaw)*Math.sin(pitch);
//	    double A33=Math.cos(pitch)*Math.cos(yaw);
//
//	    double d = Math.acos((A11+A22+A33-1d)/2d);
//	    if(d!=0d){
//	        double den=2d*Math.sin(d);
//	        Point3D p= new Point3D((A32-A23)/den,(A13-A31)/den,(A21-A12)/den);
//	        n.setRotationAxis(p);
//	        n.setRotate(Math.toDegrees(d));                    
//	    }
//	}
	
	private void setPivot(Rotate rot) {
		rot.setPivotX(0);
		rot.setPivotY(130);
		rot.setPivotZ(-10);

	}

	
	private void matrixRotateNode(Node n, double roll, double pitch, double yaw){
		
	    	Rotate headingR = null, rollR = null, pitchR =null;
	    
			n.getTransforms().clear(); 

//	    	Translate translate= new Translate(); 
//	    	translate.setZ(10);
//	    	translate.setY(-130);    	
//	    	translate.setX(+400);
//	    	n.getTransforms().add(translate);
	    	
	    	//R5 sensor 
	    	Rotate pitchR1= new Rotate(); 
		    pitchR1.setAxis(new Point3D(0,0,1));
		    pitchR1.setAngle(-90);
		    setPivot(pitchR1);
		    n.getTransforms().add(pitchR1);
	    
		    pitchR= new Rotate(); 
		    pitchR.setAxis(new Point3D(0,0,1));
		    pitchR.setAngle(pitch);
		    setPivot(pitchR);
		    n.getTransforms().add(pitchR);
////	    
////	    	 //has to be in this order
	    	headingR= new Rotate(); 
	    	headingR.setAxis(new Point3D(1,0,0));
	    	headingR.setAngle(yaw);
		    setPivot(headingR);
		    n.getTransforms().add(headingR);
//		
		    rollR= new Rotate(); 
		    rollR.setAxis(new Point3D(0,1,0));
		    rollR.setAngle(roll);
		    setPivot(rollR);
		    n.getTransforms().add(rollR);
	    	
//	    	 //has to be in this order
//	    	headingR= new Rotate(); 
//	    	headingR.setAxis(new Point3D(0,1,0));
//	    	headingR.setAngle(yaw);
//		    n.getTransforms().add(headingR);
//		    
//		    pitchR= new Rotate(); 
//		    pitchR.setAxis(new Point3D(1,0,0));
//		    pitchR.setAngle(pitch);
//		    n.getTransforms().add(pitchR);
//
//		    rollR= new Rotate(); 
//		    rollR.setAxis(new Point3D(0,0,1));
//		    rollR.setAngle(roll);
//		    n.getTransforms().add(rollR);

	}
	
	/**
	 * 3D pane which allow users to visualise calibration measurements. 
	 * @return 3D pane which shows magnetic measurements and ellipse calibration. 
	 */
	private Pane create3DPane(){

		Pane pane3D=new Pane(); 

		// Create and position camera
		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(15000);
		camera.setNearClip(0.1);
		camera.setDepthTest(DepthTest.ENABLE);
		camera.getTransforms().addAll (
				rotateY=new Rotate(-45, Rotate.Y_AXIS),
				rotateX=new Rotate(-45, Rotate.X_AXIS),
				translate=new Translate(0, 0, -3500));

		//create main 3D group 
		root3D=new Group();

		//group for calibrated measurments, 
		//		axisGroup=Array3DPane.buildAxes(100, Color.RED, Color.SALMON, Color.BLUE, Color.CYAN, Color.LIMEGREEN, Color.LIME, Color.WHITE);
		axisGroup=buildAxes(1000, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);

		//group for ellipsoid
		sensorGroup=new Group(); 
		sensorGroup.getChildren().addAll(createSensor());
	
		PointLight light = new PointLight(Color.WHITE);
		light.setTranslateX(-500);
		light.setTranslateY(-500);
		light.setTranslateZ(-500);
		
		
		PointLight light1 = new PointLight(Color.WHITE);
		light1.setTranslateX(+500);
		light1.setTranslateY(+500);
		light1.setTranslateZ(+500);

		root3D.getChildren().addAll(sensorGroup, axisGroup, light,light1);
		
		//Use a SubScene to mix 3D and 2D stuff.        
		//note- make sure depth buffer in sub scene is enabled. 
		SubScene subScene = new SubScene(root3D, 500,500, true, SceneAntialiasing.BALANCED);
		subScene.widthProperty().bind(this.widthProperty());
		subScene.heightProperty().bind(this.heightProperty());
		subScene.setDepthTest(DepthTest.ENABLE);

		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);

//		//handle mouse events for sub scene
		handleMouse(pane3D); 

		//create new group to add sub scene to 
		Group group = new Group();
		group.getChildren().add(subScene);

		//add group to window.
		pane3D.getChildren().add(group);
		pane3D.setDepthTest(DepthTest.ENABLE);

		return pane3D;
	}

	static MeshView[] loadMeshViews() {
		File file = new File(MESH_FILENAME);
		StlMeshImporter importer = new StlMeshImporter();
		importer.read(file);
		Mesh mesh = importer.getImport();

		return new MeshView[] { new MeshView(mesh) };
	} 


	private MeshView[] createSensor() {

		MeshView[] meshViews = loadMeshViews();
		
		for (int i = 0; i < meshViews.length; i++) {
			meshViews[i].setRotate(90);
			meshViews[i].setScaleX(MODEL_SCALE_FACTOR);
			meshViews[i].setScaleY(MODEL_SCALE_FACTOR);
			meshViews[i].setScaleZ(MODEL_SCALE_FACTOR);


			PhongMaterial sample = new PhongMaterial(jewelColor);
			sample.setSpecularColor(lightColor);
			sample.setSpecularPower(16);
			meshViews[i].setMaterial(sample);
		}
		
		return meshViews;
	}

	/**
	 * Create a mesh sphere for reference. 
	 * @return mesh sphere for reference. 
	 */
	public Sphere createAxisSphere(){
		Sphere sphere=new Sphere(35);
		PhongMaterial material=new PhongMaterial(); 
		material.setSpecularColor(Color.WHITE);
		material.setDiffuseColor(Color.WHITE);
		sphere.setMaterial(material);
		sphere.setDrawMode(DrawMode.LINE);
		return sphere;
	}

	/**
	 * Get the index of a sorted array
	 * @author Jamie Macaulay (from http://stackoverflow.com/questions/4859261/get-the-indices-of-an-array-after-sorting)
	 *
	 */
	public class ArrayIndexComparator implements Comparator<Integer>
	{
		private final double[] array;

		public ArrayIndexComparator(double[] array)
		{
			this.array = array;
		}

		public Integer[] createIndexArray()
		{
			Integer[] indexes = new Integer[array.length];
			for (int i = 0; i < array.length; i++)
			{
				indexes[i] = i; // Autoboxing
			}
			return indexes;
		}

		@Override
		public int compare(Integer index1, Integer index2)
		{
			// Autounbox from Integer to int to use as array indexes
			return (int) (array[index1]-array[index2]);
		}
	}




	private void handleMouse(Node scene) {

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override public void handle(MouseEvent me) {
				System.out.println("Mouse clicked"); 
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseOldX = me.getSceneX();
				mouseOldY = me.getSceneY();
			}
		});

		scene.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override public void handle(ScrollEvent event) {
				System.out.println("Scroll Event: "+event.getDeltaX() + " "+event.getDeltaY()); 
				translate.setZ(translate.getZ()+  event.getDeltaY() *0.001*translate.getZ());   // + 
			}
		});


		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent me) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = me.getSceneX();
				mousePosY = me.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;
				double modifierFactor = 0.1;

				if (me.isControlDown()) {
					modifier = 0.1;
				}
				if (me.isShiftDown()) {
					modifier = 10.0;
				}
				if (me.isPrimaryButtonDown()) {
					rotateY.setAngle(rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0);  // +
					rotateX.setAngle(rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0);  // -
				}
				if (me.isSecondaryButtonDown()) {
					translate.setX(translate.getX() -mouseDeltaX * modifierFactor * modifier * 5);
					translate.setY(translate.getY() - mouseDeltaY * modifierFactor * modifier * 5);   // +
				}


			}
		});
	}



	/**
	 * Create a 3D axis. 
	 * @param- size of the axis
	 */
	public static Group buildAxes(double axisSize, Color xAxisDiffuse, Color xAxisSpectacular,
			Color yAxisDiffuse, Color yAxisSpectacular,
			Color zAxisDiffuse, Color zAxisSpectacular,
			Color textColour) {
		Group axisGroup=new Group(); 
		double length = 2d*axisSize;
		double width = axisSize/100d;
		double radius = 2d*axisSize/100d;
		final PhongMaterial redMaterial = new PhongMaterial();
		redMaterial.setDiffuseColor(xAxisDiffuse);
		redMaterial.setSpecularColor(xAxisSpectacular);
		final PhongMaterial greenMaterial = new PhongMaterial();
		greenMaterial.setDiffuseColor(yAxisDiffuse);
		greenMaterial.setSpecularColor( yAxisSpectacular);
		final PhongMaterial blueMaterial = new PhongMaterial();
		blueMaterial.setDiffuseColor(zAxisDiffuse);
		blueMaterial.setSpecularColor(zAxisSpectacular);

		Text xText=new Text("x"); 
		xText.setStyle("-fx-font: 90px Tahoma;");
		xText.setFill(textColour);
		Text yText=new Text("y"); 
		yText.setStyle("-fx-font: 90px Tahoma; ");
		yText.setFill(textColour);
		Text zText=new Text("z"); 
		zText.setStyle("-fx-font: 90px Tahoma; ");
		zText.setFill(textColour);

		xText.setTranslateX(axisSize+5);
		yText.setTranslateY((axisSize+5));
		zText.setTranslateZ(axisSize+5);

		Sphere xSphere = new Sphere(radius);
		Sphere ySphere = new Sphere(radius);
		Sphere zSphere = new Sphere(radius);
		xSphere.setMaterial(redMaterial);
		ySphere.setMaterial(greenMaterial);
		zSphere.setMaterial(blueMaterial);

		xSphere.setTranslateX(axisSize);
		ySphere.setTranslateY(axisSize);
		zSphere.setTranslateZ(axisSize);

		Box xAxis = new Box(length, width, width);
		Box yAxis = new Box(width, length, width);
		Box zAxis = new Box(width, width, length);
		xAxis.setMaterial(redMaterial);
		yAxis.setMaterial(greenMaterial);
		zAxis.setMaterial(blueMaterial);

		axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
		axisGroup.getChildren().addAll(xText, yText, zText);
		axisGroup.getChildren().addAll(xSphere, ySphere, zSphere);
		return axisGroup;
	}


}