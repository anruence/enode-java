package com.microsoft.conference.management.domain.services;

import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.repositories.IConferenceSlugIndexRepository;

public class RegisterConferenceSlugService {
    private IConferenceSlugIndexRepository _conferenceSlugIndexRepository;

    public RegisterConferenceSlugService(IConferenceSlugIndexRepository conferenceSlugIndexRepository) {
        _conferenceSlugIndexRepository = conferenceSlugIndexRepository;
    }

    /// <summary>注册会议的Slug索引
    /// </summary>
    /// <param name="indexId"></param>
    /// <param name="conferenceId"></param>
    /// <param name="slug"></param>
    /// <returns></returns>
    public void RegisterSlug(String indexId, String conferenceId, String slug) {
        ConferenceSlugIndex slugIndex = _conferenceSlugIndexRepository.FindSlugIndex(slug);
        if (slugIndex == null) {
            _conferenceSlugIndexRepository.add(new ConferenceSlugIndex(indexId, conferenceId, slug));
        } else if (slugIndex.IndexId != indexId) {
            throw new RuntimeException("The chosen conference slug is already taken.");
        }
    }
}
