package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.NotificationPreference;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.NotificationPreferenceRepository;
import be.iccbxl.tfe.Driveshare.service.NotificationPreferenceServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationPreferenceService implements NotificationPreferenceServiceI {

    @Autowired
    private NotificationPreferenceRepository repository;

    @Override
    public NotificationPreference getPreferences(User user) {
        return repository.findByUser(user).orElse(null);
    }
    @Override
    public void savePreferences(NotificationPreference preferences) {
        repository.save(preferences);
    }
}
