package com.microsoft.conference.management.controller;

import com.google.common.base.Strings;
import com.microsoft.conference.common.exception.InvalidOperationException;
import com.microsoft.conference.common.management.commands.AddSeatType;
import com.microsoft.conference.common.management.commands.CreateConference;
import com.microsoft.conference.common.management.commands.PublishConference;
import com.microsoft.conference.common.management.commands.RemoveSeatType;
import com.microsoft.conference.common.management.commands.UnpublishConference;
import com.microsoft.conference.common.management.commands.UpdateConference;
import com.microsoft.conference.common.management.commands.UpdateSeatType;
import com.microsoft.conference.management.domain.models.SeatType;
import com.microsoft.conference.management.readmodel.ConferenceDTO;
import com.microsoft.conference.management.readmodel.ConferenceQueryService;
import com.microsoft.conference.management.readmodel.OrderDTO;
import lombok.var;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.enodeframework.common.io.Task.await;

@RestController
public class ConferenceController {

    @Autowired
    private ICommandService commandService;

    @Autowired
    private ConferenceQueryService conferenceQueryService;

    @Autowired
    private ConferenceInfo conferenceInfo;


    /// <summary>
    /// We receive the slug value as a kind of cross-cutting value that 
    /// all methods need and use, so we catch and load the conference here, 
    /// so it's available for all. Each method doesn't need the slug parameter.
    /// </summary>
    protected void OnActionExecuting() {
        String slug = "";
        if (!Strings.isNullOrEmpty(slug)) {
            ConferenceDTO conferenceDTO = conferenceQueryService.findConference(slug);
        }

    }


    @PostMapping
    public ActionResult Locate(String email, String accessCode) {
        ConferenceDTO conference = conferenceQueryService.findConference(email, accessCode);
        if (conference == null) {
            String.format("Could not locate a conference with the provided email and access code.");
            // Preserve input so the user doesn't have to type email again.

        }
        // TODO: This is not very secure. Should use a better authorization infrastructure in a real production system.
        return RedirectToAction("Index");
    }

    public ActionResult Index() {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }
        return View(this.conferenceInfo);
    }


    @PostMapping
    public ActionResult Create(ConferenceInfo conference) {

        CreateConference command = DTOExtensions.INSTANCE.ToCreateConferenceCommand(conference);
        var result = await(executeCommandAsync(command));

        if (!isSuccess(result)) {
            return View(conference);
        }

        return RedirectToAction("Index");
    }

    public ActionResult Edit() {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }
        return View(this.conferenceInfo);
    }

    @PostMapping
    public ActionResult Edit(EditableConferenceInfo conference) {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }
        if (!ModelState.IsValid) return View(conference);
        UpdateConference command = DTOExtensions.INSTANCE.ToUpdateConferenceCommand(conference);
        var result = await(executeCommandAsync(command));
        if (!isSuccess(result)) {
            return View(conference);
        }
        return RedirectToAction("Index");
    }

    @PostMapping
    public ActionResult Publish() {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }
        PublishConference command = new PublishConference();
        command.setAggregateRootId(this.conferenceInfo.Id);
        var result = await(executeCommandAsync(command));

        if (!isSuccess(result)) {
            throw new InvalidOperationException(result.getResult());
        }

        return RedirectToAction("Index");
    }

    @PostMapping
    public ActionResult Unpublish() {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }

        UnpublishConference command = new UnpublishConference();
        command.setAggregateRootId(this.conferenceInfo.Id);
        var result = await(executeCommandAsync(command));

        if (!isSuccess(result)) {
            throw new InvalidOperationException(result.getResult());
        }

        return RedirectToAction("Index");
    }

    public ActionResult SeatGrid() {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }

        this.conferenceQueryService.findSeatTypes(this.conferenceInfo.Id);
        return View();
    }

    public ActionResult SeatRow(String id) {
        this.conferenceQueryService.findSeatType(id);
        return PartialView("SeatGrid", new SeatType());
    }

    public ActionResult CreateSeat() {
        return PartialView("EditSeat");
    }

    @PostMapping
    public ActionResult CreateSeat(SeatType seat) {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }

        if (!ModelState.IsValid) {
            return PartialView("EditSeat", seat);
        }

        AddSeatType command = DTOExtensions.INSTANCE.ToAddSeatTypeCommand(seat, conferenceInfo);
        var result = await(executeCommandAsync(command));

        if (!isSuccess(result)) {
            throw new InvalidOperationException(result.getResult());
        }

        return PartialView("SeatGrid", new SeatType[]{seat});
    }

    public ActionResult EditSeat(String id) {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }
        SeatType seatType = DTOExtensions.INSTANCE.ToViewModel(this.conferenceQueryService.findSeatType(id));
        return PartialView();
    }

    @PostMapping
    public ActionResult EditSeat(SeatType seat) {
        if (this.conferenceInfo == null) {
            return HttpNotFound();
        }

        if (!ModelState.IsValid) {
            return PartialView(seat);
        }
        UpdateSeatType command = DTOExtensions.INSTANCE.ToUpdateSeatTypeCommand(seat, conferenceInfo);

        var result = await(executeCommandAsync(command));

        if (!isSuccess(result)) {
            throw new InvalidOperationException(result.getResult());
        }

        return PartialView("SeatGrid", new SeatType[]{seat});
    }

    private Boolean isSuccess(CommandResult result) {
        return CommandStatus.Success.equals(result.getStatus());
    }

    @PostMapping
    public void DeleteSeat(String id) {
        RemoveSeatType command = new RemoveSeatType(this.conferenceInfo.getId());
        command.setSeatTypeId(id);
        var result = await(executeCommandAsync(command));
    }


    public ViewResult Orders() {
        List<OrderDTO> orders = conferenceQueryService.findOrders(this.conferenceInfo.getId());
        return new ViewResult();
    }


    private CompletableFuture<CommandResult> executeCommandAsync(ICommand command) {
        return commandService.executeAsync(command, CommandReturnType.EventHandled);
    }

    private ActionResult HttpNotFound() {
        return new ActionResult();
    }

    private ActionResult PartialView(Object... objects) {
        return new ActionResult();
    }

    private ActionResult RedirectToAction(Object... objects) {
        return new ActionResult();

    }

    private ActionResult View(Object... objects) {
        return new ActionResult();
    }

    static class ModelState {
        public static Boolean IsValid;
    }
}