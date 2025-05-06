package Controllers.Navigation;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AdminNavigationStateManager {
    private static AdminNavigationStateManager instance;
    private final StringProperty currentView = new SimpleStringProperty();

    private AdminNavigationStateManager() {}

    public static AdminNavigationStateManager getInstance() {
        if (instance == null) {
            instance = new AdminNavigationStateManager();
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