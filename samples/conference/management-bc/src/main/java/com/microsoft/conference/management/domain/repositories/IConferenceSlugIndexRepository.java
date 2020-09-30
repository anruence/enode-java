package com.microsoft.conference.management.domain.repositories;

import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;

public interface IConferenceSlugIndexRepository {
    void add(ConferenceSlugIndex index);

    ConferenceSlugIndex findSlugIndex(String slug);
}
