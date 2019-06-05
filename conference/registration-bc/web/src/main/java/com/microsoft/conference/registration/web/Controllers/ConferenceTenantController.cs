using System.Web.Mvc;
using Registration.ReadModel;

namespace Registration.Web.Controllers
{
    public abstract class ConferenceTenantController : Controller
    {
        private ConferenceAlias conferenceAlias;
        private string conferenceCode;

        public ConferenceTenantController(IConferenceQueryService conferenceQueryService, IOrderQueryService orderQueryService)
        {
            ConferenceQueryService = conferenceQueryService;
            OrderQueryService = orderQueryService;
        }

        public IConferenceQueryService ConferenceQueryService { get; private set; }
        public IOrderQueryService OrderQueryService { get; private set; }

        public string ConferenceCode
        {
            get
            {
                return this.conferenceCode ??
                    (this.conferenceCode = (string)ControllerContext.RouteData.Values["conferenceCode"]);
            }
            internal set { this.conferenceCode = value; }
        }

        public ConferenceAlias ConferenceAlias
        {
            get
            {
                return this.conferenceAlias ??
                    (this.conferenceAlias = this.ConferenceQueryService.GetConferenceAlias(this.ConferenceCode));
            }
            internal set { this.conferenceAlias = value; }
        }

        protected override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            base.OnActionExecuting(filterContext);

            if (!string.IsNullOrEmpty(this.ConferenceCode) && this.ConferenceAlias == null)
            {
                filterContext.Result = new HttpNotFoundResult("Invalid conference code.");
            }
        }

        protected override void OnResultExecuting(ResultExecutingContext filterContext)
        {
            base.OnResultExecuting(filterContext);

            if (filterContext.Result is ViewResultBase)
            {
                this.ViewBag.Conference = this.ConferenceAlias;
            }
        }
    }
}