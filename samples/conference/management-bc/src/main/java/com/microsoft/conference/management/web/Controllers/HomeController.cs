using System.Web.Mvc;

namespace ConferenceManagement.Web.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
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
