package com.microsoft.conference.management.domain.repository;

import com.microsoft.conference.management.domain.model.ConferenceSlugIndex;

public interface IConferenceSlugIndexRepository {
    void add(ConferenceSlugIndex index);

    ConferenceSlugIndex findSlugIndex(String slug);
}
