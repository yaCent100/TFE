package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
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

    public String formatAndCapitalizeDate(LocalDate localDate) {
        if (localDate == null) {
            throw new IllegalArgumentException("The provided object is null");
        }

        String dayOfWeek = localDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
        dayOfWeek = capitalize(dayOfWeek.substring(0, 3)); // Pour avoir les trois premières lettres en majuscules

        String month = localDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
        month = capitalize(month.substring(0, 3)) + "."; // Ajoute un point après le mois

        return String.format("%s. %d %s %d", dayOfWeek, localDate.getDayOfMonth(), month, localDate.getYear());
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}
