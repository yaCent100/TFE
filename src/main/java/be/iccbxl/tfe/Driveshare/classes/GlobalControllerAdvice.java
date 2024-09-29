package be.iccbxl.tfe.Driveshare.classes;

import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@ControllerAdvice(basePackages = "be.iccbxl.tfe.Driveshare.controller")
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addUserToModel(Model model, @AuthenticationPrincipal CustomUserDetail customUserDetail) {
        if (customUserDetail != null) {
            model.addAttribute("authenticatedUser", customUserDetail.getUser());
        }
    }

    @ModelAttribute
    public void addGlobalAttributes(HttpServletRequest request, HttpSession session, ServletContext servletContext, Model model) {
        // Ajout d'une vérification pour éviter les objets null
        if (request != null) model.addAttribute("request", request);
        if (session != null) model.addAttribute("session", session);
        if (servletContext != null) model.addAttribute("servletContext", servletContext);
    }


}


