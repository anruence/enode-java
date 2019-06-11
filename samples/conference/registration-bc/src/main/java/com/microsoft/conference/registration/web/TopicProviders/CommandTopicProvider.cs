using Conference.Common;
using ECommon.Components;
using ENode.Commanding;
using ENode.EQueue;
using Payments.Commands;

namespace Registration.Web.TopicProviders
{
    [Component]
    public class CommandTopicProvider : AbstractTopicProvider<ICommand>
    {
        public override string GetTopic(ICommand command)
        {
            if (command is CreatePayment || command is CompletePayment || command is CancelPayment)
            {
                return Topics.PaymentCommandTopic;
            }
            return Topics.RegistrationCommandTopic;
        }
    }
}
