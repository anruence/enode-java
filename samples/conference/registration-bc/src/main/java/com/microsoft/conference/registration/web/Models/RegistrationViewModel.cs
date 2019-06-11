using Registration.ReadModel;

namespace Registration.Web.Models
{
    public class RegistrationViewModel
    {
        public Order Order { get; set; }
        public RegistrantDetails RegistrantDetails { get; set; }
    }
}