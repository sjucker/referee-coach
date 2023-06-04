package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ch.stefanjucker.refereecoach.dto.RefereeDTO;
import ch.stefanjucker.refereecoach.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
public class AdminResource {

    private final AdminService adminService;

    public AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping(value = "/referee", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RefereeDTO>> game(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/admin/referee");

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            return ResponseEntity.status(FORBIDDEN).build();
        }

        return ResponseEntity.ok(adminService.getAllReferess());

    }

}
