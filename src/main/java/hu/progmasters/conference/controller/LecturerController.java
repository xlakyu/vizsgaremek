package hu.progmasters.conference.controller;

import hu.progmasters.conference.dto.LecturerCreateUpdateCommand;
import hu.progmasters.conference.dto.LecturerInfo;
import hu.progmasters.conference.service.LecturerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/lecturers")
public class LecturerController {

    private static final String HTTP_RESPONSE = "Http response: %s, %s";
    private static final String LOG_GET = "Http request, GET /api/lecturers%s";
    private static final String LOG_POST = "Http request, POST /api/lecturers, body: {}";
    private static final String LOG_PUT = "Http request, PUT /api/lecturers%s, body: %s";
    private static final String LOG_DELETE = "Http request, DELETE /api/lecturers%s";

    private static final Logger LOGGER = LoggerFactory.getLogger(LecturerController.class);

    private LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @PostMapping
    public ResponseEntity<LecturerInfo> saveLecturer(@Valid @RequestBody LecturerCreateUpdateCommand command) {
        LOGGER.info(LOG_POST, String.format(command.toString()));
        LecturerInfo saved = lecturerService.save(command);
        LOGGER.info(String.format(HTTP_RESPONSE, "CREATED", saved));
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LecturerInfo> findLecturerById(@PathVariable("id") Integer id) {
        LOGGER.info(String.format(LOG_GET, "/" + id));
        LecturerInfo lecturerFound = lecturerService.findById(id);
        LOGGER.info(String.format(HTTP_RESPONSE, "OK", lecturerFound));
        return new ResponseEntity<>(lecturerFound, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<LecturerInfo>> findAll() {
        LOGGER.info(String.format(LOG_GET, ""));
        List<LecturerInfo> lecturers = lecturerService.findAll();
        LOGGER.info(String.format(HTTP_RESPONSE, "OK", lecturers));
        return new ResponseEntity<>(lecturers, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LecturerInfo> updateLecturer(@Valid @PathVariable("id") Integer id,
                                                       @Valid @RequestBody LecturerCreateUpdateCommand command) {
        LOGGER.info(String.format(LOG_PUT, "/" + id, command.toString()));
        LecturerInfo updateLecturer = lecturerService.updateLecturer(id, command);
        LOGGER.info(String.format(HTTP_RESPONSE, "OK", ""));
        return new ResponseEntity<>(updateLecturer, HttpStatus.OK);
    }

    @DeleteMapping("/{presentationId}/lecturers/{lecturerId}")
    public ResponseEntity<Void> delete(@PathVariable("presentationId") Integer presentationId, @PathVariable("lecturerId") Integer lecturerId) {
        LOGGER.info(String.format(LOG_DELETE, "/" + presentationId + "/lecturers/" + lecturerId));
        lecturerService.delete(presentationId, lecturerId);
        LOGGER.info(String.format(HTTP_RESPONSE, "OK", ""));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
