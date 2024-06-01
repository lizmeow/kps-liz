package com.amazon.ata.kindlepublishingservice.activity;

import com.amazon.ata.kindlepublishingservice.converters.PublicationsCoralConverter;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.exceptions.PublishingStatusNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.requests.GetPublishingStatusRequest;
import com.amazon.ata.kindlepublishingservice.models.response.GetPublishingStatusResponse;
import com.amazonaws.services.lambda.runtime.Context;

import javax.inject.Inject;
import java.util.List;

public class GetPublishingStatusActivity {
    private PublishingStatusDao publishingStatusDao;

    @Inject
    public GetPublishingStatusActivity(PublishingStatusDao publishingStatusDao) {
        this.publishingStatusDao = publishingStatusDao;
    }

    public GetPublishingStatusResponse execute(GetPublishingStatusRequest publishingStatusRequest) {
        // get the publishingStatusId
        String id = publishingStatusRequest.getPublishingRecordId();

        // get all items with the partition key = to this publishingStatusId
        List<PublishingStatusItem> items = publishingStatusDao.getPublishingStatus(id);

        // if no items are found, throw an exception
        if (items == null || items.isEmpty()) {
            throw new PublishingStatusNotFoundException("No items found for this publsihing record");
        }

        // Convert the list of PublishingStatusItem's to a list of PublihsingStatusRecord's
        return GetPublishingStatusResponse.builder()
                .withPublishingStatusHistory(PublicationsCoralConverter.toCoral(items))
                .build();
    }
}
