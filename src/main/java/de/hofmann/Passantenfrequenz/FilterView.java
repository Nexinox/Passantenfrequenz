package de.hofmann.Passantenfrequenz;

import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.access.JaasAccessControl;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletException;

@CDIView(FilterView.VIEW_NAME)
@RolesAllowed({"admins","users"})
public class FilterView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	public static final String VIEW_NAME = "filter";
    
	List<Node> nodes = new ArrayList<Node>();
	BufferedImage bi;
	Graphics2D drawable;
	Image image;
	@SuppressWarnings("unused")
	private File file;
	@SuppressWarnings("unused")
	private int clicks = 0;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    	
        
        if(JaasAccessControl.getCurrentRequest().isUserInRole("admins")) {
        	makeToAdminButton();	
        }
        
        Button filterBtn = new Button("Start");
        filterBtn.addClickListener(e -> {
        	
        });
        
        DateField startDate = new DateField();
        startDate.setCaption("Start Datum");
        
        DateField endDate = new DateField();
        endDate.setCaption("End Datum");
        
        TextField startTime = new TextField();
        startTime.setCaption("von");
        TextField endTime = new TextField();
        endTime.setCaption("bis");
        
        Label Uhr1 = new Label("Uhr");
        Uhr1.setCaption(" ");
        
        Label Uhr2 = new Label("Uhr");
        Uhr2.setCaption(" ");
        
        addComponents(new HorizontalLayout(new VerticalLayout(startDate,endDate),
        		new VerticalLayout(new HorizontalLayout(startTime, Uhr1),
        		new HorizontalLayout(endTime, Uhr2))), filterBtn);
        

       
        Button logoutBtn = new Button("Logout");
        logoutBtn.addClickListener(e -> {
        	try {
				JaasAccessControl.logout();
				Page.getCurrent().reload();
			} catch (ServletException e1) {
				e1.printStackTrace();
			}
        });
       

        
	    image = new Image("");
        addComponents(image, logoutBtn);
    }
    
    private void makeToAdminButton() {
    	Button adminBtn = new Button("Settings", click ->{
    		UI.getCurrent().getNavigator().navigateTo(AdminView.VIEW_NAME);
    	});
    	addComponent(adminBtn);
    }
}
