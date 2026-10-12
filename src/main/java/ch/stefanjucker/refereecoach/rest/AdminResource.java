package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ch.stefanjucker.refereecoach.dto.CreateUserDTO;
import ch.stefanjucker.refereecoach.dto.TagNameDTO;
import ch.stefanjucker.refereecoach.dto.TagUsageDTO;
import ch.stefanjucker.refereecoach.dto.UpdateUserDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    public void createUser(@AuthenticationPrincipal UserDetails principal, @RequestBody @Valid CreateUserDTO dto) {
        log.info("POST /api/admin/user {}", dto.email());

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            throw new ResponseStatusException(FORBIDDEN);
        }
        adminService.createUser(dto);
    }

    @PutMapping(value = "/user/{id}", consumes = APPLICATION_JSON_VALUE)
    public void updateUser(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long id, @RequestBody @Valid UpdateUserDTO dto) {
        log.info("PUT /api/admin/user/{} {}", id, dto);

        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            throw new ResponseStatusException(FORBIDDEN);
        }
        adminService.updateUser(id, dto);
    }

    @GetMapping(value = "/tag", produces = APPLICATION_JSON_VALUE)
    public List<TagUsageDTO> getTags(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/admin/tag");

        checkAdmin(principal);
        return adminService.getAllTags();
    }

    @PostMapping(value = "/tag", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public TagUsageDTO createTag(@AuthenticationPrincipal UserDetails principal, @RequestBody @Valid TagNameDTO dto) {
        log.info("POST /api/admin/tag {}", dto.name());

        checkAdmin(principal);
        return adminService.createTag(dto);
    }

    @PutMapping(value = "/tag/{id}", consumes = APPLICATION_JSON_VALUE)
    public void renameTag(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long id, @RequestBody @Valid TagNameDTO dto) {
        log.info("PUT /api/admin/tag/{} {}", id, dto.name());

        checkAdmin(principal);
        adminService.renameTag(id, dto);
    }

    @DeleteMapping(value = "/tag/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteTag(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        log.info("DELETE /api/admin/tag/{}", id);

        checkAdmin(principal);
        adminService.deleteTag(id);
    }

    private void checkAdmin(UserDetails principal) {
        if (!adminService.isAdmin(principal.getUsername())) {
            log.error("user {} tried to access admin-endpoint without having admin rights", principal.getUsername());
            throw new ResponseStatusException(FORBIDDEN);
        }
    }

}
