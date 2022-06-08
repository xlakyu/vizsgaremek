package hu.progmasters.conference.service;

import hu.progmasters.conference.domain.Participant;
import hu.progmasters.conference.domain.Presentation;
import hu.progmasters.conference.dto.*;
import hu.progmasters.conference.exceptionhandler.*;
import hu.progmasters.conference.repository.ParticipantRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ParticipantService {

    private ParticipantRepository participantRepository;
    private ModelMapper modelMapper;
    private PresentationService presentationService;

    public ParticipantService(ParticipantRepository participantRepository, ModelMapper modelMapper, PresentationService presentationService) {
        this.participantRepository = participantRepository;
        this.modelMapper = modelMapper;

        this.presentationService = presentationService;
    }

    public ParticipantInfo saveParticipant(ParticipantCreateCommand command, Integer presentationId) {
        Participant toSave = modelMapper.map(command, Participant.class);
        Optional<Presentation> presentationOptional = presentationService.findPresentationById(presentationId);
        if (presentationOptional.isEmpty()) {
            throw new PresentationNotFoundException(presentationId);
        }
        Presentation presentation = presentationOptional.get();
        if (presentation.getStartTime().isBefore(command.getRegistration())) {
            throw new RegistrationClosedException(presentationId);
        }
        toSave.setPresentation(presentation);
        presentation.getParticipants().add(toSave);
        Participant saved;
        try {
            saved = participantRepository.save(toSave);
            participantRepository.flush();
        } catch (Exception e) {
            throw  new EmailNotValidException(toSave.getEmail());
        }
        ParticipantInfo savedInfo = modelMapper.map(saved, ParticipantInfo.class);
        savedInfo.setPresentation(modelMapper.map(presentation, PresentationInfo.class));
        return savedInfo;
    }

    public void deleteParticipant(Integer presentationId, Integer participantId) {
        Optional<Presentation> presentation = presentationService.findPresentationById(presentationId);
        if (presentation.isEmpty()) {
            throw new PresentationNotFoundException(presentationId);
        }

        Optional<Participant> participant = participantRepository.findParticipantById(participantId);
        if (participant.isEmpty() || !participant.get().getPresentation().equals(presentation.get())) {
            throw new ParticipantNotFoundException(participantId);
        }
        Participant participantFound = participant.get();
        Presentation presentationFound = presentation.get();

        presentationFound.getParticipants().remove(participantFound);
        participantRepository.deleteParticipant(participantFound);
    }

    public ParticipantByIdInfo findById(Integer id) {
        Optional<Participant> participant = participantRepository.findParticipantById(id);
        if (participant.isPresent()) {
            return modelMapper.map(participant.get(), ParticipantByIdInfo.class);
        } else {
            throw new ParticipantNotFoundException(id);
        }
    }

    public List<ParticipantListItem> findAll() {
        List<Participant> participants = participantRepository.findAll();
        return participants.stream()
                .map(participant -> modelMapper.map(participant, ParticipantListItem.class))
                .collect(Collectors.toList());
    }

    public ParticipantInfo update(Integer id, ParticipantUpdateCommand command) {
        Optional<Participant> participantById = participantRepository.findParticipantById(id);
        if (participantById.isEmpty()) {
            throw new ParticipantNotFoundException(id);
        }
        Participant participant = participantById.get();
        Optional<Presentation> presentation = presentationService.findPresentationById(command.getPresentationId());
        if (presentation.isEmpty()) {
            throw new PresentationNotFoundException(command.getPresentationId());
        }
        participant.setPresentation(presentation.get());
        participantRepository.update(participant);
        return modelMapper.map(participant, ParticipantInfo.class);

//        Participant toUpdate = modelMapper.map(command, Participant.class);
//        toUpdate.setId(id);
//        Participant updated = participantRepository.update(toUpdate);
//        Optional<Presentation> presentationOptional = presentationService.findPresentationById(command.getPresentationId());
//        if (presentationOptional.isEmpty()) {
//            throw new PresentationNotFoundException();
//        }
//        Presentation presentation = presentationOptional.get();
//        updated.setPresentation(presentation);
//        return modelMapper.map(updated, ParticipantInfo.class);
    }

    public List<ParticipantInfo> findByName(String name) {
        List<Participant> participants = participantRepository.findByName(name);
        if (!participants.isEmpty()) {
            return participants.stream()
                    .map(participant -> modelMapper.map(participant, ParticipantInfo.class))
                    .collect(Collectors.toList());
        } else {
            throw new ParticipantsByNameNotFoundException();
        }
    }
}
