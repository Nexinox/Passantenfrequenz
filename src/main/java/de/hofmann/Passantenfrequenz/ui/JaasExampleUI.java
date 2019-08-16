package de.hofmann.Passantenfrequenz.ui;

import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.inject.Inject;

/**
 * Main Ui of application handles navigating and authentication
 */
@CDIUI("")
@Theme("jaasexampletheme")
public class JaasExampleUI extends UI {

	private static final long serialVersionUID = 1L;
	@Inject
    private CDIViewProvider viewProvider;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout contentArea = new VerticalLayout();
        contentArea.setMargin(false);
        setContent(contentArea);

        final Navigator navigator = new Navigator(this, contentArea);
        navigator.addProvider(viewProvider);
        navigator.setErrorView(InaccessibleErrorView.class);

        if (isUserAuthenticated(vaadinRequest)) {
            navigator.navigateTo(FilterView.VIEW_NAME);
            vaadinRequest.getUserPrincipal().getName();
        } else navigator.navigateTo(LoginView.VIEW_NAME + "/" + FilterView.VIEW_NAME);
    }

    private boolean isUserAuthenticated(final VaadinRequest vaadinRequest) {
        return vaadinRequest.getUserPrincipal() != null;
    }

}
