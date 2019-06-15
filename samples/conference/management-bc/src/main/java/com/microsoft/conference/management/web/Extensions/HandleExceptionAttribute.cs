using System;
using System.Web.Mvc;
using ECommon.Components;
using ECommon.Logging;

namespace ConferenceManagement.Web.Extensions
{
    [AttributeUsage(AttributeTargets.Class | AttributeTargets.Method, AllowMultiple = false, Inherited = true)]
    public class HandleExceptionAttribute : HandleErrorAttribute
    {
        private static ILogger _logger;

        public override void OnException(ExceptionContext filterContext)
        {
            TryLogException(filterContext.Exception);
            if (filterContext.Exception != null && filterContext.Exception is TimeoutException)
            {
                View = "TimeoutError";
            }
            base.OnException(filterContext);
        }

        private static void TryLogException(Exception ex)
        {
            if (_logger == null)
            {
                try
                {
                    _logger = ObjectContainer.Resolve<ILoggerFactory>().Create(typeof(HandleExceptionAttribute).FullName);
                }
                catch { }
            }
            if (_logger == null) return;

            _logger.Error(ex);
        }
    }
}