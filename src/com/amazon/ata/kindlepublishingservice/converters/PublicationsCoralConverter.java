package com.amazon.ata.kindlepublishingservice.converters;

import com.amazon.ata.coral.converter.CoralConverterUtil;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.PublishingStatusItem;
import com.amazon.ata.kindlepublishingservice.models.BookRecommendation;
import com.amazon.ata.kindlepublishingservice.models.PublishingStatusRecord;

import java.util.List;

public class PublicationsCoralConverter {
    private PublicationsCoralConverter() {}

    /**
     * Converts the given BookRecommendations list into the corresponding Coral BookRecommendationsList object.
     *
     * @param bookRecommendations BookRecommendations list to convert.
     * @return Coral BookRecommendations list.
     */
    public static List<PublishingStatusRecord> toCoral(List<PublishingStatusItem>
                                                           items) {
        return CoralConverterUtil.convertList(items, PublicationsCoralConverter::toCoral);
    }

    /**
     * Converts the given BookRecommendations object to the corresponding Coral BookRecommendation object.
     * @param bookRecommendation BookRecommendation object to convert
     * @return Coral BookRecommendation object.
     */
    public static PublishingStatusRecord toCoral(PublishingStatusItem
                                                     item) {
        return PublishingStatusRecord.builder()
                .withBookId(item.getBookId())
                .withStatus(item.getStatus().toString())
                .withStatusMessage(item.getStatusMessage())
                .build();
    }
}
