using System.Web.Mvc;
using Registration.ReadModel;

namespace Registration.Web.Controllers
{
    public class ConferenceController : Controller
    {
        private readonly IConferenceQueryService _conferenceQueryService;

        public ConferenceController(IConferenceQueryService conferenceQueryService)
        {
            _conferenceQueryService = conferenceQueryService;
        }

        public ActionResult Display(string conferenceCode)
        {
            return View(_conferenceQueryService.GetConferenceDetails(conferenceCode));
        }
    }
}
