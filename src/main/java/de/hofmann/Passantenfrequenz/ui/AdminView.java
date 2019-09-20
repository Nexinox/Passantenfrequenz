package de.hofmann.Passantenfrequenz.ui;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.TextField;
import com.vaadin.ui.*;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import de.hofmann.Passantenfrequenz.model.Camera;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * View to upload image and set camera location, name and rotation
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@CDIView(AdminView.VIEW_NAME)
@RolesAllowed("admins")
public class AdminView extends VerticalLayout implements View {
    static final String VIEW_NAME = "admin";
	private static final long serialVersionUID = 1L;

	private BufferedImage bi;
	private Graphics2D drawable;
	private int clicks = 0;
	private Collection<Camera> Cameras = new ArrayList<>();
	private boolean exists = false;
	private Image image;
	private Camera camera;
	private File file;
	private boolean isOpen = false;
    private Color green = new Color(0, 153, 51);
    private int scaledWidth = 1139, scaledHeight = 762;
    @SuppressWarnings("unchecked")
	@Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    	readJson();

    	image = new Image();
    	HorizontalLayout imageWrapper = new HorizontalLayout(image);

    	ImageUploader receiver = new ImageUploader();

	    Upload upload = new Upload();
	    upload.setReceiver(receiver);
        upload.setButtonCaption("Bild Hochladen");
        Button saveBtn = new Button("Spechern");

        File rsc = new File("rsc/img/");
        if(!rsc.exists()) rsc.mkdirs();

