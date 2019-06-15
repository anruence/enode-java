using System;
using System.Data;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using System.Web.Mvc;
using Conference.Common;
using ConferenceManagement.Commands;
using ConferenceManagement.ReadModel;
using ConferenceManagement.Web.Extensions;
using ConferenceManagement.Web.Models;
using ECommon.IO;
using ECommon.Logging;
using ENode.Commanding;

namespace ConferenceManagement.Web.Controllers
{
    public class ConferenceController : Controller
    {
        private ICommandService _commandService;
        private ConferenceQueryService _conferenceQueryService;
        private ConferenceInfo _conference;

        public ConferenceController(ICommandService commandService, ConferenceQueryService conferenceQueryService)
        {
            _commandService = commandService;
            _conferenceQueryService = conferenceQueryService;
        }

        /// <summary>
        /// We receive the slug value as a kind of cross-cutting value that 
        /// all methods need and use, so we catch and load the conference here, 
        /// so it's available for all. Each method doesn't need the slug parameter.
        /// </summary>
        protected override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            var slug = (string)this.ControllerContext.RequestContext.RouteData.Values["slug"];
            if (!string.IsNullOrEmpty(slug))
            {
                this.ViewBag.Slug = slug;
                this._conference = _conferenceQueryService.FindConference(slug).ToViewModel();

                if (this._conference != null)
                {
                    // check access
                    var accessCode = (string)this.ControllerContext.RequestContext.RouteData.Values["accessCode"];

                    if (accessCode == null || !string.Equals(accessCode, this._conference.AccessCode, StringComparison.Ordinal))
                    {
                        filterContext.Result = new HttpUnauthorizedResult("Invalid access code.");
                    }
                    else
                    {
                        this.ViewBag.OwnerName = this._conference.OwnerName;
                        this.ViewBag.WasEverPublished = this._conference.WasEverPublished;
                    }
                }
            }

            base.OnActionExecuting(filterContext);
        }

        #region Conference Actions

        public ActionResult Locate()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Locate(string email, string accessCode)
        {
            var conference = _conferenceQueryService.FindConference(email, accessCode);
            if (conference == null)
            {
                ModelState.AddModelError(string.Empty, "Could not locate a conference with the provided email and access code.");
                // Preserve input so the user doesn't have to type email again.
                ViewBag.Email = email;

                return View();
            }

            // TODO: This is not very secure. Should use a better authorization infrastructure in a real production system.
            return RedirectToAction("Index", new { slug = conference.Slug, accessCode });
        }

        public ActionResult Index()
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }
            return View(this._conference);
        }

        public ActionResult Create()
        {
            return View();
        }

        [HttpPost]
        public async Task<ActionResult> Create([Bind(Exclude = "Id,AccessCode,Seats,WasEverPublished")] ConferenceInfo conference)
        {
            if (!ModelState.IsValid) return View(conference);

            var command = conference.ToCreateConferenceCommand();
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                ModelState.AddModelError("Slug", result.GetErrorMessage());
                return View(conference);
            }

            return RedirectToAction("Index", new { slug = command.Slug, accessCode = command.AccessCode });
        }

        public ActionResult Edit()
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }
            return View(this._conference);
        }

        [HttpPost]
        public async Task<ActionResult> Edit(EditableConferenceInfo conference)
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            if (!ModelState.IsValid) return View(conference);

            var command = conference.ToUpdateConferenceCommand(_conference);
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                ModelState.AddModelError("Slug", result.GetErrorMessage());
                return View(conference);
            }

            return RedirectToAction("Index", new { slug = _conference.Slug, accessCode = _conference.AccessCode });
        }

        [HttpPost]
        public async Task<ActionResult> Publish()
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            var command = new PublishConference { AggregateRootId = this._conference.Id };
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                throw new InvalidOperationException(result.GetErrorMessage());
            }

            return RedirectToAction("Index", new { slug = this._conference.Slug, accessCode = this._conference.AccessCode });
        }

        [HttpPost]
        public async Task<ActionResult> Unpublish()
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            var command = new UnpublishConference { AggregateRootId = this._conference.Id };
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                throw new InvalidOperationException(result.GetErrorMessage());
            }

            return RedirectToAction("Index", new { slug = this._conference.Slug, accessCode = this._conference.AccessCode });
        }

        #endregion

        #region Seat Types Actions

        public ViewResult Seats()
        {
            return View();
        }

        public ActionResult SeatGrid()
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            return PartialView(this._conferenceQueryService.FindSeatTypes(this._conference.Id).Select(x => x.ToViewModel()));
        }

        public ActionResult SeatRow(Guid id)
        {
            return PartialView("SeatGrid", new SeatType[] { this._conferenceQueryService.FindSeatType(id).ToViewModel() });
        }

        public ActionResult CreateSeat()
        {
            return PartialView("EditSeat");
        }

        [HttpPost]
        public async Task<ActionResult> CreateSeat(SeatType seat)
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            if (!ModelState.IsValid)
            {
                return PartialView("EditSeat", seat);
            }

            var command = seat.ToAddSeatTypeCommand(this._conference);
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                throw new InvalidOperationException(result.GetErrorMessage());
            }

            return PartialView("SeatGrid", new SeatType[] { seat });
        }

        public ActionResult EditSeat(Guid id)
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            return PartialView(this._conferenceQueryService.FindSeatType(id).ToViewModel());
        }

        [HttpPost]
        public async Task<ActionResult> EditSeat(SeatType seat)
        {
            if (this._conference == null)
            {
                return HttpNotFound();
            }

            if (!ModelState.IsValid)
            {
                return PartialView(seat);
            }

            var command = seat.ToUpdateSeatTypeCommand(this._conference);
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                throw new InvalidOperationException(result.GetErrorMessage());
            }

            return PartialView("SeatGrid", new SeatType[] { seat });
        }

        [HttpPost]
        public async Task DeleteSeat(Guid id)
        {
            var command = new RemoveSeatType(this._conference.Id) { SeatTypeId = id };
            var result = await ExecuteCommandAsync(command);

            if (!result.IsSuccess())
            {
                throw new InvalidOperationException(result.GetErrorMessage());
            }
        }

        #endregion

        #region Orders

        public ViewResult Orders()
        {
            var orders = _conferenceQueryService.FindOrders(this._conference.Id);
            return View(orders);
        }

        #endregion

        private Task<AsyncTaskResult<CommandResult>> ExecuteCommandAsync(ICommand command, int millisecondsDelay = 5000)
        {
            return _commandService.ExecuteAsync(command, CommandReturnType.EventHandled).TimeoutAfter(millisecondsDelay);
        }
    }
}