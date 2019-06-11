using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel.DataAnnotations;
using Conference.Common;
using ConferenceManagement.Web.Properties;

namespace ConferenceManagement.Web.Models
{
    /// <summary>
    /// Editable information about a conference.
    /// </summary>
    public class EditableConferenceInfo
    {
        [Required(AllowEmptyStrings = false)]
        public string Name { get; set; }

        [Required(AllowEmptyStrings = false)]
        public string Description { get; set; }

        [Required(AllowEmptyStrings = false)]
        public string Location { get; set; }

        public string Tagline { get; set; }
        public string TwitterSearch { get; set; }

        [DisplayFormat(ApplyFormatInEditMode = true, DataFormatString = "{0:yyyy/MM/dd}")]
        [Display(Name = "Start")]
        public DateTime StartDate { get; set; }

        [DisplayFormat(ApplyFormatInEditMode = true, DataFormatString = "{0:yyyy/MM/dd}")]
        [Display(Name = "End")]
        public DateTime EndDate { get; set; }

        [Display(Name = "Is Published?")]
        public bool IsPublished { get; set; }
    }
    /// <summary>
    /// The full conference information.
    /// </summary>
    /// <remarks>
    /// This class inherits from <see cref="EditableConferenceInfo"/> 
    /// and exposes more information that is not user-editable once 
    /// it has been generated or provided.
    /// </remarks>
    public class ConferenceInfo : EditableConferenceInfo
    {
        public ConferenceInfo()
        {
            this.Id = GuidUtil.NewSequentialId();
            this.Seats = new ObservableCollection<SeatType>();
            this.AccessCode = StringGenerator.Generate(6);
        }

        public Guid Id { get; set; }

        [StringLength(6, MinimumLength = 6)]
        public string AccessCode { get; set; }

        [Display(Name = "Owner")]
        [Required(AllowEmptyStrings = false)]
        public string OwnerName { get; set; }

        [Display(Name = "Email")]
        [Required(AllowEmptyStrings = false)]
        [RegularExpression(@"[\w-]+(\.?[\w-])*\@[\w-]+(\.[\w-]+)+", ErrorMessageResourceType = typeof(Resources), ErrorMessageResourceName = "InvalidEmail")]
        public string OwnerEmail { get; set; }

        [Required(AllowEmptyStrings = false)]
        [RegularExpression(@"^\w+$", ErrorMessageResourceType = typeof(Resources), ErrorMessageResourceName = "InvalidSlug")]
        public string Slug { get; set; }

        public bool WasEverPublished { get; set; }

        public virtual ICollection<SeatType> Seats { get; set; }
    }
}
