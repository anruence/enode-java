using System;
using System.Data;
using ConferenceManagement.Domain.Models;
using ConferenceManagement.Domain.Repositories;
using ECommon.Components;

namespace ConferenceManagement.Domain.Services
{
    /// <summary>注册会议Slug索引领域服务，封装会议创建时Slug必须唯一的业务规则
    /// </summary>
    [Component]
    public class RegisterConferenceSlugService
    {
        private readonly IConferenceSlugIndexRepository _conferenceSlugIndexRepository;

        public RegisterConferenceSlugService(IConferenceSlugIndexRepository conferenceSlugIndexRepository)
        {
            _conferenceSlugIndexRepository = conferenceSlugIndexRepository;
        }

        /// <summary>注册会议的Slug索引
        /// </summary>
        /// <param name="indexId"></param>
        /// <param name="conferenceId"></param>
        /// <param name="slug"></param>
        /// <returns></returns>
        public void RegisterSlug(string indexId, Guid conferenceId, string slug)
        {
            var slugIndex = _conferenceSlugIndexRepository.FindSlugIndex(slug);
            if (slugIndex == null)
            {
                _conferenceSlugIndexRepository.Add(new ConferenceSlugIndex(indexId, conferenceId, slug));
            }
            else if (slugIndex.IndexId != indexId)
            {
                throw new DuplicateNameException("The chosen conference slug is already taken.");
            }
        }
    }
}
