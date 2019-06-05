using System.Reflection;
using System.Web;
using System.Web.Mvc;
using System.Web.Routing;
using Autofac.Integration.Mvc;
using Conference.Common;
using ECommon.Autofac;
using ECommon.Components;
using ECommon.Configurations;
using ECommon.Logging;
using ENode.Configurations;
using Registration.Web.Extensions;

namespace Registration.Web
{
    public partial class MvcApplication : HttpApplication
    {
        private ILogger _logger;
        private Configuration _ecommonConfiguration;
        private ENodeConfiguration _enodeConfiguration;

        protected void Application_Start()
        {
            ConfigSettings.Initialize();

            AreaRegistration.RegisterAllAreas();
            GlobalFilters.Filters.Add(new HandleExceptionAttribute());
            RegisterRoutes(RouteTable.Routes);
            InitializeECommon();
            InitializeENode();
        }

        private void InitializeECommon()
        {
            _ecommonConfiguration = Configuration
                .Create()
                .UseAutofac()
                .RegisterCommonComponents()
                .UseLog4Net()
                .UseJsonNet()
                .RegisterUnhandledExceptionHandler();
        }
        private void InitializeENode()
        {
            ConfigSettings.Initialize();

            var assemblies = new[]
            {
                Assembly.Load("Registration.Commands"),
                Assembly.Load("Registration.Domain"),
                Assembly.Load("Registration.ReadModel"),
                Assembly.Load("Payments.Commands"),
                Assembly.Load("Payments.ReadModel"),
                Assembly.GetExecutingAssembly()
            };

            _enodeConfiguration = _ecommonConfiguration
                .CreateENode()
                .RegisterENodeComponents()
                .RegisterBusinessComponents(assemblies)
                .UseEQueue()
                .RegisterControllers()
                .BuildContainer()
                .InitializeBusinessAssemblies(assemblies)
                .StartEQueue();

            DependencyResolver.SetResolver(new AutofacDependencyResolver((ObjectContainer.Current as AutofacObjectContainer).Container));

            _logger = ObjectContainer.Resolve<ILoggerFactory>().Create(GetType().FullName);
            _logger.Info("ENode initialized.");
        }

        private void RegisterRoutes(RouteCollection routes)
        {
            routes.IgnoreRoute("{resource}.axd/{*pathInfo}");
            routes.IgnoreRoute("{*favicon}", new { favicon = @"(.*/)?favicon.ico(/.*)?" });

            routes.MapRoute(
                "Home",
                string.Empty,
                new { controller = "Default", action = "Index" });

            routes.MapRoute(
                "UnAuthorized",
                "unauthorized",
                new { controller = "Default", action = "UnAuthorized" }
            );

            routes.MapRoute(
                "Forbidden",
                "forbidden",
                new { controller = "Default", action = "Forbidden" }
            );

            routes.MapRoute(
                "NotFound",
                "notfound",
                new { controller = "Default", action = "NotFound" }
            );

            // Registration routes

            routes.MapRoute(
                "ViewConference",
                "{conferenceCode}/",
                new { controller = "Conference", action = "Display" });

            routes.MapRoute(
                "RegisterStart",
                "{conferenceCode}/register",
                new { controller = "Registration", action = "StartRegistration" });

            routes.MapRoute(
                "RegisterRegistrantDetails",
                "{conferenceCode}/registrant",
                new { controller = "Registration", action = "SpecifyRegistrantAndPaymentDetails" });

            routes.MapRoute(
                "StartPayment",
                "{conferenceCode}/pay",
                new { controller = "Registration", action = "StartPayment" });

            routes.MapRoute(
                "ExpiredOrder",
                "{conferenceCode}/expired",
                new { controller = "Registration", action = "ShowExpiredOrder" });

            routes.MapRoute(
                "RejectedOrder",
                "{conferenceCode}/rejected",
                new { controller = "Registration", action = "ShowRejectedOrder" });

            routes.MapRoute(
                "RegisterConfirmation",
                "{conferenceCode}/confirmation",
                new { controller = "Registration", action = "ThankYou" });

            routes.MapRoute(
                "OrderFind",
                "{conferenceCode}/order/find",
                new { controller = "Order", action = "Find" });

            routes.MapRoute(
                "AssignSeats",
                "{conferenceCode}/order/{orderId}/seats",
                new { controller = "Order", action = "AssignSeats" });

            routes.MapRoute(
                "OrderDisplay",
                "{conferenceCode}/order/{orderId}",
                new { controller = "Order", action = "Display" });

            routes.MapRoute(
                "InitiateThirdPartyPayment",
                "{conferenceCode}/third-party-payment",
                new { controller = "Payment", action = "ThirdPartyProcessorPayment" });

            routes.MapRoute(
                "PaymentAccept",
                "{conferenceCode}/third-party-payment-accept",
                new { controller = "Payment", action = "ThirdPartyProcessorPaymentAccepted" });

            routes.MapRoute(
                "PaymentReject",
                "{conferenceCode}/third-party-payment-reject",
                new { controller = "Payment", action = "ThirdPartyProcessorPaymentRejected" });
        }
    }
}
