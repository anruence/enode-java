using Conference.Common;
using ECommon.Components;
using ENode.Commanding;
using ENode.EQueue;

namespace ConferenceManagement.Web.TopicProviders
{
    [Component]
    public class CommandTopicProvider : AbstractTopicProvider<ICommand>
    {
        public override string GetTopic(ICommand command)
        {
            return Topics.ConferenceCommandTopic;
        }
    }
}
