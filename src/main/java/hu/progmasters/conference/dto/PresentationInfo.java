package hu.progmasters.conference.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PresentationInfo {
    private Integer id;
    private LecturerInfo lecturer;
    private String title;
    private LocalDateTime startTime;
}
