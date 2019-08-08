package de.hofmann.Passantenfrequenz;


import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@CDIView(AdminView.VIEW_NAME)
@RolesAllowed("admins")
public class AdminView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	private BufferedImage bi;
	private Graphics2D drawable;
    static final String VIEW_NAME = "admin";
	private int clicks = 0;
	private List<Camera> Cameras = new ArrayList<Camera>();
	private boolean exists = false;
	private Image image;
	private Camera camera;
	private File file;
    @SuppressWarnings("unchecked")
	@Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    	readJson();

    	image = new Image();
    	HorizontalLayout imageWrapper = new HorizontalLayout(image);

    	ImageUploader receiver = new ImageUploader();

	    Upload upload = new Upload("Bild Hochladen", receiver);

        Button saveBtn = new Button("Spechern");



        File rsc = new File("rsc/");
        if(!rsc.exists()) {
        	rsc.mkdir();
        }

        File[] files = new File("rsc/").listFiles(new FilenameFilter() {
        	@Override
        	public boolean accept(File dir, String name) {
                return name.endsWith(".png") || name.endsWith(".PNG");

        	}
        });

        if(files != null) {
	        for(File file : files) {
	        	if(file.isFile()) {
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
	        }
        }

	    image.addClickListener(e -> {
	    	clicks++;

	    	Cameras.forEach(cam->{
	    		int dist = (int) Math.sqrt((e.getRelativeX()-cam.getX())*(e.getRelativeX()-cam.getX()) + (e.getRelativeY()-cam.getY())*(e.getRelativeY()-cam.getY()));
	    		if(dist <= 5) {
	    			camera = cam;
	    			exists = true;
	    		}
	    	});

	    	if(!exists) {
	    		camera = new Camera();
		    	camera.setName("Kamera" + Integer.toString(clicks));
		    	camera.setX(e.getRelativeX());
		    	camera.setY(e.getRelativeY());

		    	Cameras.add(camera);
	    	}

	    	VerticalLayout settingsWrapper = new VerticalLayout();
	    	TextField camName = new TextField("Name");
	    	camName.setValue(camera.getName());
	    	Button save = new Button("Speichern", click->{
	    		camera.setName(camName.getValue());
	    		imageWrapper.removeComponent(settingsWrapper);
	    	});
	    	Button delete = new Button("Löschen", click->{
	    		Cameras.remove(camera);
	    		imageWrapper.removeComponent(settingsWrapper);
	    		clicks++;
	    		updateImage();
	    	});
	    	settingsWrapper.addComponents(camName,save,delete);



	    	imageWrapper.addComponent(settingsWrapper);
	    	exists = false;
	    	updateImage();
	    });

	    saveBtn.addClickListener(click -> {

	    	JSONArray cameraArray = new JSONArray();


	        Cameras.forEach(camera ->{

	        	JSONObject cameraObject = new JSONObject();

	        	JSONObject cameraDetails = new JSONObject();

	        	cameraDetails.put("name", camera.getName());
	        	cameraDetails.put("x", camera.getX());
	        	cameraDetails.put("y", camera.getY());

	        	cameraObject.put("camera", cameraDetails);

	        	cameraArray.add(cameraObject);

	        });

	        try (FileWriter file = new FileWriter("cameras.json")) {

	            file.write(cameraArray.toJSONString());
	            file.flush();
	            file.close();
	            UI.getCurrent().getNavigator().navigateTo(FilterView.VIEW_NAME);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    });


	    upload.addSucceededListener(receiver);

	    addComponents(upload, imageWrapper, saveBtn);
    }

    @SuppressWarnings("unchecked")
	private void readJson() {
    	 JSONParser jsonParser = new JSONParser();

         try (FileReader reader = new FileReader("cameras.json"))
         {
             //Read JSON file
             Object obj = jsonParser.parse(reader);

             JSONArray cameraArray = (JSONArray) obj;

             //Iterate over employee array
             cameraArray.forEach(cam -> {

            	 JSONObject cameraObject = (JSONObject) cam;
            	 JSONObject cameraObj = (JSONObject) cameraObject.get("camera");

            	 Camera camera = new Camera();
            	 camera.setName((String) cameraObj.get("name"));
            	 camera.setX((int) (long) cameraObj.get("x"));
            	 camera.setY((int) (long) cameraObj.get("y"));
            	 Cameras.add(camera);
             });

         } catch (IOException | ParseException e) {
             e.printStackTrace();
         }
    }



	class ImageUploader implements Receiver, SucceededListener {
		private static final long serialVersionUID = 1L;
		public OutputStream receiveUpload(String filename,
                                          String mimeType) {

			if(filename.endsWith(".png") || filename.endsWith(".PNG")) {
	            FileOutputStream fos = null;
	            File[] files = new File("rsc/").listFiles();

	            try {
	                if(files != null) {
	        	        for(File file : files) {
	        	        	if(file.isFile()) {
	        	        		file.delete();
	        	        	}
	        	        }
	                }
	                file = new File("rsc/"+filename);
	                fos = new FileOutputStream(file);
	            } catch (final java.io.FileNotFoundException e) {
	                new Notification("failed",
	                				e.getMessage(),
	                                 Notification.Type.ERROR_MESSAGE)
	                    .show(Page.getCurrent());
	                return null;
	            }
	            return fos;
			}else {
				 new Notification("failed",
         				"file needs to be .png or .PNG",
                          Notification.Type.ERROR_MESSAGE)
             .show(Page.getCurrent());
				 return null;
			}

        }

        public void uploadSucceeded(SucceededEvent event) {
        	try {
				bi = ImageIO.read(file);
				drawable = bi.createGraphics();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	image.setSource(new FileResource(file));
        	updateImage();
        }

    };
	private void updateImage() {

		try {
			bi = ImageIO.read(file);
			drawable = bi.createGraphics();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Cameras.forEach(camera -> {

	    		drawable.setColor(Color.RED);
	    		drawable.fillOval(camera.getX()-5,camera.getY()-5,10,10);


		});

		image.setSource(createStreamResource());
	}
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
	    }, Integer.toString(clicks) + "temp.png");

	}
}
