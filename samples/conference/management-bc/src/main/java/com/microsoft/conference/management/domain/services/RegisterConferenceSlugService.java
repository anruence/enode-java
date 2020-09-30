package com.microsoft.conference.management.domain.services;

import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.repositories.IConferenceSlugIndexRepository;

public class RegisterConferenceSlugService {
    private IConferenceSlugIndexRepository conferenceSlugIndexRepository;

    public RegisterConferenceSlugService(IConferenceSlugIndexRepository conferenceSlugIndexRepository) {
        this.conferenceSlugIndexRepository = conferenceSlugIndexRepository;
    }

    /**
     * 注册会议的Slug索引
     */
    /// <param name="indexId"></param>
    /// <param name="conferenceId"></param>
    /// <param name="slug"></param>
    /// <returns></returns>
    public void RegisterSlug(String indexId, String conferenceId, String slug) {
        ConferenceSlugIndex slugIndex = conferenceSlugIndexRepository.findSlugIndex(slug);
        if (slugIndex == null) {
            conferenceSlugIndexRepository.add(new ConferenceSlugIndex(indexId, conferenceId, slug));
        } else if (!slugIndex.indexId.equals(indexId)) {
            throw new RuntimeException("The chosen conference slug is already taken.");
        }
    }
}
