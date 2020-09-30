package com.microsoft.conference.management.readmodel;

import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import com.microsoft.conference.management.domain.repositories.IConferenceSlugIndexRepository;

public class ConferenceSlugIndexRepository implements IConferenceSlugIndexRepository {
    @Override
    public void add(ConferenceSlugIndex index) {
//        using(var connection = GetConnection())
//        {
//            connection.Insert(new
//            {
//                IndexId = index.IndexId,
//                        ConferenceId = index.ConferenceId,
//                        Slug = index.Slug
//            },ConfigSettings.ConferenceSlugIndexTable);
//        }
    }

    @Override
    public ConferenceSlugIndex findSlugIndex(String slug) {
        return null;
//        using(var connection = GetConnection())
//        {
//            var record = connection.QueryList(new {
//            Slug = slug
//        },ConfigSettings.ConferenceSlugIndexTable).SingleOrDefault();
//            if (record != null) {
//                var indexId = record.IndexId as String;
//                var conferenceId = (String) record.ConferenceId;
//                return new ConferenceSlugIndex(indexId, conferenceId, slug);
//            }
//            return null;
//        }
    }
}
