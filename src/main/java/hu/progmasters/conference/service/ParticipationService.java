package hu.progmasters.conference.service;

import hu.progmasters.conference.domain.Participant;
import hu.progmasters.conference.domain.Participation;
import hu.progmasters.conference.domain.Presentation;
import hu.progmasters.conference.dto.ParticipationInfo;
import hu.progmasters.conference.dto.command.ParticipationCreateCommand;
import hu.progmasters.conference.dto.command.ParticipationUpdateCommand;
import hu.progmasters.conference.exceptionhandler.*;
import hu.progmasters.conference.repository.ParticipationRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ParticipationService {

    private final ModelMapper modelMapper;
    private final ParticipationRepository participationRepository;
    private final PresentationService presentationService;
    private final ParticipantService participantService;


    public ParticipationInfo registrate(ParticipationCreateCommand command) {
        Presentation presentation = presentationService.findPresentationById(command.getPresentationId())
                .orElseThrow(()-> new PresentationNotFoundException(command.getPresentationId()));
        Participant participant = participantService.findParticipantById(command.getParticipantId())
                .orElseThrow(()-> new ParticipantNotFoundException(command.getParticipantId()));;

        if(alreadyRegistrated(participant, presentation)) {
            throw new AlreadyRegisteredException(presentation.getId(), participant.getId());
        }
        Participation participationToSave = new Participation();
        participationToSave.setPresentation(presentation);
        participationToSave.setParticipant(participant);
        participationToSave.setRegistration(command.getRegistration());

        if(presentation.getStartTime().isBefore(command.getRegistration())) {
            throw new RegistrationClosedException(presentation.getId());
        }
        Participation participationSaved = participationRepository.save(participationToSave);

        return modelMapper.map(participationSaved, ParticipationInfo.class);
    }

    private boolean alreadyRegistrated(Participant participant, Presentation presentation) {
        return participationRepository.hasRegistration(participant, presentation);
    }

    public List<ParticipationInfo> findAll() {
        List<Participation> participations = participationRepository.findAll();
        return participations.stream()
                .map(participation -> modelMapper.map(participation, ParticipationInfo.class))
                .collect(Collectors.toList());
    }

    public ParticipationInfo findById(Integer id) {
        Participation participationById = participationRepository.findById(id)
                .orElseThrow(()-> new ParticipationNotFoundException(id));
        return modelMapper.map(participationById, ParticipationInfo.class);
    }

    public void delete(Integer id) {
        Participation participation = participationRepository.findById(id).orElseThrow(()->
                new ParticipationNotFoundException(id));
        participationRepository.delete(participation);
    }

    public ParticipationInfo updateParticipantsPresentation(Integer participationId, ParticipationUpdateCommand command) {
        Participation participation = participationRepository.findById(participationId).orElseThrow(()
                -> new ParticipationNotFoundException(participationId));
        Presentation presentation = presentationService.findPresentationById(command.getPresentationId()).orElseThrow(()
        -> new PresentationNotFoundException(command.getPresentationId()));
        participation.setPresentation(presentation);
        participationRepository.update(participation);
        return modelMapper.map(participation, ParticipationInfo.class);
    }

    public Participation findParticipationById(Integer id) {
        return participationRepository.findById(id).orElseThrow(()-> new ParticipationNotFoundException(id));
    }


}
