package com.example.application.views;


import java.util.stream.Collectors;

import org.vaadin.lineawesome.LineAwesomeIcon;

import com.example.application.customEvents.LoginEventBus;
import com.example.application.utils.AuthService;
import com.example.application.views.helloworld.AnemoiaView;
import com.example.application.views.helloworld.GamingForm;
import com.example.application.views.helloworld.TestForm;
import com.example.application.views.helloworld.TestGridView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility.AlignItems;
import com.vaadin.flow.theme.lumo.LumoUtility.BoxSizing;
import com.vaadin.flow.theme.lumo.LumoUtility.Display;
import com.vaadin.flow.theme.lumo.LumoUtility.FlexDirection;
import com.vaadin.flow.theme.lumo.LumoUtility.FontSize;
import com.vaadin.flow.theme.lumo.LumoUtility.FontWeight;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Height;
import com.vaadin.flow.theme.lumo.LumoUtility.ListStyleType;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Overflow;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import com.vaadin.flow.theme.lumo.LumoUtility.TextColor;
import com.vaadin.flow.theme.lumo.LumoUtility.Whitespace;
import com.vaadin.flow.theme.lumo.LumoUtility.Width;

/**
 * The main view is a top-level placeholder for other views.
 */
@Layout
@AnonymousAllowed
public class MainLayout extends AppLayout implements RouterLayout {
    private MenuItemInfo signInButton;
    private MenuItemInfo profileButton;
    private Registration listenerRegistration;

    /**
     * A simple navigation item component, based on ListItem element.
     */
    public static class MenuItemInfo extends ListItem {

        private final Class<? extends Component> view;

        public MenuItemInfo(String menuTitle, Component icon, Class<? extends Component> view) {
            this.view = view;
            
            if (view != MainLayout.class) {
            RouterLink link = new RouterLink();
            // Use Lumo classnames for various styling
            link.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL,
                    TextColor.BODY);
            link.setRoute(view);

            Span text = new Span(menuTitle);
            // Use Lumo classnames for various styling
            text.addClassNames(FontWeight.MEDIUM, FontSize.MEDIUM, Whitespace.NOWRAP);
            link.add(text);

            if (icon != null) {
                link.add(icon);
            } else {
                Menu menuAnnotation = view.getAnnotation(Menu.class);
                if (menuAnnotation != null) {
                    Div svgIcon = new Div();
                    svgIcon.getStyle().set("background-image", "url('/"+menuAnnotation.icon()+"')");
                    svgIcon.setWidth("24px");
                    svgIcon.setHeight("24px");
                    link.add(svgIcon);
                }
            }
                add(link);
            } else { // PRofile icon
                Icon profileIcon = new Icon(VaadinIcon.USER);

                // Create the MenuBar
                MenuBar menuBar = new MenuBar();
                menuBar.addThemeNames("small", "tertiary");

                // Add the profile icon as the root menu item
                var rootItem = menuBar.addItem(profileIcon);

                // Add dropdown items (e.g., Profile and Logout)
                rootItem.getSubMenu().addItem("Profile", event -> {
                    // Navigate to profile page or show profile dialog
                    UI.getCurrent().navigate("profile");
                });
                rootItem.getSubMenu().addItem("Logout", event -> {
                    // Trigger a POST request to /logout using a hidden form
                    UI.getCurrent().getPage().executeJs(
                        "var form = document.createElement('form');" +
                        "form.method = 'POST';" +
                        "form.action = '/logout';" +
                        "document.body.appendChild(form);" +
                        "form.submit();"
                    );
                });


                Div div = new Div();
                div.add(menuBar);
                div.addClassNames(Display.FLEX, Gap.XSMALL, Height.MEDIUM, AlignItems.CENTER, Padding.Horizontal.SMALL,
                        TextColor.BODY);

                // Add to the layout
                add(div);
            }
        }

