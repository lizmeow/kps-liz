package com.amazon.ata.kindlepublishingservice.publishing;

import com.amazon.ata.kindlepublishingservice.dao.CatalogDao;
import com.amazon.ata.kindlepublishingservice.dao.PublishingStatusDao;
import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.enums.PublishingRecordStatus;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;

import javax.inject.Inject;

public final class BookPublishTask implements Runnable {

    private BookPublishRequestManager bookPublishRequestManager;
    private PublishingStatusDao publishingStatusDao;
    private CatalogDao catalogDao;

    @Inject
    public BookPublishTask(BookPublishRequestManager bookPublishRequestManager,
                                PublishingStatusDao publishingStatusDao,
                                CatalogDao catalogDao) {
        this.bookPublishRequestManager = bookPublishRequestManager;
        this.publishingStatusDao = publishingStatusDao;
        this.catalogDao = catalogDao;
    }

    @Override
    public void run() {
        System.out.println("!!!! In the BookPublishTask run method!!!!");
        BookPublishRequest request = bookPublishRequestManager.getBookPublishRequest();
        if (request == null) {
            System.out.println("!!!!The request is null!!!!!");
            return;
        }
        try {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.valueOf("IN_PROGRESS"),
                    request.getBookId());

            KindleFormattedBook formatted = KindleFormatConverter.format(request);

            CatalogItemVersion version = catalogDao.createOrUpdateBook(formatted);

            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.valueOf("SUCCESSFUL"),
                    version.getBookId());
        }
        catch (BookNotFoundException e) {
            publishingStatusDao.setPublishingStatus(request.getPublishingRecordId(),
                    PublishingRecordStatus.valueOf("FAILED"),
                    request.getBookId(),
                    e.getMessage());
        }



    }
}
