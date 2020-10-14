package com.microsoft.conference.management.domain.services;

import com.microsoft.conference.common.exception.RegisterSlugException;
import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.repositories.IConferenceSlugIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegisterConferenceSlugService {

    @Autowired
    private IConferenceSlugIndexRepository conferenceSlugIndexRepository;

    /**
     * 注册会议的Slug索引
     */
    public void RegisterSlug(String indexId, String conferenceId, String slug) {
        ConferenceSlugIndex slugIndex = conferenceSlugIndexRepository.findSlugIndex(slug);
        if (slugIndex == null) {
            conferenceSlugIndexRepository.add(new ConferenceSlugIndex(indexId, conferenceId, slug));
        } else if (!slugIndex.getIndexId().equals(indexId)) {
            throw new RegisterSlugException("The chosen conference slug is already taken.");
        }
    }
}
