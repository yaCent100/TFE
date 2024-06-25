package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.User;

import java.time.LocalDate;
import java.util.List;

public interface PaymentServiceI {
    List<Payment> getPaymentsForUser(User user, LocalDate startDate, LocalDate endDate);
}
