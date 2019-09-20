package de.hofmann.Passantenfrequenz.ui;

import com.vaadin.annotations.JavaScript;
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
import de.hofmann.Passantenfrequenz.dataacess.DatabaseClientImpl;
import de.hofmann.Passantenfrequenz.model.Camera;
import de.hofmann.Passantenfrequenz.model.Node;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

/**
 * Main View for filtering the entrys and displaying them
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@JavaScript({"vaadin://themes/print.min.js", "vaadin://themes/main.js"})
@CDIView(FilterView.VIEW_NAME)
@RolesAllowed({ "admins", "users" })
public class FilterView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	static final String VIEW_NAME = "filter";

	private Collection<Node> nodes = new ArrayList<>();
    private Collection<Camera> Cameras = new ArrayList<>();
	private BufferedImage bi, biFile;
	private Graphics2D drawable, drawableFile;
	private Image image;
	private File file;
	private int clicks = 0;
    private DatabaseClientImpl client;
    private DateField startDate, endDate;
    private boolean printButtonVisible = false;
    private Color green = new Color(0, 153, 51);
    Button printMainImage;
    @Override
	public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        client = new DatabaseClientImpl();
        image = new Image("");
        image.addStyleName("maxSize800");
        image.setId("mainImg");
        File rsc = new File("rsc/img/");
        if(!rsc.exists()) rsc.mkdirs();

        File[] files = new File("rsc/img/").listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".PNG"));

        if(files != null) {
            for(File file : files) {
                if(file.isFile()) {
                    this.file = file;
                }
            }
        }

        readJson();

		startDate = new DateField();
		startDate.setCaption("Start Datum");
        startDate.setValue(LocalDate.now().minusDays(1));
		endDate = new DateField();
		endDate.setCaption("End Datum");
        endDate.setValue(LocalDate.now());


        TextField startTime = new TextField();
		startTime.setCaption("von");
		startTime.setValue("00:00:00");
        TextField endTime = new TextField();
		endTime.setCaption("bis");
		endTime.setValue("00:00:00");
        printMainImage = new Button("Print", e->{
            com.vaadin.ui.JavaScript.getCurrent().execute("printImg()");
        });
        printMainImage.setVisible(printButtonVisible);
		Button filterBtn = new Button("Start");
		filterBtn.addClickListener(e -> getEntrysInBetween(startDate.getValue(),
            endDate.getValue(), startTime.getValue(), endTime.getValue()));

		Label Uhr1 = new Label("Uhr");
		Uhr1.setCaption(" ");

		Label Uhr2 = new Label("Uhr");
		Uhr2.setCaption(" ");


        HorizontalLayout optLay = new HorizontalLayout();

        Button logoutBtn = new Button("Logout");
        logoutBtn.addClickListener(e -> {
            try {
                JaasAccessControl.logout();
                Page.getCurrent().reload();
            } catch (ServletException e1) {
                e1.printStackTrace();
            }
        });

        if (JaasAccessControl.getCurrentRequest().isUserInRole("admins")) {
            Button adminBtn = new Button("Settings", click -> UI.getCurrent().getNavigator().navigateTo(AdminView.VIEW_NAME));
            optLay.addComponents(adminBtn, logoutBtn);
        }else {
            optLay.addComponent(logoutBtn);
        }
        optLay.setComponentAlignment(logoutBtn, Alignment.TOP_RIGHT);
        optLay.setSizeFull();

		addComponents(optLay,
				new HorizontalLayout(new VerticalLayout(startDate, endDate, filterBtn, printMainImage),
						new VerticalLayout(new HorizontalLayout(startTime, Uhr1),
                            new HorizontalLayout(endTime, Uhr2)), image));




	}

    /**
     * @param earlyDate The start date of the query
     * @param LaterDate The end date of the query
     * @param earlyTime The start time of the query
     * @param laterTime The end date of the query
     *
     * Retrieves Entrys from the database and updates the image according to Cameras, created Nodes
     */
	private void getEntrysInBetween(LocalDate earlyDate, LocalDate LaterDate, String earlyTime,
                                    String laterTime) {
		String sql = String.format("SELECT * FROM data WHERE CONCAT(\"Date\",' ',\"StartTime\") >= '%s%s' AND CONCAT(\"Date\",' ',\"EndTime\") <  '%s%s'",
            earlyDate.toString(), earlyTime, LaterDate.toString(), laterTime);

		try {
            ResultSet rs = client.getBySql(sql);
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

			}
            updateImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private int totalIn, totalOut;
    /**
     * Redraws the image according to Nodes, Cameras and calls createStreamResource will also call one of
     * or both DrawText and DrawRotatedText and adds one to global variable clicks when called
     */
    private void updateImage() {

        try {
            biFile = ImageIO.read(file);
            bi = new BufferedImage(biFile.getWidth(), biFile.getHeight()+30, BufferedImage.TYPE_INT_ARGB);
            drawable = bi.createGraphics();
            drawableFile = biFile.createGraphics();
        } catch (IOException e) {
            e.printStackTrace();
        }
        drawable.setColor(Color.WHITE);
        drawable.fillRect(0,0, bi.getWidth(), bi.getHeight());
        drawable.setColor(Color.BLACK);
        for (Camera camera : Cameras) {
            Node node = new Node();
            for (Node node1 : nodes) if (camera.getName().equals(node1.getName())) node = node1;
            totalIn += node.getIn();
            totalOut += node.getOut();
            int barLength = 100;

            BufferedImage image = new BufferedImage(barLength + 40, 45, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = (Graphics2D) image.getGraphics();

            int[] xPointsRed = {
                image.getWidth() / 2 - barLength / 2,
                image.getWidth() / 2 - barLength / 2,
                image.getWidth() / 2 - barLength / 2 - 15,
            };
            int[] yPointsRed = {
                image.getHeight() / 2 - 11,
                image.getHeight() / 2 + 11,
                image.getHeight() / 2
            };

            int[] xPointsGre = {
                image.getWidth() / 2 + barLength / 2,
                image.getWidth() / 2 + barLength / 2,
                image.getWidth() / 2 + barLength / 2 + 15,
            };
            int[] yPointsGre = {
                image.getHeight() / 2 - 11,
                image.getHeight() / 2 + 11,
                image.getHeight() / 2
            };

            g2.setColor(Color.RED);
            g2.fillRect(image.getWidth() / 2 - barLength / 2, image.getHeight() / 2 - 5,
                barLength / 2, 10);
            g2.fillPolygon(xPointsRed, yPointsRed, 3);

            g2.setColor(green);
            g2.fillRect(image.getWidth() / 2,
                image.getHeight() / 2 - 5, barLength / 2,
                10);
            g2.fillPolygon(xPointsGre, yPointsGre, 3);

            if (camera.getRotationDeg() >= 91 && camera.getRotationDeg() <= 269) {
                drawRotatedText(node, barLength, image, g2);
            } else {
                drawText(node, barLength, image, g2);
            }

            AffineTransform at = new AffineTransform();

            at.translate(camera.getX(), camera.getY());
            at.rotate(Math.toRadians(camera.getRotationDeg()));
            at.translate(-image.getWidth() / 2, -image.getHeight() / 2);

            if (!printButtonVisible){
                printButtonVisible = true;
                printMainImage.setVisible(printButtonVisible);
            }

            drawableFile.drawImage(image, at, null);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.yy");


        drawable.setFont(new Font(drawable.getFont().getFontName(), Font.BOLD, 24));
        drawable.drawString("Passantenfrequenz", 10, 28);
        drawable.setFont(new Font(drawable.getFont().getFontName(), Font.BOLD, 14));
        String zeitraum = "Zeitraum von "+startDate.getValue().format(df)+" bis " +endDate.getValue().format(df);
        drawable.drawString(zeitraum , bi.getWidth()-drawable.getFontMetrics(drawable.getFont()).stringWidth(zeitraum), 14);
        String gesamt = "Gesamt "+ NumberFormat.getInstance(Locale.GERMANY).format(totalIn)+" hinein  "+ NumberFormat.getInstance(Locale.GERMANY).format(totalOut)+" hinaus ";
        drawable.drawString(gesamt, bi.getWidth()-drawable.getFontMetrics(drawable.getFont()).stringWidth(gesamt), 29);
        drawable.drawImage(biFile, 0, 30, null);

        totalOut = 0;
        totalIn = 0;
        nodes = new ArrayList<>();
        image.setSource(createStreamResource());
        clicks++;
    }

    /**
     * @param node the node from which to get values from
     * @param barLength the length of the bar for calculations
     * @param image the buffered image for calculations
     * @param g2 way to draw to buffered image
     *
     * used by updateImage() to draw in and out Number and make it more readable
     */
    private void drawText(Node node, int barLength, BufferedImage image, Graphics2D g2) {
        BufferedImage text = new BufferedImage(barLength, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D textDraw = (Graphics2D) text.getGraphics();
        if (node.getIn() >= 10000 || node.getOut() >= 10000) {
            textDraw.setFont(new Font(textDraw.getFont().getFontName(), Font.BOLD, 12));
        }else textDraw.setFont(new Font(textDraw.getFont().getFontName(), Font.BOLD, 16));
        textDraw.setColor(Color.RED);
        textDraw.drawString(NumberFormat.getInstance(Locale.GERMANY).format(node.getOut()), 0, 15);
        textDraw.setColor(green);
        textDraw.drawString(NumberFormat.getInstance(Locale.GERMANY).format(node.getIn()), text.getWidth() / 2, 15);

        g2.drawImage(text, image.getWidth()/2-barLength/2, 0, null);
    }

    /**
     * @param node the node from which to get values from
     * @param barLength the length of the bar for calculations
     * @param image the buffered image for calculations
     * @param g2 way to draw to buffered image
     *
     * used by updateImage() to draw in and out Number rotate by 180° if necessary and make it more readable
     */
    private void drawRotatedText(Node node, int barLength, BufferedImage image, Graphics2D g2) {
        BufferedImage text = new BufferedImage(barLength, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D textDraw = (Graphics2D) text.getGraphics();
        if (node.getIn() >= 10000 || node.getOut() >= 10000) {
            textDraw.setFont(new Font(textDraw.getFont().getFontName(), Font.BOLD, 12));
        }else textDraw.setFont(new Font(textDraw.getFont().getFontName(), Font.BOLD, 16));
        textDraw.setColor(Color.RED);
        textDraw.drawString(NumberFormat.getInstance(Locale.GERMANY).format(node.getOut()), text.getWidth() / 2, 15);
        textDraw.setColor(green);
        textDraw.drawString(NumberFormat.getInstance(Locale.GERMANY).format(node.getIn()), 0, 15);

        AffineTransform at = new AffineTransform();

        at.translate(image.getWidth()/2, image.getHeight()-text.getHeight()/2);
        at.rotate(Math.toRadians(180));
        at.translate(-text.getWidth()/2, -text.getHeight()/2);

        g2.drawImage(text, at, null);
    }

    /**
     * @return StreamResource to set As Image source
     *
     * create StreamResource Using Global Variables bi BufferedImage
     */
    private StreamResource createStreamResource() {

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
}
