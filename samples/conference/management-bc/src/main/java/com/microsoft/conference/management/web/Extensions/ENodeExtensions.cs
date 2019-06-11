using System.Collections.Generic;
using System.Net;
using System.Reflection;
using Autofac.Integration.Mvc;
using Conference.Common;
using ECommon.Autofac;
using ECommon.Components;
using ECommon.Socketing;
using ENode.Commanding;
using ENode.Configurations;
using ENode.EQueue;
using EQueue.Clients.Producers;
using EQueue.Configurations;

namespace ConferenceManagement.Web.Extensions
{
    public static class ENodeExtensions
    {
        private static CommandService _commandService;

        public static ENodeConfiguration BuildContainer(this ENodeConfiguration enodeConfiguration)
        {
            enodeConfiguration.GetCommonConfiguration().BuildContainer();
            return enodeConfiguration;
        }
        public static ENodeConfiguration RegisterControllers(this ENodeConfiguration enodeConfiguration)
        {
            var containerBuilder = (ObjectContainer.Current as AutofacObjectContainer).ContainerBuilder;
            containerBuilder.RegisterControllers(Assembly.GetExecutingAssembly());
            return enodeConfiguration;
        }
        public static ENodeConfiguration UseEQueue(this ENodeConfiguration enodeConfiguration)
        {
            var configuration = enodeConfiguration.GetCommonConfiguration();
            configuration.RegisterEQueueComponents();

            _commandService = new CommandService();
            configuration.SetDefault<ICommandService, CommandService>(_commandService);

            return enodeConfiguration;
        }
        public static ENodeConfiguration StartEQueue(this ENodeConfiguration enodeConfiguration)
        {
            _commandService.InitializeEQueue(new CommandResultProcessor().Initialize(new IPEndPoint(SocketUtils.GetLocalIPV4(), 9002)), new ProducerSetting
            {
                NameServerList = new List<IPEndPoint> { new IPEndPoint(SocketUtils.GetLocalIPV4(), ConfigSettings.NameServerPort) }
            });
            _commandService.Start();
            return enodeConfiguration;
        }
    }
}