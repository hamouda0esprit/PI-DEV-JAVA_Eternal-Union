package Controllers.Navigation;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NavigationStateManager {
    private static NavigationStateManager instance;
    private final StringProperty currentView = new SimpleStringProperty();

    private NavigationStateManager() {}

    public static NavigationStateManager getInstance() {
        if (instance == null) {
            instance = new NavigationStateManager();
        }
        return instance;
    }

    public String getCurrentView() {
        return currentView.get();
    }

    public void setCurrentView(String view) {
        currentView.set(view);
    }

    public StringProperty currentViewProperty() {
        return currentView;
    }
}