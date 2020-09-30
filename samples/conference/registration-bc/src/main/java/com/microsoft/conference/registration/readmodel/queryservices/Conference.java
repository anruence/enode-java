// ==============================================================================================================
// Microsoft patterns & practices
// CQRS Journey project
// ==============================================================================================================
// ©2012 Microsoft. All rights reserved. Certain content used with permission from contributors
// http://go.microsoft.com/fwlink/p/?LinkID=258575
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance 
// with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed under the License is 
// distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
// See the License for the specific language governing permissions and limitations under the License.
// ==============================================================================================================
package com.microsoft.conference.registration.readmodel.queryservices;

import java.util.Date;
import java.util.List;

public class Conference {
    public String id;
    public String code;
    public String name;
    public String description;
    public String location;
    public String tagline;
    public String twitterSearch;
    public Date startDate;
    public boolean isPublished;

    public Conference(String id, String code, String name, String description, String location, String tagline, String twitterSearch, Date startDate, List<SeatType> seats) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.location = location;
        this.tagline = tagline;
        this.twitterSearch = twitterSearch;
        this.startDate = startDate;
    }

    public Conference() {
    }
}
