package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Gain;
import be.iccbxl.tfe.Driveshare.repository.GainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GainService {

    @Autowired
    private GainRepository gainRepository;

    public List<Gain> getGainsForUser(Long userId) {
        return gainRepository.findGainsByUserId(userId);
    }


    public void saveGain(Gain gain) {
        gainRepository.save(gain);
    }

    public List<Gain> getGainsForUserByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return gainRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
}
