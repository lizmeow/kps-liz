package com.amazon.ata.kindlepublishingservice.dao;

import com.amazon.ata.kindlepublishingservice.dynamodb.models.CatalogItemVersion;
import com.amazon.ata.kindlepublishingservice.exceptions.BookNotFoundException;
import com.amazon.ata.kindlepublishingservice.models.Book;
import com.amazon.ata.kindlepublishingservice.publishing.KindleFormattedBook;
import com.amazon.ata.kindlepublishingservice.utils.KindlePublishingUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import javax.inject.Inject;

public class CatalogDao {

    private final DynamoDBMapper dynamoDbMapper;

    /**
     * Instantiates a new CatalogDao object.
     *
     * @param dynamoDbMapper The {@link DynamoDBMapper} used to interact with the catalog table.
     */
    @Inject
    public CatalogDao(DynamoDBMapper dynamoDbMapper) {
        this.dynamoDbMapper = dynamoDbMapper;
    }

    /**
     * Returns the latest version of the book from the catalog corresponding to the specified book id.
     * Throws a BookNotFoundException if the latest version is not active or no version is found.
     * @param bookId Id associated with the book.
     * @return The corresponding CatalogItem from the catalog table.
     */
    public CatalogItemVersion getBookFromCatalog(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);

        if (book == null || book.isInactive()) {
            throw new BookNotFoundException(String.format("No book found for id: %s", bookId));
        }

        return book;
    }



    public void removeBookFromCatalog(String bookId) {
        CatalogItemVersion book = getBookFromCatalog(bookId);

        book.setInactive(true);
        dynamoDbMapper.save(book);
    }

    public void validateBookExists(String bookId) {
        CatalogItemVersion book = getLatestVersionOfBook(bookId);
        if (book == null) throw new BookNotFoundException("No entries exist for bookId: " + bookId);
    }

    public CatalogItemVersion createOrUpdateBook(KindleFormattedBook book) {
        if (book.getBookId() == null) {
            String bookId = KindlePublishingUtils.generateBookId();

            CatalogItemVersion addedBook = new CatalogItemVersion();
            addedBook.setBookId(bookId);
            addedBook.setAuthor(book.getAuthor());
            addedBook.setText(book.getText());
            addedBook.setGenre(book.getGenre());
            addedBook.setTitle(book.getTitle());
            addedBook.setInactive(false);
            addedBook.setVersion(1);

            dynamoDbMapper.save(addedBook);
            return addedBook;
        } else {
            CatalogItemVersion previousVersion = getBookFromCatalog(book.getBookId());
            if (previousVersion == null) {
                throw new BookNotFoundException("No book found for id: " + book.getBookId());
            } else {
                CatalogItemVersion newVersion = new CatalogItemVersion();
                newVersion.setBookId(book.getBookId());
                newVersion.setAuthor(book.getAuthor());
                newVersion.setText(book.getText());
                newVersion.setGenre(book.getGenre());
                newVersion.setTitle(book.getTitle());
                newVersion.setVersion(previousVersion.getVersion() + 1);
                newVersion.setInactive(false);

                dynamoDbMapper.save(newVersion);

                previousVersion.setInactive(true);
                dynamoDbMapper.save(previousVersion);

                return newVersion;
            }
        }
    }


    // Returns null if no version exists for the provided bookId
    private CatalogItemVersion getLatestVersionOfBook(String bookId) {
        CatalogItemVersion book = new CatalogItemVersion();
        book.setBookId(bookId);

        DynamoDBQueryExpression<CatalogItemVersion> queryExpression = new DynamoDBQueryExpression<CatalogItemVersion>()
                .withHashKeyValues(book)
                .withScanIndexForward(false)
                .withLimit(1);

        List<CatalogItemVersion> results = dynamoDbMapper.query(CatalogItemVersion.class, queryExpression);
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

}