        public Class<?> getView() {
            return view;
        }

    }

    public MainLayout() {
        
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();

        mainContent.add(createHeaderContent()); // header

        VerticalLayout contentArea = new VerticalLayout();
        contentArea.setSizeFull(); 
        mainContent.add(contentArea); // render view ( placeholder )

        mainContent.add(createFooterContent()); // footer

        updateNavigationVisibility();
        setContent(mainContent);

        listenerRegistration = LoginEventBus.getInstance().addListener(() -> {updateNavigationVisibility(); });
    }

    @Override 
    public void showRouterLayoutContent(HasElement content) {
        VerticalLayout contentArea = findContentArea(getContent());
        if (contentArea != null) {
            contentArea.removeAll();
            contentArea.add((Component) content);
        }
    }

    @Override
    public void onDetach(DetachEvent event) {
        super.onDetach(event);
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private void updateNavigationVisibility() {
        signInButton.setVisible(!AuthService.isLoggedIn());
        profileButton.setVisible(AuthService.isLoggedIn());
    }

    private VerticalLayout findContentArea(Component root) {
        if (root instanceof VerticalLayout) {
            VerticalLayout layout = (VerticalLayout) root;
            for (Component child : layout.getChildren().collect(Collectors.toList())) {
                if (child instanceof VerticalLayout) {
                    return (VerticalLayout) child;
                } 
            }
        }
        return null;
    }

    private Component createHeaderContent() {
        Header header = new Header();
        header.addClassNames(BoxSizing.BORDER, Display.FLEX, FlexDirection.COLUMN, Width.FULL);

        Div layout = new Div();
        layout.addClassNames(Display.FLEX, AlignItems.CENTER, Padding.Horizontal.LARGE);

        H3 appName = new H3("First App");
        appName.addClassNames(Margin.Vertical.MEDIUM, Margin.End.AUTO, FontSize.LARGE);
        layout.add(appName);

        Nav nav = new Nav();
        nav.addClassNames(Display.FLEX, Overflow.AUTO, Padding.Horizontal.MEDIUM, Padding.Vertical.XSMALL);

        // Wrap the links in a list; improves accessibility
        UnorderedList list = new UnorderedList();
        list.addClassNames(Display.FLEX, Gap.SMALL, ListStyleType.NONE, Margin.NONE, Padding.NONE);
        nav.add(list);

        for (MenuItemInfo menuItem : createMenuItems()) {
            if (menuItem.getView() == SigninView.class) {
                signInButton = menuItem;
            } else if (menuItem.getView() == MainLayout.class) {
                profileButton = menuItem;
            }
            list.add(menuItem);
        }

        header.add(layout, nav);
        return header;
    }

    private Component createFooterContent() {
        HorizontalLayout footer = new HorizontalLayout();
        Div errorMsgBox = new Div();
        errorMsgBox.addClassName("error-msg");
        footer.addClassNames(Display.FLEX, Gap.MEDIUM, AlignItems.CENTER, TextColor.SECONDARY);
        footer.add("© 2025 My App - Cheeky footer");
        footer.add(errorMsgBox);
        return footer;
    }

    private MenuItemInfo[] createMenuItems() {
        return new MenuItemInfo[]{ //
            new MenuItemInfo("Home", LineAwesomeIcon.ADOBE.create(), HomeView.class), //
            new MenuItemInfo("Anemoia", LineAwesomeIcon.ACCUSOFT.create(), AnemoiaView.class),
            new MenuItemInfo("Gaming Details", LineAwesomeIcon.ANDROID.create(), GamingForm.class),
            new MenuItemInfo("Kolmas Linkki", null, TestForm.class),
            new MenuItemInfo("Gridding way", LineAwesomeIcon.ZHIHU.create(), TestGridView.class),
            new MenuItemInfo("Sign In", LineAwesomeIcon.AMBULANCE_SOLID.create(), SigninView.class),
            new MenuItemInfo("", LineAwesomeIcon.POWER_OFF_SOLID.create(), MainLayout.class)
         };
    }

}
