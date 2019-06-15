using ConferenceManagement.Domain.Models;

namespace ConferenceManagement.Domain.Repositories
{
    public interface IConferenceSlugIndexRepository
    {
        void Add(ConferenceSlugIndex index);
        ConferenceSlugIndex FindSlugIndex(string slug);
    }
}
