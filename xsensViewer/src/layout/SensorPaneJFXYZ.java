package layout;


import java.io.File;
import java.net.URL;
import org.fxyz3d.importers.Importer3D;
import org.fxyz3d.importers.Model3D;
import org.fxyz3d.shapes.primitives.SpringMesh;
import org.fxyz3d.utils.CameraTransformer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;

/**
 * Show a .obj file using fxyz3d
 * @author Jamie Macaulay
 *
 */
public class SensorPaneJFXYZ extends Application {


	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		PerspectiveCamera camera = new PerspectiveCamera(true);        
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		//	        camera.setTranslateX(10);
		camera.setTranslateZ(-500);
		camera.setFieldOfView(100);

		CameraTransformer cameraTransform = new CameraTransformer();
		cameraTransform.getChildren().add(camera);
		//	        cameraTransform.ry.setAngle(-30.0);
		//	        cameraTransform.rx.setAngle(-15.0);


		SpringMesh spring = new SpringMesh(10, 2, 2, 8 * 2 * Math.PI, 200, 100, 0, 0);
		spring.setCullFace(CullFace.NONE);
		spring.setTextureModeVertices3D(1530, p -> p.f);


		String objFile = "C:\\Users\\Jamie Macaulay\\OneDrive\\SMRU\\Equipment Projects\\SoundNet1_v1\\housing_design\\sensor package housing\\R5\\positive_model_dual_battery_revD.obj"; 

		File file = new File(objFile); 
		System.out.println("File exists? " +  file.exists());


		URL path = file.toURI().toURL();
		//	       Importer3D objImporter =new Importer3D ();
		Model3D model = Importer3D.load(path);

		System.out.println(model.getMeshNames());

		MeshView view = (MeshView) model.getMeshView("default"); 

		Group group = new Group(cameraTransform, view);

		Scene scene = new Scene(group, 600, 400, true, SceneAntialiasing.BALANCED);
		scene.setFill(Color.BISQUE);
		scene.setCamera(camera);

		primaryStage.setScene(scene);
		primaryStage.setTitle("FXyz3D Import Sample");
		primaryStage.show();
	}

}
