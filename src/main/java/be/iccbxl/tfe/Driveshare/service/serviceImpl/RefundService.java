package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Refund;
import be.iccbxl.tfe.Driveshare.repository.RefundRepository;
import be.iccbxl.tfe.Driveshare.service.RefundServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefundService implements RefundServiceI {

    @Autowired
    private RefundRepository refundRepository;

    @Override
    public void saveRefund(Refund refund) {
        refundRepository.save(refund);
    }
}
