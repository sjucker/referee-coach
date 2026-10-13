package ch.stefanjucker.refereecoach.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import ch.stefanjucker.refereecoach.dto.CreateUserDTO;
import ch.stefanjucker.refereecoach.dto.TagNameDTO;
import ch.stefanjucker.refereecoach.dto.TagUsageDTO;
import ch.stefanjucker.refereecoach.dto.UpdateUserDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Secured("ADMIN")
public class AdminResource {

    private final AdminService adminService;

    public AdminResource(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping(value = "/referee", produces = APPLICATION_JSON_VALUE)
    public List<UserDTO> getReferee() {
        log.info("GET /api/admin/referee");

        return adminService.getAllReferees();
    }

    @GetMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
    public List<UserDTO> getUsers() {
        log.info("GET /api/admin/user");

        return adminService.getAllUsers();
    }

    @PostMapping(value = "/user", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public void createUser(@RequestBody @Valid CreateUserDTO dto) {
        log.info("POST /api/admin/user {}", dto.email());

        adminService.createUser(dto);
    }

    @PutMapping(value = "/user/{id}", consumes = APPLICATION_JSON_VALUE)
    public void updateUser(@PathVariable Long id, @RequestBody @Valid UpdateUserDTO dto) {
        log.info("PUT /api/admin/user/{} {}", id, dto);

        adminService.updateUser(id, dto);
    }

    @GetMapping(value = "/tag", produces = APPLICATION_JSON_VALUE)
    public List<TagUsageDTO> getTags() {
        log.info("GET /api/admin/tag");

        return adminService.getAllTags();
    }

    @PostMapping(value = "/tag", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(CREATED)
    public TagUsageDTO createTag(@RequestBody @Valid TagNameDTO dto) {
        log.info("POST /api/admin/tag {}", dto.name());

        return adminService.createTag(dto);
    }

    @PutMapping(value = "/tag/{id}", consumes = APPLICATION_JSON_VALUE)
    public void renameTag(@PathVariable Long id, @RequestBody @Valid TagNameDTO dto) {
        log.info("PUT /api/admin/tag/{} {}", id, dto.name());

        adminService.renameTag(id, dto);
    }

    @DeleteMapping(value = "/tag/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteTag(@PathVariable Long id) {
        log.info("DELETE /api/admin/tag/{}", id);

        adminService.deleteTag(id);
    }

}
