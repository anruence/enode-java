using System.Web.Mvc;
using Registration.ReadModel;

namespace Registration.Web.Controllers
{
    public class DefaultController : Controller
    {
        private readonly IConferenceQueryService _conferenceQueryService;

        public DefaultController(IConferenceQueryService conferenceQueryService)
        {
            _conferenceQueryService = conferenceQueryService;
        }

        public ActionResult Index()
        {
            return View(_conferenceQueryService.GetPublishedConferences());
        }

        public ActionResult UnAuthorized()
        {
            return View();
        }
        public ActionResult Forbidden()
        {
            return View();
        }
        public ActionResult NotFound()
        {
            return View();
        }
    }
}
