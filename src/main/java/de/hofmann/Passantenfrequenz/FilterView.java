package de.hofmann.Passantenfrequenz;

import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.access.JaasAccessControl;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@CDIView(FilterView.VIEW_NAME)
@RolesAllowed({ "admins", "users" })
public class FilterView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	static final String VIEW_NAME = "filter";

	private List<Node> nodes = new ArrayList<Node>();
    private List<Camera> Cameras = new ArrayList<Camera>();
	private BufferedImage bi;
	private Graphics2D drawable;
	private Image image;
	private File file;

    @Override
	public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

		if (JaasAccessControl.getCurrentRequest().isUserInRole("admins")) {
			makeToAdminButton();
		}
        image = new Image("");
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
                    try {
                        this.file = file;
                        bi = ImageIO.read(file);
                        drawable = bi.createGraphics();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        readJson();

		DateField startDate = new DateField();
		startDate.setCaption("Start Datum");

		DateField endDate = new DateField();
		endDate.setCaption("End Datum");



		TextField startTime = new TextField();
		startTime.setCaption("von");
		startTime.setPlaceholder("00:00:00");
		TextField endTime = new TextField();
		endTime.setCaption("bis");
		endTime.setPlaceholder("00:00:00");

		Button filterBtn = new Button("Start");
		filterBtn.addClickListener(e -> {

			getEntrysInBetween(startDate.getValue(), endDate.getValue(), startTime.getValue(), endTime.getValue());

		});

		Label Uhr1 = new Label("Uhr");
		Uhr1.setCaption(" ");

		Label Uhr2 = new Label("Uhr");
		Uhr2.setCaption(" ");

		addComponents(
				new HorizontalLayout(new VerticalLayout(startDate, endDate),
						new VerticalLayout(new HorizontalLayout(startTime, Uhr1), new HorizontalLayout(endTime, Uhr2))),
				filterBtn);

		Button logoutBtn = new Button("Logout");
		logoutBtn.addClickListener(e -> {
			try {
				JaasAccessControl.logout();
				Page.getCurrent().reload();
			} catch (ServletException e1) {
				e1.printStackTrace();
			}
		});


		addComponents(image, logoutBtn);
	}

	private void makeToAdminButton() {
		Button adminBtn = new Button("Settings", click -> {
			UI.getCurrent().getNavigator().navigateTo(AdminView.VIEW_NAME);
		});
		addComponent(adminBtn);
	}

	@SuppressWarnings("unused")
	private void getEntrysInBetween(LocalDate earlyDate, LocalDate LaterDate, String earlyTime,
                                    String laterTime) {
		Connection c;
		Statement stmt;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/overlay", "postgres", "Ac062002");
			stmt = c.createStatement();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
			return;
		}

		String sql = "SELECT * FROM data WHERE CONCAT(\"Date\",' ',\"StartTime\") >= '"+earlyDate.toString()+ earlyTime +"' "
										+ "AND CONCAT(\"Date\",' ',\"EndTime\") <  '"+LaterDate.toString()+ laterTime+"'";

		try {
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				boolean writen = false;
					String name = rs.getString("CamName");
					for(Node node: nodes) {
						if (node.getName().equals(name)) {
							node.setIn(node.getIn()+rs.getInt("TotalIn"));
							node.setOut(node.getOut()+rs.getInt("totalout"));
							writen = true;
						}
					}
					if (!writen) {
						Node node = new Node();
						node.setName(name);
						node.setIn(node.getIn()+rs.getInt("TotalIn"));
						node.setOut(node.getOut()+rs.getInt("totalout"));
						nodes.add(node);
					}
					updateImage();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private void updateImage() {

        try {
            bi = ImageIO.read(file);
            drawable = bi.createGraphics();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Cameras.forEach(camera -> {
            Node node = new Node();
            for (Node node1 : nodes) {
                if (camera.getName().equals(node1.getName())){
                    node = node1;
                }
            }

           int barlength = 150;
//            int barlengthRed = barlength / 2;
//            int barlengthGre = barlength / 2;
//
//            if (node.getOut() < node.getIn()){
//                barlengthGre = (barlengthGre/100)*(100+((barlengthGre/100) * ((node.getIn()/100) *(100+(node.getOut() /100)))/100));
//                barlengthRed = barlength - barlengthGre;
//            }else {
//                barlengthRed = (barlengthRed/100)*(100+((barlengthRed/100) * ((node.getOut()/100) *(100+(node.getIn() /100)))/100));
//                barlengthGre = barlength - barlengthRed;
//            }


            int[] xPointsRed = {
                camera.getX()-barlength/2,
                camera.getX()-barlength/2,
                camera.getX()-barlength/2-10,
            };
            int[] yPointsRed = {
                camera.getY()-8,
                camera.getY()+8,
                camera.getY()
            };

            int[] xPointsGre = {
                camera.getX()+barlength/2,
                camera.getX()+barlength/2,
                camera.getX()+barlength/2+10,
            };
            int[] yPointsGre = {
                camera.getY()-8,
                camera.getY()+8,
                camera.getY()
            };

            drawable.setColor(Color.RED);
            drawable.fillRect(camera.getX()-barlength/2, camera.getY()-4,
                barlength/2, 8);
            drawable.fillPolygon(xPointsRed, yPointsRed, 3);
            drawable.drawString(Integer.toString(node.getOut()), camera.getX()-barlength/2, camera.getY()-10);

            drawable.setColor(Color.GREEN);
            drawable.fillRect(camera.getX(),
                camera.getY()-4, barlength/2,
                8);
            drawable.fillPolygon(xPointsGre, yPointsGre, 3);
            drawable.drawString(Integer.toString(node.getIn()), camera.getX(), camera.getY()-10);


        });

        image.setSource(createStreamResource());
    }
    private StreamResource createStreamResource() {
        int clicks = 0;
        return new StreamResource(new StreamResource.StreamSource() {
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
        }, clicks + "temp.png");

    }
    private void readJson() {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader("cameras.json"))
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
                Cameras.add(camera);
            });

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
