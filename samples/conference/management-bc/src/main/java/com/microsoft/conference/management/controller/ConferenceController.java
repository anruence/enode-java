package com.microsoft.conference.management.controller;

import com.microsoft.conference.common.exception.InvalidOperationException;
import com.microsoft.conference.common.management.commands.AddSeatType;
import com.microsoft.conference.common.management.commands.CreateConference;
import com.microsoft.conference.common.management.commands.PublishConference;
import com.microsoft.conference.common.management.commands.RemoveSeatType;
import com.microsoft.conference.common.management.commands.UnpublishConference;
import com.microsoft.conference.common.management.commands.UpdateConference;
import com.microsoft.conference.common.management.commands.UpdateSeatType;
import com.microsoft.conference.management.domain.model.SeatType;
import com.microsoft.conference.management.readmodel.ConferenceDTO;
import com.microsoft.conference.management.readmodel.ConferenceQueryService;
import com.microsoft.conference.management.readmodel.SeatTypeDTO;
import io.swagger.annotations.Api;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.enodeframework.common.io.Task.await;

@RestController
@RequestMapping("/api/v1/")
@Api(tags = "conference manager")
public class ConferenceController {

    @Autowired
    private ICommandService commandService;

    @Autowired
    private ConferenceQueryService conferenceQueryService;

    @GetMapping("locate")
    public ActionResult locate(@RequestParam String email, @RequestParam String accessCode) {
        ConferenceDTO conference = conferenceQueryService.findConference(email, accessCode);
        if (conference == null) {
            String errMsg = String.format("Could not locate a conference with the provided email and access code. %s : %s", email, accessCode);
            // Preserve input so the user doesn't have to type email again.
            return ActionResult.error("500", errMsg);
        }
        // TODO: This is not very secure. Should use a better authorization infrastructure in a real production system.
        return view(conference);
    }

    @PostMapping("conference")
    public ActionResult create(ConferenceInfo conference) {
        CreateConference command = DTOExtensions.INSTANCE.ToCreateConferenceCommand(conference);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            return view(conference);
        }
        return view(result);
    }

    @PutMapping("conference")
    public ActionResult edit(@RequestBody EditableConferenceInfo conference) {
        UpdateConference command = DTOExtensions.INSTANCE.ToUpdateConferenceCommand(conference);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            return view(conference);
        }
        return view(result);
    }

    @PostMapping("publish")
    public ActionResult publish(@RequestParam String id) {
        PublishConference command = new PublishConference();
        command.setAggregateRootId(id);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            throw new InvalidOperationException(result.getResult());
        }
        return view(result);
    }

    @PostMapping("unpublish")
    public ActionResult unpublish(@RequestParam String id) {
        UnpublishConference command = new UnpublishConference();
        command.setAggregateRootId(id);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            throw new InvalidOperationException(result.getResult());
        }
        return view(result);
    }

    @GetMapping("seattypes")
    public ActionResult seatGrid() {
        ConferenceInfo conferenceInfo = new ConferenceInfo();
        List<SeatTypeDTO> seatTypes = this.conferenceQueryService.findSeatTypes(conferenceInfo.getId());
        return view(seatTypes);
    }


    @GetMapping("seattypes/{id}")
    public ActionResult seatRow(@PathVariable String id) {
        SeatTypeDTO seatTypeDTO = this.conferenceQueryService.findSeatType(id);
        return view(seatTypeDTO);
    }

    @PostMapping("seat")
    public ActionResult createSeat(SeatType seat) {
        ConferenceInfo conferenceInfo = new ConferenceInfo();
        AddSeatType command = DTOExtensions.INSTANCE.ToAddSeatTypeCommand(seat, conferenceInfo);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            throw new InvalidOperationException(result.getResult());
        }
        return view(result);
    }


    @PutMapping("seat")
    public ActionResult editSeat(SeatType seat) {
        ConferenceInfo conferenceInfo = new ConferenceInfo();
        UpdateSeatType command = DTOExtensions.INSTANCE.ToUpdateSeatTypeCommand(seat, conferenceInfo);
        CommandResult result = await(executeCommandAsync(command));
        if (!isSuceess(result)) {
            throw new InvalidOperationException(result.getResult());
        }
        return view(seat);
    }

    @DeleteMapping("seat")
    public ActionResult deleteSeat(String id) {
        RemoveSeatType command = new RemoveSeatType(id);
        command.setSeatTypeId(id);
        CommandResult result = await(executeCommandAsync(command));
        return view(result);
    }

    private boolean isSuceess(CommandResult result) {
        return CommandStatus.Success.equals(result.getStatus());
    }

    private CompletableFuture<CommandResult> executeCommandAsync(ICommand command) {
        return commandService.executeAsync(command, CommandReturnType.EventHandled);
    }

    private ActionResult view(Object... objects) {
        return new ActionResult<>(objects);
    }
}