        File[] files = new File("rsc/img/").listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".PNG"));

        if(files != null) for (File file : files)
            if (file.isFile()) {
                image.setSource(new FileResource(file));
                try {
                    this.file = file;
                    bi = ImageIO.read(file);
                    drawable = bi.createGraphics();
                    updateImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

	    image.addClickListener(e -> {

	        if (!isOpen){
                for (Camera cam : Cameras) {
                    int dist = (int) Math.sqrt((e.getRelativeX() - cam.getX()) * (e.getRelativeX() - cam.getX()) +
                        (e.getRelativeY() - cam.getY()) * (e.getRelativeY() - cam.getY()));
                    if (dist <= 5) {
                        camera = cam;
                        exists = true;
                    }
                }

                if(!exists) {
                    camera = new Camera();
                    camera.setName("Kamera" + clicks);
                    camera.setX(e.getRelativeX());
                    camera.setY(e.getRelativeY());
                    camera.setRotationDeg(0);
                    Cameras.add(camera);
                }

                VerticalLayout settingsWrapper = new VerticalLayout();
                TextField camName = new TextField("Name");
                TextField rotate = new TextField("Um ?° drehen");
                camName.setValue(camera.getName());
                rotate.setValue(Integer.toString(camera.getRotationDeg()));
                rotate.addValueChangeListener(c->{
                    if (rotate.getValue().matches("[0-9]+")){
                        camera.setRotationDeg(Integer.parseInt(rotate.getValue()));
                        updateImage();
                    }else Notification.show("no valid °");
                });
                Button save = new Button("Speichern", click->{
                    if (rotate.getValue().matches("[0-9]+")){
                        camera.setName(camName.getValue());
                        camera.setRotationDeg(Integer.parseInt(rotate.getValue()));
                        imageWrapper.removeComponent(settingsWrapper);
                        updateImage();
                        isOpen = false;
                    }else Notification.show("no valid °");

                });
                Button delete = new Button("Löschen", click->{
                    Cameras.remove(camera);
                    imageWrapper.removeComponent(settingsWrapper);
                    isOpen = false;
                    updateImage();
                });
                settingsWrapper.addComponents(camName,rotate,save,delete);

                imageWrapper.addComponent(settingsWrapper);
                exists = false;
                isOpen = true;
                updateImage();
            }

	    });

	    saveBtn.addClickListener(click -> {

	    	JSONArray cameraArray = new JSONArray();

	        Cameras.forEach(camera ->{

	        	JSONObject cameraObject = new JSONObject();

	        	JSONObject cameraDetails = new JSONObject();

	        	cameraDetails.put("name", camera.getName());
	        	cameraDetails.put("x", camera.getX());
	        	cameraDetails.put("y", camera.getY());
	        	cameraDetails.put("rot", camera.getRotationDeg());

	        	cameraObject.put("camera", cameraDetails);

	        	cameraArray.add(cameraObject);

	        });

	        try (FileWriter file = new FileWriter("rsc/cameras.json")) {

	            file.write(cameraArray.toJSONString());
	            file.flush();
	            file.close();
	            UI.getCurrent().getNavigator().navigateTo(FilterView.VIEW_NAME);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    });

	    upload.addSucceededListener(receiver);

	    addComponents(new HorizontalLayout(upload, saveBtn), imageWrapper);
    }

    /**
     * Reads the Cameras from cameras.json and adds them to Global Collection cameras
     */
    @SuppressWarnings("unchecked")
	private void readJson() {
    	 JSONParser jsonParser = new JSONParser();
         try (FileReader reader = new FileReader("rsc/cameras.json"))
         {
             Object obj = jsonParser.parse(reader);

             JSONArray cameraArray = (JSONArray) obj;

             cameraArray.forEach(cam -> {

            	 JSONObject cameraObject = (JSONObject) cam;
            	 JSONObject cameraObj = (JSONObject) cameraObject.get("camera");

            	 Camera camera = new Camera();
            	 camera.setName((String) cameraObj.get("name"));
            	 camera.setX((int) (long) cameraObj.get("x"));
            	 camera.setY((int) (long) cameraObj.get("y"));
            	 camera.setRotationDeg((int) (long) cameraObj.get("rot"));
            	 Cameras.add(camera);
             });

         } catch (IOException | ParseException e) {
             e.printStackTrace();
         }
    }

    /**
     * Receiver and SucceededListener implementation for upload
     */
	class ImageUploader implements Receiver, SucceededListener {
		private static final long serialVersionUID = 1L;
		public OutputStream receiveUpload(String filename,
                                          String mimeType) {

			if(filename.endsWith(".png") || filename.endsWith(".PNG")) {
	            FileOutputStream fos;
	            File[] files = new File("rsc/img/").listFiles();

	            try {
	                if(files != null) for (File file : files)
                        if (file.isFile()) file.delete();
	                file = new File("rsc/img/"+filename);
	                fos = new FileOutputStream(file);
	            } catch (final java.io.FileNotFoundException e) {
	                new Notification("failed",
	                				e.getMessage(),
	                                 Notification.Type.ERROR_MESSAGE)
	                    .show(Page.getCurrent());
	                return null;
	            }
	            return fos;
			}
            new Notification("failed",
                    "file needs to end with .png or .PNG",
                            Notification.Type.ERROR_MESSAGE)
                            .show(Page.getCurrent());
			return null;
        }

        public void uploadSucceeded(SucceededEvent event) {

            try {
                BufferedImage origBi = ImageIO.read(file);
                BufferedImage scaledBi = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
                scaledBi.getGraphics().drawImage(origBi, 0, 0, scaledWidth, scaledHeight, null);
                file.delete();
                ImageIO.write(scaledBi, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }


            try {
				bi = ImageIO.read(file);
				drawable = bi.createGraphics();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	image.setSource(new FileResource(file));
        	updateImage();
        }

    }

    /**
     * Updates the image draws cameras and helplines for aligning and calls createStreamResource
     * and adds one to global variable clicks when called
     */
	private void updateImage() {
        clicks++;
		try {
			bi = ImageIO.read(file);
			drawable = bi.createGraphics();
		} catch (IOException e) {
			e.printStackTrace();
		}
        for (de.hofmann.Passantenfrequenz.model.Camera Camera : Cameras) {
            drawable.setColor(Color.RED);
            drawable.fillOval(Camera.getX() - 5, Camera.getY() - 5, 10, 10);

            BufferedImage image = new BufferedImage(100, 3, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D) image.getGraphics();

            g2.setColor(Color.RED);
            g2.fillRect(0, 0, image.getWidth() / 2, 3);
            g2.setColor(green);
            g2.fillRect(image.getWidth() / 2, 0, image.getWidth() / 2, 3);

            AffineTransform at = new AffineTransform();

            at.translate(Camera.getX(), Camera.getY());
            at.rotate(Math.toRadians(Camera.getRotationDeg()));
            at.translate(-image.getWidth() / 2, -image.getHeight() / 2);

            drawable.drawImage(image, at, null);
        }

        image.setSource(createStreamResource());
	}

	/**
     * @return StreamResource to set As Image source
     *
     * create StreamResource Using Global Variables bi BufferedImage
     */
	private StreamResource createStreamResource() {
	    return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = 1L;

			@Override
	        public InputStream getStream() {
	        	try {
	                ByteArrayOutputStream bos = new ByteArrayOutputStream();
	                ImageIO.write(bi, "png", bos);
	                return new ByteArrayInputStream(bos.toByteArray());
	            } catch (IOException e) {
	                e.printStackTrace();
	                return null;
	            }
	        }
	    }, String.format("%dtemp.png", clicks));
	}
}
