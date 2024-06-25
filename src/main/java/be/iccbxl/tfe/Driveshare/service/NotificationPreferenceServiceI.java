package be.iccbxl.tfe.Driveshare.service;


import be.iccbxl.tfe.Driveshare.model.NotificationPreference;
import be.iccbxl.tfe.Driveshare.model.User;

public interface NotificationPreferenceServiceI {

    NotificationPreference getPreferences(User user);
    void savePreferences(NotificationPreference preferences);
}
