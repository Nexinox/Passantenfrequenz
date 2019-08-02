package de.hofmann.Passantenfrequenz;

import javax.servlet.ServletException;

import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.access.JaasAccessControl;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@CDIView("unauthorized")
public class InaccessibleErrorView extends VerticalLayout implements View {

	private static final long serialVersionUID = 1L;

	@Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        addComponent(new Label("Sorry, view does not exist or you are not authorized to access it"));
        
        addComponent(new Button("To Login", c->{
        	
        	if(JaasAccessControl.getCurrentRequest().getUserPrincipal() != null) {
        		try {
					JaasAccessControl.logout();
					UI.getCurrent().getNavigator().navigateTo(LoginView.VIEW_NAME + "/" + FilterView.VIEW_NAME);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else {
        		UI.getCurrent().getNavigator().navigateTo(LoginView.VIEW_NAME + "/" + FilterView.VIEW_NAME);
        	}
        
        	
        	
        }));
    }

}
