using System;
using System.ComponentModel.DataAnnotations;

namespace Registration.Web.Models
{
    public class RegistrantDetails
    {
        public Guid OrderId { get; set; }

        [Required(AllowEmptyStrings = false)]
        public string FirstName { get; set; }

        [Required(AllowEmptyStrings = false)]
        public string LastName { get; set; }

        [Required(AllowEmptyStrings = false)]
        [RegularExpression(@"[\w-]+(\.?[\w-])*\@[\w-]+(\.[\w-]+)+", ErrorMessageResourceType = typeof(Resources), ErrorMessageResourceName = "InvalidEmail")]
        public string Email { get; set; }
    }
}
