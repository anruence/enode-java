using System.Reflection;
using System.Web;
using System.Web.Mvc;
using System.Web.Routing;
using Autofac.Integration.Mvc;
using Conference.Common;
using ConferenceManagement.Web.Extensions;
using ECommon.Autofac;
using ECommon.Components;
using ECommon.Configurations;
using ECommon.Logging;
using ENode.Configurations;

namespace ConferenceManagement.Web
{
    public class MvcApplication : HttpApplication
    {
        private ILogger _logger;
        private Configuration _ecommonConfiguration;
        private ENodeConfiguration _enodeConfiguration;

        protected void Application_Start()
        {
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
                Assembly.Load("ConferenceManagement.Commands"),
                Assembly.Load("ConferenceManagement.ReadModel"),
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
                name: "Conference.Locate",
                url: "locate",
                defaults: new { controller = "Conference", action = "Locate" }
            );

            routes.MapRoute(
                name: "Conference.Create",
                url: "create",
                defaults: new { controller = "Conference", action = "Create" }
            );

            routes.MapRoute(
                name: "Conference",
                url: "{slug}/{accessCode}/{action}",
                defaults: new { controller = "Conference", action = "Index" }
            );

            routes.MapRoute(
                name: "Home",
                url: "",
                defaults: new { controller = "Home", action = "Index" }
            );

            routes.MapRoute(
                name: "UnAuthorized",
                url: "unauthorized",
                defaults: new { controller = "Home", action = "UnAuthorized" }
            );

            routes.MapRoute(
                name: "Forbidden",
                url: "forbidden",
                defaults: new { controller = "Home", action = "Forbidden" }
            );

            routes.MapRoute(
                name: "NotFound",
                url: "notfound",
                defaults: new { controller = "Home", action = "NotFound" }
            );
        }
    }
}
