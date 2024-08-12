package be.iccbxl.tfe.Driveshare.classes;

import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addUserToModel(Model model, @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail != null) {
            model.addAttribute("authenticatedUser", customUserDetail.getUser());
        }

    }
    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, HttpSession session, ServletContext servletContext, Model model) {
        model.addAttribute("request", request);
        model.addAttribute("session", session);
        model.addAttribute("servletContext", servletContext);
    }
}
