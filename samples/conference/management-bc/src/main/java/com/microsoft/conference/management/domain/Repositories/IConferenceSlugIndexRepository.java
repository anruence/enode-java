package com.microsoft.conference.management.domain.Repositories;

import com.microsoft.conference.management.domain.Models.ConferenceSlugIndex;

public interface IConferenceSlugIndexRepository {
    void add(ConferenceSlugIndex index);

    ConferenceSlugIndex FindSlugIndex(String slug);
}
