package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message Management", description = "Gestion des messages de réservation")
public class ChatMessageRestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Operation(summary = "Obtenir les messages par réservation", description = "Retourne la liste des messages échangés pour une réservation spécifique.")
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<List<ChatMessageDTO>> getMessagesByReservation(
            @Parameter(description = "L'ID de la réservation", required = true) @PathVariable Long reservationId,
            Principal principal) {

        // Vérification de l'authentification
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Récupération de l'utilisateur courant
        CustomUserDetail userDetails = (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        Long currentUserId = userDetails.getUser().getId();

        // Récupération des messages par réservation et en fonction de l'utilisateur actuel
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByReservationId(reservationId, currentUserId);

        return ResponseEntity.ok(messages);
    }


}
