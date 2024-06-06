package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DateService {

    public List<Integer> range(int start, int end) {
        return IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }

    public Map<Integer, String> getMonths() {
        String[] months = new DateFormatSymbols().getMonths();
        Map<Integer, String> monthMap = new LinkedHashMap<>();
        for (int i = 0; i < months.length - 1; i++) { // ignore empty last entry
            monthMap.put(i + 1, months[i]);
        }
        return monthMap;
    }

    public List<Integer> getYears(int yearsBack) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        return IntStream.rangeClosed(currentYear - yearsBack, currentYear).boxed().collect(Collectors.toList());
    }
}
