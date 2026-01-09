package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ch.stefanjucker.refereecoach.dto.CreateUserDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
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
    public ResponseEntity<List<UserDTO>> getReferee(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/admin/referee");

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            return ResponseEntity.status(FORBIDDEN).build();
        }

        return ResponseEntity.ok(adminService.getAllReferees());
    }

    @GetMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDTO>> getUsers(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/admin/user");

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            return ResponseEntity.status(FORBIDDEN).build();
        }

        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping(value = "/user", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public ResponseEntity<Void> createUser(@AuthenticationPrincipal UserDetails principal, @RequestBody @Valid CreateUserDTO dto) {
        log.info("POST /api/admin/user {}", dto.email());

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            throw new ResponseStatusException(FORBIDDEN);
        }

        adminService.createUser(dto);

        return ResponseEntity.ok().build();
    }

}
